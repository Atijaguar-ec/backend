package com.abelium.inatrace.components.stockorder.mappers;

import com.abelium.inatrace.components.codebook.measure_unit_type.MeasureUnitTypeMapper;
import com.abelium.inatrace.components.codebook.semiproduct.SemiProductMapper;
import com.abelium.inatrace.components.codebook.semiproduct.api.ApiSemiProduct;
import com.abelium.inatrace.components.common.CommonApiTools;
import com.abelium.inatrace.components.common.mappers.ActivityProofMapper;
import com.abelium.inatrace.components.company.mappers.CompanyCustomerMapper;
import com.abelium.inatrace.components.company.mappers.CompanyMapper;
import com.abelium.inatrace.components.company.mappers.UserCustomerMapper;
import com.abelium.inatrace.components.facility.FacilityMapper;
import com.abelium.inatrace.components.payment.PaymentMapper;
import com.abelium.inatrace.components.processingorder.mappers.ProcessingOrderMapper;
import com.abelium.inatrace.components.product.ProductApiTools;
import com.abelium.inatrace.components.productorder.mappers.ProductOrderMapper;
import com.abelium.inatrace.components.stockorder.api.ApiStockOrder;
import com.abelium.inatrace.components.stockorder.api.ApiStockOrderEvidenceTypeValue;
import com.abelium.inatrace.components.user.mappers.UserMapper;
import com.abelium.inatrace.db.entities.laboratory.LaboratoryAnalysis;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import com.abelium.inatrace.types.Language;
import org.apache.commons.lang3.BooleanUtils;

import java.util.stream.Collectors;

public class StockOrderMapper {
    
    /**
     * Populate laboratory analysis fields from LaboratoryAnalysis entity to ApiStockOrder.
     * This method is used when reading stock orders for editing, to display existing lab data.
     * 
     * @param apiStockOrder The API stock order to populate
     * @param laboratoryAnalysis The laboratory analysis entity with sensorial data
     */
    public static void populateLaboratoryAnalysisFields(ApiStockOrder apiStockOrder, LaboratoryAnalysis laboratoryAnalysis) {
        if (laboratoryAnalysis == null) {
            return;
        }
        
        apiStockOrder.setSensorialRawOdor(laboratoryAnalysis.getSensorialRawOdor());
        apiStockOrder.setSensorialRawOdorIntensity(laboratoryAnalysis.getSensorialRawOdorIntensity());
        apiStockOrder.setSensorialRawTaste(laboratoryAnalysis.getSensorialRawTaste());
        apiStockOrder.setSensorialRawTasteIntensity(laboratoryAnalysis.getSensorialRawTasteIntensity());
        apiStockOrder.setSensorialRawColor(laboratoryAnalysis.getSensorialRawColor());
        apiStockOrder.setSensorialCookedOdor(laboratoryAnalysis.getSensorialCookedOdor());
        apiStockOrder.setSensorialCookedOdorIntensity(laboratoryAnalysis.getSensorialCookedOdorIntensity());
        apiStockOrder.setSensorialCookedTaste(laboratoryAnalysis.getSensorialCookedTaste());
        apiStockOrder.setSensorialCookedTasteIntensity(laboratoryAnalysis.getSensorialCookedTasteIntensity());
        apiStockOrder.setSensorialCookedColor(laboratoryAnalysis.getSensorialCookedColor());
        apiStockOrder.setQualityNotes(laboratoryAnalysis.getQualityNotes());
        apiStockOrder.setMetabisulfiteLevelAcceptable(laboratoryAnalysis.getMetabisulfiteLevelAcceptable());
        apiStockOrder.setApprovedForPurchase(laboratoryAnalysis.getApprovedForPurchase());
    }

    public static ApiStockOrder toApiStockOrderBase(StockOrder entity) {

        if(entity == null) {
            return null;
        }

        ApiStockOrder apiStockOrder = new ApiStockOrder();
        apiStockOrder.setId(entity.getId());
        apiStockOrder.setIdentifier(entity.getIdentifier());
        apiStockOrder.setLotPrefix(entity.getLotPrefix());
        apiStockOrder.setInternalLotNumber(setupInternalLotNumberForSacked(entity.getInternalLotNumber(), entity.getSacNumber()));
        apiStockOrder.setCurrency(entity.getCurrency());
        apiStockOrder.setPreferredWayOfPayment(entity.getPreferredWayOfPayment());
        apiStockOrder.setFinalPriceDiscount(entity.getFinalPriceDiscount());
        apiStockOrder.setTotalQuantity(entity.getTotalQuantity());
        apiStockOrder.setFulfilledQuantity(entity.getFulfilledQuantity());
        apiStockOrder.setBalance(entity.getBalance());
        apiStockOrder.setOrderType(entity.getOrderType());
        apiStockOrder.setMeasureUnitType(
                MeasureUnitTypeMapper.toApiMeasureUnitType(entity.getMeasurementUnitType()));

        // If present map the QR code tag and the Final product for which the QR code tag was generated
        if (entity.getQrCodeTag() != null) {
            apiStockOrder.setQrCodeTag(entity.getQrCodeTag());
            apiStockOrder.setQrCodeTagFinalProduct(ProductApiTools.toApiFinalProductBase(entity.getQrCodeTagFinalProduct()));
        }

        // Map women share, organic and week number
        apiStockOrder.setWomenShare(entity.getWomenShare());
        apiStockOrder.setOrganic(entity.getOrganic());
        apiStockOrder.setWeekNumber(entity.getWeekNumber());
        apiStockOrder.setParcelLot(entity.getParcelLot());
        apiStockOrder.setVariety(entity.getVariety());
        apiStockOrder.setOrganicCertification(entity.getOrganicCertification());

        // Farmer
        apiStockOrder.setProducerUserCustomer(
                UserCustomerMapper.toApiUserCustomerBase(entity.getProducerUserCustomer()));

        // Collector
        apiStockOrder.setRepresentativeOfProducerUserCustomer(
                UserCustomerMapper.toApiUserCustomerBase(entity.getRepresentativeOfProducerUserCustomer()));

        return apiStockOrder;
    }

    public static ApiStockOrder toApiStockOrder(StockOrder entity, Long userId, Language language) {
        return toApiStockOrder(entity, userId, language, false);
    }

    public static ApiStockOrder toApiStockOrder(StockOrder entity, Long userId, Language language, Boolean withProcessingOrder) {

        if (entity == null) {
            return null;
        }

        ApiStockOrder apiStockOrder = new ApiStockOrder();
        apiStockOrder.setId(entity.getId());
        apiStockOrder.setIdentifier(entity.getIdentifier());
        apiStockOrder.setCreatedBy(UserMapper.toSimpleApiUser(entity.getCreatedBy()));
        apiStockOrder.setUpdatedBy(UserMapper.toSimpleApiUser(entity.getUpdatedBy()));
        apiStockOrder.setCreationTimestamp(entity.getCreationTimestamp());
        apiStockOrder.setUpdateTimestamp(entity.getUpdateTimestamp());
        apiStockOrder.setCreatorId(entity.getCreatorId());
        apiStockOrder.setProductionLocation(
                StockOrderLocationMapper.toApiStockOrderLocation(entity.getProductionLocation()));
        apiStockOrder.setRepresentativeOfProducerUserCustomer(
                UserCustomerMapper.toApiUserCustomer(entity.getRepresentativeOfProducerUserCustomer()));
        apiStockOrder.setProducerUserCustomer(
                UserCustomerMapper.toApiUserCustomer(entity.getProducerUserCustomer()));

        // Map the activity proofs
        if (!entity.getActivityProofs().isEmpty()) {
            apiStockOrder.setActivityProofs(entity.getActivityProofs().stream()
                    .map(ap -> ActivityProofMapper.toApiActivityProof(ap.getActivityProof(), userId))
                    .collect(Collectors.toList()));
        }

        // Map the instances (values) of processing evidence fields
        if (!entity.getProcessingEFValues().isEmpty()) {
            apiStockOrder.setRequiredEvidenceFieldValues(entity.getProcessingEFValues().stream()
                    .map(StockOrderEvidenceFieldValueMapper::toApiStockOrderEvidenceFieldValue)
                    .collect(Collectors.toList()));
        }

        // Map the instances (values) of processing evidence types (both required and other evidences)
        entity.getDocumentRequirements().forEach(stockOrderPETypeValue -> {

            ApiStockOrderEvidenceTypeValue apiEvidenceTypeValue = StockOrderEvidenceTypeValueMapper.toApiStockOrderEvidenceTypeValue(
                    stockOrderPETypeValue, userId, language);

            if (BooleanUtils.isTrue(stockOrderPETypeValue.getOtherEvidence())) {
                apiStockOrder.getOtherEvidenceDocuments().add(apiEvidenceTypeValue);
            } else {
                apiStockOrder.getRequiredEvidenceTypeValues().add(apiEvidenceTypeValue);
            }
        });

        // Map the semi-product that is represented by this stock order
        apiStockOrder.setSemiProduct(SemiProductMapper.toApiSemiProduct(entity.getSemiProduct(), ApiSemiProduct.class, language));

        apiStockOrder.setPriceDeterminedLater(entity.getPriceDeterminedLater());

        // Map the final product that is represented by this stock order
        apiStockOrder.setFinalProduct(ProductApiTools.toApiFinalProduct(entity.getFinalProduct()));

        // Set the facility and company of the stock order
        apiStockOrder.setFacility(FacilityMapper.toApiFacility(entity.getFacility(), language));
        apiStockOrder.setCompany(CompanyMapper.toApiCompanyBase(entity.getCompany()));

        // Set the measure unit of the stock order
        apiStockOrder.setMeasureUnitType(MeasureUnitTypeMapper.toApiMeasureUnitType(entity.getMeasurementUnitType()));

        // Set the quoted facility
        apiStockOrder.setQuoteFacility(FacilityMapper.toApiFacility(entity.getQuoteFacility(), language));

        // Set the quote company
        apiStockOrder.setQuoteCompany(CompanyMapper.toApiCompanyBase(entity.getQuoteCompany()));

        // Set the company customer for whom the stock order was created
        apiStockOrder.setConsumerCompanyCustomer(CompanyCustomerMapper.toApiCompanyCustomer(
                entity.getConsumerCompanyCustomer()));

        // Set the product order that triggered the creation of this stock order (if product order is not provided, set orderId from this Stock order)
        apiStockOrder.setProductOrder(ProductOrderMapper.toApiProductOrder(entity.getProductOrder(), language));
        if (apiStockOrder.getProductOrder() == null) {
            apiStockOrder.setOrderId(entity.getOrderId());
        }

        // Set the stock order quantities
        apiStockOrder.setTotalQuantity(entity.getTotalQuantity());
        apiStockOrder.setTotalGrossQuantity(entity.getTotalGrossQuantity());
        apiStockOrder.setFulfilledQuantity(entity.getFulfilledQuantity());
        apiStockOrder.setAvailableQuantity(entity.getAvailableQuantity());
        apiStockOrder.setTare(entity.getTare());
        apiStockOrder.setAvailable(entity.getAvailable());
        apiStockOrder.setOpenOrder(entity.getIsOpenOrder());
        apiStockOrder.setOutQuantityNotInRange(entity.getOutQuantityNotInRange());

        // Set dates
        apiStockOrder.setProductionDate(entity.getProductionDate());
        apiStockOrder.setDeliveryTime(entity.getDeliveryTime());

        // Set currency, prices and cost
        apiStockOrder.setCurrency(entity.getCurrency());
        apiStockOrder.setPricePerUnit(entity.getPricePerUnit());
        apiStockOrder.setDamagedPriceDeduction(entity.getDamagedPriceDeduction());
        apiStockOrder.setFinalPriceDiscount(entity.getFinalPriceDiscount());
        apiStockOrder.setDamagedWeightDeduction(entity.getDamagedWeightDeduction());
        apiStockOrder.setMoisturePercentage(entity.getMoisturePercentage());
        apiStockOrder.setMoistureWeightDeduction(entity.getMoistureWeightDeduction());
        apiStockOrder.setNetQuantity(entity.getNetQuantity());
        // 🦐 Shrimp-specific fields
        apiStockOrder.setNumberOfGavetas(entity.getNumberOfGavetas());
        apiStockOrder.setNumberOfBines(entity.getNumberOfBines());
        apiStockOrder.setNumberOfPiscinas(entity.getNumberOfPiscinas());
        apiStockOrder.setGuiaRemisionNumber(entity.getGuiaRemisionNumber());
        // 🦐 Shrimp processing-specific fields
        apiStockOrder.setCuttingType(entity.getCuttingType());
        apiStockOrder.setCuttingEntryDate(entity.getCuttingEntryDate());
        apiStockOrder.setCuttingExitDate(entity.getCuttingExitDate());
        apiStockOrder.setCuttingTemperatureControl(entity.getCuttingTemperatureControl());
        apiStockOrder.setTreatmentType(entity.getTreatmentType());
        apiStockOrder.setTreatmentEntryDate(entity.getTreatmentEntryDate());
        apiStockOrder.setTreatmentExitDate(entity.getTreatmentExitDate());
        apiStockOrder.setTreatmentTemperatureControl(entity.getTreatmentTemperatureControl());
        apiStockOrder.setTreatmentChemicalUsed(entity.getTreatmentChemicalUsed());
        // 🦐 Shrimp processing: freezing fields
        apiStockOrder.setFreezingType(entity.getFreezingType());
        apiStockOrder.setFreezingEntryDate(entity.getFreezingEntryDate());
        apiStockOrder.setFreezingExitDate(entity.getFreezingExitDate());
        apiStockOrder.setFreezingTemperatureControl(entity.getFreezingTemperatureControl());
        apiStockOrder.setTunnelProductionDate(entity.getTunnelProductionDate());
        apiStockOrder.setTunnelExpirationDate(entity.getTunnelExpirationDate());
        apiStockOrder.setTunnelNetWeight(entity.getTunnelNetWeight());
        apiStockOrder.setTunnelSupplier(entity.getTunnelSupplier());
        apiStockOrder.setTunnelFreezingType(entity.getTunnelFreezingType());
        apiStockOrder.setTunnelEntryDate(entity.getTunnelEntryDate());
        apiStockOrder.setTunnelExitDate(entity.getTunnelExitDate());
        apiStockOrder.setWashingWaterTemperature(entity.getWashingWaterTemperature());
        apiStockOrder.setWashingShrimpTemperatureControl(entity.getWashingShrimpTemperatureControl());
        // 🔬 Laboratory-specific fields
        apiStockOrder.setSampleNumber(entity.getSampleNumber());
        apiStockOrder.setReceptionTime(entity.getReceptionTime());
        apiStockOrder.setQualityDocument(CommonApiTools.toApiDocument(entity.getQualityDocument(), userId));
        // 🔍 Field inspection (sensory testing) specific fields
        apiStockOrder.setFlavorTestResult(entity.getFlavorTestResult());
        if (entity.getFlavorDefectType() != null) {
            apiStockOrder.setFlavorDefectTypeId(entity.getFlavorDefectType().getId());
            apiStockOrder.setFlavorDefectTypeCode(entity.getFlavorDefectType().getCode());
            apiStockOrder.setFlavorDefectTypeLabel(entity.getFlavorDefectType().getName());
        }
        apiStockOrder.setPurchaseRecommended(entity.getPurchaseRecommended());
        apiStockOrder.setInspectionNotes(entity.getInspectionNotes());
        apiStockOrder.setCost(entity.getCost());
        apiStockOrder.setPaid(entity.getPaid());
        apiStockOrder.setBalance(entity.getBalance());
        apiStockOrder.setPreferredWayOfPayment(entity.getPreferredWayOfPayment());

        // Set identifiers and type of the stock order
        apiStockOrder.setOrderType(entity.getOrderType());
        apiStockOrder.setLotPrefix(entity.getLotPrefix());
        apiStockOrder.setInternalLotNumber(setupInternalLotNumberForSacked(entity.getInternalLotNumber(), entity.getSacNumber()));
        apiStockOrder.setSacNumber(entity.getSacNumber());
        apiStockOrder.setRepackedOriginStockOrderId(entity.getRepackedOriginStockOrderId());
        apiStockOrder.setPurchaseOrder(entity.getPurchaseOrder());

        // Set other data fields
        apiStockOrder.setComments(entity.getComments());
        apiStockOrder.setWomenShare(entity.getWomenShare());
        apiStockOrder.setOrganic(entity.getOrganic());
        apiStockOrder.setWeekNumber(entity.getWeekNumber());
        apiStockOrder.setParcelLot(entity.getParcelLot());
        apiStockOrder.setVariety(entity.getVariety());
        apiStockOrder.setOrganicCertification(entity.getOrganicCertification());

        // Set price and currency for end customer (used in Quote orders for final products)
        apiStockOrder.setPricePerUnitForEndCustomer(entity.getPricePerUnitForEndCustomer());
        apiStockOrder.setCurrencyForEndCustomer(entity.getCurrencyForEndCustomer());

        // Map the consumer company customer
        apiStockOrder.setConsumerCompanyCustomer(
                CompanyCustomerMapper.toApiCompanyCustomerBase(entity.getConsumerCompanyCustomer()));

        // Map the stock order QR code tag and the final product for which the QR code tag is generated
        apiStockOrder.setQrCodeTag(entity.getQrCodeTag());
        apiStockOrder.setQrCodeTagFinalProduct(ProductApiTools.toApiFinalProductBase(entity.getQrCodeTagFinalProduct()));

        // If requested mapping with Processing order, map the Processing order that created this stock order
        if (BooleanUtils.isTrue(withProcessingOrder)) {
            apiStockOrder.setProcessingOrder(ProcessingOrderMapper.toApiProcessingOrderBase(entity.getProcessingOrder()));
        }

        apiStockOrder.setPayments(entity.getPayments().stream().map(payment -> PaymentMapper.toApiPayment(payment, null)).collect(Collectors.toList()));

        return apiStockOrder;
    }

    public static ApiStockOrder toApiStockOrderHistory(StockOrder entity, Language language) {
        return toApiStockOrderHistory(entity, null, language);
    }

    public static ApiStockOrder toApiStockOrderHistory(StockOrder entity, Long userId, Language language) {
        return toApiStockOrder(entity, userId, language, true);
    }

    /**
     * Maps a StockOrder entity to ApiStockOrder for timeline history items.
     * Includes facility with company info for proper timeline grouping and display.
     *
     * @param entity   The StockOrder entity to map
     * @param language The language for translations
     * @return ApiStockOrder with essential fields for history timeline display
     */
    public static ApiStockOrder toApiStockOrderHistoryItem(StockOrder entity, Language language) {

        if (entity == null) {
            return null;
        }

        ApiStockOrder apiStockOrder = new ApiStockOrder();
        apiStockOrder.setId(entity.getId());
        apiStockOrder.setIdentifier(entity.getIdentifier());
        apiStockOrder.setInternalLotNumber(setupInternalLotNumberForSacked(entity.getInternalLotNumber(), entity.getSacNumber()));
        apiStockOrder.setSacNumber(entity.getSacNumber());
        apiStockOrder.setOrderType(entity.getOrderType());
        apiStockOrder.setProductionDate(entity.getProductionDate());

        // Quantities for timeline display
        apiStockOrder.setTotalQuantity(entity.getTotalQuantity());
        apiStockOrder.setTotalGrossQuantity(entity.getTotalGrossQuantity());
        apiStockOrder.setMeasureUnitType(MeasureUnitTypeMapper.toApiMeasureUnitType(entity.getMeasurementUnitType()));

        // Financial data
        apiStockOrder.setCurrency(entity.getCurrency());
        apiStockOrder.setCost(entity.getCost());
        apiStockOrder.setPaid(entity.getPaid());
        apiStockOrder.setBalance(entity.getBalance());

        // Facility with company - essential for timeline company grouping
        apiStockOrder.setFacility(FacilityMapper.toApiFacilityBase(entity.getFacility(), language));

        // Products
        apiStockOrder.setSemiProduct(SemiProductMapper.toApiSemiProductBase(entity.getSemiProduct(), ApiSemiProduct.class, language));
        apiStockOrder.setFinalProduct(ProductApiTools.toApiFinalProductBase(entity.getFinalProduct()));

        // Evidence fields for timeline detail display
        if (!entity.getProcessingEFValues().isEmpty()) {
            apiStockOrder.setRequiredEvidenceFieldValues(entity.getProcessingEFValues().stream()
                    .map(StockOrderEvidenceFieldValueMapper::toApiStockOrderEvidenceFieldValue)
                    .collect(Collectors.toList()));
        }

        return apiStockOrder;
    }

    private static String setupInternalLotNumberForSacked(String internalLotNumber, Integer sacNumber) {
        if(internalLotNumber == null || sacNumber == null) {
            return internalLotNumber;
        }
        return String.format("%s/%d", internalLotNumber, sacNumber);
    }

}
