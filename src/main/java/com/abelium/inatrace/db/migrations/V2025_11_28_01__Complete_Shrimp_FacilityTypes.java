package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.FacilityType;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.facility.Facility;
import com.abelium.inatrace.db.entities.facility.FacilityTranslation;
import com.abelium.inatrace.types.Language;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;
import java.util.List;

/**
 * Migration to complete FacilityTypes and create Facilities for shrimp processing plant.
 * <p>
 * Creates FacilityTypes (catalog) and Facilities (instances per Company) with special properties:
 * <p>
 * | FacilityType Code      | Facility Name                  | Special Property          |
 * |------------------------|--------------------------------|---------------------------|
 * | INSPECCIONCAMPO        | Inspección de Campo            | isFieldInspection         |
 * | CENTROACOPIO           | Centro de Acopio               | isCollectionFacility      |
 * | AREADERECIBO           | Área de Recibo                 | -                         |
 * | LABORATORIODECALIDAD   | Laboratorio de Calidad         | isLaboratory              |
 * | AREADEPESADO           | Área de Pesado                 | -                         |
 * | TOLVADERECIBO          | Tolva de Recibo                | -                         |
 * | AREADECLASIFICADO      | Área de Clasificado            | isClassificationProcess   |
 * | AREADEREPOSO           | Área de Reposo                 | isRestArea                |
 * | AREADEDESCABEZADO      | Área de Descabezado            | -                         |
 * | AREADELAVADO           | Área de Lavado                 | isWashingArea             |
 * | AREADECONGELACION      | Área de Congelación (Entero)   | isFreezingProcess         |
 * | TUNELDECONGELACION     | Túnel de Congelación (Entero)  | isTunnelFreezing          |
 * | LOTEPARAEXPORTAR       | Lote para Exportar (Entero)    | -                         |
 * | AREADECORTADO          | Área de Cortado                | isCuttingProcess          |
 * | AREADETRATADO          | Área de Tratado                | isTreatmentProcess        |
 * | AREADECONGELACIONCOLA  | Área de Congelación (Cola)     | isFreezingProcess         |
 * | TUNELDECONGELACIONCOLA | Túnel de Congelación (Cola)    | isTunnelFreezing          |
 * | LOTEPARAEXPORTARCOLA   | Lote para Exportar (Cola)      | -                         |
 * | AREAVARORAGREGADO      | Área de Valor Agregado         | -                         |
 *
 * @author INATrace Team
 */
public class V2025_11_28_01__Complete_Shrimp_FacilityTypes implements JpaMigration {

    @Override
    public void migrate(EntityManager em, Environment environment) throws Exception {
        String productTypeConfig = environment.getProperty("INATrace.product.type", "COFFEE");

        if (!"SHRIMP".equalsIgnoreCase(productTypeConfig) && !"CAMARON".equalsIgnoreCase(productTypeConfig)) {
            return;
        }

        // =====================================================
        // 1. Crear FacilityTypes faltantes (catálogo)
        // =====================================================
        createFacilityTypeIfNotExists(em, "INSPECCIONCAMPO", "Inspección de Campo", 0);
        createFacilityTypeIfNotExists(em, "CENTROACOPIO", "Centro de Acopio", 1);
        createFacilityTypeIfNotExists(em, "AREADELAVADO", "Área de Lavado", 6);
        createFacilityTypeIfNotExists(em, "AREADECORTADO", "Área de Cortado", 7);
        createFacilityTypeIfNotExists(em, "AREADETRATADO", "Área de Tratado", 8);
        createFacilityTypeIfNotExists(em, "AREADECONGELACIONCOLA", "Área de Congelación Cola", 9);
        createFacilityTypeIfNotExists(em, "TUNELDECONGELACIONCOLA", "Túnel de Congelación Cola", 10);
        createFacilityTypeIfNotExists(em, "LOTEPARAEXPORTARCOLA", "Lote para Exportar Cola", 11);
        createFacilityTypeIfNotExists(em, "AREAVARORAGREGADO", "Área de Valor Agregado", 12);

        em.flush();

        // =====================================================
        // 2. Crear Facilities para cada Company existente
        // =====================================================
        List<Company> companies = em.createQuery("SELECT c FROM Company c", Company.class).getResultList();
        
        for (Company company : companies) {
            createShrimpFacilitiesForCompany(em, company);
        }
    }

    /**
     * Create all shrimp processing facilities for a company.
     * Parameters: isFieldInspection, isCollectionFacility, isLaboratory, isClassificationProcess,
     *             isFreezingProcess, isCuttingProcess, isTreatmentProcess, isTunnelFreezing, isWashingArea, isRestArea
     */
    private void createShrimpFacilitiesForCompany(EntityManager em, Company company) {
        // 1. Inspección de Campo - isFieldInspection
        createFacilityIfNotExists(em, company, "INSPECCIONCAMPO", "Inspección de Campo", 
                true, false, false, false, false, false, false, false, false, false);

        // 2. Centro de Acopio - isCollectionFacility
        createFacilityIfNotExists(em, company, "CENTROACOPIO", "Centro de Acopio",
                false, true, false, false, false, false, false, false, false, false);

        // 3. Área de Recibo (ya existe en catálogo original)
        createFacilityIfNotExists(em, company, "AREADERECIBO", "Área de Recibo",
                false, false, false, false, false, false, false, false, false, false);

        // 4. Laboratorio de Calidad - isLaboratory
        createFacilityIfNotExists(em, company, "LABORATORIODECALIDAD", "Laboratorio de Calidad",
                false, false, true, false, false, false, false, false, false, false);

        // 5. Área de Pesado
        createFacilityIfNotExists(em, company, "AREADEPESADO", "Área de Pesado",
                false, false, false, false, false, false, false, false, false, false);

        // 6. Tolva de Recibo
        createFacilityIfNotExists(em, company, "TOLVADERECIBO", "Tolva de Recibo",
                false, false, false, false, false, false, false, false, false, false);

        // 7. Área de Clasificado - isClassificationProcess
        createFacilityIfNotExists(em, company, "AREADECLASIFICADO", "Área de Clasificado",
                false, false, false, true, false, false, false, false, false, false);

        // 8. Área de Reposo - isRestArea
        createFacilityIfNotExists(em, company, "AREADEREPOSO", "Área de Reposo",
                false, false, false, false, false, false, false, false, false, true);

        // 9. Área de Descabezado
        createFacilityIfNotExists(em, company, "AREADEDESCABEZADO", "Área de Descabezado",
                false, false, false, false, false, false, false, false, false, false);

        // 10. Área de Lavado - isWashingArea
        createFacilityIfNotExists(em, company, "AREADELAVADO", "Área de Lavado",
                false, false, false, false, false, false, false, false, true, false);

        // 11. Área de Congelación (Entero) - isFreezingProcess
        createFacilityIfNotExists(em, company, "AREADECONGELACION", "Área de Congelación Entero",
                false, false, false, false, true, false, false, false, false, false);

        // 12. Túnel de Congelación (Entero) - isTunnelFreezing
        createFacilityIfNotExists(em, company, "TUNELDECONGELACION", "Túnel de Congelación Entero",
                false, false, false, false, false, false, false, true, false, false);

        // 13. Lote para Exportar (Entero)
        createFacilityIfNotExists(em, company, "LOTEPARAEXPORTAR", "Lote para Exportar Entero",
                false, false, false, false, false, false, false, false, false, false);

        // 14. Área de Cortado - isCuttingProcess
        createFacilityIfNotExists(em, company, "AREADECORTADO", "Área de Cortado",
                false, false, false, false, false, true, false, false, false, false);

        // 15. Área de Tratado - isTreatmentProcess
        createFacilityIfNotExists(em, company, "AREADETRATADO", "Área de Tratado",
                false, false, false, false, false, false, true, false, false, false);

        // 16. Área de Congelación (Cola) - isFreezingProcess
        createFacilityIfNotExists(em, company, "AREADECONGELACIONCOLA", "Área de Congelación Cola",
                false, false, false, false, true, false, false, false, false, false);

        // 17. Túnel de Congelación (Cola) - isTunnelFreezing
        createFacilityIfNotExists(em, company, "TUNELDECONGELACIONCOLA", "Túnel de Congelación Cola",
                false, false, false, false, false, false, false, true, false, false);

        // 18. Lote para Exportar (Cola)
        createFacilityIfNotExists(em, company, "LOTEPARAEXPORTARCOLA", "Lote para Exportar Cola",
                false, false, false, false, false, false, false, false, false, false);

        // 19. Área de Valor Agregado
        createFacilityIfNotExists(em, company, "AREAVARORAGREGADO", "Área de Valor Agregado",
                false, false, false, false, false, false, false, false, false, false);
    }

    /**
     * Create FacilityType if not exists
     */
    private void createFacilityTypeIfNotExists(EntityManager em, String code, String label, Integer order) {
        Long count = em.createQuery("SELECT COUNT(ft) FROM FacilityType ft WHERE ft.code = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult();
        
        if (count > 0) {
            return;
        }

        FacilityType ft = new FacilityType(code, label, order);
        em.persist(ft);
    }

    /**
     * Create Facility if not exists for a company.
     * Parameters order: isFieldInspection, isCollectionFacility, isLaboratory, isClassificationProcess,
     *                   isFreezingProcess, isCuttingProcess, isTreatmentProcess, isTunnelFreezing, isWashingArea, isRestArea
     */
    private void createFacilityIfNotExists(EntityManager em, Company company, String facilityTypeCode, String name,
                                            boolean isFieldInspection, boolean isCollectionFacility, boolean isLaboratory,
                                            boolean isClassificationProcess, boolean isFreezingProcess, boolean isCuttingProcess,
                                            boolean isTreatmentProcess, boolean isTunnelFreezing, boolean isWashingArea, boolean isRestArea) {
        // Check if facility already exists for this company with this type
        Long count = em.createQuery(
                "SELECT COUNT(f) FROM Facility f WHERE f.company.id = :companyId AND f.facilityType.code = :ftCode", Long.class)
                .setParameter("companyId", company.getId())
                .setParameter("ftCode", facilityTypeCode)
                .getSingleResult();

        if (count > 0) {
            return; // Already exists
        }

        // Get FacilityType
        FacilityType facilityType = em.createQuery(
                "SELECT ft FROM FacilityType ft WHERE ft.code = :code", FacilityType.class)
                .setParameter("code", facilityTypeCode)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (facilityType == null) {
            return; // FacilityType not found
        }

        // Create Facility
        Facility facility = new Facility();
        facility.setName(name);
        facility.setCompany(company);
        facility.setFacilityType(facilityType);
        facility.setIsPublic(false);
        facility.setIsDeactivated(false);

        // Set special properties
        facility.setIsFieldInspection(isFieldInspection ? true : null);
        facility.setIsCollectionFacility(isCollectionFacility ? true : null);
        facility.setIsLaboratory(isLaboratory ? true : null);
        facility.setIsClassificationProcess(isClassificationProcess ? true : null);
        facility.setIsFreezingProcess(isFreezingProcess ? true : null);
        facility.setIsCuttingProcess(isCuttingProcess ? true : null);
        facility.setIsTreatmentProcess(isTreatmentProcess ? true : null);
        facility.setIsTunnelFreezing(isTunnelFreezing ? true : null);
        facility.setIsWashingArea(isWashingArea ? true : null);
        facility.setIsRestArea(isRestArea ? true : null);

        em.persist(facility);

        // Create translations for all supported languages (required for Facility to display)
        for (Language language : List.of(Language.EN, Language.DE, Language.RW, Language.ES)) {
            FacilityTranslation translation = new FacilityTranslation(language);
            translation.setFacility(facility);
            translation.setName(name);
            em.persist(translation);
        }
    }
}
