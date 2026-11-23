package com.abelium.inatrace.components.integration.v1.mappers;

import com.abelium.inatrace.components.integration.v1.dto.ApiFacilityIntegration;
import com.abelium.inatrace.components.integration.v1.dto.ApiFacilityIntegration.FacilityLocationDto;
import com.abelium.inatrace.components.integration.v1.services.LotCompositionService;
import com.abelium.inatrace.db.entities.codebook.SemiProduct;
import com.abelium.inatrace.db.entities.codebook.SemiProductTranslation;
import com.abelium.inatrace.db.entities.facility.Facility;
import com.abelium.inatrace.db.entities.facility.FacilityTranslation;
import com.abelium.inatrace.types.Language;

public class FacilityIntegrationMapper {

    public static String resolveFacilityName(Facility facility) {
        if (facility == null) {
            return null;
        }
        if (facility.getName() != null && !facility.getName().isBlank()) {
            return facility.getName().trim();
        }
        try {
            if (facility.getFacilityTranslations() != null && !facility.getFacilityTranslations().isEmpty()) {
                String esName = null;
                String enName = null;
                String anyName = null;
                for (FacilityTranslation ft : facility.getFacilityTranslations()) {
                    if (ft != null && ft.getName() != null && !ft.getName().isBlank()) {
                        String trimmed = ft.getName().trim();
                        if (anyName == null) {
                            anyName = trimmed;
                        }
                        if (ft.getLanguage() == Language.ES) {
                            esName = trimmed;
                        } else if (ft.getLanguage() == Language.EN) {
                            enName = trimmed;
                        }
                    }
                }
                if (esName != null) {
                    return esName;
                }
                if (enName != null) {
                    return enName;
                }
                if (anyName != null) {
                    return anyName;
                }
            }
        } catch (Exception ignored) {
            // Ignore LazyInitializationException when detached without translations
        }
        return LotCompositionService.trimOrNull(facility.getName());
    }

    public static String resolveSemiProductName(SemiProduct sp) {
        if (sp == null) {
            return null;
        }
        if (sp.getName() != null && !sp.getName().isBlank()) {
            return sp.getName().trim();
        }
        try {
            if (sp.getSemiProductTranslations() != null && !sp.getSemiProductTranslations().isEmpty()) {
                String esName = null;
                String enName = null;
                String anyName = null;
                for (SemiProductTranslation st : sp.getSemiProductTranslations()) {
                    if (st != null && st.getName() != null && !st.getName().isBlank()) {
                        String trimmed = st.getName().trim();
                        if (anyName == null) {
                            anyName = trimmed;
                        }
                        if (st.getLanguage() == Language.ES) {
                            esName = trimmed;
                        } else if (st.getLanguage() == Language.EN) {
                            enName = trimmed;
                        }
                    }
                }
                if (esName != null) {
                    return esName;
                }
                if (enName != null) {
                    return enName;
                }
                if (anyName != null) {
                    return anyName;
                }
            }
        } catch (Exception ignored) {
            // Ignore LazyInitializationException when detached without translations
        }
        return LotCompositionService.trimOrNull(sp.getName());
    }

    public static ApiFacilityIntegration toDto(Facility facility) {
        if (facility == null) {
            return null;
        }

        ApiFacilityIntegration dto = new ApiFacilityIntegration();
        dto.setId(facility.getId());
        if (facility.getCompany() != null) {
            dto.setCompanyId(facility.getCompany().getId());
        }
        dto.setName(resolveFacilityName(facility));
        dto.setIsCollectionCenter(Boolean.TRUE.equals(facility.getIsCollectionFacility()));

        if (facility.getFacilityLocation() != null) {
            FacilityLocationDto loc = new FacilityLocationDto();
            loc.setLatitude(facility.getFacilityLocation().getLatitude());
            loc.setLongitude(facility.getFacilityLocation().getLongitude());
            if (facility.getFacilityLocation().getAddress() != null) {
                var addr = facility.getFacilityLocation().getAddress();
                loc.setAddress(LotCompositionService.trimOrNull(addr.getAddress()));
                loc.setCity(LotCompositionService.trimOrNull(addr.getCity()));
                loc.setState(LotCompositionService.trimOrNull(addr.getState()));
            }
            dto.setLocation(loc);
        }

        dto.setUpdatedAt(facility.getUpdateTimestamp() != null ? facility.getUpdateTimestamp() : facility.getCreationTimestamp());

        return dto;
    }
}
