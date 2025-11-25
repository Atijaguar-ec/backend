package com.abelium.inatrace.components.codebook.shrimp_flavor_defect;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_flavor_defect.api.ApiShrimpFlavorDefect;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.db.entities.codebook.ShrimpFlavorDefect;
import com.abelium.inatrace.db.entities.codebook.ShrimpFlavorDefectTranslation;
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
 * Service for ShrimpFlavorDefect entity.
 *
 * @author INATrace Team
 */
@Lazy
@Service
public class ShrimpFlavorDefectService extends BaseService {

    public ApiPaginatedList<ApiShrimpFlavorDefect> getShrimpFlavorDefectList(ApiPaginatedRequest request, Language language) {
        return PaginationTools.createPaginatedResponse(em, request, () -> queryObject(request),
                entity -> ShrimpFlavorDefectMapper.toApiShrimpFlavorDefectBase(entity, language));
    }

    public List<ApiShrimpFlavorDefect> getActiveShrimpFlavorDefects(Language language) {
        List<ShrimpFlavorDefect> entities = em.createNamedQuery("ShrimpFlavorDefect.listActive", ShrimpFlavorDefect.class)
                .getResultList();
        return entities.stream()
                .map(e -> ShrimpFlavorDefectMapper.toApiShrimpFlavorDefectBase(e, language))
                .toList();
    }

    private ShrimpFlavorDefect queryObject(ApiPaginatedRequest request) {
        ShrimpFlavorDefect proxy = Torpedo.from(ShrimpFlavorDefect.class);

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

    public ApiShrimpFlavorDefect getShrimpFlavorDefect(Long id, Language language) throws ApiException {
        return ShrimpFlavorDefectMapper.toApiShrimpFlavorDefect(fetchEntity(id), language);
    }

    @Transactional
    public ApiBaseEntity createOrUpdateShrimpFlavorDefect(CustomUserDetails authUser,
                                                          ApiShrimpFlavorDefect api) throws ApiException {
        ShrimpFlavorDefect entity;

        if (api.getId() != null) {
            // Editing is not permitted for Regional admin
            if (authUser.getUserRole() == UserRole.REGIONAL_ADMIN) {
                throw new ApiException(ApiStatus.UNAUTHORIZED, "Regional admin not authorized!");
            }
            entity = fetchEntity(api.getId());
        } else {
            entity = new ShrimpFlavorDefect();
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
            ShrimpFlavorDefectTranslation translation = entity.getTranslations().stream()
                    .filter(t -> t.getLanguage().equals(apiTranslation.getLanguage()))
                    .findFirst()
                    .orElse(new ShrimpFlavorDefectTranslation());
            translation.setName(apiTranslation.getName());
            translation.setDescription(apiTranslation.getDescription());
            translation.setLanguage(apiTranslation.getLanguage());
            translation.setShrimpFlavorDefect(entity);
            entity.getTranslations().add(translation);
        });

        if (entity.getId() == null) {
            em.persist(entity);
        }

        return new ApiBaseEntity(entity);
    }

    @Transactional
    public void deleteShrimpFlavorDefect(Long id) throws ApiException {
        ShrimpFlavorDefect entity = fetchEntity(id);
        em.remove(entity);
    }

    public ShrimpFlavorDefect fetchEntity(Long id) throws ApiException {
        ShrimpFlavorDefect entity = Queries.get(em, ShrimpFlavorDefect.class, id);
        if (entity == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Invalid shrimp flavor defect ID");
        }
        return entity;
    }
}
