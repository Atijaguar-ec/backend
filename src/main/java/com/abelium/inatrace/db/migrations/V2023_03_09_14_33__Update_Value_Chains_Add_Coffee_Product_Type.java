package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.common.UserCustomer;
import com.abelium.inatrace.db.entities.common.UserCustomerProductType;
import com.abelium.inatrace.db.entities.codebook.ProductType;
import com.abelium.inatrace.db.entities.value_chain.ValueChain;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;
import java.util.List;

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
            case "COFFEE":
            default:
                primaryName = "Coffee";
                primaryDescription = "Coffee product type";
                break;
        }

        ProductType primaryProductType = findOrCreateProductTypeByName(em, primaryName, primaryDescription);

        for (ValueChain valueChain : em.createQuery("SELECT vc FROM ValueChain vc", ValueChain.class).getResultList()) {
            valueChain.setProductType(primaryProductType);
        }

        if ("COFFEE".equalsIgnoreCase(productTypeConfig)) {
            findOrCreateProductTypeByName(em, "Macadamia", "Macadamia product type");
        }

        List<Long> userCustomerIdsWithoutLink = em.createQuery(
                        "SELECT u.id FROM UserCustomer u WHERE u.id NOT IN "
                                + "(SELECT ucpt.userCustomer.id FROM UserCustomerProductType ucpt WHERE ucpt.productType = :productType)",
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
