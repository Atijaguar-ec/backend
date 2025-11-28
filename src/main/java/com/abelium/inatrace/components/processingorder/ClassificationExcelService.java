package com.abelium.inatrace.components.processingorder;

import com.abelium.inatrace.db.entities.processingorder.ProcessingClassificationBatch;
import com.abelium.inatrace.db.entities.processingorder.ProcessingClassificationBatchDetail;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating Excel reports for shrimp processing:
 * - Liquidación de Pesca (Fishing Settlement) - Classification by size
 * - Liquidación de Compra (Purchase Settlement) - Full monetary settlement
 * 
 * Follows the professional pattern used in PaymentService and DashboardService.
 * 
 * @author INATrace Team
 */
@Service
public class ClassificationExcelService {

    private static final BigDecimal KG_TO_LB_FACTOR = new BigDecimal("2.20462");
    private static final String CURRENCY = "USD";

    /**
     * Generate Excel file for classification batch.
     * 
     * @param batch The classification batch with details
     * @return Excel file as byte array
     * @throws IOException if file generation fails
     */
    public byte[] generateLiquidacionExcel(ProcessingClassificationBatch batch) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Liquidación de Pesca");

            // Create cell styles
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerInfoStyle = createHeaderInfoStyle(workbook);
            CellStyle tableHeaderStyle = createTableHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle totalStyle = createTotalStyle(workbook);

            int rowNum = 0;

            // =====================================================
            // Title Row
            // =====================================================
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("LIQUIDACIÓN DE PESCA");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
            rowNum++; // Empty row

            // =====================================================
            // Header Information
            // =====================================================
            String lotNumber = batch.getTargetStockOrder() != null && batch.getTargetStockOrder().getInternalLotNumber() != null
                    ? batch.getTargetStockOrder().getInternalLotNumber()
                    : "N/A";

            rowNum = createInfoRow(sheet, rowNum, "Lote:", lotNumber, headerInfoStyle, dataStyle);
            rowNum = createInfoRow(sheet, rowNum, "Hora de Inicio:", batch.getStartTime() != null ? batch.getStartTime() : "", headerInfoStyle, dataStyle);
            rowNum = createInfoRow(sheet, rowNum, "Hora Termina:", batch.getEndTime() != null ? batch.getEndTime() : "", headerInfoStyle, dataStyle);
            rowNum = createInfoRow(sheet, rowNum, "Orden de Producción:", batch.getProductionOrder() != null ? batch.getProductionOrder() : "", headerInfoStyle, dataStyle);
            rowNum = createInfoRow(sheet, rowNum, "Tipo de Congelación:", batch.getFreezingType() != null ? batch.getFreezingType() : "", headerInfoStyle, dataStyle);
            rowNum = createInfoRow(sheet, rowNum, "Máquina:", batch.getMachine() != null ? batch.getMachine() : "", headerInfoStyle, dataStyle);
            rowNum = createInfoRow(sheet, rowNum, "Marca:", batch.getBrandHeader() != null ? batch.getBrandHeader() : "", headerInfoStyle, dataStyle);
            rowNum++; // Empty row

            // =====================================================
            // Table Header
            // =====================================================
            Row tableHeaderRow = sheet.createRow(rowNum++);
            String[] headers = {"Talla", "Aspecto", "Cajas", "Peso/caja", "Formato", "Peso Total", "Libras"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = tableHeaderRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(tableHeaderStyle);
            }

            // =====================================================
            // Data Rows
            // =====================================================
            List<ProcessingClassificationBatchDetail> details = batch.getDetails().stream()
                    .collect(Collectors.toList());

            int grandTotalBoxes = 0;
            BigDecimal grandTotalWeight = BigDecimal.ZERO;
            BigDecimal grandTotalPounds = BigDecimal.ZERO;

            for (ProcessingClassificationBatchDetail detail : details) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;

                // Size (Talla)
                Cell sizeCell = dataRow.createCell(col++);
                sizeCell.setCellValue(detail.getSize() != null ? detail.getSize() : "");
                sizeCell.setCellStyle(dataStyle);

                // Presentation Type (Aspecto: SHELLON-A, SHELLON-B, BROKEN, etc.)
                Cell presentationCell = dataRow.createCell(col++);
                presentationCell.setCellValue(getPresentationTypeLabel(detail.getPresentationType()));
                presentationCell.setCellStyle(dataStyle);

                // Boxes (Cajas)
                Cell boxesCell = dataRow.createCell(col++);
                int boxes = detail.getBoxes() != null ? detail.getBoxes() : 0;
                boxesCell.setCellValue(boxes);
                boxesCell.setCellStyle(numberStyle);
                grandTotalBoxes += boxes;

                // Weight per box (Peso/caja)
                Cell weightCell = dataRow.createCell(col++);
                if (detail.getWeightPerBox() != null) {
                    weightCell.setCellValue(detail.getWeightPerBox().doubleValue());
                }
                weightCell.setCellStyle(numberStyle);

                // Format (Formato)
                Cell formatCell = dataRow.createCell(col++);
                formatCell.setCellValue(detail.getWeightFormat() != null ? detail.getWeightFormat() : "LB");
                formatCell.setCellStyle(dataStyle);

                // Total weight (Peso Total)
                BigDecimal totalWeight = detail.getTotalWeight();
                Cell totalCell = dataRow.createCell(col++);
                totalCell.setCellValue(totalWeight.doubleValue());
                totalCell.setCellStyle(numberStyle);
                grandTotalWeight = grandTotalWeight.add(totalWeight);

                // Pounds per size (Libras)
                BigDecimal poundsPerSize = detail.getPoundsPerSize();
                Cell poundsCell = dataRow.createCell(col++);
                poundsCell.setCellValue(poundsPerSize.doubleValue());
                poundsCell.setCellStyle(numberStyle);
                grandTotalPounds = grandTotalPounds.add(poundsPerSize);
            }

            // =====================================================
            // Grand Total Row
            // =====================================================
            Row totalRow = sheet.createRow(rowNum++);
            
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("GRAN TOTAL");
            totalLabelCell.setCellStyle(totalStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

            Cell totalBoxesCell = totalRow.createCell(2);
            totalBoxesCell.setCellValue(grandTotalBoxes);
            totalBoxesCell.setCellStyle(totalStyle);

            // Empty cells
            for (int i = 3; i <= 4; i++) {
                Cell emptyCell = totalRow.createCell(i);
                emptyCell.setCellStyle(totalStyle);
            }

            Cell totalWeightCell = totalRow.createCell(5);
            totalWeightCell.setCellValue(grandTotalWeight.doubleValue());
            totalWeightCell.setCellStyle(totalStyle);

            Cell totalPoundsCell = totalRow.createCell(6);
            totalPoundsCell.setCellValue(grandTotalPounds.doubleValue());
            totalPoundsCell.setCellStyle(totalStyle);

            // =====================================================
            // Auto-size columns
            // =====================================================
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // Add some padding
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024);
            }

            workbook.write(byteArrayOutputStream);
        }

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Generate Excel file for Purchase Settlement (Liquidación de Compra).
     * Includes pricing information and monetary totals.
     * 
     * @param batch The classification batch with details including prices
     * @return Excel file as byte array
     * @throws IOException if file generation fails
     */
    public byte[] generateLiquidacionCompraExcel(ProcessingClassificationBatch batch) throws IOException {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Liquidación de Compra");

            // Create cell styles
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle headerInfoStyle = createHeaderInfoStyle(workbook);
            CellStyle tableHeaderStyle = createTableHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle totalStyle = createTotalStyle(workbook);

            int rowNum = 0;

            // =====================================================
            // Title Row
            // =====================================================
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("LIQUIDACIÓN DE COMPRA");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));
            rowNum++; // Empty row

            // =====================================================
            // Header Information
            // =====================================================
            String lotNumber = batch.getTargetStockOrder() != null && batch.getTargetStockOrder().getInternalLotNumber() != null
                    ? batch.getTargetStockOrder().getInternalLotNumber()
                    : "N/A";
            
            String weekNumber = batch.getTargetStockOrder() != null && batch.getTargetStockOrder().getWeekNumber() != null
                ? String.valueOf(batch.getTargetStockOrder().getWeekNumber())
                : "N/A";

            String settlementNumber = batch.getSettlementNumber() != null ? batch.getSettlementNumber() : "N/A";
            String processType = batch.getProcessType() != null ? batch.getProcessType() : "N/A";

            rowNum = createInfoRowExtended(sheet, rowNum, "N° Liquidación:", settlementNumber, headerInfoStyle, dataStyle);
            rowNum = createInfoRowExtended(sheet, rowNum, "Lote:", lotNumber, headerInfoStyle, dataStyle);
            rowNum = createInfoRowExtended(sheet, rowNum, "N° de Semana:", weekNumber, headerInfoStyle, dataStyle);
            rowNum = createInfoRowExtended(sheet, rowNum, "Tipo de Proceso:", getProcessTypeLabel(processType), headerInfoStyle, dataStyle);
            rowNum = createInfoRowExtended(sheet, rowNum, "Hora de Inicio:", batch.getStartTime() != null ? batch.getStartTime() : "", headerInfoStyle, dataStyle);
            rowNum = createInfoRowExtended(sheet, rowNum, "Hora Termina:", batch.getEndTime() != null ? batch.getEndTime() : "", headerInfoStyle, dataStyle);
            
            // Weight summary
            if (batch.getPoundsReceived() != null) {
                rowNum = createInfoRowExtended(sheet, rowNum, "Libras Recibidas:", String.format("%.2f", batch.getPoundsReceived()), headerInfoStyle, dataStyle);
            }
            if (batch.getPoundsWaste() != null) {
                rowNum = createInfoRowExtended(sheet, rowNum, "Libras Basura:", String.format("%.2f", batch.getPoundsWaste()), headerInfoStyle, dataStyle);
            }
            if (batch.getPoundsNetReceived() != null) {
                rowNum = createInfoRowExtended(sheet, rowNum, "Libras Netas:", String.format("%.2f", batch.getPoundsNetReceived()), headerInfoStyle, dataStyle);
            }
            if (batch.getYieldPercentage() != null) {
                rowNum = createInfoRowExtended(sheet, rowNum, "Rendimiento:", String.format("%.2f%%", batch.getYieldPercentage()), headerInfoStyle, dataStyle);
            }
            rowNum++; // Empty row

            // =====================================================
            // Table Header
            // =====================================================
            Row tableHeaderRow = sheet.createRow(rowNum++);
            String[] headers = {"Talla", "Aspecto", "Cajas", "Peso/Caja", "Libras", "Precio/Lb", "Total " + CURRENCY, "%"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = tableHeaderRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(tableHeaderStyle);
            }

            // =====================================================
            // Data Rows
            // =====================================================
            List<ProcessingClassificationBatchDetail> details = batch.getDetails().stream()
                    .collect(Collectors.toList());

            int grandTotalBoxes = 0;
            BigDecimal grandTotalPounds = BigDecimal.ZERO;
            BigDecimal grandTotalAmount = BigDecimal.ZERO;

            for (ProcessingClassificationBatchDetail detail : details) {
                Row dataRow = sheet.createRow(rowNum++);
                int col = 0;

                // Size (Talla)
                Cell sizeCell = dataRow.createCell(col++);
                sizeCell.setCellValue(detail.getSize() != null ? detail.getSize() : "");
                sizeCell.setCellStyle(dataStyle);

                // Presentation Type (Aspecto)
                Cell presentationCell = dataRow.createCell(col++);
                presentationCell.setCellValue(getPresentationTypeLabel(detail.getPresentationType()));
                presentationCell.setCellStyle(dataStyle);

                // Boxes
                Cell boxesCell = dataRow.createCell(col++);
                int boxes = detail.getBoxes() != null ? detail.getBoxes() : 0;
                boxesCell.setCellValue(boxes);
                boxesCell.setCellStyle(numberStyle);
                grandTotalBoxes += boxes;

                // Weight per box
                Cell weightCell = dataRow.createCell(col++);
                if (detail.getWeightPerBox() != null) {
                    weightCell.setCellValue(detail.getWeightPerBox().doubleValue());
                }
                weightCell.setCellStyle(numberStyle);

                // Pounds
                BigDecimal pounds = detail.getPoundsPerSize();
                Cell poundsCell = dataRow.createCell(col++);
                poundsCell.setCellValue(pounds.doubleValue());
                poundsCell.setCellStyle(numberStyle);
                grandTotalPounds = grandTotalPounds.add(pounds);

                // Price per pound
                Cell priceCell = dataRow.createCell(col++);
                if (detail.getPricePerPound() != null) {
                    priceCell.setCellValue(detail.getPricePerPound().doubleValue());
                }
                priceCell.setCellStyle(currencyStyle);

                // Line total
                BigDecimal lineTotal = detail.getLineTotal() != null ? detail.getLineTotal() : detail.getCalculatedLineTotal();
                Cell lineTotalCell = dataRow.createCell(col++);
                lineTotalCell.setCellValue(lineTotal.doubleValue());
                lineTotalCell.setCellStyle(currencyStyle);
                grandTotalAmount = grandTotalAmount.add(lineTotal);

                // Percentage (calculated later)
                Cell percentCell = dataRow.createCell(col);
                percentCell.setCellStyle(numberStyle);
            }

            // Calculate percentages
            if (grandTotalAmount.compareTo(BigDecimal.ZERO) > 0) {
                int dataStartRow = rowNum - details.size();
                for (int i = 0; i < details.size(); i++) {
                    Row dataRow = sheet.getRow(dataStartRow + i);
                    Cell lineTotalCell = dataRow.getCell(6);
                    double lineTotal = lineTotalCell.getNumericCellValue();
                    double percentage = (lineTotal / grandTotalAmount.doubleValue()) * 100;
                    Cell percentCell = dataRow.getCell(7);
                    percentCell.setCellValue(percentage);
                }
            }

            rowNum++; // Empty row

            // =====================================================
            // Grand Total Row
            // =====================================================
            Row totalRow = sheet.createRow(rowNum++);
            
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("TOTAL A PAGAR");
            totalLabelCell.setCellStyle(totalStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

            Cell totalBoxesCell = totalRow.createCell(2);
            totalBoxesCell.setCellValue(grandTotalBoxes);
            totalBoxesCell.setCellStyle(totalStyle);

            // Empty cell for Peso/Caja
            Cell emptyCell = totalRow.createCell(3);
            emptyCell.setCellStyle(totalStyle);

            Cell totalPoundsCell = totalRow.createCell(4);
            totalPoundsCell.setCellValue(grandTotalPounds.doubleValue());
            totalPoundsCell.setCellStyle(totalStyle);

            // Average price
            Cell avgPriceCell = totalRow.createCell(5);
            if (grandTotalPounds.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal avgPrice = grandTotalAmount.divide(grandTotalPounds, 4, BigDecimal.ROUND_HALF_UP);
                avgPriceCell.setCellValue(avgPrice.doubleValue());
            }
            avgPriceCell.setCellStyle(totalStyle);

            Cell grandTotalCell = totalRow.createCell(6);
            grandTotalCell.setCellValue(grandTotalAmount.doubleValue());
            grandTotalCell.setCellStyle(totalStyle);

            Cell totalPercentCell = totalRow.createCell(7);
            totalPercentCell.setCellValue(100.0);
            totalPercentCell.setCellStyle(totalStyle);

            // =====================================================
            // Auto-size columns
            // =====================================================
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1024);
            }

            workbook.write(byteArrayOutputStream);
        }

        return byteArrayOutputStream.toByteArray();
    }

    private String getProcessTypeLabel(String processType) {
        if (processType == null) return "";
        switch (processType) {
            case "HEAD_ON": return "Con Cabeza";
            case "SHELL_ON": return "En Cola";
            case "VALUE_ADDED": return "Valor Agregado";
            default: return processType;
        }
    }

    /**
     * Convert presentation type code to readable label
     */
    private String getPresentationTypeLabel(String presentationType) {
        if (presentationType == null) return "";
        switch (presentationType) {
            case "SHELL_ON_A": return "Shell-On A";
            case "SHELL_ON_B": return "Shell-On B";
            case "BROKEN_VS": return "Broken VS";
            case "BROKEN_SMALL": return "Broken Small";
            case "BROKEN_MEDIUM": return "Broken Medium";
            case "BROKEN_LARGE": return "Broken Large";
            case "TITI": return "TITI";
            case "ROJO": return "ROJO";
            case "BVS": return "BVS";
            default: return presentationType;
        }
    }

    // =====================================================
    // Helper Methods for Row Creation
    // =====================================================

    private int createInfoRow(Sheet sheet, int rowNum, String label, String value, 
                              CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);
        
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));

        Cell valueCell = row.createCell(2);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 2, 6));

        return rowNum + 1;
    }

    private int createInfoRowExtended(Sheet sheet, int rowNum, String label, String value, 
                              CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);
        
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 2));

        Cell valueCell = row.createCell(3);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(valueStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 3, 7));

        return rowNum + 1;
    }

    // =====================================================
    // Cell Style Creation Methods
    // =====================================================

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(style);
        return style;
    }

    private CellStyle createHeaderInfoStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        setBorders(style);
        return style;
    }

    private CellStyle createTableHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(style);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        setBorders(style);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        setBorders(style);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("$#,##0.00"));
        setBorders(style);
        return style;
    }

    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        setBorders(style);
        return style;
    }

    private void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
