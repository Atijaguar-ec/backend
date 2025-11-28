package com.abelium.inatrace.db.migrations;

import com.abelium.inatrace.components.flyway.JpaMigration;
import com.abelium.inatrace.db.entities.codebook.FacilityType;
import org.springframework.core.env.Environment;

import jakarta.persistence.EntityManager;

/**
 * Migration to complete FacilityTypes for shrimp processing plant.
 * <p>
 * Adds missing facility types based on real shrimp plant operations:
 * - Inspección de campo (field inspection)
 * - Centro de acopio (collection center)
 * - Área de lavado (washing area)
 * - Área de cortado (cutting area)
 * - Área de tratado (treatment area)
 * <p>
 * Note: Some existing types from V2021_08_11_11_33__Prefill_FacilityTypes:
 * - AREADERECIBO, LABORATORIODECALIDAD, TOLVADERECIBO, AREADECLASIFICADO,
 * - AREADECONGELACION, TUNELDECONGELACION, LOTEPARAEXPORTAR, AREADEPESADO,
 * - AREADEREPOSO, AREADEDESCABEZADO
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
        // Áreas del proceso de planta de camarón (faltantes)
        // =====================================================

        // 1. Inspección de campo - Punto de inspección inicial
        createFacilityTypeIfNotExists(em, "INSPECCIONCAMPO", "Inspección de Campo", 0);

        // 2. Centro de acopio - Punto de recolección
        createFacilityTypeIfNotExists(em, "CENTROACOPIO", "Centro de Acopio", 1);

        // 3. Área de lavado - Proceso de limpieza
        createFacilityTypeIfNotExists(em, "AREADELAVADO", "Área de Lavado", 6);

        // 4. Área de cortado - Proceso de corte (valor agregado)
        createFacilityTypeIfNotExists(em, "AREADECORTADO", "Área de Cortado", 7);

        // 5. Área de tratado - Proceso de tratamiento químico
        createFacilityTypeIfNotExists(em, "AREADETRATADO", "Área de Tratado", 8);

        // 6. Área de congelación cola - Para camarón en cola específicamente
        createFacilityTypeIfNotExists(em, "AREADECONGELACIONCOLA", "Área de Congelación Cola", 9);

        // 7. Túnel de congelación cola - Para camarón en cola
        createFacilityTypeIfNotExists(em, "TUNELDECONGELACIONCOLA", "Túnel de Congelación Cola", 10);

        // 8. Lote para exportar cola - Camarón en cola listo para exportar
        createFacilityTypeIfNotExists(em, "LOTEPARAEXPORTARCOLA", "Lote para Exportar Cola", 11);

        // 9. Área de valor agregado - Pelado, desvenado, marinado
        createFacilityTypeIfNotExists(em, "AREAVARORAGREGADO", "Área de Valor Agregado", 12);
    }

    /**
     * Create FacilityType if not exists
     */
    private void createFacilityTypeIfNotExists(EntityManager em, String code, String label, Integer order) {
        Long count = em.createQuery("SELECT COUNT(ft) FROM FacilityType ft WHERE ft.code = :code", Long.class)
                .setParameter("code", code)
                .getSingleResult();
        
        if (count > 0) {
            return; // Already exists
        }

        FacilityType ft = new FacilityType(code, label, order);
        em.persist(ft);
    }
}
