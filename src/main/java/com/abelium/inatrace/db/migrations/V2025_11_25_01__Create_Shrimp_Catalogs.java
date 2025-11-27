package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.*;
import com.abelium.inatrace.db.enums.CodebookStatus;
import com.abelium.inatrace.db.enums.ShrimpSizeType;
import com.abelium.inatrace.types.Language;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;

/**
 * Migration to create shrimp-specific catalogs.
 * <p>
 * This migration ONLY runs when INATrace.product.type is SHRIMP or CAMARON.
 * It creates the following catalogs:
 * <ul>
 *   <li>ShrimpFlavorDefect - Defectos de sabor (Arena, Palo, Tierra, Lodo, Combustible)</li>
 *   <li>ShrimpSizeGrade - Tallas de camarón (WHOLE and TAIL sizes)</li>
 *   <li>ShrimpColorGrade - Grados de color (A-1, A-2, A-3, A-4)</li>
 *   <li>ShrimpProcessType - Tipos de proceso (Head-On, Shell-On, Value Added)</li>
 * </ul>
 * <p>
 * This migration is idempotent and can be safely re-run.
 *
 * @author INATrace Team
 */
public class V2025_11_25_01__Create_Shrimp_Catalogs implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        String productTypeConfig = environment.getProperty("INATrace.product.type", "COFFEE");

        // Only run for SHRIMP deployments
        if (!"SHRIMP".equalsIgnoreCase(productTypeConfig) && !"CAMARON".equalsIgnoreCase(productTypeConfig)) {
            return;
        }

        // Create all shrimp catalogs
        createFlavorDefects(em);
        createSizeGrades(em);
        createColorGrades(em);
        createProcessTypes(em);
    }

    /**
     * Creates ShrimpFlavorDefect catalog with translations.
     */
    private void createFlavorDefects(EntityManager em) {
        createFlavorDefect(em, "ARENA", "Sand", "Arena", "Sandy taste in shrimp", "Sabor a arena en el camarón", 1);
        createFlavorDefect(em, "PALO", "Wood", "Palo", "Woody taste in shrimp", "Sabor a madera/palo en el camarón", 2);
        createFlavorDefect(em, "TIERRA", "Earth", "Tierra", "Earthy taste in shrimp", "Sabor terroso en el camarón", 3);
        createFlavorDefect(em, "LODO", "Mud", "Lodo", "Muddy taste in shrimp", "Sabor a lodo en el camarón", 4);
        createFlavorDefect(em, "COMBUSTIBLE", "Iodine", "Combustible", "Iodine taste in shrimp", "Sabor a Combustible en el camarón", 5);
        createFlavorDefect(em, "OTHER", "Other", "Otro", "Other flavor defect", "Otro defecto de sabor", 6);
    }

    private void createFlavorDefect(EntityManager em, String code, String nameEn, String nameEs,
                                    String descEn, String descEs, int order) {
        // Check if already exists
        Long count = em.createQuery("SELECT COUNT(sfd) FROM ShrimpFlavorDefect sfd WHERE sfd.code = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult();
        if (count > 0) return;

        ShrimpFlavorDefect defect = new ShrimpFlavorDefect();
        defect.setCode(code);
        defect.setName(nameEn);
        defect.setDescription(descEn);
        defect.setDisplayOrder(order);
        defect.setStatus(CodebookStatus.ACTIVE);
        em.persist(defect);

        // English translation
        ShrimpFlavorDefectTranslation translationEn = new ShrimpFlavorDefectTranslation();
        translationEn.setShrimpFlavorDefect(defect);
        translationEn.setLanguage(Language.EN);
        translationEn.setName(nameEn);
        translationEn.setDescription(descEn);
        em.persist(translationEn);

        // Spanish translation
        ShrimpFlavorDefectTranslation translationEs = new ShrimpFlavorDefectTranslation();
        translationEs.setShrimpFlavorDefect(defect);
        translationEs.setLanguage(Language.ES);
        translationEs.setName(nameEs);
        translationEs.setDescription(descEs);
        em.persist(translationEs);
    }

    /**
     * Creates ShrimpSizeGrade catalog.
     */
    private void createSizeGrades(EntityManager em) {
        // WHOLE sizes (Head-On / Camarón Entero)
        createSizeGrade(em, "WHOLE_20_30", "20-30", ShrimpSizeType.WHOLE, 1);
        createSizeGrade(em, "WHOLE_30_40", "30-40", ShrimpSizeType.WHOLE, 2);
        createSizeGrade(em, "WHOLE_40_50", "40-50", ShrimpSizeType.WHOLE, 3);
        createSizeGrade(em, "WHOLE_50_60", "50-60", ShrimpSizeType.WHOLE, 4);
        createSizeGrade(em, "WHOLE_60_70", "60-70", ShrimpSizeType.WHOLE, 5);
        createSizeGrade(em, "WHOLE_70_80", "70-80", ShrimpSizeType.WHOLE, 6);
        createSizeGrade(em, "WHOLE_80_100", "80-100", ShrimpSizeType.WHOLE, 7);
        createSizeGrade(em, "WHOLE_100_120", "100-120", ShrimpSizeType.WHOLE, 8);
        createSizeGrade(em, "WHOLE_POMADA", "Pomada", ShrimpSizeType.WHOLE, 9);

        // TAIL sizes (Shell-On / Value Added)
        createSizeGrade(em, "TAIL_U_7", "U-7", ShrimpSizeType.TAIL, 1);
        createSizeGrade(em, "TAIL_U_10", "U-10", ShrimpSizeType.TAIL, 2);
        createSizeGrade(em, "TAIL_U_12", "U-12", ShrimpSizeType.TAIL, 3);
        createSizeGrade(em, "TAIL_13_15", "13-15", ShrimpSizeType.TAIL, 4);
        createSizeGrade(em, "TAIL_16_20", "16-20", ShrimpSizeType.TAIL, 5);
        createSizeGrade(em, "TAIL_21_25", "21-25", ShrimpSizeType.TAIL, 6);
        createSizeGrade(em, "TAIL_26_30", "26-30", ShrimpSizeType.TAIL, 7);
        createSizeGrade(em, "TAIL_31_35", "31-35", ShrimpSizeType.TAIL, 8);
        createSizeGrade(em, "TAIL_36_40", "36-40", ShrimpSizeType.TAIL, 9);
        createSizeGrade(em, "TAIL_41_50", "41-50", ShrimpSizeType.TAIL, 10);
        createSizeGrade(em, "TAIL_51_60", "51-60", ShrimpSizeType.TAIL, 11);
        createSizeGrade(em, "TAIL_61_70", "61-70", ShrimpSizeType.TAIL, 12);
        createSizeGrade(em, "TAIL_71_90", "71-90", ShrimpSizeType.TAIL, 13);
        createSizeGrade(em, "TAIL_91_110", "91-110", ShrimpSizeType.TAIL, 14);
        createSizeGrade(em, "TAIL_111_130", "111-130", ShrimpSizeType.TAIL, 15);
        createSizeGrade(em, "TAIL_131_150", "131-150", ShrimpSizeType.TAIL, 16);
        createSizeGrade(em, "TAIL_150_UP", "150-UP", ShrimpSizeType.TAIL, 17);
    }

    private void createSizeGrade(EntityManager em, String code, String label, ShrimpSizeType sizeType, int order) {
        Long count = em.createQuery("SELECT COUNT(ssg) FROM ShrimpSizeGrade ssg WHERE ssg.code = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult();
        if (count > 0) return;

        ShrimpSizeGrade grade = new ShrimpSizeGrade();
        grade.setCode(code);
        grade.setLabel(label);
        grade.setSizeType(sizeType);
        grade.setDisplayOrder(order);
        grade.setStatus(CodebookStatus.ACTIVE);
        em.persist(grade);
    }

    /**
     * Creates ShrimpColorGrade catalog.
     */
    private void createColorGrades(EntityManager em) {
        createColorGrade(em, "A_1", "A-1", "Premium color grade (highest)", 1);
        createColorGrade(em, "A_2", "A-2", "Standard color grade", 2);
        createColorGrade(em, "A_3", "A-3", "Secondary color grade", 3);
        createColorGrade(em, "A_4", "A-4", "Lower color grade", 4);
    }

    private void createColorGrade(EntityManager em, String code, String label, String description, int order) {
        Long count = em.createQuery("SELECT COUNT(scg) FROM ShrimpColorGrade scg WHERE scg.code = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult();
        if (count > 0) return;

        ShrimpColorGrade grade = new ShrimpColorGrade();
        grade.setCode(code);
        grade.setLabel(label);
        grade.setDescription(description);
        grade.setDisplayOrder(order);
        grade.setStatus(CodebookStatus.ACTIVE);
        em.persist(grade);
    }

    /**
     * Creates ShrimpProcessType catalog with translations.
     */
    private void createProcessTypes(EntityManager em) {
        createProcessType(em, "HEAD_ON", "Head-On", "Con Cabeza",
                "Whole shrimp with head", "Camarón entero con cabeza", 1);
        createProcessType(em, "SHELL_ON", "Shell-On", "Cola con Cáscara",
                "Headless shrimp with shell", "Camarón descabezado con cáscara", 2);
        createProcessType(em, "VALUE_ADDED", "Value Added", "Valor Agregado",
                "Peeled, deveined, or processed shrimp", "Camarón pelado, desvenado o procesado", 3);
    }

    private void createProcessType(EntityManager em, String code, String nameEn, String nameEs,
                                   String descEn, String descEs, int order) {
        Long count = em.createQuery("SELECT COUNT(spt) FROM ShrimpProcessType spt WHERE spt.code = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult();
        if (count > 0) return;

        ShrimpProcessType processType = new ShrimpProcessType();
        processType.setCode(code);
        processType.setName(nameEn);
        processType.setDescription(descEn);
        processType.setDisplayOrder(order);
        processType.setStatus(CodebookStatus.ACTIVE);
        em.persist(processType);

        // English translation
        ShrimpProcessTypeTranslation translationEn = new ShrimpProcessTypeTranslation();
        translationEn.setShrimpProcessType(processType);
        translationEn.setLanguage(Language.EN);
        translationEn.setName(nameEn);
        translationEn.setDescription(descEn);
        em.persist(translationEn);

        // Spanish translation
        ShrimpProcessTypeTranslation translationEs = new ShrimpProcessTypeTranslation();
        translationEs.setShrimpProcessType(processType);
        translationEs.setLanguage(Language.ES);
        translationEs.setName(nameEs);
        translationEs.setDescription(descEs);
        em.persist(translationEs);
    }
}
