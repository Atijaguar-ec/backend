package com.abelium.inatrace.components.codebook.certification_type;

import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.components.codebook.certification_type.api.ApiCertificationType;
import com.abelium.inatrace.components.codebook.certification_type.api.ApiCertificationTypeTranslation;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.tools.Queries;
import com.abelium.inatrace.db.entities.codebook.CertificationType;
import com.abelium.inatrace.db.entities.codebook.CertificationTypeTranslation;
import com.abelium.inatrace.types.Language;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CertificationTypeService extends BaseService {

    public List<ApiCertificationType> listActiveCertificationTypes(Language language) {
        List<CertificationType> entities = em.createNamedQuery("CertificationType.findAllActive", CertificationType.class)
                .getResultList();
        
        return entities.stream()
                .map(entity -> CertificationTypeMapper.toApiCertificationTypeBase(entity, language))
                .collect(Collectors.toList());
    }

    public ApiCertificationType getCertificationType(Long id, Language language) throws ApiException {
        CertificationType entity = fetchEntity(id);
        return CertificationTypeMapper.toApiCertificationType(entity, language);
    }

    @Transactional
    public ApiCertificationType createOrUpdateCertificationType(ApiCertificationType apiDTO) throws ApiException {
        
        if (apiDTO.getCode() == null || apiDTO.getName() == null || apiDTO.getCategory() == null || apiDTO.getStatus() == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Required fields are missing");
        }

        CertificationType entity;
        if (apiDTO.getId() != null) {
            entity = fetchEntity(apiDTO.getId());
        } else {
            entity = new CertificationType();
            // Check uniqueness of code
            List<CertificationType> existing = em.createNamedQuery("CertificationType.findByCode", CertificationType.class)
                    .setParameter("code", apiDTO.getCode())
                    .getResultList();
            if (!existing.isEmpty()) {
                throw new ApiException(ApiStatus.INVALID_REQUEST, "Certification type with code " + apiDTO.getCode() + " already exists");
            }
        }

        entity.setCode(apiDTO.getCode());
        entity.setName(apiDTO.getName());
        entity.setCategory(apiDTO.getCategory());
        entity.setStatus(apiDTO.getStatus());

        if (apiDTO.getTranslations() != null) {
            entity.getTranslations().clear();
            for (ApiCertificationTypeTranslation apiTranslation : apiDTO.getTranslations()) {
                CertificationTypeTranslation translation = new CertificationTypeTranslation();
                translation.setLanguage(apiTranslation.getLanguage());
                translation.setName(apiTranslation.getName());
                translation.setCertificationType(entity);
                entity.getTranslations().add(translation);
            }
        }

        if (entity.getId() == null) {
            em.persist(entity);
        }

        return CertificationTypeMapper.toApiCertificationType(entity, Language.EN);
    }

    @Transactional
    public void deleteCertificationType(Long id) throws ApiException {
        CertificationType entity = fetchEntity(id);
        em.remove(entity);
    }

    private CertificationType fetchEntity(Long id) throws ApiException {
        CertificationType entity = Queries.get(em, CertificationType.class, id);
        if (entity == null) {
            throw new ApiException(ApiStatus.NOT_FOUND, "CertificationType not found");
        }
        return entity;
    }
}
