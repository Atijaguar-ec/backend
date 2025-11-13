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
 * Service for generating Excel "Liquidación de Pesca" reports.
 * Follows the professional pattern used in PaymentService and DashboardService.
 * 
 * @author INATrace Team
 */
@Service
public class ClassificationExcelService {

    private static final BigDecimal KG_TO_LB_FACTOR = new BigDecimal("2.20462");

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
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
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

            rowNum = createInfoRow(sheet, rowNum, "Lote:", lotNumber, headerInfoStyle, dataStyle);
            rowNum = createInfoRow(sheet, rowNum, "N° de Semana:", weekNumber, headerInfoStyle, dataStyle);
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
            String[] headers = {"Marca", "Tallas", "Cajas", "Clasificación U", "Clasificación #", 
                                "Peso/caja", "Formato", "Totales", "Libras por tallas"};
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

                // Brand
                Cell brandCell = dataRow.createCell(0);
                brandCell.setCellValue(detail.getBrandDetail() != null ? detail.getBrandDetail() : "");
                brandCell.setCellStyle(dataStyle);

                // Size
                Cell sizeCell = dataRow.createCell(1);
                sizeCell.setCellValue(detail.getSize() != null ? detail.getSize() : "");
                sizeCell.setCellStyle(dataStyle);

                // Boxes
                Cell boxesCell = dataRow.createCell(2);
                int boxes = detail.getBoxes() != null ? detail.getBoxes() : 0;
                boxesCell.setCellValue(boxes);
                boxesCell.setCellStyle(numberStyle);
                grandTotalBoxes += boxes;

                // Classification U
                Cell classUCell = dataRow.createCell(3);
                if (detail.getClassificationU() != null) {
                    classUCell.setCellValue(detail.getClassificationU().doubleValue());
                }
                classUCell.setCellStyle(numberStyle);

                // Classification #
                Cell classNumCell = dataRow.createCell(4);
                if (detail.getClassificationNumber() != null) {
                    classNumCell.setCellValue(detail.getClassificationNumber().doubleValue());
                }
                classNumCell.setCellStyle(numberStyle);

                // Weight per box
                Cell weightCell = dataRow.createCell(5);
                if (detail.getWeightPerBox() != null) {
                    weightCell.setCellValue(detail.getWeightPerBox().doubleValue());
                }
                weightCell.setCellStyle(numberStyle);

                // Format
                Cell formatCell = dataRow.createCell(6);
                formatCell.setCellValue(detail.getWeightFormat() != null ? detail.getWeightFormat() : "LB");
                formatCell.setCellStyle(dataStyle);

                // Total weight
                BigDecimal totalWeight = detail.getTotalWeight();
                Cell totalCell = dataRow.createCell(7);
                totalCell.setCellValue(totalWeight.doubleValue());
                totalCell.setCellStyle(numberStyle);
                grandTotalWeight = grandTotalWeight.add(totalWeight);

                // Pounds per size
                BigDecimal poundsPerSize = detail.getPoundsPerSize();
                Cell poundsCell = dataRow.createCell(8);
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

            // Empty cells for classifications
            for (int i = 3; i <= 6; i++) {
                Cell emptyCell = totalRow.createCell(i);
                emptyCell.setCellStyle(totalStyle);
            }

            Cell totalWeightCell = totalRow.createCell(7);
            totalWeightCell.setCellValue(grandTotalWeight.doubleValue());
            totalWeightCell.setCellStyle(totalStyle);

            Cell totalPoundsCell = totalRow.createCell(8);
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
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 2, 8));

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
