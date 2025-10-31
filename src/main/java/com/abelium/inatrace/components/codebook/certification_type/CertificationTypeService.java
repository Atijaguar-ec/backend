package com.abelium.inatrace.components.codebook.certification_type;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.codebook.certification_type.api.ApiCertificationType;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.db.entities.codebook.CertificationType;
import com.abelium.inatrace.db.entities.codebook.CertificationTypeTranslation;
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

/**
 * Service for certification type entity.
 *
 * @author Álvaro Sánchez
 */
@Lazy
@Service
public class CertificationTypeService extends BaseService {

    public ApiPaginatedList<ApiCertificationType> getCertificationTypeList(ApiPaginatedRequest request, Language language) {

        return PaginationTools.createPaginatedResponse(em, request, () -> certificationTypeQueryObject(request),
                certificationType -> CertificationTypeMapper.toApiCertificationTypeBase(certificationType, language));
    }

    private CertificationType certificationTypeQueryObject(ApiPaginatedRequest request) {

        CertificationType certificationType = Torpedo.from(CertificationType.class);

        switch (request.sortBy) {
            case "code":
                QueryTools.orderBy(request.sort, certificationType.getCode());
                break;
            case "name":
                QueryTools.orderBy(request.sort, certificationType.getName());
                break;
            case "category":
                QueryTools.orderBy(request.sort, certificationType.getCategory());
                break;
            case "status":
                QueryTools.orderBy(request.sort, certificationType.getStatus());
                break;
            default:
                QueryTools.orderBy(request.sort, certificationType.getId());
        }

        return certificationType;
    }

    public ApiCertificationType getCertificationType(Long id, Language language) throws ApiException {

        return CertificationTypeMapper.toApiCertificationType(fetchCertificationType(id), language);
    }

    @Transactional
    public ApiBaseEntity createOrUpdateCertificationType(CustomUserDetails authUser,
                                                         ApiCertificationType apiCertificationType) throws ApiException {

        CertificationType entity;

        if (apiCertificationType.getId() != null) {

            // Editing is not permitted for Regional admin
            if (authUser.getUserRole() == UserRole.REGIONAL_ADMIN) {
                throw new ApiException(ApiStatus.UNAUTHORIZED, "Regional admin not authorized!");
            }

            entity = fetchCertificationType(apiCertificationType.getId());
        } else {

            entity = new CertificationType();
            entity.setCode(apiCertificationType.getCode());
        }

        entity.setName(apiCertificationType.getLabel());
        entity.setCategory(apiCertificationType.getCategory());
        entity.setStatus(apiCertificationType.getStatus());

        // Remove translation if not in request
        entity.getTranslations().removeIf(translation -> apiCertificationType
                .getTranslations()
                .stream()
                .noneMatch(apiTranslation -> translation.getLanguage().equals(apiTranslation.getLanguage())));

        // Add or edit translations
        apiCertificationType.getTranslations().forEach(apiTranslation -> {
            CertificationTypeTranslation translation = entity.getTranslations().stream()
                    .filter(t -> t.getLanguage().equals(apiTranslation.getLanguage()))
                    .findFirst()
                    .orElse(new CertificationTypeTranslation());
            translation.setName(apiTranslation.getName());
            translation.setLanguage(apiTranslation.getLanguage());
            translation.setCertificationType(entity);
            entity.getTranslations().add(translation);
        });

        if (entity.getId() == null) {
            em.persist(entity);
        }

        return new ApiBaseEntity(entity);
    }

    @Transactional
    public void deleteCertificationType(Long id) throws ApiException {

        CertificationType certificationType = fetchCertificationType(id);
        em.remove(certificationType);
    }

    public CertificationType fetchCertificationType(Long id) throws ApiException {

        CertificationType certificationType = Queries.get(em, CertificationType.class, id);
        if (certificationType == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Invalid certification type ID");
        }

        return certificationType;
    }
}
