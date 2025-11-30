package com.abelium.inatrace.components.codebook.shrimp_brand;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_brand.api.ApiShrimpBrand;
import com.abelium.inatrace.components.codebook.shrimp_brand.api.ApiShrimpBrandTranslation;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.db.entities.codebook.ShrimpBrand;
import com.abelium.inatrace.db.entities.codebook.ShrimpBrandTranslation;
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
 * Service for ShrimpBrand catalog.
 */
@Service
public class ShrimpBrandService extends BaseService {

    public ApiPaginatedList<ApiShrimpBrand> getShrimpBrandList(ApiPaginatedRequest request, Language language) {
        return PaginationTools.createPaginatedResponse(em, request,
                () -> brandQueryObject(request),
                entity -> ShrimpBrandMapper.toApiShrimpBrand(entity, language));
    }

    private ShrimpBrand brandQueryObject(ApiPaginatedRequest request) {
        ShrimpBrand proxy = Torpedo.from(ShrimpBrand.class);

        switch (request.sortBy) {
            case "code":
                QueryTools.orderBy(request.sort, proxy.getCode());
                break;
            case "label":
                QueryTools.orderBy(request.sort, proxy.getLabel());
                break;
            case "weightPerBox":
                QueryTools.orderBy(request.sort, proxy.getWeightPerBox());
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

    public List<ApiShrimpBrand> getActiveShrimpBrands(Language language) {
        List<ShrimpBrand> brands = em.createQuery(
                "SELECT b FROM ShrimpBrand b WHERE b.status = :status ORDER BY b.displayOrder", 
                ShrimpBrand.class)
                .setParameter("status", CodebookStatus.ACTIVE)
                .getResultList();
        return brands.stream()
                .map(b -> ShrimpBrandMapper.toApiShrimpBrand(b, language))
                .collect(Collectors.toList());
    }

    public ApiShrimpBrand getShrimpBrand(Long id, Language language) throws ApiException {
        ShrimpBrand entity = fetchEntity(id, ShrimpBrand.class);
        return ShrimpBrandMapper.toApiShrimpBrand(entity, language);
    }

    @Transactional
    public ApiBaseEntity createOrUpdateShrimpBrand(CustomUserDetails authUser, ApiShrimpBrand api) throws ApiException {
        ShrimpBrand entity;

        if (api.getId() != null) {
            entity = fetchEntity(api.getId(), ShrimpBrand.class);
        } else {
            entity = new ShrimpBrand();
        }

        entity.setCode(api.getCode());
        entity.setLabel(api.getLabel());
        entity.setDescription(api.getDescription());
        entity.setWeightPerBox(api.getWeightPerBox());
        entity.setMeasureUnit(api.getMeasureUnit());
        entity.setDisplayOrder(api.getDisplayOrder());
        entity.setStatus(api.getStatus() != null ? api.getStatus() : CodebookStatus.ACTIVE);

        // Handle translations
        if (api.getTranslations() != null) {
            entity.getTranslations().clear();
            for (ApiShrimpBrandTranslation apiT : api.getTranslations()) {
                ShrimpBrandTranslation translation = new ShrimpBrandTranslation();
                translation.setShrimpBrand(entity);
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
    public ApiBaseEntity deleteShrimpBrand(Long id) throws ApiException {
        ShrimpBrand entity = fetchEntity(id, ShrimpBrand.class);
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
