package com.abelium.inatrace.components.integration.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class ApiFarmerIntegration {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("farmer_internal_id")
    private String farmerInternalId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("national_id")
    private String nationalId;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("status")
    private String status;

    @JsonProperty("companies")
    private List<FarmerCompanyDto> companies;

    @JsonProperty("location")
    private FarmerLocationDto location;

    @JsonProperty("farm")
    private FarmDto farm;

    @JsonProperty("plots")
    private List<PlotDto> plots;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    public static class FarmerCompanyDto {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("role")
        private String role;

        public FarmerCompanyDto() {}
        public FarmerCompanyDto(Long id, String name, String role) {
            this.id = id;
            this.name = name;
            this.role = role;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class FarmerLocationDto {
        @JsonProperty("country_code")
        private String countryCode;

        @JsonProperty("address")
        private String address;

        @JsonProperty("city")
        private String city;

        @JsonProperty("state")
        private String state;

        @JsonProperty("is_publicly_visible")
        private Boolean isPubliclyVisible;

        public FarmerLocationDto() {}
        public FarmerLocationDto(String countryCode, String address, String city, String state, Boolean isPubliclyVisible) {
            this.countryCode = countryCode;
            this.address = address;
            this.city = city;
            this.state = state;
            this.isPubliclyVisible = isPubliclyVisible;
        }

        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public Boolean getIsPubliclyVisible() { return isPubliclyVisible; }
        public void setIsPubliclyVisible(Boolean isPubliclyVisible) { this.isPubliclyVisible = isPubliclyVisible; }
    }

    public static class FarmDto {
        @JsonProperty("area_unit")
        private String areaUnit;

        @JsonProperty("total_area")
        private Double totalArea;

        @JsonProperty("organic")
        private Boolean organic;

        @JsonProperty("max_production_quantity")
        private MaxProductionQuantityDto maxProductionQuantity;

        public FarmDto() {}

        public String getAreaUnit() { return areaUnit; }
        public void setAreaUnit(String areaUnit) { this.areaUnit = areaUnit; }
        public Double getTotalArea() { return totalArea; }
        public void setTotalArea(Double totalArea) { this.totalArea = totalArea; }
        public Boolean getOrganic() { return organic; }
        public void setOrganic(Boolean organic) { this.organic = organic; }
        public MaxProductionQuantityDto getMaxProductionQuantity() { return maxProductionQuantity; }
        public void setMaxProductionQuantity(MaxProductionQuantityDto maxProductionQuantity) { this.maxProductionQuantity = maxProductionQuantity; }
    }

    public static class MaxProductionQuantityDto {
        @JsonProperty("value")
        private BigDecimal value;

        @JsonProperty("unit")
        private String unit;

        public MaxProductionQuantityDto() {}
        public MaxProductionQuantityDto(BigDecimal value, String unit) {
            this.value = value;
            this.unit = unit;
        }

        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
    }

    public static class PlotDto {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("crop")
        private String crop;

        @JsonProperty("size")
        private Double size;

        @JsonProperty("size_unit")
        private String sizeUnit;

        @JsonProperty("number_of_plants")
        private Integer numberOfPlants;

        @JsonProperty("cocoa_variety")
        private CodeLabelDto cocoaVariety;

        @JsonProperty("certification_type")
        private CodeLabelDto certificationType;

        @JsonProperty("geo_id")
        private String geoId;

        @JsonProperty("geometry")
        private GeoJsonGeometryDto geometry;

        @JsonProperty("updated_at")
        private Instant updatedAt;

        public PlotDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCrop() { return crop; }
        public void setCrop(String crop) { this.crop = crop; }
        public Double getSize() { return size; }
        public void setSize(Double size) { this.size = size; }
        public String getSizeUnit() { return sizeUnit; }
        public void setSizeUnit(String sizeUnit) { this.sizeUnit = sizeUnit; }
        public Integer getNumberOfPlants() { return numberOfPlants; }
        public void setNumberOfPlants(Integer numberOfPlants) { this.numberOfPlants = numberOfPlants; }
        public CodeLabelDto getCocoaVariety() { return cocoaVariety; }
        public void setCocoaVariety(CodeLabelDto cocoaVariety) { this.cocoaVariety = cocoaVariety; }
        public CodeLabelDto getCertificationType() { return certificationType; }
        public void setCertificationType(CodeLabelDto certificationType) { this.certificationType = certificationType; }
        public String getGeoId() { return geoId; }
        public void setGeoId(String geoId) { this.geoId = geoId; }
        public GeoJsonGeometryDto getGeometry() { return geometry; }
        public void setGeometry(GeoJsonGeometryDto geometry) { this.geometry = geometry; }
        public Instant getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class CodeLabelDto {
        @JsonProperty("code")
        private String code;

        @JsonProperty("label")
        private String label;

        public CodeLabelDto() {}
        public CodeLabelDto(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    public static class GeoJsonGeometryDto {
        @JsonProperty("type")
        private String type;

        @JsonProperty("coordinates")
        private Object coordinates;

        public GeoJsonGeometryDto() {}
        public GeoJsonGeometryDto(String type, Object coordinates) {
            this.type = type;
            this.coordinates = coordinates;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Object getCoordinates() { return coordinates; }
        public void setCoordinates(Object coordinates) { this.coordinates = coordinates; }
    }

    public ApiFarmerIntegration() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFarmerInternalId() { return farmerInternalId; }
    public void setFarmerInternalId(String farmerInternalId) { this.farmerInternalId = farmerInternalId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<FarmerCompanyDto> getCompanies() { return companies; }
    public void setCompanies(List<FarmerCompanyDto> companies) { this.companies = companies; }
    public FarmerLocationDto getLocation() { return location; }
    public void setLocation(FarmerLocationDto location) { this.location = location; }
    public FarmDto getFarm() { return farm; }
    public void setFarm(FarmDto farm) { this.farm = farm; }
    public List<PlotDto> getPlots() { return plots; }
    public void setPlots(List<PlotDto> plots) { this.plots = plots; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
