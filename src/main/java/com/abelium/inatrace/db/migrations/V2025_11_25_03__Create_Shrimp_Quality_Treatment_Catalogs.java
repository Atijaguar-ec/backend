package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.ShrimpQualityGrade;
import com.abelium.inatrace.db.entities.codebook.ShrimpQualityGradeTranslation;
import com.abelium.inatrace.db.entities.codebook.ShrimpTreatmentType;
import com.abelium.inatrace.db.entities.codebook.ShrimpTreatmentTypeTranslation;
import com.abelium.inatrace.db.enums.CodebookStatus;
import com.abelium.inatrace.types.Language;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;

/**
 * Migration to create shrimp quality grade and treatment type catalogs.
 * <p>
 * This migration ONLY runs when INATrace.product.type is SHRIMP or CAMARON.
 * It creates the following catalogs:
 * <ul>
 *   <li>ShrimpQualityGrade - Grados de calidad (A, B, C)</li>
 *   <li>ShrimpTreatmentType - Tipos de tratamiento (Bisulfito, Metabisulfito, etc.)</li>
 * </ul>
 * <p>
 * This migration is idempotent and can be safely re-run.
 *
 * @author INATrace Team
 */
public class V2025_11_25_03__Create_Shrimp_Quality_Treatment_Catalogs implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        String productTypeConfig = environment.getProperty("INATrace.product.type", "COFFEE");

        // Only run for SHRIMP deployments
        if (!"SHRIMP".equalsIgnoreCase(productTypeConfig) && !"CAMARON".equalsIgnoreCase(productTypeConfig)) {
            return;
        }

        // Create catalogs
        createQualityGrades(em);
        createTreatmentTypes(em);
    }

    /**
     * Creates ShrimpQualityGrade catalog with translations.
     * Quality grades: A (Primera), B (Segunda), C (Otros)
     */
    private void createQualityGrades(EntityManager em) {
        createQualityGrade(em, "A", "Grade A", "Grado A",
                "First quality - Premium grade", "Primera calidad - Grado premium", 1);
        createQualityGrade(em, "B", "Grade B", "Grado B",
                "Second quality - Standard grade", "Segunda calidad - Grado estándar", 2);
        createQualityGrade(em, "C", "Grade C", "Grado C",
                "Other quality - Lower grade", "Otra calidad - Grado inferior", 3);
    }

    private void createQualityGrade(EntityManager em, String code, String labelEn, String labelEs,
                                    String descEn, String descEs, int order) {
        // Check if already exists
        Long count = em.createQuery("SELECT COUNT(sqg) FROM ShrimpQualityGrade sqg WHERE sqg.code = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult();
        if (count > 0) return;

        ShrimpQualityGrade grade = new ShrimpQualityGrade();
        grade.setCode(code);
        grade.setLabel(labelEn);
        grade.setDescription(descEn);
        grade.setDisplayOrder(order);
        grade.setStatus(CodebookStatus.ACTIVE);
        em.persist(grade);

        // English translation
        ShrimpQualityGradeTranslation translationEn = new ShrimpQualityGradeTranslation();
        translationEn.setShrimpQualityGrade(grade);
        translationEn.setLanguage(Language.EN);
        translationEn.setLabel(labelEn);
        translationEn.setDescription(descEn);
        em.persist(translationEn);

        // Spanish translation
        ShrimpQualityGradeTranslation translationEs = new ShrimpQualityGradeTranslation();
        translationEs.setShrimpQualityGrade(grade);
        translationEs.setLanguage(Language.ES);
        translationEs.setLabel(labelEs);
        translationEs.setDescription(descEs);
        em.persist(translationEs);
    }

    /**
     * Creates ShrimpTreatmentType catalog with translations.
     * Treatment types: Bisulfito, Metabisulfito, Ninguno, Otro
     */
    private void createTreatmentTypes(EntityManager em) {
        createTreatmentType(em, "BISULFITO", "Bisulfite", "Bisulfito",
                "Sodium bisulfite treatment", "Tratamiento con bisulfito de sodio", 1);
        createTreatmentType(em, "METABISULFITO", "Metabisulfite", "Metabisulfito",
                "Sodium metabisulfite treatment", "Tratamiento con metabisulfito de sodio", 2);
        createTreatmentType(em, "NINGUNO", "None", "Ninguno",
                "No chemical treatment applied", "Sin tratamiento químico aplicado", 3);
        createTreatmentType(em, "OTRO", "Other", "Otro",
                "Other treatment type", "Otro tipo de tratamiento", 4);
    }

    private void createTreatmentType(EntityManager em, String code, String labelEn, String labelEs,
                                     String descEn, String descEs, int order) {
        // Check if already exists
        Long count = em.createQuery("SELECT COUNT(stt) FROM ShrimpTreatmentType stt WHERE stt.code = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult();
        if (count > 0) return;

        ShrimpTreatmentType treatment = new ShrimpTreatmentType();
        treatment.setCode(code);
        treatment.setLabel(labelEn);
        treatment.setDescription(descEn);
        treatment.setDisplayOrder(order);
        treatment.setStatus(CodebookStatus.ACTIVE);
        em.persist(treatment);

        // English translation
        ShrimpTreatmentTypeTranslation translationEn = new ShrimpTreatmentTypeTranslation();
        translationEn.setShrimpTreatmentType(treatment);
        translationEn.setLanguage(Language.EN);
        translationEn.setLabel(labelEn);
        translationEn.setDescription(descEn);
        em.persist(translationEn);

        // Spanish translation
        ShrimpTreatmentTypeTranslation translationEs = new ShrimpTreatmentTypeTranslation();
        translationEs.setShrimpTreatmentType(treatment);
        translationEs.setLanguage(Language.ES);
        translationEs.setLabel(labelEs);
        translationEs.setDescription(descEs);
        em.persist(translationEs);
    }
}
