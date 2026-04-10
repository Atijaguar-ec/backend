package com.abelium.inatrace.components.codebook.certification_type;

import com.abelium.inatrace.components.codebook.certification_type.api.ApiCertificationType;
import com.abelium.inatrace.components.codebook.certification_type.api.ApiCertificationTypeTranslation;
import com.abelium.inatrace.db.entities.codebook.CertificationType;
import com.abelium.inatrace.db.entities.codebook.CertificationTypeTranslation;
import com.abelium.inatrace.types.Language;

import java.util.stream.Collectors;

public class CertificationTypeMapper {

    private CertificationTypeMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static ApiCertificationType toApiCertificationTypeBase(CertificationType entity, Language language) {
        if(entity == null) {
            return null;
        }

        ApiCertificationType api = new ApiCertificationType();
        api.setId(entity.getId());
        api.setCode(entity.getCode());
        api.setCategory(entity.getCategory());
        api.setStatus(entity.getStatus());

        CertificationTypeTranslation translation = entity.getTranslations().stream()
                .filter(t -> t.getLanguage().equals(language))
                .findFirst()
                .orElse(null);

        api.setName(translation != null ? translation.getName() : entity.getName());

        return api;
    }

    public static ApiCertificationType toApiCertificationType(CertificationType entity, Language language) {
        ApiCertificationType api = toApiCertificationTypeBase(entity, language);

        if(api == null) {
            return null;
        }

        api.setTranslations(entity.getTranslations().stream()
                .map(CertificationTypeMapper::toApiCertificationTypeTranslation)
                .collect(Collectors.toList()));

        return api;
    }

    public static ApiCertificationTypeTranslation toApiCertificationTypeTranslation(CertificationTypeTranslation entity) {
        if(entity == null) {
            return null;
        }

        ApiCertificationTypeTranslation api = new ApiCertificationTypeTranslation();
        api.setName(entity.getName());
        api.setLanguage(entity.getLanguage());
        return api;
    }
}
