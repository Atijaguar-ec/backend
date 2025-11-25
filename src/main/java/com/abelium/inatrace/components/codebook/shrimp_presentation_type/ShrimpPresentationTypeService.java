package com.abelium.inatrace.components.codebook.shrimp_presentation_type;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_presentation_type.api.ApiShrimpPresentationType;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.db.entities.codebook.ShrimpPresentationType;
import com.abelium.inatrace.db.entities.codebook.ShrimpPresentationTypeTranslation;
import com.abelium.inatrace.db.enums.CodebookStatus;
import com.abelium.inatrace.db.enums.ShrimpPresentationCategory;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.tools.PaginationTools;
import com.abelium.inatrace.tools.Queries;
import com.abelium.inatrace.tools.QueryTools;
import com.abelium.inatrace.types.Language;
import com.abelium.inatrace.types.UserRole;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.torpedoquery.jakarta.jpa.Torpedo;

import java.util.List;

/**
 * Service for ShrimpPresentationType entity.
 *
 * @author INATrace Team
 */
@Lazy
@Service
public class ShrimpPresentationTypeService extends BaseService {

    public ApiPaginatedList<ApiShrimpPresentationType> getShrimpPresentationTypeList(ApiPaginatedRequest request, Language language) {
        return PaginationTools.createPaginatedResponse(em, request, () -> queryObject(request),
                entity -> ShrimpPresentationTypeMapper.toApiShrimpPresentationTypeBase(entity, language));
    }

    public List<ApiShrimpPresentationType> getActiveShrimpPresentationTypes(Language language) {
        List<ShrimpPresentationType> entities = em.createNamedQuery("ShrimpPresentationType.listActive", ShrimpPresentationType.class)
                .getResultList();
        return entities.stream()
                .map(e -> ShrimpPresentationTypeMapper.toApiShrimpPresentationTypeBase(e, language))
                .toList();
    }

    public List<ApiShrimpPresentationType> getShrimpPresentationTypesByCategory(ShrimpPresentationCategory category, Language language) {
        List<ShrimpPresentationType> entities = em.createNamedQuery("ShrimpPresentationType.listByCategory", ShrimpPresentationType.class)
                .setParameter("category", category)
                .getResultList();
        return entities.stream()
                .map(e -> ShrimpPresentationTypeMapper.toApiShrimpPresentationTypeBase(e, language))
                .toList();
    }

    private ShrimpPresentationType queryObject(ApiPaginatedRequest request) {
        ShrimpPresentationType proxy = Torpedo.from(ShrimpPresentationType.class);

        switch (request.sortBy) {
            case "code":
                QueryTools.orderBy(request.sort, proxy.getCode());
                break;
            case "label":
                QueryTools.orderBy(request.sort, proxy.getLabel());
                break;
            case "category":
                QueryTools.orderBy(request.sort, proxy.getCategory());
                break;
            case "displayOrder":
                QueryTools.orderBy(request.sort, proxy.getDisplayOrder());
                break;
            case "status":
                QueryTools.orderBy(request.sort, proxy.getStatus());
                break;
            default:
                QueryTools.orderBy(request.sort, proxy.getCategory());
                QueryTools.orderBy(request.sort, proxy.getDisplayOrder());
        }

        return proxy;
    }

    public ApiShrimpPresentationType getShrimpPresentationType(Long id, Language language) throws ApiException {
        return ShrimpPresentationTypeMapper.toApiShrimpPresentationType(fetchEntity(id), language);
    }

    @Transactional
    public ApiBaseEntity createOrUpdateShrimpPresentationType(CustomUserDetails authUser,
                                                               ApiShrimpPresentationType api) throws ApiException {
        ShrimpPresentationType entity;

        if (api.getId() != null) {
            if (authUser.getUserRole() == UserRole.REGIONAL_ADMIN) {
                throw new ApiException(ApiStatus.UNAUTHORIZED, "Regional admin not authorized!");
            }
            entity = fetchEntity(api.getId());
        } else {
            entity = new ShrimpPresentationType();
            entity.setCode(api.getCode());
        }

        entity.setLabel(api.getLabel());
        entity.setCategory(api.getCategory());
        entity.setDescription(api.getDescription());
        entity.setDisplayOrder(api.getDisplayOrder());
        entity.setStatus(api.getStatus() != null ? api.getStatus() : CodebookStatus.ACTIVE);

        // Remove translations not in request
        entity.getTranslations().removeIf(translation -> api.getTranslations().stream()
                .noneMatch(apiTranslation -> translation.getLanguage().equals(apiTranslation.getLanguage())));

        // Add or edit translations
        api.getTranslations().forEach(apiTranslation -> {
            ShrimpPresentationTypeTranslation translation = entity.getTranslations().stream()
                    .filter(t -> t.getLanguage().equals(apiTranslation.getLanguage()))
                    .findFirst()
                    .orElse(new ShrimpPresentationTypeTranslation());
            translation.setLabel(apiTranslation.getLabel());
            translation.setDescription(apiTranslation.getDescription());
            translation.setLanguage(apiTranslation.getLanguage());
            translation.setShrimpPresentationType(entity);
            entity.getTranslations().add(translation);
        });

        if (entity.getId() == null) {
            em.persist(entity);
        }

        return new ApiBaseEntity(entity);
    }

    @Transactional
    public void deleteShrimpPresentationType(Long id) throws ApiException {
        ShrimpPresentationType entity = fetchEntity(id);
        em.remove(entity);
    }

    public ShrimpPresentationType fetchEntity(Long id) throws ApiException {
        ShrimpPresentationType entity = Queries.get(em, ShrimpPresentationType.class, id);
        if (entity == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Invalid shrimp presentation type ID");
        }
        return entity;
    }
}
