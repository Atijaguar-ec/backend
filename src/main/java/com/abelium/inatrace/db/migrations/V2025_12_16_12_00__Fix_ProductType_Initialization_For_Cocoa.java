package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.ProductType;
import com.abelium.inatrace.db.entities.codebook.ProductTypeTranslation;
import com.abelium.inatrace.db.entities.common.UserCustomerProductType;
import com.abelium.inatrace.db.entities.value_chain.ValueChain;
import com.abelium.inatrace.types.Language;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.core.env.Environment;

public class V2025_12_16_12_00__Fix_ProductType_Initialization_For_Cocoa implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {

        String configuredProductType = environment.getProperty("INATrace.product.type", "COFFEE");
        if (!"COCOA".equalsIgnoreCase(configuredProductType)) {
            return;
        }

        ProductType cacaoProductType = findProductType(em, "CACAO", "Cacao");
        if (cacaoProductType == null) {
            cacaoProductType = new ProductType();
            cacaoProductType.setCode("CACAO");
            cacaoProductType.setName("Cacao");
            cacaoProductType.setDescription("Cacao product type");
            em.persist(cacaoProductType);
        }

        ensureTranslations(em, cacaoProductType);

        ProductType coffeeProductType = findProductType(em, "COFFEE", "Coffee");
        if (coffeeProductType == null) {
            return;
        }

        for (ValueChain valueChain : em.createQuery("SELECT vc FROM ValueChain vc", ValueChain.class).getResultList()) {
            if (valueChain.getProductType() != null && coffeeProductType.getId() != null
                    && coffeeProductType.getId().equals(valueChain.getProductType().getId())) {
                valueChain.setProductType(cacaoProductType);
            }
        }

        List<UserCustomerProductType> userCustomerProductTypes = em.createQuery(
                        "SELECT ucpt FROM UserCustomerProductType ucpt WHERE ucpt.productType = :pt", UserCustomerProductType.class)
                .setParameter("pt", coffeeProductType)
                .getResultList();

        for (UserCustomerProductType userCustomerProductType : userCustomerProductTypes) {
            Long existingCount = em.createQuery(
                            "SELECT COUNT(ucpt) FROM UserCustomerProductType ucpt WHERE ucpt.userCustomer = :uc AND ucpt.productType = :pt",
                            Long.class)
                    .setParameter("uc", userCustomerProductType.getUserCustomer())
                    .setParameter("pt", cacaoProductType)
                    .getSingleResult();

            if (existingCount != null && existingCount > 0L) {
                em.remove(userCustomerProductType);
            } else {
                userCustomerProductType.setProductType(cacaoProductType);
            }
        }
    }

    private ProductType findProductType(EntityManager em, String code, String name) {
        List<ProductType> productTypes = em.createQuery(
                        "SELECT pt FROM ProductType pt WHERE UPPER(pt.code) = :code OR UPPER(pt.name) = :name", ProductType.class)
                .setParameter("code", code.toUpperCase())
                .setParameter("name", name.toUpperCase())
                .getResultList();

        return productTypes.isEmpty() ? null : productTypes.get(0);
    }

    private void ensureTranslations(EntityManager em, ProductType productType) {
        for (Language language : List.of(Language.EN, Language.ES)) {
            Long existingCount = em.createQuery(
                            "SELECT COUNT(t) FROM ProductTypeTranslation t WHERE t.productType = :pt AND t.language = :lang", Long.class)
                    .setParameter("pt", productType)
                    .setParameter("lang", language)
                    .getSingleResult();

            if (existingCount == null || existingCount == 0L) {
                ProductTypeTranslation translation = new ProductTypeTranslation();
                translation.setName(productType.getName());
                translation.setDescription(productType.getDescription());
                translation.setLanguage(language);
                translation.setProductType(productType);
                em.persist(translation);
            }
        }
    }
}
