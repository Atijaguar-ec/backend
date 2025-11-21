package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.common.UserCustomer;
import com.abelium.inatrace.db.entities.common.UserCustomerProductType;
import com.abelium.inatrace.db.entities.codebook.ProductType;
import com.abelium.inatrace.db.entities.value_chain.ValueChain;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Migration to create and assign the primary ProductType based on the deployment configuration.
 * <p>
 * This migration reads the environment variable INATrace.product.type and creates or reuses
 * the appropriate ProductType (Coffee, Cacao, or Camarón/Shrimp). It then assigns this
 * ProductType to all existing ValueChains and UserCustomers.
 * <p>
 * Supported product types:
 * <ul>
 *   <li>COFFEE - Creates "Coffee" ProductType (default) and "Macadamia" as secondary</li>
 *   <li>COCOA - Creates "Cacao" ProductType</li>
 *   <li>SHRIMP/CAMARON - Creates "Camarón" ProductType</li>
 * </ul>
 * <p>
 * This migration is idempotent and can be safely re-run.
 *
 * @author INATrace Team
 */
public class V2023_03_09_14_33__Update_Value_Chains_Add_Coffee_Product_Type implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        String productTypeConfig = environment.getProperty("INATrace.product.type", "COFFEE");
        String primaryName;
        String primaryDescription;

        switch (productTypeConfig.toUpperCase()) {
            case "COCOA":
                primaryName = "Cacao";
                primaryDescription = "Cacao product type";
                break;
            case "SHRIMP":
            case "CAMARON":
                primaryName = "Camarón";
                primaryDescription = "Shrimp product type";
                break;
            case "COFFEE":
            default:
                primaryName = "Coffee";
                primaryDescription = "Coffee product type";
                break;
        }

        // Ensure primary product type exists (or reuse existing one by name)
        ProductType primaryProductType = findOrCreateProductTypeByName(em, primaryName, primaryDescription);

        // Assign primary product type to all value chains
        for (ValueChain valueChain : em.createQuery("SELECT vc FROM ValueChain vc", ValueChain.class).getResultList()) {
            valueChain.setProductType(primaryProductType);
        }

        // For COFFEE deployments keep Macadamia as a secondary product type (if needed)
        if ("COFFEE".equalsIgnoreCase(productTypeConfig)) {
            findOrCreateProductTypeByName(em, "Macadamia", "Macadamia product type");
        }

        // Ensure all farmers (userCustomers) are linked to the primary product type
        // Optimized: fetch only IDs of users that don't have the link yet
        List<Long> userCustomerIdsWithoutLink = em.createQuery(
                "SELECT u.id FROM UserCustomer u WHERE u.id NOT IN " +
                "(SELECT ucpt.userCustomer.id FROM UserCustomerProductType ucpt WHERE ucpt.productType = :productType)",
                Long.class)
                .setParameter("productType", primaryProductType)
                .getResultList();

        for (Long userCustomerId : userCustomerIdsWithoutLink) {
            UserCustomer userCustomerRef = em.getReference(UserCustomer.class, userCustomerId);
            UserCustomerProductType userCustomerProductType = new UserCustomerProductType();
            userCustomerProductType.setUserCustomer(userCustomerRef);
            userCustomerProductType.setProductType(primaryProductType);
            em.persist(userCustomerProductType);
        }
    }

    /**
     * Finds an existing ProductType by name or creates a new one if it doesn't exist.
     * <p>
     * This method ensures idempotency by checking for existing ProductTypes before creating.
     *
     * @param em EntityManager for database operations
     * @param name The name of the ProductType
     * @param description The description of the ProductType
     * @return The existing or newly created ProductType
     */
    private ProductType findOrCreateProductTypeByName(EntityManager em, String name, String description) {
        List<ProductType> existing = em.createQuery(
                "SELECT pt FROM ProductType pt WHERE pt.name = :name", ProductType.class)
                .setParameter("name", name)
                .setMaxResults(1)
                .getResultList();

        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        ProductType productType = new ProductType();
        productType.setName(name);
        productType.setDescription(description);
        em.persist(productType);
        return productType;
    }
}
