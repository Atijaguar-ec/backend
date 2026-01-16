package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.MeasureUnitType;
import com.abelium.inatrace.db.entities.codebook.SemiProduct;
import com.abelium.inatrace.db.entities.codebook.SemiProductTranslation;
import com.abelium.inatrace.types.Language;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Migration to prefill SemiProducts based on the product type configuration.
 * <p>
 * This migration creates product-specific semi-products with appropriate measurement units,
 * flags (isBuyable, isSKU, isSKUEndCustomer), and translations in English and Spanish.
 * <p>
 * Supported product types:
 * <ul>
 *   <li>SHRIMP/CAMARON - Creates 6 shrimp semi-products with LIBRAS as measurement unit</li>
 *   <li>COCOA - Creates 6 cocoa semi-products with PESOKG (kg) as measurement unit</li>
 * </ul>
 * <p>
 * This migration is idempotent and will not create duplicates if semi-products already exist.
 *
 * @author INATrace Team
 */
public class V2025_11_19_05_00__Prefill_SemiProducts implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        String productType = environment.getProperty("INATrace.product.type", "COCOA");

        if ("SHRIMP".equalsIgnoreCase(productType) || "CAMARON".equalsIgnoreCase(productType)) {
            prefillShrimpSemiProducts(em);
        } else if ("COCOA".equalsIgnoreCase(productType)) {
            prefillCocoaSemiProducts(em);
        }
    }

    private void prefillShrimpSemiProducts(EntityManager em) {
        MeasureUnitType libras = findMeasureUnitTypeByCode(em, "LIBRAS");

        createSemiProductIfNotExists(em,
                "Camarón Entero",
                "Camarón Entero",
                libras,
                true,
                true,
                false
        );

        createSemiProductIfNotExists(em,
                "Camarón Entero Clasificado",
                "Camarón Entero Clasificado",
                libras,
                true,
                true,
                false
        );

        createSemiProductIfNotExists(em,
                "Camarón descabezado",
                "Camarón descabezado",
                libras,
                true,
                true,
                false
        );

        createSemiProductIfNotExists(em,
                "Camarón descabezado clasificado",
                "Camarón descabezado clasificado",
                libras,
                true,
                true,
                false
        );

        createSemiProductIfNotExists(em,
                "Camarón Cortado",
                "Camarón Cortado",
                libras,
                true,
                true,
                false
        );

        createSemiProductIfNotExists(em,
                "Camarón cortado tratado",
                "Camarón cortado tratado",
                libras,
                true,
                true,
                false
        );
    }
    private void prefillCocoaSemiProducts(EntityManager em) {
        MeasureUnitType kg = findMeasureUnitTypeByCode(em, "PESOKG");

        // 1. Cacao en Baba
        createSemiProductIfNotExists(em,
                "Cacao en Baba",
                "Cacao en baba procedente de productores",
                kg,
                true,   // isBuyable
                true,   // isSKU
                false   // isSKUEndCustomer
        );

        // 2. Cacao Fermentado
        createSemiProductIfNotExists(em,
                "Cacao Fermentado",
                "El cacao en baba es enviado a los cajones de fermentación donde es fermentado y es verificado si requiere de mayor tiempo.",
                kg,
                true,
                true,
                false
        );

        // 3. Cacao Semiseco
        createSemiProductIfNotExists(em,
                "Cacao Semiseco",
                "Proceso del cacao entre el secado natural y que será enviado al proceso de secado artificial",
                kg,
                true,
                true,
                false
        );

        // 4. Cacao Seco en Kg
        createSemiProductIfNotExists(em,
                "Cacao Seco en Kg",
                "Proceso de secado por medio de marquesinas o tendales. (Hasta 7%)",
                kg,
                true,
                true,
                false
        );

        // 5. Cacao Seco Clasificado
        createSemiProductIfNotExists(em,
                "Cacao Seco Clasificado",
                "Cacao Seco Clasificado de acuerdo a requerimientos del clientes final",
                kg,
                true,
                false,
                true
        );

        // 6. Cacao Seco
        createSemiProductIfNotExists(em,
                "Cacao Seco",
                "Proceso de secado por medio de marquesinas o tendales. (Hasta 7%)",
                kg,
                true,   // se puede comprar (confirmado)
                true,
                false
        );
    }

    /**
     * Finds a MeasureUnitType by its code.
     *
     * @param em EntityManager for database operations
     * @param code The code of the MeasureUnitType to find
     * @return The MeasureUnitType if found, null otherwise
     */
    private MeasureUnitType findMeasureUnitTypeByCode(EntityManager em, String code) {
        List<MeasureUnitType> result = em.createQuery(
                        "SELECT m FROM MeasureUnitType m WHERE m.code = :code", MeasureUnitType.class)
                .setParameter("code", code)
                .setMaxResults(1)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Creates a SemiProduct if it doesn't already exist.
     * <p>
     * This method checks for existing semi-products by name to ensure idempotency.
     * It also creates translations in English and Spanish.
     *
     * @param em EntityManager for database operations
     * @param name The name of the semi-product
     * @param description The description of the semi-product
     * @param measureUnitType The measurement unit type (can be null)
     * @param isBuyable Whether the product is buyable
     * @param isSKU Whether the product is a stock keeping unit
     * @param isSKUEndCustomer Whether the product is a SKU for end customers
     */
    private void createSemiProductIfNotExists(EntityManager em,
                                              String name,
                                              String description,
                                              MeasureUnitType measureUnitType,
                                              boolean isBuyable,
                                              boolean isSKU,
                                              boolean isSKUEndCustomer) {

        List<SemiProduct> existing = em.createQuery(
                        "SELECT sp FROM SemiProduct sp WHERE sp.name = :name", SemiProduct.class)
                .setParameter("name", name)
                .setMaxResults(1)
                .getResultList();

        if (!existing.isEmpty()) {
            return;
        }

        SemiProduct semiProduct = new SemiProduct();
        semiProduct.setName(name);
        semiProduct.setDescription(description);
        semiProduct.setMeasurementUnitType(measureUnitType);
        semiProduct.setBuyable(isBuyable);
        semiProduct.setSKU(isSKU);
        semiProduct.setSKUEndCustomer(isSKUEndCustomer);

        em.persist(semiProduct);

        // Create translations for EN and ES, mirroring name/description
        for (Language language : List.of(Language.EN, Language.ES)) {
            SemiProductTranslation translation = new SemiProductTranslation();
            translation.setName(name);
            translation.setDescription(description);
            translation.setLanguage(language);
            translation.setSemiProduct(semiProduct);
            em.persist(translation);
        }
    }
}
