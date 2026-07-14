package com.abelium.inatrace.tools;

import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.types.PaginatedRequestType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaQuery;
import org.torpedoquery.jakarta.jpa.Torpedo;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


public class PaginationTools 
{
    public static <T> ApiPaginatedList<T> createPaginatedResponse(PaginatedRequestType  type, 
            Supplier<List<T>> itemListSupplier, Supplier<Long> countSupplier) {
        List<T> items = (type == null || type == PaginatedRequestType.FETCH) ? 
            itemListSupplier.get() : Collections.emptyList();
        Long count = (type == null || type == PaginatedRequestType.COUNT) ?
            countSupplier.get() : null;
        return new ApiPaginatedList<>(items, count);
    }
    
    public static <DBT, APIT> ApiPaginatedList<APIT> createPaginatedResponse(EntityManager em, ApiPaginatedRequest request, 
            Supplier<DBT> proxyObjectSupplier, Function<DBT, APIT> typeConverter) {
        List<APIT> items;
        long count;
        
        if (request.requestType == PaginatedRequestType.COUNT) {
            items = Collections.emptyList();
        } else {
            List<DBT> dbItems = Torpedo.select(proxyObjectSupplier.get()).
                    setFirstResult(request.offset).setMaxResults(request.limit).list(em);
            items = dbItems.stream().map(typeConverter).collect(Collectors.toList());
        }
        if (request.requestType == PaginatedRequestType.FETCH) {
            count = 0;
        } else {
            // Re-execute the supplier to rebuild the WHERE clause, then count
            try {
                count = Torpedo.select(Torpedo.count(proxyObjectSupplier.get())).get(em)
                        .orElse(0L);
            } catch (Exception e) {
                // Fallback: if count query fails, estimate from items
                // If items.size() < limit, we know we're on the last page
                if (items.size() < request.limit) {
                    count = request.offset + items.size();
                } else {
                    // Estimate: we don't know the exact count, set a high value to enable pagination
                    count = request.offset + items.size() + 1;
                }
            }
        }
        return new ApiPaginatedList<>(items, count);
    }
    
    public static <DBT, APIT> ApiPaginatedList<APIT> createPaginatedResponse1(EntityManager em, ApiPaginatedRequest request, 
            Supplier<org.torpedoquery.jakarta.jpa.Function<DBT>> proxyObjectSupplier, Function<DBT, APIT> typeConverter) {
        List<APIT> items;
        long count;
        
        if (request.requestType == PaginatedRequestType.COUNT) {
            items = Collections.emptyList();
        } else {
            List<DBT> dbItems = Torpedo.select(proxyObjectSupplier.get()).
                    setFirstResult(request.offset).setMaxResults(request.limit).list(em);
            items = dbItems.stream().map(typeConverter).collect(Collectors.toList());
        }
        if (request.requestType == PaginatedRequestType.FETCH) {
            count = 0;
        } else {
            // Improved fallback count estimation
            if (items.size() < request.limit) {
                count = request.offset + items.size();
            } else {
                count = request.offset + items.size() + 1;
            }
        }
        return new ApiPaginatedList<>(items, count);
    }
    

    public static <DBT, APIT> ApiPaginatedList<APIT> createPaginatedResponse(EntityManager em, ApiPaginatedRequest request, 
    		Supplier<TorpedoProjector<DBT, APIT>> projectorSupplier) {
        List<APIT> items;
        long count;
        
        if (request.requestType == PaginatedRequestType.COUNT) items = Collections.emptyList();
        else items = projectorSupplier.get().list(em, request.offset, request.limit);

        if (request.requestType == PaginatedRequestType.FETCH) {
            count = 0;
        } else {
            // Improved fallback count estimation
            if (items.size() < request.limit) {
                count = request.offset + items.size();
            } else {
                count = request.offset + items.size() + 1;
            }
        }

        return new ApiPaginatedList<>(items, count);
    }
    
    public static <DBT, APIT> ApiPaginatedList<APIT> createPaginatedResponse(EntityManager em, ApiPaginatedRequest request,
            CriteriaQuery<DBT> query, CriteriaQuery<Long> queryCount, Function<DBT, APIT> typeConverter) {
        List<APIT> items;
        long count;
        
        if (request.requestType == PaginatedRequestType.COUNT) {
            items = Collections.emptyList();
        } else {
            List<DBT> dbItems = em.createQuery(query).
                    setFirstResult(request.offset).setMaxResults(request.limit).getResultList();
            items = dbItems.stream().map(typeConverter).collect(Collectors.toList());
        }
        if (request.requestType == PaginatedRequestType.FETCH) {
            count = 0;
        } else {
            count = em.createQuery(queryCount).getSingleResult();
        }
        return new ApiPaginatedList<>(items, count);
    }
    
}
