package com.abelium.inatrace.db.repositories.fieldinspection;

import com.abelium.inatrace.db.entities.fieldinspection.FieldInspection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for FieldInspection entity.
 * 
 * @author INATrace Team
 */
@Repository
public interface FieldInspectionRepository extends JpaRepository<FieldInspection, Long> {

    /**
     * Find all field inspections that are recommended for purchase and not yet linked
     * to a destination stock order (i.e. still available for linking).
     */
    List<FieldInspection> findByPurchaseRecommendedTrueAndDestinationStockOrderIsNull();

    /**
     * Find all available field inspections (not linked) for a specific company.
     */
    @Query("SELECT fi FROM FieldInspection fi " +
           "WHERE fi.company.id = :companyId " +
           "AND fi.destinationStockOrder IS NULL " +
           "ORDER BY fi.inspectionDate DESC")
    List<FieldInspection> findAvailableByCompanyId(@Param("companyId") Long companyId);

    /**
     * Find all available field inspections that are recommended for purchase for a company.
     */
    @Query("SELECT fi FROM FieldInspection fi " +
           "WHERE fi.company.id = :companyId " +
           "AND fi.destinationStockOrder IS NULL " +
           "AND fi.purchaseRecommended = true " +
           "ORDER BY fi.inspectionDate DESC")
    List<FieldInspection> findAvailableRecommendedByCompanyId(@Param("companyId") Long companyId);

    /**
     * Find field inspection by source stock order ID.
     */
    FieldInspection findBySourceStockOrderId(Long sourceStockOrderId);

    /**
     * Check if a field inspection exists for the given source stock order.
     */
    boolean existsBySourceStockOrderId(Long sourceStockOrderId);
}
