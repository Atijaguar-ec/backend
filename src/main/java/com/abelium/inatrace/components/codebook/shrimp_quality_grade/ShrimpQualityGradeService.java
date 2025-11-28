package com.abelium.inatrace.components.codebook.shrimp_quality_grade;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_quality_grade.api.ApiShrimpQualityGrade;
import com.abelium.inatrace.components.codebook.shrimp_quality_grade.api.ApiShrimpQualityGradeTranslation;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.db.entities.codebook.ShrimpQualityGrade;
import com.abelium.inatrace.db.entities.codebook.ShrimpQualityGradeTranslation;
import com.abelium.inatrace.db.enums.CodebookStatus;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.tools.PaginationTools;
import com.abelium.inatrace.tools.Queries;
import com.abelium.inatrace.tools.QueryTools;
import com.abelium.inatrace.types.Language;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.torpedoquery.jakarta.jpa.Torpedo;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for ShrimpQualityGrade catalog.
 */
@Service
public class ShrimpQualityGradeService extends BaseService {

    public ApiPaginatedList<ApiShrimpQualityGrade> getShrimpQualityGradeList(ApiPaginatedRequest request, Language language) {
        return PaginationTools.createPaginatedResponse(em, request,
                () -> qualityGradeQueryObject(request),
                entity -> ShrimpQualityGradeMapper.toApiShrimpQualityGrade(entity, language));
    }

    private ShrimpQualityGrade qualityGradeQueryObject(ApiPaginatedRequest request) {
        ShrimpQualityGrade proxy = Torpedo.from(ShrimpQualityGrade.class);

        switch (request.sortBy) {
            case "code":
                QueryTools.orderBy(request.sort, proxy.getCode());
                break;
            case "label":
                QueryTools.orderBy(request.sort, proxy.getLabel());
                break;
            case "displayOrder":
                QueryTools.orderBy(request.sort, proxy.getDisplayOrder());
                break;
            case "status":
                QueryTools.orderBy(request.sort, proxy.getStatus());
                break;
            default:
                QueryTools.orderBy(request.sort, proxy.getDisplayOrder());
        }

        return proxy;
    }

    public List<ApiShrimpQualityGrade> getActiveShrimpQualityGrades(Language language) {
        List<ShrimpQualityGrade> grades = em.createQuery(
                "SELECT q FROM ShrimpQualityGrade q WHERE q.status = :status ORDER BY q.displayOrder", 
                ShrimpQualityGrade.class)
                .setParameter("status", CodebookStatus.ACTIVE)
                .getResultList();
        return grades.stream()
                .map(g -> ShrimpQualityGradeMapper.toApiShrimpQualityGrade(g, language))
                .collect(Collectors.toList());
    }

    public ApiShrimpQualityGrade getShrimpQualityGrade(Long id, Language language) throws ApiException {
        ShrimpQualityGrade entity = fetchEntity(id, ShrimpQualityGrade.class);
        return ShrimpQualityGradeMapper.toApiShrimpQualityGrade(entity, language);
    }

    @Transactional
    public ApiBaseEntity createOrUpdateShrimpQualityGrade(CustomUserDetails authUser, ApiShrimpQualityGrade api) throws ApiException {
        ShrimpQualityGrade entity;

        if (api.getId() != null) {
            entity = fetchEntity(api.getId(), ShrimpQualityGrade.class);
        } else {
            entity = new ShrimpQualityGrade();
        }

        entity.setCode(api.getCode());
        entity.setLabel(api.getLabel());
        entity.setDescription(api.getDescription());
        entity.setDisplayOrder(api.getDisplayOrder());
        entity.setStatus(api.getStatus() != null ? api.getStatus() : CodebookStatus.ACTIVE);

        // Handle translations
        if (api.getTranslations() != null) {
            entity.getTranslations().clear();
            for (ApiShrimpQualityGradeTranslation apiT : api.getTranslations()) {
                ShrimpQualityGradeTranslation translation = new ShrimpQualityGradeTranslation();
                translation.setShrimpQualityGrade(entity);
                translation.setLanguage(apiT.getLanguage());
                translation.setLabel(apiT.getLabel());
                translation.setDescription(apiT.getDescription());
                entity.getTranslations().add(translation);
            }
        }

        if (entity.getId() == null) {
            em.persist(entity);
        }

        return new ApiBaseEntity(entity);
    }

    @Transactional
    public ApiBaseEntity deleteShrimpQualityGrade(Long id) throws ApiException {
        ShrimpQualityGrade entity = fetchEntity(id, ShrimpQualityGrade.class);
        em.remove(entity);
        return new ApiBaseEntity(entity);
    }

    private <E> E fetchEntity(Long id, Class<E> entityClass) throws ApiException {
        E entity = Queries.get(em, entityClass, id);
        if (entity == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Invalid " + entityClass.getSimpleName() + " ID");
        }
        return entity;
    }
}
