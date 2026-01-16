package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.FacilityType;
import com.abelium.inatrace.db.entities.codebook.MeasureUnitType;
import com.abelium.inatrace.db.entities.codebook.ProductType;
import com.abelium.inatrace.db.entities.codebook.SemiProduct;
import com.abelium.inatrace.db.entities.codebook.SemiProductTranslation;
import com.abelium.inatrace.db.entities.common.UserCustomer;
import com.abelium.inatrace.db.entities.common.UserCustomerProductType;
import com.abelium.inatrace.db.entities.value_chain.ValueChain;
import com.abelium.inatrace.db.entities.value_chain.ValueChainFacilityType;
import com.abelium.inatrace.db.entities.value_chain.ValueChainMeasureUnitType;
import com.abelium.inatrace.types.Language;
import jakarta.persistence.EntityManager;
import org.springframework.core.env.Environment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * FORCE replacement of COFFEE catalogs with COCOA catalogs.
 * <p>
 * This migration unconditionally replaces all catalog entries regardless of what exists.
 * It checks for WASHING_STATION (COFFEE indicator) and if found, deletes everything
 * and recreates with COCOA catalogs.
 *
 * @author INATrace Team
 */
public class V2026_01_16_05_00__Force_Replace_Coffee_With_Cocoa_Catalogs implements JpaMigration {

    private static final Map<String, FacilityTypeData> COCOA_FACILITY_TYPES = Map.ofEntries(
            Map.entry("ACOPIO", new FacilityTypeData("Collection Center", 1)),
            Map.entry("ESCURRIDO", new FacilityTypeData("Draining", 2)),
            Map.entry("FERMENTACION", new FacilityTypeData("Fermentation Area", 3)),
            Map.entry("SECADO", new FacilityTypeData("Drying Area", 4)),
            Map.entry("SECADON", new FacilityTypeData("Natural Drying", 5)),
            Map.entry("SECADOA", new FacilityTypeData("Artificial Drying", 6)),
            Map.entry("ALIMPIEZA", new FacilityTypeData("Cleaning Area", 7)),
            Map.entry("ACLASIFICADO", new FacilityTypeData("Grading Area", 8)),
            Map.entry("AEMPACADO", new FacilityTypeData("Packing Area", 9)),
            Map.entry("ALMACEN", new FacilityTypeData("Storage Area", 10)),
            Map.entry("VENTA", new FacilityTypeData("Point of Sale", 11))
    );

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        // Check if WASHING_STATION exists (COFFEE catalog indicator)
        Long coffeeCount = em.createQuery(
                "SELECT COUNT(ft) FROM FacilityType ft WHERE ft.code = :code", Long.class)
                .setParameter("code", "WASHING_STATION")
                .getSingleResult();

        if (coffeeCount == null || coffeeCount == 0L) {
            return; // No COFFEE catalog, nothing to replace
        }

        System.out.println("=== FORCE REPLACING COFFEE CATALOGS WITH COCOA ===");

        // 1. Nullify FK references
        System.out.println("Nullifying FK references...");
        em.createQuery("UPDATE Facility f SET f.facilityType = NULL WHERE f.facilityType IS NOT NULL")
                .executeUpdate();
        em.flush();

        // 2. Delete all links
        System.out.println("Deleting catalog links...");
        em.createQuery("DELETE FROM ValueChainFacilityType vcft").executeUpdate();
        em.createQuery("DELETE FROM ValueChainMeasureUnitType vcmut").executeUpdate();
        em.createQuery("DELETE FROM ValueChainSemiProduct vcsp").executeUpdate();
        em.createQuery("DELETE FROM FacilitySemiProduct fsp").executeUpdate();
        em.flush();

        // 3. Delete catalog entries
        System.out.println("Deleting old catalog entries...");
        em.createQuery("DELETE FROM SemiProductTranslation spt").executeUpdate();
        em.createQuery("DELETE FROM SemiProduct sp").executeUpdate();
        em.createQuery("DELETE FROM FacilityType ft").executeUpdate();
        em.createQuery("DELETE FROM MeasureUnitType mut").executeUpdate();
        em.flush();

        // 4. Create COCOA catalogs
        System.out.println("Creating COCOA catalogs...");
        createCocoaCatalogs(em);

        System.out.println("=== COFFEE -> COCOA REPLACEMENT COMPLETE ===");
    }

    private void createCocoaCatalogs(EntityManager em) {
        // 1. Ensure ProductType "Cacao" exists
        ProductType cacaoType = findOrCreateProductType(em);

        // 2. Create MeasureUnitTypes
        createMeasureUnitType(em, "PESOKG", "kg", new BigDecimal("1"));
        createMeasureUnitType(em, "PESOLIBRA", "Libra", new BigDecimal("2.2"));
        createMeasureUnitType(em, "SACOS_60", "Sacos (60 kg)", new BigDecimal("60"));
        createMeasureUnitType(em, "SACOS_69", "Sacos (69 kg)", new BigDecimal("69"));

        em.flush();

        // 3. Create SemiProducts
        MeasureUnitType kg = findMeasureUnitTypeByCode(em, "PESOKG");
        createSemiProduct(em, "Cacao en Baba", "Cacao en baba procedente de productores", kg, true, true, false);
        createSemiProduct(em, "Cacao Fermentado", "El cacao en baba es enviado a los cajones de fermentación donde es fermentado y es verificado si requiere de mayor tiempo.", kg, true, true, false);
        createSemiProduct(em, "Cacao Semiseco", "Proceso del cacao entre el secado natural y que será enviado al proceso de secado artificial", kg, true, true, false);
        createSemiProduct(em, "Cacao Seco en Kg", "Proceso de secado por medio de marquesinas o tendales. (Hasta 7%)", kg, true, true, false);
        createSemiProduct(em, "Cacao Seco Clasificado", "Cacao Seco Clasificado de acuerdo a requerimientos del clientes final", kg, true, false, true);
        createSemiProduct(em, "Cacao Seco", "Proceso de secado por medio de marquesinas o tendales. (Hasta 7%)", kg, true, true, false);

        em.flush();

        // 4. Create FacilityTypes
        for (Map.Entry<String, FacilityTypeData> entry : COCOA_FACILITY_TYPES.entrySet()) {
            FacilityType ft = new FacilityType(entry.getKey(), entry.getValue().label, entry.getValue().order);
            em.persist(ft);
        }

        em.flush();

        // 5. Link to ValueChains
        linkCatalogsToValueChains(em, cacaoType);
    }

    private ProductType findOrCreateProductType(EntityManager em) {
        List<ProductType> existing = em.createQuery(
                "SELECT pt FROM ProductType pt WHERE pt.name = :name", ProductType.class)
                .setParameter("name", "Cacao")
                .getResultList();

        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        ProductType pt = new ProductType();
        pt.setName("Cacao");
        pt.setDescription("Cacao product type");
        em.persist(pt);
        em.flush();
        return pt;
    }

    private void createMeasureUnitType(EntityManager em, String code, String label, BigDecimal weight) {
        MeasureUnitType mut = new MeasureUnitType(code, label, weight);
        em.persist(mut);
    }

    private MeasureUnitType findMeasureUnitTypeByCode(EntityManager em, String code) {
        List<MeasureUnitType> result = em.createQuery(
                "SELECT m FROM MeasureUnitType m WHERE m.code = :code", MeasureUnitType.class)
                .setParameter("code", code)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    private void createSemiProduct(EntityManager em, String name, String description,
                                   MeasureUnitType measureUnitType, boolean isBuyable,
                                   boolean isSKU, boolean isSKUEndCustomer) {
        SemiProduct sp = new SemiProduct();
        sp.setName(name);
        sp.setDescription(description);
        sp.setMeasurementUnitType(measureUnitType);
        sp.setBuyable(isBuyable);
        sp.setSKU(isSKU);
        sp.setSKUEndCustomer(isSKUEndCustomer);
        em.persist(sp);

        for (Language language : List.of(Language.EN, Language.ES)) {
            SemiProductTranslation translation = new SemiProductTranslation();
            translation.setName(name);
            translation.setDescription(description);
            translation.setLanguage(language);
            translation.setSemiProduct(sp);
            em.persist(translation);
        }
    }

    private void linkCatalogsToValueChains(EntityManager em, ProductType productType) {
        List<ValueChain> valueChains = em.createQuery("SELECT vc FROM ValueChain vc", ValueChain.class)
                .getResultList();

        if (valueChains.isEmpty()) {
            return;
        }

        // Link ProductType
        for (ValueChain vc : valueChains) {
            if (vc.getProductType() == null) {
                vc.setProductType(productType);
            }
        }

        // Link FacilityTypes
        List<FacilityType> facilityTypes = em.createQuery("SELECT ft FROM FacilityType ft", FacilityType.class)
                .getResultList();
        for (ValueChain vc : valueChains) {
            for (FacilityType ft : facilityTypes) {
                em.persist(new ValueChainFacilityType(vc, ft));
            }
        }

        // Link MeasureUnitTypes
        List<MeasureUnitType> measureUnitTypes = em.createQuery("SELECT mut FROM MeasureUnitType mut", MeasureUnitType.class)
                .getResultList();
        for (ValueChain vc : valueChains) {
            for (MeasureUnitType mut : measureUnitTypes) {
                em.persist(new ValueChainMeasureUnitType(vc, mut));
            }
        }

        // Link UserCustomers to ProductType
        List<Long> userCustomerIds = em.createQuery("SELECT u.id FROM UserCustomer u", Long.class)
                .getResultList();
        for (Long userId : userCustomerIds) {
            UserCustomer userRef = em.getReference(UserCustomer.class, userId);
            UserCustomerProductType link = new UserCustomerProductType();
            link.setUserCustomer(userRef);
            link.setProductType(productType);
            em.persist(link);
        }
    }

    private static class FacilityTypeData {
        final String label;
        final Integer order;

        FacilityTypeData(String label, Integer order) {
            this.label = label;
            this.order = order;
        }
    }
}
