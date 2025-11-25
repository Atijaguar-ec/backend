package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.*;
import com.abelium.inatrace.db.enums.CodebookStatus;
import com.abelium.inatrace.db.enums.ShrimpPresentationCategory;
import com.abelium.inatrace.db.enums.ShrimpSizeType;
import com.abelium.inatrace.types.Language;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;

/**
 * Migration to complete all shrimp catalogs based on real operational documents.
 * <p>
 * Adds:
 * - Missing TAIL sizes (36-40, 71-90, 91-110, 110-130, 130, 150)
 * - BROKEN_VS to ShrimpPresentationType
 * - ShrimpQualityGrade catalog (A, B, C)
 * - ShrimpTreatmentType catalog (Bisulfito, Metabisulfito, Sin tratamiento)
 * <p>
 * Based on analysis of documents: 0073808, A-3, B-3, 71694, 71695 (Lote 1662)
 *
 * @author INATrace Team
 */
public class V2025_11_25_03__Complete_Shrimp_Catalogs implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        String productTypeConfig = environment.getProperty("INATrace.product.type", "COFFEE");

        if (!"SHRIMP".equalsIgnoreCase(productTypeConfig) && !"CAMARON".equalsIgnoreCase(productTypeConfig)) {
            return;
        }

        addMissingSizeGrades(em);
        addBrokenVsPresentation(em);
        createQualityGrades(em);
        createTreatmentTypes(em);
    }

    /**
     * Add missing TAIL size grades based on real liquidation documents
     */
    private void addMissingSizeGrades(EntityManager em) {
        // TAIL sizes missing from original migration
        createSizeGradeIfNotExists(em, "TAIL_36_40", "36-40", ShrimpSizeType.TAIL, 8);
        createSizeGradeIfNotExists(em, "TAIL_71_90", "71-90", ShrimpSizeType.TAIL, 12);
        createSizeGradeIfNotExists(em, "TAIL_91_110", "91-110", ShrimpSizeType.TAIL, 13);
        createSizeGradeIfNotExists(em, "TAIL_110_130", "110-130", ShrimpSizeType.TAIL, 14);
        createSizeGradeIfNotExists(em, "TAIL_130", "130", ShrimpSizeType.TAIL, 15);
        createSizeGradeIfNotExists(em, "TAIL_150", "150", ShrimpSizeType.TAIL, 16);
    }

    private void createSizeGradeIfNotExists(EntityManager em, String code, String label, ShrimpSizeType type, int order) {
        Long count = em.createQuery("SELECT COUNT(s) FROM ShrimpSizeGrade s WHERE s.code = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult();
        if (count > 0) return;

        ShrimpSizeGrade grade = new ShrimpSizeGrade();
        grade.setCode(code);
        grade.setLabel(label);
        grade.setSizeType(type);
        grade.setDisplayOrder(order);
        grade.setStatus(CodebookStatus.ACTIVE);
        em.persist(grade);

        // Translations
        ShrimpSizeGradeTranslation transEn = new ShrimpSizeGradeTranslation();
        transEn.setShrimpSizeGrade(grade);
        transEn.setLanguage(Language.EN);
        transEn.setName(label);
        em.persist(transEn);

        ShrimpSizeGradeTranslation transEs = new ShrimpSizeGradeTranslation();
        transEs.setShrimpSizeGrade(grade);
        transEs.setLanguage(Language.ES);
        transEs.setName(label);
        em.persist(transEs);
    }

    /**
     * Add BROKEN_VS to ShrimpPresentationType
     */
    private void addBrokenVsPresentation(EntityManager em) {
        Long count = em.createQuery("SELECT COUNT(spt) FROM ShrimpPresentationType spt WHERE spt.code = :code", Long.class)
                .setParameter("code", "BROKEN_VS")
                .getSingleResult();
        if (count > 0) return;

        ShrimpPresentationType pt = new ShrimpPresentationType();
        pt.setCode("BROKEN_VS");
        pt.setLabel("Broken vs");
        pt.setCategory(ShrimpPresentationCategory.BROKEN);
        pt.setDescription("Broken pieces - vs (variable size)");
        pt.setDisplayOrder(0);
        pt.setStatus(CodebookStatus.ACTIVE);
        em.persist(pt);

        ShrimpPresentationTypeTranslation transEn = new ShrimpPresentationTypeTranslation();
        transEn.setShrimpPresentationType(pt);
        transEn.setLanguage(Language.EN);
        transEn.setLabel("Broken vs");
        transEn.setDescription("Broken pieces - variable size");
        em.persist(transEn);

        ShrimpPresentationTypeTranslation transEs = new ShrimpPresentationTypeTranslation();
        transEs.setShrimpPresentationType(pt);
        transEs.setLanguage(Language.ES);
        transEs.setLabel("Quebrado vs");
        transEs.setDescription("Quebrado - tamaño variable");
        em.persist(transEs);
    }

    /**
     * Create ShrimpQualityGrade catalog (A, B, C)
     */
    private void createQualityGrades(EntityManager em) {
        createQualityGradeIfNotExists(em, "A", "Class A", "Clase A", 
                "First quality - premium grade", "Primera calidad - grado premium", 1);
        createQualityGradeIfNotExists(em, "B", "Class B", "Clase B",
                "Second quality - standard grade", "Segunda calidad - grado estándar", 2);
        createQualityGradeIfNotExists(em, "C", "Class C", "Clase C",
                "Third quality - other", "Tercera calidad - otros", 3);
    }

    private void createQualityGradeIfNotExists(EntityManager em, String code, String labelEn, String labelEs,
                                                String descEn, String descEs, int order) {
        Long count = em.createQuery("SELECT COUNT(q) FROM ShrimpQualityGrade q WHERE q.code = :code", Long.class)
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

        ShrimpQualityGradeTranslation transEn = new ShrimpQualityGradeTranslation();
        transEn.setShrimpQualityGrade(grade);
        transEn.setLanguage(Language.EN);
        transEn.setLabel(labelEn);
        transEn.setDescription(descEn);
        em.persist(transEn);

        ShrimpQualityGradeTranslation transEs = new ShrimpQualityGradeTranslation();
        transEs.setShrimpQualityGrade(grade);
        transEs.setLanguage(Language.ES);
        transEs.setLabel(labelEs);
        transEs.setDescription(descEs);
        em.persist(transEs);
    }

    /**
     * Create ShrimpTreatmentType catalog
     */
    private void createTreatmentTypes(EntityManager em) {
        createTreatmentTypeIfNotExists(em, "BISULFITO", "Bisulfite", "Bisulfito",
                "Bisulfite treatment for preservation", "Tratamiento con bisulfito para preservación", 1);
        createTreatmentTypeIfNotExists(em, "METABISULFITO", "Metabisulfite", "Metabisulfito",
                "Metabisulfite treatment for preservation", "Tratamiento con metabisulfito para preservación", 2);
        createTreatmentTypeIfNotExists(em, "SIN_TRATAMIENTO", "No Treatment", "Sin Tratamiento",
                "No chemical treatment applied", "Sin tratamiento químico", 3);
    }

    private void createTreatmentTypeIfNotExists(EntityManager em, String code, String labelEn, String labelEs,
                                                 String descEn, String descEs, int order) {
        Long count = em.createQuery("SELECT COUNT(t) FROM ShrimpTreatmentType t WHERE t.code = :code", Long.class)
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

        ShrimpTreatmentTypeTranslation transEn = new ShrimpTreatmentTypeTranslation();
        transEn.setShrimpTreatmentType(treatment);
        transEn.setLanguage(Language.EN);
        transEn.setLabel(labelEn);
        transEn.setDescription(descEn);
        em.persist(transEn);

        ShrimpTreatmentTypeTranslation transEs = new ShrimpTreatmentTypeTranslation();
        transEs.setShrimpTreatmentType(treatment);
        transEs.setLanguage(Language.ES);
        transEs.setLabel(labelEs);
        transEs.setDescription(descEs);
        em.persist(transEs);
    }
}
