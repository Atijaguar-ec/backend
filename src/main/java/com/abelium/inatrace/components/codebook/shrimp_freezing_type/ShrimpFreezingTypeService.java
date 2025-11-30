package com.abelium.inatrace.components.codebook.shrimp_freezing_type;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_freezing_type.api.ApiShrimpFreezingType;
import com.abelium.inatrace.components.codebook.shrimp_freezing_type.api.ApiShrimpFreezingTypeTranslation;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.db.entities.codebook.ShrimpFreezingType;
import com.abelium.inatrace.db.entities.codebook.ShrimpFreezingTypeTranslation;
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
 * Service for ShrimpFreezingType catalog.
 */
@Service
public class ShrimpFreezingTypeService extends BaseService {

    public ApiPaginatedList<ApiShrimpFreezingType> getShrimpFreezingTypeList(ApiPaginatedRequest request, Language language) {
        return PaginationTools.createPaginatedResponse(em, request,
                () -> freezingTypeQueryObject(request),
                entity -> ShrimpFreezingTypeMapper.toApiShrimpFreezingType(entity, language));
    }

    private ShrimpFreezingType freezingTypeQueryObject(ApiPaginatedRequest request) {
        ShrimpFreezingType proxy = Torpedo.from(ShrimpFreezingType.class);

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

    public List<ApiShrimpFreezingType> getActiveShrimpFreezingTypes(Language language) {
        List<ShrimpFreezingType> types = em.createQuery(
                "SELECT f FROM ShrimpFreezingType f WHERE f.status = :status ORDER BY f.displayOrder", 
                ShrimpFreezingType.class)
                .setParameter("status", CodebookStatus.ACTIVE)
                .getResultList();
        return types.stream()
                .map(t -> ShrimpFreezingTypeMapper.toApiShrimpFreezingType(t, language))
                .collect(Collectors.toList());
    }

    public ApiShrimpFreezingType getShrimpFreezingType(Long id, Language language) throws ApiException {
        ShrimpFreezingType entity = fetchEntity(id, ShrimpFreezingType.class);
        return ShrimpFreezingTypeMapper.toApiShrimpFreezingType(entity, language);
    }

    @Transactional
    public ApiBaseEntity createOrUpdateShrimpFreezingType(CustomUserDetails authUser, ApiShrimpFreezingType api) throws ApiException {
        ShrimpFreezingType entity;

        if (api.getId() != null) {
            entity = fetchEntity(api.getId(), ShrimpFreezingType.class);
        } else {
            entity = new ShrimpFreezingType();
        }

        entity.setCode(api.getCode());
        entity.setLabel(api.getLabel());
        entity.setDescription(api.getDescription());
        entity.setDisplayOrder(api.getDisplayOrder());
        entity.setStatus(api.getStatus() != null ? api.getStatus() : CodebookStatus.ACTIVE);

        // Handle translations
        if (api.getTranslations() != null) {
            entity.getTranslations().clear();
            for (ApiShrimpFreezingTypeTranslation apiT : api.getTranslations()) {
                ShrimpFreezingTypeTranslation translation = new ShrimpFreezingTypeTranslation();
                translation.setShrimpFreezingType(entity);
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
    public ApiBaseEntity deleteShrimpFreezingType(Long id) throws ApiException {
        ShrimpFreezingType entity = fetchEntity(id, ShrimpFreezingType.class);
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
