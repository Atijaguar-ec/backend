package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import jakarta.persistence.EntityManager;
import org.springframework.core.env.Environment;
import java.util.List;

/**
 * Cleanup legacy Shrimp Facility Types and their associated data.
 * This migration safely removes shrimp-specific FacilityType entries 
 * and related facilities across all branches/environments 
 * to ensure a Cocoa-only business model.
 */
public class V2026_04_08_16_00__Delete_Legacy_Shrimp_FacilityTypes implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        List<String> shrimpCodes = List.of(
            "AREADERECIBO", "LABORATORIODECALIDAD", "TOLVADERECIBO", "AREADECLASIFICADO", 
            "AREADECONGELACION", "TUNELDECONGELACION", "LOTEPARAEXPORTAR", "AREADEPESADO", 
            "AREADEREPOSO", "AREADEDESCABEZADO", "INSPECCIONCAMPO", "AREADELAVADO", 
            "AREADECORTADO", "AREADETRATADO", "AREADECONGELACIONCOLA", "TUNELDECONGELACIONCOLA", 
            "LOTEPARAEXPORTARCOLA", "AREAVARORAGREGADO"
        );

        // 0. Safeguard: Check if the foundational tables exist. If they don't, this database 
        // doesn't have the schema initialized or it was purposely omitted, so nothing to clean up.
        Number baseTablesCount = (Number) em.createNativeQuery(
            "SELECT count(*) FROM information_schema.tables WHERE table_name IN ('facility', 'facility_type') AND table_schema = DATABASE()")
            .getSingleResult();
        
        if (baseTablesCount == null || baseTablesCount.intValue() < 2) {
            return; // Skip migration entirely
        }

        // 1. Delete mapping to value chains, only if the table exists (it might have been removed or never existed in some DBs)
        Number count = (Number) em.createNativeQuery("SELECT count(*) FROM information_schema.tables WHERE table_name = 'value_chain_facility_type' AND table_schema = DATABASE()")
                                  .getSingleResult();
        
        if (count != null && count.intValue() > 0) {
            em.createNativeQuery("DELETE FROM value_chain_facility_type WHERE facility_type_id IN (SELECT id FROM facility_type WHERE code IN (:codes))")
              .setParameter("codes", shrimpCodes)
              .executeUpdate();
        }

        // 2. Fetch all facilities associated with these types to delete their cascading dependencies
        List<Object[]> facilities = em.createNativeQuery(
            "SELECT f.id, f.facility_location_id FROM facility f JOIN facility_type ft ON f.facility_type_id = ft.id WHERE ft.code IN (:codes)")
            .setParameter("codes", shrimpCodes)
            .getResultList();

        for (Object[] f : facilities) {
            Number facilityId = (Number) f[0];
            Number locationId = (Number) f[1];
            
            // Delete translations
            em.createNativeQuery("DELETE FROM facility_translation WHERE facility_id = :fid")
              .setParameter("fid", facilityId)
              .executeUpdate();
              
            // Delete the facility itself
            em.createNativeQuery("DELETE FROM facility WHERE id = :fid")
              .setParameter("fid", facilityId)
              .executeUpdate();
              
            if (locationId != null) {
                // Find address to cascade deletion
                List<Number> addressIds = em.createNativeQuery("SELECT address_id FROM facility_location WHERE id = :lid")
                    .setParameter("lid", locationId)
                    .getResultList();
                    
                em.createNativeQuery("DELETE FROM facility_location WHERE id = :lid")
                  .setParameter("lid", locationId)
                  .executeUpdate();
                  
                for (Number aid : addressIds) {
                    if (aid != null) {
                        em.createNativeQuery("DELETE FROM address WHERE id = :aid")
                          .setParameter("aid", aid)
                          .executeUpdate();
                    }
                }
            }
        }
        
        // 3. Finally, delete the FacilityType entries themselves
        em.createNativeQuery("DELETE FROM facility_type WHERE code IN (:codes)")
          .setParameter("codes", shrimpCodes)
          .executeUpdate();
    }
}
