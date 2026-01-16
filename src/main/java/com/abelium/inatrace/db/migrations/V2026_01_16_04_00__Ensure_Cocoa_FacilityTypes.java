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
 * Ensure all COCOA catalogs exist in COCOA deployments.
 * <p>
 * This migration checks if the deployment is COCOA and verifies that all expected
 * catalogs for cocoa processing exist:
 * - ProductType (Cacao)
 * - MeasureUnitTypes (PESOKG, PESOLIBRA, SACOS_60, SACOS_69)
 * - SemiProducts (Cacao en Baba, Cacao Fermentado, etc.)
 * - FacilityTypes (ACOPIO, FERMENTACION, etc.)
 * <p>
 * If any are missing, they are created and linked to all existing ValueChains.
 * <p>
 * This migration is safe and idempotent:
 * - Only adds missing catalog entries, never deletes existing ones
 * - Only creates missing links to ValueChains
 * - Skips if not a COCOA deployment
 *
 * @author INATrace Team
 */
public class V2026_01_16_04_00__Ensure_Cocoa_FacilityTypes implements JpaMigration {

    // Expected COCOA FacilityTypes: code -> (label, order)
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
        String productType = environment.getProperty("INATrace.product.type", "COCOA");

        // Only run for COCOA deployments
        if (!"COCOA".equalsIgnoreCase(productType)) {
            return;
        }

        // 1. Ensure ProductType "Cacao" exists
        ensureProductType(em);

        // 2. Ensure MeasureUnitTypes exist
        ensureMeasureUnitTypes(em);

        // 3. Ensure SemiProducts exist
        ensureSemiProducts(em);

        // 4. Ensure FacilityTypes exist
        ensureFacilityTypes(em);

        // 5. Link all catalogs to ValueChains
        linkCatalogsToValueChains(em);
    }

    private void ensureProductType(EntityManager em) {
        List<ProductType> existing = em.createQuery(
                "SELECT pt FROM ProductType pt WHERE pt.name = :name", ProductType.class)
                .setParameter("name", "Cacao")
                .getResultList();

        if (!existing.isEmpty()) {
            // Ensure all ValueChains and UserCustomers are linked
            ProductType cacaoType = existing.get(0);
            linkProductTypeToValueChains(em, cacaoType);
            linkProductTypeToUserCustomers(em, cacaoType);
            return;
        }

        // Create Cacao ProductType
        ProductType cacaoType = new ProductType();
        cacaoType.setName("Cacao");
        cacaoType.setDescription("Cacao product type");
        em.persist(cacaoType);
        em.flush();

        linkProductTypeToValueChains(em, cacaoType);
        linkProductTypeToUserCustomers(em, cacaoType);
    }

    private void linkProductTypeToValueChains(EntityManager em, ProductType productType) {
        List<ValueChain> valueChains = em.createQuery("SELECT vc FROM ValueChain vc", ValueChain.class)
                .getResultList();

        for (ValueChain vc : valueChains) {
            if (vc.getProductType() == null) {
                vc.setProductType(productType);
                em.merge(vc);
            }
        }
    }

    private void linkProductTypeToUserCustomers(EntityManager em, ProductType productType) {
        List<Long> userCustomerIdsWithoutLink = em.createQuery(
                "SELECT u.id FROM UserCustomer u WHERE u.id NOT IN " +
                        "(SELECT ucpt.userCustomer.id FROM UserCustomerProductType ucpt WHERE ucpt.productType = :productType)",
                Long.class)
                .setParameter("productType", productType)
                .getResultList();

        for (Long userCustomerId : userCustomerIdsWithoutLink) {
            UserCustomer userCustomerRef = em.getReference(UserCustomer.class, userCustomerId);
            UserCustomerProductType link = new UserCustomerProductType();
            link.setUserCustomer(userCustomerRef);
            link.setProductType(productType);
            em.persist(link);
        }
    }

    private void ensureMeasureUnitTypes(EntityManager em) {
        // PESOKG (kg)
        createMeasureUnitTypeIfNotExists(em, "PESOKG", "kg", new BigDecimal("1"));

        // PESOLIBRA (Libra)
        createMeasureUnitTypeIfNotExists(em, "PESOLIBRA", "Libra", new BigDecimal("2.2"));

        // SACOS_60 (Sacos 60 kg)
        createMeasureUnitTypeIfNotExists(em, "SACOS_60", "Sacos (60 kg)", new BigDecimal("60"));

        // SACOS_69 (Sacos 69 kg)
        createMeasureUnitTypeIfNotExists(em, "SACOS_69", "Sacos (69 kg)", new BigDecimal("69"));
    }

    private void createMeasureUnitTypeIfNotExists(EntityManager em, String code, String label, BigDecimal weight) {
        Long count = em.createQuery(
                "SELECT COUNT(mut) FROM MeasureUnitType mut WHERE mut.code = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult();

        if (count == null || count == 0L) {
            MeasureUnitType mut = new MeasureUnitType(code, label, weight);
            em.persist(mut);
        }
    }

    private void ensureSemiProducts(EntityManager em) {
        MeasureUnitType kg = findMeasureUnitTypeByCode(em, "PESOKG");

        // 1. Cacao en Baba
        createSemiProductIfNotExists(em, "Cacao en Baba",
                "Cacao en baba procedente de productores", kg, true, true, false);

        // 2. Cacao Fermentado
        createSemiProductIfNotExists(em, "Cacao Fermentado",
                "El cacao en baba es enviado a los cajones de fermentación donde es fermentado y es verificado si requiere de mayor tiempo.",
                kg, true, true, false);

        // 3. Cacao Semiseco
        createSemiProductIfNotExists(em, "Cacao Semiseco",
                "Proceso del cacao entre el secado natural y que será enviado al proceso de secado artificial",
                kg, true, true, false);

        // 4. Cacao Seco en Kg
        createSemiProductIfNotExists(em, "Cacao Seco en Kg",
                "Proceso de secado por medio de marquesinas o tendales. (Hasta 7%)",
                kg, true, true, false);

        // 5. Cacao Seco Clasificado
        createSemiProductIfNotExists(em, "Cacao Seco Clasificado",
                "Cacao Seco Clasificado de acuerdo a requerimientos del clientes final",
                kg, true, false, true);

        // 6. Cacao Seco
        createSemiProductIfNotExists(em, "Cacao Seco",
                "Proceso de secado por medio de marquesinas o tendales. (Hasta 7%)",
                kg, true, true, false);
    }

    private MeasureUnitType findMeasureUnitTypeByCode(EntityManager em, String code) {
        List<MeasureUnitType> result = em.createQuery(
                "SELECT m FROM MeasureUnitType m WHERE m.code = :code", MeasureUnitType.class)
                .setParameter("code", code)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    private void createSemiProductIfNotExists(EntityManager em, String name, String description,
                                              MeasureUnitType measureUnitType, boolean isBuyable,
                                              boolean isSKU, boolean isSKUEndCustomer) {
        List<SemiProduct> existing = em.createQuery(
                "SELECT sp FROM SemiProduct sp WHERE sp.name = :name", SemiProduct.class)
                .setParameter("name", name)
                .getResultList();

        if (!existing.isEmpty()) {
            return;
        }

        SemiProduct sp = new SemiProduct();
        sp.setName(name);
        sp.setDescription(description);
        sp.setMeasurementUnitType(measureUnitType);
        sp.setBuyable(isBuyable);
        sp.setSKU(isSKU);
        sp.setSKUEndCustomer(isSKUEndCustomer);
        em.persist(sp);

        // Create translations
        for (Language language : List.of(Language.EN, Language.ES)) {
            SemiProductTranslation translation = new SemiProductTranslation();
            translation.setName(name);
            translation.setDescription(description);
            translation.setLanguage(language);
            translation.setSemiProduct(sp);
            em.persist(translation);
        }
    }

    private void ensureFacilityTypes(EntityManager em) {
        for (Map.Entry<String, FacilityTypeData> entry : COCOA_FACILITY_TYPES.entrySet()) {
            String code = entry.getKey();
            FacilityTypeData data = entry.getValue();

            Long count = em.createQuery(
                    "SELECT COUNT(ft) FROM FacilityType ft WHERE ft.code = :code", Long.class)
                    .setParameter("code", code)
                    .getSingleResult();

            if (count == null || count == 0L) {
                FacilityType ft = new FacilityType(code, data.label, data.order);
                em.persist(ft);
            }
        }
    }

    private void linkCatalogsToValueChains(EntityManager em) {
        em.flush();

        List<ValueChain> valueChains = em.createQuery("SELECT vc FROM ValueChain vc", ValueChain.class)
                .getResultList();

        if (valueChains.isEmpty()) {
            return;
        }

        // Link FacilityTypes
        List<FacilityType> facilityTypes = em.createQuery(
                "SELECT ft FROM FacilityType ft WHERE ft.code IN :codes", FacilityType.class)
                .setParameter("codes", COCOA_FACILITY_TYPES.keySet())
                .getResultList();

        for (ValueChain vc : valueChains) {
            for (FacilityType ft : facilityTypes) {
                Long linkCount = em.createQuery(
                        "SELECT COUNT(vcft) FROM ValueChainFacilityType vcft " +
                                "WHERE vcft.valueChain = :vc AND vcft.facilityType = :ft", Long.class)
                        .setParameter("vc", vc)
                        .setParameter("ft", ft)
                        .getSingleResult();

                if (linkCount == null || linkCount == 0L) {
                    em.persist(new ValueChainFacilityType(vc, ft));
                }
            }
        }

        // Link MeasureUnitTypes
        List<MeasureUnitType> measureUnitTypes = em.createQuery(
                "SELECT mut FROM MeasureUnitType mut WHERE mut.code IN ('PESOKG', 'PESOLIBRA', 'SACOS_60', 'SACOS_69')",
                MeasureUnitType.class)
                .getResultList();

        for (ValueChain vc : valueChains) {
            for (MeasureUnitType mut : measureUnitTypes) {
                Long linkCount = em.createQuery(
                        "SELECT COUNT(vcmut) FROM ValueChainMeasureUnitType vcmut " +
                                "WHERE vcmut.valueChain = :vc AND vcmut.measureUnitType = :mut", Long.class)
                        .setParameter("vc", vc)
                        .setParameter("mut", mut)
                        .getSingleResult();

                if (linkCount == null || linkCount == 0L) {
                    em.persist(new ValueChainMeasureUnitType(vc, mut));
                }
            }
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
