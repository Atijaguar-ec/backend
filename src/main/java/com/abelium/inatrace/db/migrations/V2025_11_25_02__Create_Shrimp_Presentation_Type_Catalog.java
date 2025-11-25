package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.ShrimpPresentationType;
import com.abelium.inatrace.db.entities.codebook.ShrimpPresentationTypeTranslation;
import com.abelium.inatrace.db.enums.CodebookStatus;
import com.abelium.inatrace.db.enums.ShrimpPresentationCategory;
import com.abelium.inatrace.types.Language;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;

/**
 * Migration to create shrimp presentation type catalog.
 * <p>
 * This migration ONLY runs when INATrace.product.type is SHRIMP or CAMARON.
 * It creates the ShrimpPresentationType catalog used in technical liquidation sheets
 * for final product classification (Shell-On A/B, Broken, TITI/ROJO).
 * <p>
 * This migration is idempotent and can be safely re-run.
 *
 * @author INATrace Team
 */
public class V2025_11_25_02__Create_Shrimp_Presentation_Type_Catalog implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        String productTypeConfig = environment.getProperty("INATrace.product.type", "COFFEE");

        // Only run for SHRIMP deployments
        if (!"SHRIMP".equalsIgnoreCase(productTypeConfig) && !"CAMARON".equalsIgnoreCase(productTypeConfig)) {
            return;
        }

        createPresentationTypes(em);
    }

    /**
     * Creates ShrimpPresentationType catalog with translations.
     */
    private void createPresentationTypes(EntityManager em) {
        // SHELL_ON category - Shell-On grades A and B
        createPresentationType(em, "SHELL_ON_A", "Shell-On A", "Cola A",
                ShrimpPresentationCategory.SHELL_ON,
                "Shell-On Grade A (premium)", "Cola con cáscara Grado A (premium)", 1);
        createPresentationType(em, "SHELL_ON_B", "Shell-On B", "Cola B",
                ShrimpPresentationCategory.SHELL_ON,
                "Shell-On Grade B (standard)", "Cola con cáscara Grado B (estándar)", 2);

        // BROKEN category - Broken pieces by size
        createPresentationType(em, "BROKEN_LARGE", "Broken Large", "Quebrado Grande",
                ShrimpPresentationCategory.BROKEN,
                "Broken pieces - large", "Quebrado - grande", 1);
        createPresentationType(em, "BROKEN_MEDIUM", "Broken Medium", "Quebrado Mediano",
                ShrimpPresentationCategory.BROKEN,
                "Broken pieces - medium", "Quebrado - mediano", 2);
        createPresentationType(em, "BROKEN_SMALL", "Broken Small", "Quebrado Pequeño",
                ShrimpPresentationCategory.BROKEN,
                "Broken pieces - small", "Quebrado - pequeño", 3);

        // OTHER category - Other presentation types
        createPresentationType(em, "OTHER_TITI", "TITI", "TITI",
                ShrimpPresentationCategory.OTHER,
                "Small shrimp variety (TITI)", "Camarón TITI", 1);
        createPresentationType(em, "OTHER_ROJO", "ROJO", "ROJO",
                ShrimpPresentationCategory.OTHER,
                "Red shrimp variety", "Camarón ROJO", 2);
        createPresentationType(em, "OTHER_BVS", "BVS", "BVS",
                ShrimpPresentationCategory.OTHER,
                "By-product variety (BVS)", "Subproducto BVS", 3);
    }

    private void createPresentationType(EntityManager em, String code, String labelEn, String labelEs,
                                         ShrimpPresentationCategory category,
                                         String descEn, String descEs, int order) {
        // Check if already exists
        Long count = em.createQuery("SELECT COUNT(spt) FROM ShrimpPresentationType spt WHERE spt.code = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult();
        if (count > 0) return;

        ShrimpPresentationType presentationType = new ShrimpPresentationType();
        presentationType.setCode(code);
        presentationType.setLabel(labelEn);
        presentationType.setCategory(category);
        presentationType.setDescription(descEn);
        presentationType.setDisplayOrder(order);
        presentationType.setStatus(CodebookStatus.ACTIVE);
        em.persist(presentationType);

        // English translation
        ShrimpPresentationTypeTranslation translationEn = new ShrimpPresentationTypeTranslation();
        translationEn.setShrimpPresentationType(presentationType);
        translationEn.setLanguage(Language.EN);
        translationEn.setLabel(labelEn);
        translationEn.setDescription(descEn);
        em.persist(translationEn);

        // Spanish translation
        ShrimpPresentationTypeTranslation translationEs = new ShrimpPresentationTypeTranslation();
        translationEs.setShrimpPresentationType(presentationType);
        translationEs.setLanguage(Language.ES);
        translationEs.setLabel(labelEs);
        translationEs.setDescription(descEs);
        em.persist(translationEs);
    }
}
