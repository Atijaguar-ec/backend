package com.abelium.inatrace.components.integration.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "Collection center, post-harvest processing area, or storage facility")
public class ApiFacilityIntegration {

    @JsonProperty("id")
    @Schema(description = "Unique facility ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @JsonProperty("company_id")
    @Schema(description = "Owning organization ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long companyId;

    @JsonProperty("name")
    @Schema(description = "Facility name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @JsonProperty("is_collection_center")
    @Schema(description = "True if the facility operates as a primary cocoa collection center", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isCollectionCenter;

    @JsonProperty("location")
    @Schema(description = "Physical address and optional coordinates")
    private LocationDto location;

    @JsonProperty("updated_at")
    @Schema(description = "Last modification UTC timestamp (RFC 3339)")
    private Instant updatedAt;

    @Schema(name = "FacilityLocationDto", description = "Facility address and geographic coordinates")
    public static class LocationDto {
        @JsonProperty("address")
        private String address;

        @JsonProperty("city")
        private String city;

        @JsonProperty("state")
        private String state;

        @JsonProperty("latitude")
        private Double latitude;

        @JsonProperty("longitude")
        private Double longitude;

        public LocationDto() {}

        public LocationDto(String address, String city, String state, Double latitude, Double longitude) {
            this.address = address;
            this.city = city;
            this.state = state;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        public String getState() { return state; }
        public void setState(String state) { this.state = state; }

        public Double getLatitude() { return latitude; }
        public void setLatitude(Double latitude) { this.latitude = latitude; }

        public Double getLongitude() { return longitude; }
        public void setLongitude(Double longitude) { this.longitude = longitude; }
    }

    public ApiFacilityIntegration() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getIsCollectionCenter() { return isCollectionCenter; }
    public void setIsCollectionCenter(Boolean isCollectionCenter) { this.isCollectionCenter = isCollectionCenter; }

    public LocationDto getLocation() { return location; }
    public void setLocation(LocationDto location) { this.location = location; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
