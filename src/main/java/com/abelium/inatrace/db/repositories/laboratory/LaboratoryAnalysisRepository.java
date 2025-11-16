package com.abelium.inatrace.db.repositories.laboratory;

import com.abelium.inatrace.db.entities.laboratory.LaboratoryAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LaboratoryAnalysisRepository extends JpaRepository<LaboratoryAnalysis, Long> {

    /**
     * Find all analyses that are approved for purchase and not yet linked to a
     * destination stock order (i.e. still available to be used).
     */
    List<LaboratoryAnalysis> findByApprovedForPurchaseTrueAndDestinationStockOrderIsNull();
}
