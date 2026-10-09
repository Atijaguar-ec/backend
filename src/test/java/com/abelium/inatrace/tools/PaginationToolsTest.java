package com.abelium.inatrace.tools;

import com.abelium.inatrace.db.entities.product.ProductLabelBatch;
import com.abelium.inatrace.types.SortDirection;
import org.junit.jupiter.api.Test;
import org.torpedoquery.jakarta.jpa.Query;
import org.torpedoquery.jakarta.jpa.Torpedo;
import org.torpedoquery.jakarta.jpa.internal.TorpedoProxy;

import static org.junit.jupiter.api.Assertions.*;

class PaginationToolsTest {

    @Test
    void testTorpedoCountQueryContainsOrderBy() {
        ProductLabelBatch plbProxy = Torpedo.from(ProductLabelBatch.class);
        Torpedo.where(plbProxy.getLabel().getId()).eq(1L);
        QueryTools.orderBy(SortDirection.ASC, plbProxy.getId());

        Query<Long> countQuery = Torpedo.select(Torpedo.count(plbProxy));
        String hql = countQuery.getQuery();
        System.out.println("HQL with orderBy: " + hql);
        assertTrue(hql.toLowerCase().contains("order by"), "Expected ORDER BY in HQL");
    }

    @Test
    void testTorpedoCountQueryWithoutOrderByWhenCleared() {
        ProductLabelBatch plbProxy = Torpedo.from(ProductLabelBatch.class);
        Torpedo.where(plbProxy.getLabel().getId()).eq(1L);
        QueryTools.orderBy(SortDirection.ASC, plbProxy.getId());

        PaginationTools.clearOrderBy(plbProxy);

        Query<Long> countQuery = Torpedo.select(Torpedo.count(plbProxy));
        String hql = countQuery.getQuery();
        System.out.println("HQL after clearOrderBy: " + hql);
        assertFalse(hql.toLowerCase().contains("order by"), "Expected NO order by in count HQL");
    }

    @Test
    void testTorpedoStockOrderCountQueryWithoutOrderByWhenCleared() {
        com.abelium.inatrace.db.entities.stockorder.StockOrder soProxy = Torpedo.from(com.abelium.inatrace.db.entities.stockorder.StockOrder.class);
        Torpedo.where(soProxy.getId()).isNotNull();
        QueryTools.orderBy(SortDirection.DESC, soProxy.getProductionDate());

        PaginationTools.clearOrderBy(soProxy);

        Query<Long> countQuery = Torpedo.select(Torpedo.count(soProxy));
        String hql = countQuery.getQuery();
        System.out.println("StockOrder HQL after clearOrderBy: " + hql);
        assertFalse(hql.toLowerCase().contains("order by"), "Expected NO order by in StockOrder count HQL");
    }

    @Test
    void testTorpedoCountDistinctWithClearedOrderBy() {
        com.abelium.inatrace.db.entities.company.CompanyUser cuProxy = Torpedo.from(com.abelium.inatrace.db.entities.company.CompanyUser.class);
        Torpedo.where(cuProxy.getCompany().getId()).eq(1L);
        QueryTools.orderBy(SortDirection.ASC, cuProxy.getUser().getName());
        org.torpedoquery.jakarta.jpa.Function<com.abelium.inatrace.db.entities.common.User> distinctUser = Torpedo.distinct(cuProxy.getUser());

        PaginationTools.clearOrderBy(distinctUser);

        Query<Long> countQuery = Torpedo.select(Torpedo.count(distinctUser));
        String hql = countQuery.getQuery();
        System.out.println("HQL distinct after clearOrderBy: " + hql);
        assertFalse(hql.toLowerCase().contains("order by"), "Expected NO order by in count distinct HQL");
    }
}
