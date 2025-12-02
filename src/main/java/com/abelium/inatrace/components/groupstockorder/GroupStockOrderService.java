package com.abelium.inatrace.components.groupstockorder;

import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.components.groupstockorder.api.ApiGroupStockOrder;
import com.abelium.inatrace.tools.PaginationTools;
import com.abelium.inatrace.types.Language;
import jakarta.persistence.TypedQuery;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for managing grouped stock orders and generating Excel exports.
 * Groups stock orders by common attributes (date, lot, product) for reporting.
 *
 * @author INATrace Development Team
 */
@Lazy
@Service
public class GroupStockOrderService extends BaseService {

    private static final Logger logger = LoggerFactory.getLogger(GroupStockOrderService.class);
    private static final int EXCEL_HEADER_FONT_SIZE = 12;
    private static final int MONTHS_LOOKBACK = 12;

    public ApiPaginatedList<ApiGroupStockOrder> getGroupedStockOrderList(
            ApiPaginatedRequest request,
            GroupStockOrderQueryRequest queryRequest,
            Language language
    ) {

        // SELECT query string for which columns to get from database and put them into DTO ApiGroupStockOrder
        StringBuilder queryString = new StringBuilder(
            "SELECT new com.abelium.inatrace.components.groupstockorder.api.ApiGroupStockOrder(" +
            "GROUP_CONCAT(SO.id), " +
            "SO.productionDate AS date, SO.internalLotNumber AS id, COUNT(SO.sacNumber) as noOfSacs, " +
            "SO.orderType, SPT.name, CONCAT(FP.name, ' (', P.name, ')'), SO.weekNumber, SO.parcelLot, SO.variety, SO.organicCertification, " +
            "SUM(SO.totalQuantity), SUM(SO.fulfilledQuantity), SUM(SO.availableQuantity), " +
            "MUT.label, SO.deliveryTime AS deliveryTime, PO.updateTimestamp AS updateTimestamp, " +
            "SO.isAvailable " +
            ") FROM StockOrder SO " +
            "LEFT JOIN SO.processingOrder PO " +
            "LEFT JOIN SO.measurementUnitType MUT " +
            "LEFT JOIN SO.semiProduct SP " +
            "LEFT JOIN SO.finalProduct FP " +
            "LEFT JOIN FP.product P " +
            "LEFT JOIN SP.semiProductTranslations SPT "
        );

        // Build WHERE query string for filtering purposes
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" WHERE (SPT.language IS NULL OR SPT.language = :language)");
        if(queryRequest.facilityId != null) {
            whereClause.append(" AND SO.facility.id = :facilityId");
        }
        if(queryRequest.availableOnly != null && queryRequest.availableOnly) {
            whereClause.append(" AND SO.isAvailable = true");
        }
        if(queryRequest.isPurchaseOrderOnly != null && queryRequest.isPurchaseOrderOnly) {
            whereClause.append(" AND SO.isPurchaseOrder = true");
        }
        if(queryRequest.semiProducId != null) {
            whereClause.append(" AND SP.id = :semiProductId");
        }
        queryString.append(whereClause);

        // Add GROUP BY query string for grouping common columns
        queryString.append(
                " GROUP BY SO.productionDate, SO.internalLotNumber, SO.orderType, SPT.name, SO.weekNumber, SO.parcelLot, SO.variety, SO.organicCertification, MUT.label, " +
                "SO.deliveryTime, PO.updateTimestamp, SO.isAvailable, FP.name, P.name "
        );

        // Add ORDER BY query string to sort on requested field and direction
        queryString.append("ORDER BY ");
        queryString.append(request.sortBy);
        queryString.append(" ");
        queryString.append(request.sort.toString());

        TypedQuery<ApiGroupStockOrder> query = em.createQuery(queryString.toString(), ApiGroupStockOrder.class);
        query.setParameter("language", language);
        if(queryRequest.facilityId != null) {
            query.setParameter("facilityId", queryRequest.facilityId);
        }
        if(queryRequest.semiProducId != null) {
            query.setParameter("semiProductId", queryRequest.semiProducId);
        }

        long count = query.getResultList().size();  // Get count of all entities for pagination
        return PaginationTools.createPaginatedResponse(
                null,
                () -> query.setFirstResult(request.offset).setMaxResults(request.limit).getResultList(),
                () -> count
        );
    }

    /**
     * Exports grouped stock orders to Excel for a specific facility.
     *
     * @param facilityId The facility ID to filter by
     * @param language Language for translations (ES/EN)
     * @return Excel file as ByteArrayInputStream
     * @throws IOException If Excel generation fails
     */
    public ByteArrayInputStream exportGroupedStockOrdersToExcel(Long facilityId, Language language) throws IOException {
        logger.info("Exporting grouped stock orders to Excel for facility: {}, language: {}", facilityId, language);
        List<ApiGroupStockOrder> orders = fetchGroupedStockOrders(language, facilityId, null);
        logger.info("Found {} grouped orders for facility {}", orders.size(), facilityId);
        return generateExcelFile(orders, language);
    }

    /**
     * Exports grouped stock orders to Excel for a specific company.
     *
     * @param companyId The company ID to filter by
     * @param language Language for translations (ES/EN)
     * @return Excel file as ByteArrayInputStream
     * @throws IOException If Excel generation fails
     */
    public ByteArrayInputStream exportGroupedStockOrdersToExcelByCompany(Long companyId, Language language) throws IOException {
        logger.info("Exporting grouped stock orders to Excel for company: {}, language: {}", companyId, language);
        List<ApiGroupStockOrder> orders = fetchGroupedStockOrders(language, null, companyId);
        logger.info("Found {} grouped orders for company {}", orders.size(), companyId);
        return generateExcelFile(orders, language);
    }

    /**
     * Fetches grouped stock orders from database with farmer names.
     * Data is filtered by last 12 months and optionally by facility or company.
     *
     * @param language Language for semi-product translations
     * @param facilityId Optional facility filter (null = no filter)
     * @param companyId Optional company filter (null = no filter)
     * @return List of grouped stock orders with aggregated data
     */
    private List<ApiGroupStockOrder> fetchGroupedStockOrders(Language language, Long facilityId, Long companyId) {

        LocalDate oneYearAgo = LocalDate.now().minusMonths(MONTHS_LOOKBACK);
        logger.debug("Fetching grouped orders from {} onwards", oneYearAgo);

        StringBuilder queryString = new StringBuilder(
                "SELECT new com.abelium.inatrace.components.groupstockorder.api.ApiGroupStockOrder(" +
                        "GROUP_CONCAT(SO.id), " +
                "COALESCE(FT.name, F.name), " +
                "SO.productionDate AS date, SO.internalLotNumber AS id, COUNT(SO.sacNumber) as noOfSacs, " +
                "SO.orderType, SPT.name, CONCAT(FP.name, ' (', P.name, ')'), SO.weekNumber, SO.parcelLot, SO.variety, SO.organicCertification, " +
                "SUM(SO.totalQuantity), SUM(SO.fulfilledQuantity), SUM(SO.availableQuantity), " +
                "MUT.label, SO.deliveryTime AS deliveryTime, PO.updateTimestamp AS updateTimestamp, " +
                "SO.isAvailable, " +
                "GROUP_CONCAT(DISTINCT CONCAT(COALESCE(PC.name, ''), ' ', COALESCE(PC.surname, ''))) " +
                ") FROM StockOrder SO " +
                        "LEFT JOIN SO.processingOrder PO " +
                        "LEFT JOIN SO.measurementUnitType MUT " +
                        "LEFT JOIN SO.semiProduct SP " +
                        "LEFT JOIN SO.finalProduct FP " +
                        "LEFT JOIN FP.product P " +
                        "LEFT JOIN SP.semiProductTranslations SPT " +
                        "LEFT JOIN SO.facility F " +
                        "LEFT JOIN F.facilityTranslations FT WITH FT.language = :language " +
                        "LEFT JOIN SO.producerUserCustomer PC "  // Farmer/producer info
        );

        queryString.append(" WHERE (SPT.language IS NULL OR SPT.language = :language)");
        queryString.append(" AND SO.productionDate >= :oneYearAgo");

        if (facilityId != null) {
            queryString.append(" AND SO.facility.id = :facilityId");
        }

        if (companyId != null) {
            queryString.append(" AND SO.company.id = :companyId");
        }

        queryString.append(
                " GROUP BY SO.productionDate, SO.internalLotNumber, SO.orderType, SPT.name, SO.weekNumber, SO.parcelLot, SO.variety, SO.organicCertification, MUT.label, " +
                        "SO.deliveryTime, PO.updateTimestamp, SO.isAvailable, FP.name, P.name, F.name, FT.name " +
                        "ORDER BY SO.productionDate DESC"
        );

        TypedQuery<ApiGroupStockOrder> query = em.createQuery(queryString.toString(), ApiGroupStockOrder.class);
        query.setParameter("language", language);
        query.setParameter("oneYearAgo", oneYearAgo);

        if (facilityId != null) {
            query.setParameter("facilityId", facilityId);
        }

        if (companyId != null) {
            query.setParameter("companyId", companyId);
        }

        List<ApiGroupStockOrder> result = query.getResultList();
        logger.debug("Query returned {} grouped stock orders", result.size());
        return result;
    }
    
    /**
     * Generates Excel file from grouped stock orders.
     *
     * @param orders List of grouped orders to export
     * @param language Language for column headers and formatting
     * @return Excel file as ByteArrayInputStream
     * @throws IOException If Excel generation fails
     */
    private ByteArrayInputStream generateExcelFile(List<ApiGroupStockOrder> orders, Language language) throws IOException {
        
        logger.debug("Generating Excel file with {} orders in language {}", orders.size(), language);
        
        String[] columns = getColumnHeaders(language);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(getSheetName(language));
            
            // Create header row style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) EXCEL_HEADER_FONT_SIZE);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerCellStyle.setBorderBottom(BorderStyle.THIN);
            headerCellStyle.setBorderTop(BorderStyle.THIN);
            headerCellStyle.setBorderRight(BorderStyle.THIN);
            headerCellStyle.setBorderLeft(BorderStyle.THIN);
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }
            
            // Create data rows
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            CellStyle dataCellStyle = workbook.createCellStyle();
            dataCellStyle.setBorderBottom(BorderStyle.THIN);
            dataCellStyle.setBorderTop(BorderStyle.THIN);
            dataCellStyle.setBorderRight(BorderStyle.THIN);
            dataCellStyle.setBorderLeft(BorderStyle.THIN);
            
            int rowIdx = 1;
            for (ApiGroupStockOrder order : orders) {
                Row row = sheet.createRow(rowIdx++);
                
                // Safely handle null values in all cells
                
                createCell(row, 0, order.getFacilityName() != null ? order.getFacilityName() : "", dataCellStyle);
                // Farmer name(s) - may be multiple names if grouped
                String farmerNames = order.getFarmerName() != null ? order.getFarmerName().trim() : "";
                createCell(row, 1, farmerNames, dataCellStyle);
                createCell(row, 2, order.getProductionDate() != null ? order.getProductionDate().format(dateFormatter) : "", dataCellStyle);
                createCell(row, 3, order.getInternalLotNumber() != null ? order.getInternalLotNumber() : "", dataCellStyle);
                createCell(row, 4, order.getNoOfSacs() != null && order.getNoOfSacs() > 0 ? order.getNoOfSacs().toString() : "0", dataCellStyle);
                createCell(row, 5, order.getOrderType() != null ? formatOrderType(order.getOrderType().toString(), language) : "", dataCellStyle);
                
                String productName = order.getSemiProductName() != null ? order.getSemiProductName() : 
                                    (order.getFinalProductName() != null ? order.getFinalProductName() : "");
                createCell(row, 6, productName, dataCellStyle);
                
                createCell(row, 7, order.getWeekNumber() != null ? order.getWeekNumber().toString() : "-", dataCellStyle);
                createCell(row, 8, order.getParcelLot() != null ? order.getParcelLot() : "", dataCellStyle);
                createCell(row, 9, order.getVariety() != null ? order.getVariety() : "", dataCellStyle);
                createCell(row, 10, order.getOrganicCertification() != null ? order.getOrganicCertification() : "", dataCellStyle);
                createCell(row, 11, order.getTotalQuantity() != null ? String.format("%.2f", order.getTotalQuantity()) : "0.00", dataCellStyle);
                createCell(row, 12, order.getFulfilledQuantity() != null ? String.format("%.2f", order.getFulfilledQuantity()) : "0.00", dataCellStyle);
                createCell(row, 13, order.getAvailableQuantity() != null ? String.format("%.2f", order.getAvailableQuantity()) : "0.00", dataCellStyle);
                createCell(row, 14, order.getUnitLabel() != null ? order.getUnitLabel() : "", dataCellStyle);
                createCell(row, 15, order.getDeliveryTime() != null ? order.getDeliveryTime().format(dateFormatter) : "", dataCellStyle);
                createCell(row, 16, order.getUpdateTimestamp() != null ? timestampFormatter.withZone(ZoneId.systemDefault()).format(order.getUpdateTimestamp()) : "", dataCellStyle);
                createCell(row, 17, formatAvailability(order.getAvailable(), language), dataCellStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            logger.info("Excel file generated successfully with {} data rows", orders.size());
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
    
    /**
     * Creates and styles a cell in the Excel sheet.
     *
     * @param row The row to add the cell to
     * @param column The column index (0-based)
     * @param value The cell value (null-safe)
     * @param style The cell style to apply
     */
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }
    
    /**
     * Returns localized column headers for Excel export.
     *
     * @param language Language for headers (ES/EN)
     * @return Array of column header strings
     */
    private String[] getColumnHeaders(Language language) {
        if (language == Language.ES) {
            return new String[]{
                "Área",
                "Agricultor",
                "Fecha de Producción",
                "Número de Lote Interno",
                "No. de Sacos",
                "Tipo",
                "Semi-producto / Producto Final",
                "Número de Semana",
                "Lote (Parcela)",
                "Variedad",
                "Certificación Orgánica",
                "Cantidad Total",
                "Cantidad Cumplida",
                "Cantidad Disponible",
                "Unidad",
                "Fecha de Entrega",
                "Última Modificación",
                "Estado"
            };
        }
        // Default English
        return new String[]{
            "Area",
            "Farmer",
            "Production Date",
            "Internal Lot Number",
            "No. of Sacs",
            "Type",
            "Semi-product / Final Product",
            "Week Number",
            "Parcel Lot",
            "Variety",
            "Organic Certification",
            "Total Quantity",
            "Fulfilled Quantity",
            "Available Quantity",
            "Unit",
            "Delivery Date",
            "Last Change",
            "Status"
        };
    }
    
    /**
     * Returns localized sheet name for Excel workbook.
     *
     * @param language Language for sheet name
     * @return Localized sheet name
     */
    private String getSheetName(Language language) {
        return language == Language.ES ? "Órdenes de Stock Agrupadas" : "Grouped Stock Orders";
    }
    
    /**
     * Formats order type enum for display.
     *
     * @param orderType The order type enum value
     * @param language Language for formatting
     * @return Formatted order type string
     */
    private String formatOrderType(String orderType, Language language) {
        if (orderType == null) return "";
        if (language == Language.ES) {
            switch (orderType) {
                case "PURCHASE_ORDER": return "Orden de Compra";
                case "PROCESSING_ORDER": return "Orden de Procesamiento";
                case "GENERAL_ORDER": return "Orden General";
                case "TRANSFER_ORDER": return "Orden de Transferencia";
                default: return orderType;
            }
        }
        // English format
        return orderType.replace("_", " ");
    }
    
    /**
     * Formats availability boolean for display.
     *
     * @param available Availability status
     * @param language Language for formatting
     * @return Formatted availability string
     */
    private String formatAvailability(Boolean available, Language language) {
        if (available == null) return "-";
        if (language == Language.ES) {
            return available ? "Disponible" : "No Disponible";
        }
        return available ? "Available" : "Not Available";
    }

}
