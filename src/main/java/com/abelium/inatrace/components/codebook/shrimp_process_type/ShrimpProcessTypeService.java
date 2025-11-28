package com.abelium.inatrace.components.codebook.shrimp_process_type;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_process_type.api.ApiShrimpProcessType;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.db.entities.codebook.ShrimpProcessType;
import com.abelium.inatrace.db.entities.codebook.ShrimpProcessTypeTranslation;
import com.abelium.inatrace.db.enums.CodebookStatus;
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
 * Service for ShrimpProcessType entity.
 *
 * @author INATrace Team
 */
@Lazy
@Service
public class ShrimpProcessTypeService extends BaseService {

    public ApiPaginatedList<ApiShrimpProcessType> getShrimpProcessTypeList(ApiPaginatedRequest request, Language language) {
        return PaginationTools.createPaginatedResponse(em, request, () -> queryObject(request),
                entity -> ShrimpProcessTypeMapper.toApiShrimpProcessTypeBase(entity, language));
    }

    public List<ApiShrimpProcessType> getActiveShrimpProcessTypes(Language language) {
        List<ShrimpProcessType> entities = em.createNamedQuery("ShrimpProcessType.listActive", ShrimpProcessType.class)
                .getResultList();
        return entities.stream()
                .map(e -> ShrimpProcessTypeMapper.toApiShrimpProcessTypeBase(e, language))
                .toList();
    }

    private ShrimpProcessType queryObject(ApiPaginatedRequest request) {
        ShrimpProcessType proxy = Torpedo.from(ShrimpProcessType.class);

        switch (request.sortBy) {
            case "code":
                QueryTools.orderBy(request.sort, proxy.getCode());
                break;
            case "name":
                QueryTools.orderBy(request.sort, proxy.getName());
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

    public ApiShrimpProcessType getShrimpProcessType(Long id, Language language) throws ApiException {
        return ShrimpProcessTypeMapper.toApiShrimpProcessType(fetchEntity(id), language);
    }

    @Transactional
    public ApiBaseEntity createOrUpdateShrimpProcessType(CustomUserDetails authUser,
                                                         ApiShrimpProcessType api) throws ApiException {
        ShrimpProcessType entity;

        if (api.getId() != null) {
            if (authUser.getUserRole() == UserRole.REGIONAL_ADMIN) {
                throw new ApiException(ApiStatus.UNAUTHORIZED, "Regional admin not authorized!");
            }
            entity = fetchEntity(api.getId());
        } else {
            entity = new ShrimpProcessType();
            entity.setCode(api.getCode());
        }

        entity.setName(api.getLabel());
        entity.setDescription(api.getDescription());
        entity.setDisplayOrder(api.getDisplayOrder());
        entity.setStatus(api.getStatus() != null ? api.getStatus() : CodebookStatus.ACTIVE);

        // Remove translations not in request
        entity.getTranslations().removeIf(translation -> api.getTranslations().stream()
                .noneMatch(apiTranslation -> translation.getLanguage().equals(apiTranslation.getLanguage())));

        // Add or edit translations
        api.getTranslations().forEach(apiTranslation -> {
            ShrimpProcessTypeTranslation translation = entity.getTranslations().stream()
                    .filter(t -> t.getLanguage().equals(apiTranslation.getLanguage()))
                    .findFirst()
                    .orElse(new ShrimpProcessTypeTranslation());
            translation.setName(apiTranslation.getName());
            translation.setDescription(apiTranslation.getDescription());
            translation.setLanguage(apiTranslation.getLanguage());
            translation.setShrimpProcessType(entity);
            entity.getTranslations().add(translation);
        });

        if (entity.getId() == null) {
            em.persist(entity);
        }

        return new ApiBaseEntity(entity);
    }

    @Transactional
    public void deleteShrimpProcessType(Long id) throws ApiException {
        ShrimpProcessType entity = fetchEntity(id);
        em.remove(entity);
    }

    public ShrimpProcessType fetchEntity(Long id) throws ApiException {
        ShrimpProcessType entity = Queries.get(em, ShrimpProcessType.class, id);
        if (entity == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Invalid shrimp process type ID");
        }
        return entity;
    }
}
