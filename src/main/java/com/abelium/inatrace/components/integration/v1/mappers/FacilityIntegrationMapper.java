package com.abelium.inatrace.components.integration.v1.mappers;

import com.abelium.inatrace.components.integration.v1.dto.ApiFacilityIntegration;
import com.abelium.inatrace.components.integration.v1.dto.ApiFacilityIntegration.LocationDto;
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
            return facility.getName();
        }
        try {
            if (facility.getFacilityTranslations() != null && !facility.getFacilityTranslations().isEmpty()) {
                String esName = null;
                String enName = null;
                String anyName = null;
                for (FacilityTranslation ft : facility.getFacilityTranslations()) {
                    if (ft != null && ft.getName() != null && !ft.getName().isBlank()) {
                        if (anyName == null) {
                            anyName = ft.getName();
                        }
                        if (ft.getLanguage() == Language.ES) {
                            esName = ft.getName();
                        } else if (ft.getLanguage() == Language.EN) {
                            enName = ft.getName();
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
        return facility.getName();
    }

    public static String resolveSemiProductName(SemiProduct sp) {
        if (sp == null) {
            return null;
        }
        if (sp.getName() != null && !sp.getName().isBlank()) {
            return sp.getName();
        }
        try {
            if (sp.getSemiProductTranslations() != null && !sp.getSemiProductTranslations().isEmpty()) {
                String esName = null;
                String enName = null;
                String anyName = null;
                for (SemiProductTranslation st : sp.getSemiProductTranslations()) {
                    if (st != null && st.getName() != null && !st.getName().isBlank()) {
                        if (anyName == null) {
                            anyName = st.getName();
                        }
                        if (st.getLanguage() == Language.ES) {
                            esName = st.getName();
                        } else if (st.getLanguage() == Language.EN) {
                            enName = st.getName();
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
        return sp.getName();
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
            LocationDto loc = new LocationDto();
            loc.setLatitude(facility.getFacilityLocation().getLatitude());
            loc.setLongitude(facility.getFacilityLocation().getLongitude());
            if (facility.getFacilityLocation().getAddress() != null) {
                var addr = facility.getFacilityLocation().getAddress();
                loc.setAddress(addr.getAddress());
                loc.setCity(addr.getCity());
                loc.setState(addr.getState());
            }
            dto.setLocation(loc);
        }

        dto.setUpdatedAt(facility.getUpdateTimestamp() != null ? facility.getUpdateTimestamp() : facility.getCreationTimestamp());

        return dto;
    }
}
