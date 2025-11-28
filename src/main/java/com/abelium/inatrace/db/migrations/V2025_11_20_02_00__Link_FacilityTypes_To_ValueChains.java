package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.FacilityType;
import com.abelium.inatrace.db.entities.value_chain.ValueChain;
import com.abelium.inatrace.db.entities.value_chain.ValueChainFacilityType;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Migration to automatically link all FacilityTypes to all ValueChains.
 * <p>
 * This migration ensures that all value chains have access to all facility types
 * that have been prefilled based on the product type configuration. This provides
 * a complete initial setup where all facilities are available to all value chains.
 * <p>
 * This migration is idempotent and will not create duplicate links.
 *
 * @author INATrace Team
 */
public class V2025_11_20_02_00__Link_FacilityTypes_To_ValueChains implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        // Fetch all value chains and facility types
        List<ValueChain> valueChains = em.createQuery("SELECT vc FROM ValueChain vc", ValueChain.class)
                .getResultList();
        List<FacilityType> facilityTypes = em.createQuery("SELECT ft FROM FacilityType ft", FacilityType.class)
                .getResultList();

        // Skip if no value chains or facility types exist
        if (valueChains.isEmpty() || facilityTypes.isEmpty()) {
            return;
        }

        // Create links for each combination of value chain and facility type
        for (ValueChain vc : valueChains) {
            for (FacilityType ft : facilityTypes) {
                // Check if link already exists to ensure idempotency
                Long existingCount = em.createQuery(
                                "SELECT COUNT(vcft) FROM ValueChainFacilityType vcft " +
                                        "WHERE vcft.valueChain = :vc AND vcft.facilityType = :ft",
                                Long.class)
                        .setParameter("vc", vc)
                        .setParameter("ft", ft)
                        .getSingleResult();

                if (existingCount != null && existingCount > 0) {
                    continue; // link already exists
                }

                ValueChainFacilityType link = new ValueChainFacilityType(vc, ft);
                em.persist(link);
            }
        }
    }
}
