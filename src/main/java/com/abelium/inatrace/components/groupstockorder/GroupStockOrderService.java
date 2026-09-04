package com.abelium.inatrace.components.groupstockorder;

import com.abelium.inatrace.api.ApiPaginatedList;
import com.abelium.inatrace.api.ApiPaginatedRequest;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.components.groupstockorder.api.ApiGroupStockOrder;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.facility.Facility;
import com.abelium.inatrace.tools.PaginationTools;
import com.abelium.inatrace.tools.WeekNumberTools;
import com.abelium.inatrace.types.Language;
import jakarta.persistence.TypedQuery;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Lazy

@Service
public class GroupStockOrderService extends BaseService {

    /**
     * Columnas por las que el cliente puede ordenar, mapeadas a su expresión JPQL calificada.
     *
     * PostgreSQL rechaza con SQLState 42803 cualquier ORDER BY sobre una columna que no esté
     * en el GROUP BY (MySQL lo toleraba). Cada valor de este mapa DEBE aparecer literalmente
     * en el GROUP BY de {@code getGroupedStockOrderList}, o ser una función de agregación.
     *
     * Ojo con 'updateTimestamp': sin calificar, JPQL lo resuelve contra la entidad raíz
     * (StockOrder hereda updateTimestamp de TimestampEntity) y no contra PO, que es el que
     * está agrupado. Ese era el origen del error en la pantalla de Entregas.
     */
    private static final Map<String, String> SORTABLE_COLUMNS = Map.ofEntries(
            Map.entry("date", "SO.productionDate"),
            Map.entry("productionDate", "SO.productionDate"),
            Map.entry("identifier", "SO.internalLotNumber"),
            Map.entry("orderType", "SO.orderType"),
            Map.entry("semiProduct", "SPT.name"),
            Map.entry("unit", "MUT.label"),
            Map.entry("deliveryTime", "SO.deliveryTime"),
            Map.entry("updateTimestamp", "PO.updateTimestamp"),
            Map.entry("isAvailable", "SO.isAvailable"),
            Map.entry("sacNumber", "COUNT(SO.sacNumber)"),
            Map.entry("quantity", "SUM(SO.totalQuantity)")
    );

    /** Usado cuando sortBy viene vacío o con un valor no reconocido (el default global es "id",
     *  que tampoco está agrupado y por eso rompía). */
    private static final String DEFAULT_SORT_COLUMN = "SO.productionDate";

    public ApiPaginatedList<ApiGroupStockOrder> getGroupedStockOrderList(
            ApiPaginatedRequest request,
            GroupStockOrderQueryRequest queryRequest,
            Language language
    ) {

        // SELECT query string for which columns to get from database and put them into DTO ApiGroupStockOrder
        StringBuilder queryString = new StringBuilder(
            "SELECT new com.abelium.inatrace.components.groupstockorder.api.ApiGroupStockOrder(" +
            "STRING_AGG(cast(SO.id as string), ','), " +
            "SO.productionDate AS date, SO.internalLotNumber AS id, COUNT(SO.sacNumber) as noOfSacs, " +
            "SO.orderType, SPT.name, CONCAT(FP.name, ' (', P.name, ')'), " +
            "SUM(SO.totalQuantity), SUM(SO.fulfilledQuantity), SUM(SO.availableQuantity), " +
            "MUT.label, SO.deliveryTime AS deliveryTime, PO.updateTimestamp AS updateTimestamp, " +
            "SO.isAvailable, " +
            "SO.weekNumber, SO.parcelLot, SO.variety, SO.organicCertification, " +
            "F.name, CONCAT(UC.name, ' ', UC.surname) " +
            ") FROM StockOrder SO " +
            "LEFT JOIN SO.processingOrder PO " +
            "LEFT JOIN SO.measurementUnitType MUT " +
            "LEFT JOIN SO.semiProduct SP " +
            "LEFT JOIN SO.finalProduct FP " +
            "LEFT JOIN FP.product P " +
            "LEFT JOIN SP.semiProductTranslations SPT " +
            "LEFT JOIN SO.facility F " +
            "LEFT JOIN SO.producerUserCustomer UC "
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
                " GROUP BY SO.productionDate, SO.internalLotNumber, SO.orderType, SPT.name, MUT.label, " +
                "SO.deliveryTime, PO.updateTimestamp, SO.isAvailable, FP.name, P.name, " +
                "SO.weekNumber, SO.parcelLot, SO.variety, SO.organicCertification, F.name, " +
                "UC.name, UC.surname "
        );

        // Add ORDER BY query string to sort on requested field and direction.
        // Se resuelve contra SORTABLE_COLUMNS en vez de concatenar request.sortBy directamente:
        // garantiza que la columna esté agrupada (PostgreSQL 42803) y evita inyección de JPQL.
        queryString.append("ORDER BY ");
        queryString.append(SORTABLE_COLUMNS.getOrDefault(request.sortBy, DEFAULT_SORT_COLUMN));
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

    private static final Logger logger = LoggerFactory.getLogger(GroupStockOrderService.class);
    private static final int EXCEL_HEADER_FONT_SIZE = 12;
    private static final int MONTHS_LOOKBACK = 12;

    public ByteArrayInputStream exportGroupedStockOrdersToExcel(Long facilityId, Language language) throws IOException {
        logger.info("Exporting grouped stock orders to Excel for facility: {}, language: {}", facilityId, language);
        List<ApiGroupStockOrder> orders = fetchGroupedStockOrders(language, facilityId, null);
        logger.info("Found {} grouped orders for facility {}", orders.size(), facilityId);
        return generateExcelFile(orders, language, weekColorCodesEnabledFor(facilityId, null));
    }

    public ByteArrayInputStream exportGroupedStockOrdersToExcelByCompany(Long companyId, Language language) throws IOException {
        logger.info("Exporting grouped stock orders to Excel for company: {}, language: {}", companyId, language);
        List<ApiGroupStockOrder> orders = fetchGroupedStockOrders(language, null, companyId);
        logger.info("Found {} grouped orders for company {}", orders.size(), companyId);
        return generateExcelFile(orders, language, weekColorCodesEnabledFor(null, companyId));
    }

    private List<ApiGroupStockOrder> fetchGroupedStockOrders(Language language, Long facilityId, Long companyId) {
        LocalDate oneYearAgo = LocalDate.now().minusMonths(MONTHS_LOOKBACK);
        logger.debug("Fetching grouped orders from {} onwards", oneYearAgo);

        StringBuilder queryString = new StringBuilder(
            "SELECT new com.abelium.inatrace.components.groupstockorder.api.ApiGroupStockOrder(" +
            "STRING_AGG(cast(SO.id as string), ','), " +
            "SO.productionDate AS date, SO.internalLotNumber AS id, COUNT(SO.sacNumber) as noOfSacs, " +
            "SO.orderType, SPT.name, CONCAT(FP.name, ' (', P.name, ')'), " +
            "SUM(SO.totalQuantity), SUM(SO.fulfilledQuantity), SUM(SO.availableQuantity), " +
            "MUT.label, SO.deliveryTime AS deliveryTime, PO.updateTimestamp AS updateTimestamp, " +
            "SO.isAvailable, " +
            "SO.weekNumber, SO.parcelLot, SO.variety, SO.organicCertification, " +
            "F.name, CONCAT(UC.name, ' ', UC.surname) " +
            ") FROM StockOrder SO " +
            "LEFT JOIN SO.processingOrder PO " +
            "LEFT JOIN SO.measurementUnitType MUT " +
            "LEFT JOIN SO.semiProduct SP " +
            "LEFT JOIN SO.finalProduct FP " +
            "LEFT JOIN FP.product P " +
            "LEFT JOIN SP.semiProductTranslations SPT " +
            "LEFT JOIN SO.facility F " +
            "LEFT JOIN SO.producerUserCustomer UC "
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
                " GROUP BY SO.productionDate, SO.internalLotNumber, SO.orderType, SPT.name, MUT.label, " +
                "SO.deliveryTime, PO.updateTimestamp, SO.isAvailable, FP.name, P.name, " +
                "SO.weekNumber, SO.parcelLot, SO.variety, SO.organicCertification, F.name, " +
                "UC.name, UC.surname " +
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
     * La empresa que marca sus entregas con el color de la semana (Fortaleza) lo lleva
     * tambien al Excel: es lo que se usa para cuadrar los sacos con el hilo de bodega.
     */
    private boolean weekColorCodesEnabledFor(Long facilityId, Long companyId) {

        Company company = null;
        if (companyId != null) {
            company = em.find(Company.class, companyId);
        } else if (facilityId != null) {
            Facility facility = em.find(Facility.class, facilityId);
            company = facility != null ? facility.getCompany() : null;
        }

        return company != null && WeekNumberTools.weekColorCodesEnabled(company.getConfiguration());
    }

    private ByteArrayInputStream generateExcelFile(List<ApiGroupStockOrder> orders, Language language,
                                                  boolean withWeekColor) throws IOException {
        logger.debug("Generating Excel file with {} orders in language {}", orders.size(), language);
        // La columna de color va al final para no correr las que ya consume quien usa
        // este archivo, y solo aparece si la empresa trabaja con colores de semana.
        String[] columns = getColumnHeaders(language, withWeekColor);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(getSheetName(language));
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
            
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            
            CellStyle dataCellStyle = workbook.createCellStyle();
            dataCellStyle.setBorderBottom(BorderStyle.THIN);
            dataCellStyle.setBorderTop(BorderStyle.THIN);
            dataCellStyle.setBorderRight(BorderStyle.THIN);
            dataCellStyle.setBorderLeft(BorderStyle.THIN);
            
            // Un estilo por color, fuera del bucle: crear uno por celda infla el archivo
            // y Excel tiene un tope de estilos por libro.
            Map<String, CellStyle> weekColorStyles = withWeekColor
                    ? createWeekColorStyles(workbook)
                    : Map.of();

            int rowIdx = 1;
            for (ApiGroupStockOrder order : orders) {
                Row row = sheet.createRow(rowIdx++);
                
                createCell(row, 0, order.getFacilityName() != null ? order.getFacilityName() : "", dataCellStyle);
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

                if (withWeekColor) {
                    String color = WeekNumberTools.weekColorName(order.getWeekNumber());
                    createCell(row, 18, color != null ? color : "",
                            weekColorStyles.getOrDefault(color, dataCellStyle));
                }
            }
            
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            logger.info("Excel file generated successfully with {} data rows", orders.size());
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
    
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }
    
    private Map<String, CellStyle> createWeekColorStyles(Workbook workbook) {

        Map<String, Short> colorIndexes = Map.of(
                "ROJO", IndexedColors.RED.getIndex(),
                "AZUL", IndexedColors.BLUE.getIndex(),
                "BLANCO", IndexedColors.WHITE.getIndex(),
                "VERDE", IndexedColors.BRIGHT_GREEN.getIndex(),
                "AMARILLO", IndexedColors.YELLOW.getIndex());

        Map<String, CellStyle> styles = new HashMap<>();
        colorIndexes.forEach((color, index) -> {
            CellStyle style = workbook.createCellStyle();
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setFillForegroundColor(index);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            styles.put(color, style);
        });

        return styles;
    }

    private String[] getColumnHeaders(Language language, boolean withWeekColor) {

        String[] headers = baseColumnHeaders(language);
        if (!withWeekColor) {
            return headers;
        }

        String[] withColor = Arrays.copyOf(headers, headers.length + 1);
        withColor[headers.length] = language == Language.ES ? "Color de Semana" : "Week Color";
        return withColor;
    }

    private String[] baseColumnHeaders(Language language) {
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
    
    private String getSheetName(Language language) {
        return language == Language.ES ? "Órdenes de Stock Agrupadas" : "Grouped Stock Orders";
    }
    
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
