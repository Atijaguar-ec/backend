package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.facility.Facility;
import com.abelium.inatrace.db.entities.facility.FacilityTranslation;
import com.abelium.inatrace.tools.Queries;
import com.abelium.inatrace.types.Language;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * @author Nejc Rebernik, Sunesis d.o.o.
 */
public class V2021_10_21_14_33__Update_Facility_Translations implements JpaMigration {
    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        List<Object[]> facilityRows = em
                .createQuery("SELECT f.id, f.name FROM Facility f", Object[].class)
                .getResultList();

        for (Object[] row : facilityRows) {
            Long facilityId = (Long) row[0];
            String facilityName = (String) row[1];
            Facility facilityRef = em.getReference(Facility.class, facilityId);

            for (Language language : List.of(Language.EN, Language.ES)) {
                FacilityTranslation facilityTranslation = new FacilityTranslation();
                facilityTranslation.setFacility(facilityRef);
                facilityTranslation.setLanguage(language);
                facilityTranslation.setName(facilityName);
                em.persist(facilityTranslation);
            }
        }
    }
}
