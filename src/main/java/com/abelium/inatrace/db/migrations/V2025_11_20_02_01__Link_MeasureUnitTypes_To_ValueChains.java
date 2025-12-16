package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.MeasureUnitType;
import com.abelium.inatrace.db.entities.value_chain.ValueChain;
import com.abelium.inatrace.db.entities.value_chain.ValueChainMeasureUnitType;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Migration to automatically link all MeasureUnitTypes to all ValueChains.
 * <p>
 * This migration ensures that all value chains have access to all measurement unit types
 * that have been prefilled based on the product type configuration. This provides
 * a complete initial setup where all measurement units are available to all value chains.
 * <p>
 * This migration is idempotent and will not create duplicate links.
 *
 * @author INATrace Team
 */
public class V2025_11_20_02_01__Link_MeasureUnitTypes_To_ValueChains implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        // Fetch all value chains and measure unit types
        List<ValueChain> valueChains = em.createQuery("SELECT vc FROM ValueChain vc", ValueChain.class)
                .getResultList();
        List<MeasureUnitType> measureUnitTypes = em.createQuery("SELECT mut FROM MeasureUnitType mut", MeasureUnitType.class)
                .getResultList();

        // Skip if no value chains or measure unit types exist
        if (valueChains.isEmpty() || measureUnitTypes.isEmpty()) {
            return;
        }

        // Create links for each combination of value chain and measure unit type
        for (ValueChain vc : valueChains) {
            for (MeasureUnitType mut : measureUnitTypes) {
                // Check if link already exists to ensure idempotency
                Long existingCount = em.createQuery(
                                "SELECT COUNT(vcm) FROM ValueChainMeasureUnitType vcm " +
                                        "WHERE vcm.valueChain = :vc AND vcm.measureUnitType = :mut",
                                Long.class)
                        .setParameter("vc", vc)
                        .setParameter("mut", mut)
                        .getSingleResult();

                if (existingCount != null && existingCount > 0) {
                    continue; // link already exists
                }

                ValueChainMeasureUnitType link = new ValueChainMeasureUnitType(vc, mut);
                em.persist(link);
            }
        }
    }
}
