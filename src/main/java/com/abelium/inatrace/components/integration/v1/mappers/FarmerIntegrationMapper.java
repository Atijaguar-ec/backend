package com.abelium.inatrace.components.integration.v1.mappers;

import com.abelium.inatrace.components.integration.v1.dto.ApiFarmerIntegration;
import com.abelium.inatrace.components.integration.v1.dto.ApiFarmerIntegration.*;
import com.abelium.inatrace.db.entities.common.Plot;
import com.abelium.inatrace.db.entities.common.PlotCoordinate;
import com.abelium.inatrace.db.entities.common.UserCustomer;
import com.abelium.inatrace.tools.PlotGeometryTools;
import com.abelium.inatrace.types.CocoaVariety;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class FarmerIntegrationMapper {

    public static Double round6(Double val) {
        if (val == null) {
            return null;
        }
        return BigDecimal.valueOf(val).setScale(6, RoundingMode.HALF_UP).doubleValue();
    }

    public static ApiFarmerIntegration toDto(UserCustomer farmer) {
        return toDto(farmer, null);
    }

    public static ApiFarmerIntegration toDto(UserCustomer farmer, Map<Long, List<PlotCoordinate>> coordinatesByPlotId) {
        if (farmer == null) {
            return null;
        }

        ApiFarmerIntegration dto = new ApiFarmerIntegration();
        dto.setId(farmer.getId());
        dto.setFarmerInternalId(farmer.getFarmerCompanyInternalId());
        dto.setName(farmer.getName());
        dto.setSurname(farmer.getSurname());
        // UserCustomer stores national identifier or internal ID in farmerCompanyInternalId
        dto.setNationalId(farmer.getFarmerCompanyInternalId() != null && farmer.getFarmerCompanyInternalId().matches("^[0-9]{10,13}$") ? farmer.getFarmerCompanyInternalId() : null);
        dto.setGender(farmer.getGender() != null ? farmer.getGender().name() : null);
        dto.setStatus(farmer.getStatus() != null ? farmer.getStatus().name() : "ACTIVE");

        // Companies
        if (farmer.getCompany() != null) {
            dto.setCompanies(Collections.singletonList(new FarmerCompanyDto(
                    farmer.getCompany().getId(),
                    farmer.getCompany().getName(),
                    "FARMER"
            )));
        } else {
            dto.setCompanies(Collections.emptyList());
        }

        // Location (LOPDP sanitized - NO phone, NO email)
        FarmerLocationDto locDto = new FarmerLocationDto();
        locDto.setCountryCode("EC");
        if (farmer.getUserCustomerLocation() != null) {
            locDto.setIsPubliclyVisible(Boolean.TRUE.equals(farmer.getUserCustomerLocation().getPubliclyVisible()));
            if (farmer.getUserCustomerLocation().getAddress() != null) {
                var addr = farmer.getUserCustomerLocation().getAddress();
                locDto.setAddress(addr.getAddress());
                locDto.setCity(addr.getCity());
                locDto.setState(addr.getState());
                if (addr.getCountry() != null && addr.getCountry().getCode() != null) {
                    locDto.setCountryCode(addr.getCountry().getCode());
                }
            }
        } else {
            locDto.setIsPubliclyVisible(false);
        }
        dto.setLocation(locDto);

        // Farm
        FarmDto farmDto = new FarmDto();
        farmDto.setAreaUnit("ha");
        double totalArea = 0.0;
        BigDecimal plotEstimatedQq = BigDecimal.ZERO;
        if (farmer.getPlots() != null) {
            for (Plot p : farmer.getPlots()) {
                if (p.getSize() != null) {
                    totalArea += p.getSize();
                }
                if (p.getProductionEstimate() != null) {
                    plotEstimatedQq = plotEstimatedQq.add(p.getProductionEstimate());
                }
            }
        }
        if (farmer.getFarm() != null) {
            if (farmer.getFarm().getTotalCultivatedArea() != null) {
                farmDto.setTotalArea(farmer.getFarm().getTotalCultivatedArea().doubleValue());
            } else {
                farmDto.setTotalArea(totalArea > 0 ? totalArea : null);
            }
            farmDto.setOrganic(Boolean.TRUE.equals(farmer.getFarm().getOrganic()));
            if (farmer.getFarm().getAreaUnit() != null) {
                farmDto.setAreaUnit(farmer.getFarm().getAreaUnit());
            }
            if (farmer.getFarm().getMaxProductionQuantity() != null) {
                farmDto.setMaxProductionQuantity(new MaxProductionQuantityDto(
                        farmer.getFarm().getMaxProductionQuantity(),
                        "QQ"
                ));
            } else if (plotEstimatedQq.compareTo(BigDecimal.ZERO) > 0) {
                farmDto.setMaxProductionQuantity(new MaxProductionQuantityDto(plotEstimatedQq, "QQ"));
            }
        } else {
            farmDto.setTotalArea(totalArea > 0 ? totalArea : null);
            farmDto.setOrganic(false);
            if (plotEstimatedQq.compareTo(BigDecimal.ZERO) > 0) {
                farmDto.setMaxProductionQuantity(new MaxProductionQuantityDto(plotEstimatedQq, "QQ"));
            }
        }
        dto.setFarm(farmDto);

        // Plots
        List<PlotDto> plotDtos = new ArrayList<>();
        if (farmer.getPlots() != null) {
            for (Plot plot : farmer.getPlots()) {
                PlotDto pDto = new PlotDto();
                pDto.setId(plot.getId());
                pDto.setName(plot.getPlotName());
                String cropName = "COCOA";
                if (plot.getCrop() != null) {
                    if (plot.getCrop().getName() != null && !plot.getCrop().getName().isBlank()) {
                        cropName = plot.getCrop().getName();
                    } else if (plot.getCrop().getCode() != null && !plot.getCrop().getCode().isBlank()) {
                        cropName = plot.getCrop().getCode();
                    }
                }
                pDto.setCrop(cropName);
                pDto.setSize(plot.getSize());
                pDto.setSizeUnit(plot.getUnit() != null ? plot.getUnit() : "ha");
                pDto.setNumberOfPlants(plot.getNumberOfPlants());
                pDto.setGeoId(plot.getGeoId());

                // Variety
                if (plot.getCocoaVariety() != null) {
                    if (plot.getCocoaVariety() == CocoaVariety.CCN51) {
                        pDto.setCocoaVariety(new CodeLabelDto("CCN51", "CCN-51"));
                    } else {
                        pDto.setCocoaVariety(new CodeLabelDto("NACIONAL", "Nacional / Fino de Aroma"));
                    }
                }

                // Certification
                if (plot.getCertificationType() != null) {
                    pDto.setCertificationType(new CodeLabelDto(
                            plot.getCertificationType().getCode(),
                            plot.getCertificationType().getName()
                    ));
                }

                // GeoJSON Geometry (Polygon >= 3 coordinates or Point fallback)
                List<PlotCoordinate> coords = (coordinatesByPlotId != null && plot.getId() != null && coordinatesByPlotId.containsKey(plot.getId()))
                        ? coordinatesByPlotId.get(plot.getId())
                        : plot.getCoordinates();
                GeoJsonGeometryDto geomDto = null;
                if (coords != null && coords.size() >= 3) {
                    List<PlotCoordinate> ring = PlotGeometryTools.closedCounterClockwiseRing(coords);
                    if (ring != null && ring.size() >= 4) {
                        List<List<Double>> ringList = new ArrayList<>();
                        for (PlotCoordinate c : ring) {
                            if (c.getLongitude() != null && c.getLatitude() != null) {
                                ringList.add(Arrays.asList(round6(c.getLongitude()), round6(c.getLatitude())));
                            }
                        }
                        if (ringList.size() >= 4) {
                            geomDto = new GeoJsonGeometryDto("Polygon", Collections.singletonList(ringList));
                        }
                    }
                }
                if (geomDto == null && coords != null && !coords.isEmpty()) {
                    for (PlotCoordinate c : coords) {
                        if (c != null && c.getLatitude() != null && c.getLongitude() != null) {
                            geomDto = new GeoJsonGeometryDto("Point", Arrays.asList(round6(c.getLongitude()), round6(c.getLatitude())));
                            break;
                        }
                    }
                }
                pDto.setGeometry(geomDto);

                if (plot.getLastUpdated() != null) {
                    pDto.setUpdatedAt(plot.getLastUpdated().toInstant());
                }
                plotDtos.add(pDto);
            }
        }
        plotDtos.sort(Comparator.comparing(PlotDto::getId, Comparator.nullsLast(Comparator.naturalOrder())));
        dto.setPlots(plotDtos);

        var createdTs = farmer.getCreationTimestamp() != null ? farmer.getCreationTimestamp() : farmer.getStatusUpdateTimestamp();
        var updatedTs = farmer.getUpdateTimestamp() != null ? farmer.getUpdateTimestamp() : createdTs;
        dto.setCreatedAt(createdTs);
        dto.setUpdatedAt(updatedTs);

        return dto;
    }
}
