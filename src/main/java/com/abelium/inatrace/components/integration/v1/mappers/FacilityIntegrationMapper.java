package com.abelium.inatrace.components.integration.v1.mappers;

import com.abelium.inatrace.components.integration.v1.dto.ApiFacilityIntegration;
import com.abelium.inatrace.components.integration.v1.dto.ApiFacilityIntegration.LocationDto;
import com.abelium.inatrace.db.entities.facility.Facility;

public class FacilityIntegrationMapper {

    public static ApiFacilityIntegration toDto(Facility facility) {
        if (facility == null) {
            return null;
        }

        ApiFacilityIntegration dto = new ApiFacilityIntegration();
        dto.setId(facility.getId());
        if (facility.getCompany() != null) {
            dto.setCompanyId(facility.getCompany().getId());
        }
        dto.setName(facility.getName());
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
