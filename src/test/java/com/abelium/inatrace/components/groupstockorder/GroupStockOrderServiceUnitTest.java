package com.abelium.inatrace.components.groupstockorder;

import com.abelium.inatrace.components.groupstockorder.api.ApiGroupStockOrder;
import com.abelium.inatrace.db.entities.company.Company;
import com.abelium.inatrace.db.entities.facility.Facility;
import com.abelium.inatrace.db.entities.stockorder.enums.OrderType;
import com.abelium.inatrace.types.Language;
import jakarta.persistence.EntityManager;
import org.apache.poi.ss.usermodel.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupStockOrderServiceUnitTest {

    @Mock
    private EntityManager entityManager;

    private GroupStockOrderService service;
    private Method weekColorCodesEnabledForMethod;
    private Method formatOrderTypeMethod;
    private Method formatAvailabilityMethod;

    @BeforeEach
    void setUp() throws Exception {
        service = new GroupStockOrderService();
        ReflectionTestUtils.setField(service, "em", entityManager);

        weekColorCodesEnabledForMethod = GroupStockOrderService.class.getDeclaredMethod("weekColorCodesEnabledFor", Long.class, Long.class);
        weekColorCodesEnabledForMethod.setAccessible(true);

        formatOrderTypeMethod = GroupStockOrderService.class.getDeclaredMethod("formatOrderType", String.class, Language.class);
        formatOrderTypeMethod.setAccessible(true);

        formatAvailabilityMethod = GroupStockOrderService.class.getDeclaredMethod("formatAvailability", Boolean.class, Language.class);
        formatAvailabilityMethod.setAccessible(true);
    }

    // -------------------------------------------------------------------------
    // HU-16: Sortable Columns Map Integrity & Default Fallback
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("SORTABLE_COLUMNS contains all required columns mapped to valid JPQL expressions")
    @SuppressWarnings("unchecked")
    void sortableColumns_containsAllExpectedMappers() {
        Map<String, String> sortableColumns = (Map<String, String>) ReflectionTestUtils.getField(GroupStockOrderService.class, "SORTABLE_COLUMNS");
        assertNotNull(sortableColumns);

        assertEquals("SO.productionDate", sortableColumns.get("date"));
        assertEquals("SO.productionDate", sortableColumns.get("productionDate"));
        assertEquals("SO.internalLotNumber", sortableColumns.get("identifier"));
        assertEquals("SO.orderType", sortableColumns.get("orderType"));
        assertEquals("SPT.name", sortableColumns.get("semiProduct"));
        assertEquals("MUT.label", sortableColumns.get("unit"));
        assertEquals("SO.deliveryTime", sortableColumns.get("deliveryTime"));
        assertEquals("PO.updateTimestamp", sortableColumns.get("updateTimestamp"));
        assertEquals("SO.isAvailable", sortableColumns.get("isAvailable"));
        assertEquals("COUNT(SO.sacNumber)", sortableColumns.get("sacNumber"));
        assertEquals("SUM(SO.totalQuantity)", sortableColumns.get("quantity"));

        String defaultSort = (String) ReflectionTestUtils.getField(GroupStockOrderService.class, "DEFAULT_SORT_COLUMN");
        assertEquals("SO.productionDate", defaultSort);
    }

    // -------------------------------------------------------------------------
    // HU-16: Streaming Excel Generation for Grouped Deliveries
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("generateExcelFile creates valid SXSSF workbook with week color codes when enabled")
    void generateExcelFile_withWeekColor_createsCompleteWorkbook() throws Exception {
        ApiGroupStockOrder order = new ApiGroupStockOrder(
                "1,2,3",
                LocalDate.of(2026, 9, 24),
                "LOT-EC-2026-001",
                12L,
                OrderType.PURCHASE_ORDER,
                "Cacao Nacional en Baba",
                "Cacao Fino (Cacao)",
                new BigDecimal("1200.00"),
                new BigDecimal("1200.00"),
                new BigDecimal("1200.00"),
                "kg",
                LocalDate.of(2026, 9, 24),
                null,
                true,
                38,
                "PARCELA-05",
                "NACIONAL",
                "ORGANICO",
                "Centro de Acopio Vinces",
                "Carlos Mendoza"
        );

        List<ApiGroupStockOrder> list = Collections.singletonList(order);

        try (ByteArrayInputStream in = service.generateExcelFile(list, Language.ES, true);
             Workbook workbook = WorkbookFactory.create(in)) {

            assertNotNull(workbook);
            Sheet sheet = workbook.getSheetAt(0);
            assertNotNull(sheet);
            assertEquals("Órdenes de Stock Agrupadas", sheet.getSheetName());

            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);
            // Verify header contains Week Color column when withWeekColor is true
            boolean foundColorHeader = false;
            for (Cell cell : headerRow) {
                if (cell.getStringCellValue().toLowerCase().contains("color")) {
                    foundColorHeader = true;
                    break;
                }
            }
            assertTrue(foundColorHeader, "Header row must contain the week color column");

            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow);
            assertEquals("Centro de Acopio Vinces", dataRow.getCell(0).getStringCellValue());
            assertEquals("Carlos Mendoza", dataRow.getCell(1).getStringCellValue());
            assertEquals("2026-09-24", dataRow.getCell(2).getStringCellValue());
            assertEquals("LOT-EC-2026-001", dataRow.getCell(3).getStringCellValue());
            assertEquals("12", dataRow.getCell(4).getStringCellValue());
        }
    }

    @Test
    @DisplayName("generateExcelFile creates valid SXSSF workbook without week color column when disabled")
    void generateExcelFile_withoutWeekColor_createsWorkbookWithoutColorColumn() throws Exception {
        ApiGroupStockOrder order = new ApiGroupStockOrder(
                "4",
                LocalDate.of(2026, 9, 24),
                "LOT-EC-2026-002",
                5L,
                OrderType.GENERAL_ORDER,
                "Cacao Seco",
                "Cacao (General)",
                new BigDecimal("500.00"),
                new BigDecimal("500.00"),
                new BigDecimal("500.00"),
                "qq",
                null,
                null,
                true,
                38,
                "PARCELA-01",
                "CCN51",
                "CONVENCIONAL",
                "Bodega Central",
                "Juan Perez"
        );

        List<ApiGroupStockOrder> list = Collections.singletonList(order);

        try (ByteArrayInputStream in = service.generateExcelFile(list, Language.EN, false);
             Workbook workbook = WorkbookFactory.create(in)) {

            assertNotNull(workbook);
            Sheet sheet = workbook.getSheetAt(0);
            assertEquals("Grouped Stock Orders", sheet.getSheetName());

            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow);

            // Verify header does NOT contain Week Color column when withWeekColor is false
            for (Cell cell : headerRow) {
                assertFalse(cell.getStringCellValue().contains("Week color")
                        || cell.getStringCellValue().contains("Color de semana"));
            }
        }
    }

    // -------------------------------------------------------------------------
    // HU-16: weekColorCodesEnabledFor Business Logic
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("weekColorCodesEnabledFor checks company configuration JSON properly")
    void weekColorCodesEnabledFor_checksCompanyConfig() throws Exception {
        Company companyWithColors = new Company();
        companyWithColors.setConfiguration(Collections.singletonMap("weekColorCodes", true));
        when(entityManager.find(Company.class, 10L)).thenReturn(companyWithColors);

        boolean enabled = (boolean) weekColorCodesEnabledForMethod.invoke(service, null, 10L);
        assertTrue(enabled);

        Company companyWithoutColors = new Company();
        companyWithoutColors.setConfiguration(Collections.singletonMap("weekColorCodes", false));
        when(entityManager.find(Company.class, 11L)).thenReturn(companyWithoutColors);

        boolean disabled = (boolean) weekColorCodesEnabledForMethod.invoke(service, null, 11L);
        assertFalse(disabled);

        // Facility lookup
        Facility facility = new Facility();
        facility.setCompany(companyWithColors);
        when(entityManager.find(Facility.class, 20L)).thenReturn(facility);

        boolean facilityEnabled = (boolean) weekColorCodesEnabledForMethod.invoke(service, 20L, null);
        assertTrue(facilityEnabled);

        // Null company / null config
        when(entityManager.find(Company.class, 99L)).thenReturn(null);
        boolean notFound = (boolean) weekColorCodesEnabledForMethod.invoke(service, null, 99L);
        assertFalse(notFound);
    }

    // -------------------------------------------------------------------------
    // HU-16: formatOrderType Localization Logic
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("formatOrderType formats order types in Spanish and English")
    void formatOrderType_translations() throws Exception {
        assertEquals("Orden de Compra", formatOrderTypeMethod.invoke(service, "PURCHASE_ORDER", Language.ES));
        assertEquals("PURCHASE ORDER", formatOrderTypeMethod.invoke(service, "PURCHASE_ORDER", Language.EN));

        assertEquals("Orden General", formatOrderTypeMethod.invoke(service, "GENERAL_ORDER", Language.ES));
        assertEquals("GENERAL ORDER", formatOrderTypeMethod.invoke(service, "GENERAL_ORDER", Language.EN));

        assertEquals("Orden de Procesamiento", formatOrderTypeMethod.invoke(service, "PROCESSING_ORDER", Language.ES));
        assertEquals("PROCESSING ORDER", formatOrderTypeMethod.invoke(service, "PROCESSING_ORDER", Language.EN));

        assertEquals("Orden de Transferencia", formatOrderTypeMethod.invoke(service, "TRANSFER_ORDER", Language.ES));
        assertEquals("TRANSFER ORDER", formatOrderTypeMethod.invoke(service, "TRANSFER_ORDER", Language.EN));
    }

    // -------------------------------------------------------------------------
    // HU-16: formatAvailability & Null-Safety in Excel Generation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("formatAvailability translates boolean availability correctly in Spanish and English")
    void formatAvailability_translations() throws Exception {
        assertEquals("Disponible", formatAvailabilityMethod.invoke(service, Boolean.TRUE, Language.ES));
        assertEquals("No Disponible", formatAvailabilityMethod.invoke(service, Boolean.FALSE, Language.ES));
        assertEquals("-", formatAvailabilityMethod.invoke(service, null, Language.ES));

        assertEquals("Available", formatAvailabilityMethod.invoke(service, Boolean.TRUE, Language.EN));
        assertEquals("Not Available", formatAvailabilityMethod.invoke(service, Boolean.FALSE, Language.EN));
        assertEquals("-", formatAvailabilityMethod.invoke(service, null, Language.EN));
    }

    @Test
    @DisplayName("generateExcelFile handles null fields gracefully without NullPointerException")
    void generateExcelFile_withNullFields_handlesGracefully() throws Exception {
        ApiGroupStockOrder emptyOrder = new ApiGroupStockOrder(
                "1", null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null
        );

        List<ApiGroupStockOrder> list = Collections.singletonList(emptyOrder);

        try (ByteArrayInputStream in = service.generateExcelFile(list, Language.ES, false);
             Workbook workbook = WorkbookFactory.create(in)) {

            assertNotNull(workbook);
            Sheet sheet = workbook.getSheetAt(0);
            assertNotNull(sheet);
            assertEquals(2, sheet.getPhysicalNumberOfRows()); // Header + 1 data row
            Row dataRow = sheet.getRow(1);
            assertNotNull(dataRow);
            assertEquals("-", dataRow.getCell(17).getStringCellValue()); // formatAvailability(null)
        }
    }
}
