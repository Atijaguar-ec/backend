package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.CurrencyType;
import jakarta.persistence.EntityManager;
import org.springframework.core.env.Environment;

/**
 * Migration to ensure that the USD currency exists in the CurrencyType codebook.
 *
 * This migration is idempotent: if a CurrencyType with code "USD" already exists,
 * it does nothing. Otherwise it creates an enabled entry with label "US Dollar".
 */
public class V2025_11_25_06__Ensure_USD_CurrencyType implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        Long count = em.createQuery(
                        "SELECT COUNT(c) FROM CurrencyType c WHERE c.code = :code",
                        Long.class)
                .setParameter("code", "USD")
                .getSingleResult();

        if (count != null && count > 0) {
            return; // USD already present
        }

        CurrencyType usd = new CurrencyType();
        usd.setCode("USD");
        usd.setLabel("US Dollar");
        usd.setEnabled(Boolean.TRUE);
        em.persist(usd);
    }
}
