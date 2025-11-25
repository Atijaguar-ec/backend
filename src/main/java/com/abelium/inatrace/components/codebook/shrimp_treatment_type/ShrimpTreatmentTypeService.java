package com.abelium.inatrace.components.codebook.shrimp_treatment_type;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.shrimp_treatment_type.api.ApiShrimpTreatmentType;
import com.abelium.inatrace.components.codebook.shrimp_treatment_type.api.ApiShrimpTreatmentTypeTranslation;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.db.entities.codebook.ShrimpTreatmentType;
import com.abelium.inatrace.db.entities.codebook.ShrimpTreatmentTypeTranslation;
import com.abelium.inatrace.db.enums.CodebookStatus;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.tools.PaginationTools;
import com.abelium.inatrace.tools.Queries;
import com.abelium.inatrace.types.Language;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for ShrimpTreatmentType catalog.
 */
@Service
public class ShrimpTreatmentTypeService extends BaseService {

    public ApiPaginatedList<ApiShrimpTreatmentType> getShrimpTreatmentTypeList(ApiPaginatedRequest request, Language language) {
        return PaginationTools.createPaginatedResponse(em, request,
                () -> treatmentTypeQueryObject(request),
                entity -> ShrimpTreatmentTypeMapper.toApiShrimpTreatmentType(entity, language));
    }

    private Queries.QueryCreator<ShrimpTreatmentType> treatmentTypeQueryObject(ApiPaginatedRequest request) {
        return Queries.createQueryFrom(em, ShrimpTreatmentType.class)
                .orderBy(request.sortBy, request.sort);
    }

    public List<ApiShrimpTreatmentType> getActiveShrimpTreatmentTypes(Language language) {
        List<ShrimpTreatmentType> types = em.createQuery(
                        "SELECT t FROM ShrimpTreatmentType t WHERE t.status = :status ORDER BY t.displayOrder",
                        ShrimpTreatmentType.class)
                .setParameter("status", CodebookStatus.ACTIVE)
                .getResultList();
        return types.stream()
                .map(t -> ShrimpTreatmentTypeMapper.toApiShrimpTreatmentType(t, language))
                .collect(Collectors.toList());
    }

    public ApiShrimpTreatmentType getShrimpTreatmentType(Long id, Language language) throws ApiException {
        ShrimpTreatmentType entity = fetchEntity(id, ShrimpTreatmentType.class);
        return ShrimpTreatmentTypeMapper.toApiShrimpTreatmentType(entity, language);
    }

    @Transactional
    public ApiBaseEntity createOrUpdateShrimpTreatmentType(CustomUserDetails authUser, ApiShrimpTreatmentType api) throws ApiException {
        ShrimpTreatmentType entity;

        if (api.getId() != null) {
            entity = fetchEntity(api.getId(), ShrimpTreatmentType.class);
        } else {
            entity = new ShrimpTreatmentType();
        }

        entity.setCode(api.getCode());
        entity.setLabel(api.getLabel());
        entity.setDescription(api.getDescription());
        entity.setDisplayOrder(api.getDisplayOrder());
        entity.setStatus(api.getStatus() != null ? api.getStatus() : CodebookStatus.ACTIVE);

        if (api.getTranslations() != null) {
            entity.getTranslations().clear();
            for (ApiShrimpTreatmentTypeTranslation apiT : api.getTranslations()) {
                ShrimpTreatmentTypeTranslation translation = new ShrimpTreatmentTypeTranslation();
                translation.setShrimpTreatmentType(entity);
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
    public ApiBaseEntity deleteShrimpTreatmentType(Long id) throws ApiException {
        ShrimpTreatmentType entity = fetchEntity(id, ShrimpTreatmentType.class);
        em.remove(entity);
        return new ApiBaseEntity(entity);
    }
}
