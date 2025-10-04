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

@Lazy
@Service
public class GroupStockOrderService extends BaseService {

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
            "SO.orderType, SPT.name, CONCAT(FP.name, ' (', P.name, ')'), SO.weekNumber, " +
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
                " GROUP BY SO.productionDate, SO.internalLotNumber, SO.orderType, SPT.name, SO.weekNumber, MUT.label, " +
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

    public ByteArrayInputStream exportGroupedStockOrdersToExcel(Long facilityId, Language language) throws IOException {
        
        // Calculate date one year ago
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        
        // Build query to get all records from last year
        StringBuilder queryString = new StringBuilder(
            "SELECT new com.abelium.inatrace.components.groupstockorder.api.ApiGroupStockOrder(" +
            "GROUP_CONCAT(SO.id), " +
            "SO.productionDate AS date, SO.internalLotNumber AS id, COUNT(SO.sacNumber) as noOfSacs, " +
            "SO.orderType, SPT.name, CONCAT(FP.name, ' (', P.name, ')'), SO.weekNumber, " +
            "SUM(SO.totalQuantity), SUM(SO.fulfilledQuantity), SUM(SO.availableQuantity), " +
            "MUT.label, SO.deliveryTime AS deliveryTime, PO.updateTimestamp AS updateTimestamp, " +
            "SO.isAvailable " +
            ") FROM StockOrder SO " +
            "LEFT JOIN SO.processingOrder PO " +
            "LEFT JOIN SO.measurementUnitType MUT " +
            "LEFT JOIN SO.semiProduct SP " +
            "LEFT JOIN SO.finalProduct FP " +
            "LEFT JOIN FP.product P " +
            "LEFT JOIN SP.semiProductTranslations SPT " +
            "WHERE (SPT.language IS NULL OR SPT.language = :language) " +
            "AND SO.facility.id = :facilityId " +
            "AND SO.productionDate >= :oneYearAgo " +
            "GROUP BY SO.productionDate, SO.internalLotNumber, SO.orderType, SPT.name, SO.weekNumber, MUT.label, " +
            "SO.deliveryTime, PO.updateTimestamp, SO.isAvailable, FP.name, P.name " +
            "ORDER BY SO.productionDate DESC"
        );
        
        TypedQuery<ApiGroupStockOrder> query = em.createQuery(queryString.toString(), ApiGroupStockOrder.class);
        query.setParameter("language", language);
        query.setParameter("facilityId", facilityId);
        query.setParameter("oneYearAgo", oneYearAgo);
        
        List<ApiGroupStockOrder> orders = query.getResultList();
        
        return generateExcelFile(orders, language);
    }
    
    private ByteArrayInputStream generateExcelFile(List<ApiGroupStockOrder> orders, Language language) throws IOException {
        
        String[] columns = getColumnHeaders(language);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(getSheetName(language));
            
            // Create header row style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
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
                
                createCell(row, 0, order.getProductionDate() != null ? order.getProductionDate().format(dateFormatter) : "", dataCellStyle);
                createCell(row, 1, order.getInternalLotNumber() != null ? order.getInternalLotNumber() : "", dataCellStyle);
                createCell(row, 2, order.getNoOfSacs() != null && order.getNoOfSacs() > 0 ? order.getNoOfSacs().toString() : "0", dataCellStyle);
                createCell(row, 3, order.getOrderType() != null ? formatOrderType(order.getOrderType().toString(), language) : "", dataCellStyle);
                
                String productName = order.getSemiProductName() != null ? order.getSemiProductName() : 
                                    (order.getFinalProductName() != null ? order.getFinalProductName() : "");
                createCell(row, 4, productName, dataCellStyle);
                
                createCell(row, 5, order.getWeekNumber() != null ? order.getWeekNumber().toString() : "-", dataCellStyle);
                createCell(row, 6, order.getTotalQuantity() != null ? String.format("%.2f", order.getTotalQuantity()) : "0.00", dataCellStyle);
                createCell(row, 7, order.getFulfilledQuantity() != null ? String.format("%.2f", order.getFulfilledQuantity()) : "0.00", dataCellStyle);
                createCell(row, 8, order.getAvailableQuantity() != null ? String.format("%.2f", order.getAvailableQuantity()) : "0.00", dataCellStyle);
                createCell(row, 9, order.getUnitLabel() != null ? order.getUnitLabel() : "", dataCellStyle);
                createCell(row, 10, order.getDeliveryTime() != null ? order.getDeliveryTime().format(dateFormatter) : "", dataCellStyle);
                createCell(row, 11, order.getUpdateTimestamp() != null ? timestampFormatter.withZone(ZoneId.systemDefault()).format(order.getUpdateTimestamp()) : "", dataCellStyle);
                createCell(row, 12, formatAvailability(order.getAvailable(), language), dataCellStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
    
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
    
    private String[] getColumnHeaders(Language language) {
        if (language == Language.ES) {
            return new String[]{
                "Fecha de Producción",
                "Número de Lote Interno",
                "No. de Sacos",
                "Tipo",
                "Semi-producto / Producto Final",
                "Número de Semana",
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
            "Production Date",
            "Internal Lot Number",
            "No. of Sacs",
            "Type",
            "Semi-product / Final Product",
            "Week Number",
            "Total Quantity",
            "Fulfilled Quantity",
            "Available Quantity",
            "Unit",
            "Delivery Date",
            "Last Change",
            "Status"
        };
    }
    
    private String getSheetName(Language language) {
        return language == Language.ES ? "Órdenes de Stock Agrupadas" : "Grouped Stock Orders";
    }
    
    private String formatOrderType(String orderType, Language language) {
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
    
    private String formatAvailability(Boolean available, Language language) {
        if (available == null) return "-";
        if (language == Language.ES) {
            return available ? "Disponible" : "No Disponible";
        }
        return available ? "Available" : "Not Available";
    }

}
