package com.abelium.inatrace.db.repositories.processingaction;

import com.abelium.inatrace.db.entities.processingaction.CompanyProcessingAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyProcessingActionRepository extends JpaRepository<CompanyProcessingAction, Long> {

    /**
     * Find company processing action configuration by company and processing action IDs.
     */
    Optional<CompanyProcessingAction> findByCompanyIdAndProcessingActionId(Long companyId, Long processingActionId);

    /**
     * Get all enabled processing actions for a company, ordered by effective order.
     * Uses COALESCE to apply order override or fall back to global sortOrder.
     */
    @Query("SELECT cpa FROM CompanyProcessingAction cpa " +
           "JOIN FETCH cpa.processingAction pa " +
           "WHERE cpa.company.id = :companyId AND cpa.enabled = true " +
           "ORDER BY COALESCE(cpa.orderOverride, CAST(pa.sortOrder AS integer)) ASC")
    List<CompanyProcessingAction> findEnabledByCompanyIdOrderByEffectiveOrder(@Param("companyId") Long companyId);

    /**
     * Get all processing action configurations for a company (enabled and disabled).
     * Ordered by effective order for management UI.
     */
    @Query("SELECT cpa FROM CompanyProcessingAction cpa " +
           "JOIN FETCH cpa.processingAction pa " +
           "WHERE cpa.company.id = :companyId " +
           "ORDER BY COALESCE(cpa.orderOverride, CAST(pa.sortOrder AS integer)) ASC")
    List<CompanyProcessingAction> findAllByCompanyIdOrderByEffectiveOrder(@Param("companyId") Long companyId);

    /**
     * Count enabled processing actions for a company.
     */
    long countByCompanyIdAndEnabledTrue(Long companyId);

    /**
     * Delete all configurations for a specific processing action (when action is deleted).
     */
    void deleteByProcessingActionId(Long processingActionId);

    /**
     * Delete all configurations for a specific company (when company is deleted).
     */
    void deleteByCompanyId(Long companyId);
}
