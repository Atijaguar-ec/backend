package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.SemiProduct;
import com.abelium.inatrace.db.entities.value_chain.ValueChain;
import com.abelium.inatrace.db.entities.value_chain.ValueChainSemiProduct;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Migration to automatically link SemiProducts to all ValueChains based on product type.
 * <p>
 * This migration creates associations between SemiProducts and ValueChains for cocoa and
 * shrimp product types, ensuring that all value chains have access to the appropriate
 * semi-products for their product type.
 * <p>
 * Supported product types:
 * <ul>
 *   <li>COCOA - Links 6 cocoa semi-products to all value chains</li>
 *   <li>SHRIMP/CAMARON - Links 6 shrimp semi-products to all value chains</li>
 * </ul>
 * <p>
 * This migration is idempotent and will not create duplicate links.
 *
 * @author INATrace Team
 */
public class V2025_11_20_01_00__Link_SemiProducts_To_ValueChains implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        String productType = environment.getProperty("INATrace.product.type", "COFFEE");

        if ("COCOA".equalsIgnoreCase(productType)) {
            linkSemiProductsToValueChains(em, List.of(
                    "Cacao en Baba",
                    "Cacao Fermentado",
                    "Cacao Semiseco",
                    "Cacao Seco en Kg",
                    "Cacao Seco Clasificado",
                    "Cacao Seco"
            ));
        } else if ("SHRIMP".equalsIgnoreCase(productType) || "CAMARON".equalsIgnoreCase(productType)) {
            linkSemiProductsToValueChains(em, List.of(
                    "Camarón Entero",
                    "Camarón Entero Clasificado",
                    "Camarón descabezado",
                    "Camarón descabezado clasificado",
                    "Camarón Cortado",
                    "Camarón cortado tratado"
            ));
        }
    }

    /**
     * Links the specified semi-products to all value chains.
     * <p>
     * This method fetches all value chains and the specified semi-products, then creates
     * associations between them if they don't already exist.
     *
     * @param em EntityManager for database operations
     * @param semiProductNames List of semi-product names to link
     */
    private void linkSemiProductsToValueChains(EntityManager em, List<String> semiProductNames) {
        // Fetch all value chains
        List<ValueChain> valueChains = em.createQuery("SELECT vc FROM ValueChain vc", ValueChain.class)
                .getResultList();

        if (valueChains.isEmpty()) {
            return;
        }

        // Fetch all relevant semi products by name
        List<SemiProduct> semiProducts = em.createQuery(
                        "SELECT sp FROM SemiProduct sp WHERE sp.name IN :names", SemiProduct.class)
                .setParameter("names", semiProductNames)
                .getResultList();

        if (semiProducts.isEmpty()) {
            return;
        }

        // Create links for each combination of value chain and semi-product
        for (ValueChain vc : valueChains) {
            for (SemiProduct sp : semiProducts) {
                // Check if link already exists to ensure idempotency
                Long existingCount = em.createQuery(
                                "SELECT COUNT(vcs) FROM ValueChainSemiProduct vcs " +
                                        "WHERE vcs.valueChain = :vc AND vcs.semiProduct = :sp",
                                Long.class)
                        .setParameter("vc", vc)
                        .setParameter("sp", sp)
                        .getSingleResult();

                if (existingCount != null && existingCount > 0) {
                    continue; // link already exists
                }

                ValueChainSemiProduct link = new ValueChainSemiProduct(vc, sp);
                em.persist(link);
            }
        }
    }
}
