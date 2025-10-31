package com.abelium.inatrace.components.codebook.certification_type;

import com.abelium.inatrace.components.codebook.certification_type.api.ApiCertificationType;
import com.abelium.inatrace.components.codebook.certification_type.api.ApiCertificationTypeTranslation;
import com.abelium.inatrace.db.entities.codebook.CertificationType;
import com.abelium.inatrace.db.entities.codebook.CertificationTypeTranslation;
import com.abelium.inatrace.types.Language;

import java.util.stream.Collectors;

/**
 * Mapper for CertificationType entity.
 *
 * @author Álvaro Sánchez
 */
public final class CertificationTypeMapper {

    private CertificationTypeMapper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Mapping the base entity attributes - no associations are included.
     *
     * @param entity   DB entity.
     * @param language Requested language.
     * @return API model entity.
     */
    public static ApiCertificationType toApiCertificationTypeBase(CertificationType entity, Language language) {

        if (entity == null) {
            return null;
        }

        CertificationTypeTranslation translation = entity.getTranslations().stream()
                .filter(t -> t.getLanguage().equals(language))
                .findFirst()
                .orElse(new CertificationTypeTranslation());

        // Fallback to English if translation not found
        if (translation.getName() == null || translation.getName().isEmpty()) {
            translation = entity.getTranslations().stream()
                    .filter(t -> t.getLanguage().equals(Language.EN))
                    .findFirst()
                    .orElse(new CertificationTypeTranslation());
        }

        ApiCertificationType apiCertificationType = new ApiCertificationType();
        apiCertificationType.setId(entity.getId());
        apiCertificationType.setCode(entity.getCode());
        apiCertificationType.setLabel(translation.getName() != null ? translation.getName() : entity.getName());
        apiCertificationType.setCategory(entity.getCategory());
        apiCertificationType.setStatus(entity.getStatus());

        return apiCertificationType;
    }

    /**
     * Mapping of the base attributes and all the associations.
     *
     * @param entity   DB entity.
     * @param language Requested language.
     * @return API model entity.
     */
    public static ApiCertificationType toApiCertificationType(CertificationType entity, Language language) {

        ApiCertificationType apiCertificationType = CertificationTypeMapper.toApiCertificationTypeBase(entity, language);

        if (apiCertificationType == null) {
            return null;
        }

        apiCertificationType.setTranslations(
                entity.getTranslations().stream()
                        .map(CertificationTypeMapper::toApiCertificationTypeTranslation)
                        .collect(Collectors.toList())
        );

        return apiCertificationType;
    }

    public static ApiCertificationTypeTranslation toApiCertificationTypeTranslation(CertificationTypeTranslation translation) {
        ApiCertificationTypeTranslation apiTranslation = new ApiCertificationTypeTranslation();
        apiTranslation.setLanguage(translation.getLanguage());
        apiTranslation.setName(translation.getName());
        return apiTranslation;
    }
}
