package com.abelium.inatrace.components.stockorder;

import com.abelium.inatrace.components.stockorder.api.ApiStockOrder;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import com.abelium.inatrace.components.facility.FacilityService;
import com.abelium.inatrace.components.codebook.processingevidencefield.ProcessingEvidenceFieldService;
import com.abelium.inatrace.components.codebook.processing_evidence_type.ProcessingEvidenceTypeService;
import com.abelium.inatrace.components.codebook.semiproduct.SemiProductService;
import com.abelium.inatrace.components.product.FinalProductService;
import com.abelium.inatrace.components.currencies.CurrencyService;
import com.abelium.inatrace.components.company.CompanyQueries;
import org.springframework.context.MessageSource;

class StockOrderServiceTest {

    private StockOrderService stockOrderService;
    private Method calculateNetQuantityMethod;

    @BeforeEach
    void setUp() throws Exception {
        stockOrderService = new StockOrderService(
            mock(FacilityService.class),
            mock(ProcessingEvidenceFieldService.class),
            mock(ProcessingEvidenceTypeService.class),
            mock(SemiProductService.class),
            mock(FinalProductService.class),
            mock(CurrencyService.class),
            mock(CompanyQueries.class),
            mock(MessageSource.class)
        );
        // Use reflection to test the private method calculateNetQuantity
        calculateNetQuantityMethod = StockOrderService.class.getDeclaredMethod("calculateNetQuantity", ApiStockOrder.class, StockOrder.class);
        calculateNetQuantityMethod.setAccessible(true);
    }

    @Test
    void testCalculateNetQuantity_withMoisture() throws Exception {
        ApiStockOrder api = new ApiStockOrder();
        api.setTotalGrossQuantity(new BigDecimal("1000.00"));
        api.setTare(new BigDecimal("50.00"));
        api.setDamagedWeightDeduction(new BigDecimal("20.00"));
        api.setMoisturePercentage(new BigDecimal("10.00"));

        StockOrder entity = new StockOrder();

        calculateNetQuantityMethod.invoke(stockOrderService, api, entity);

        // grossTareDamaged = 1000 - 50 - 20 = 930
        // moistureDeduction = 930 * 10 / 100 = 93
        // expected net = 930 - 93 = 837

        assertEquals(new BigDecimal("93.00"), entity.getMoistureWeightDeduction().setScale(2, RoundingMode.HALF_UP));
        assertEquals(new BigDecimal("837.00"), entity.getNetQuantity().setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void testCalculateNetQuantity_noMoisture() throws Exception {
        ApiStockOrder api = new ApiStockOrder();
        api.setTotalGrossQuantity(new BigDecimal("500.00"));
        api.setTare(new BigDecimal("10.00"));
        api.setDamagedWeightDeduction(new BigDecimal("5.00"));
        api.setMoisturePercentage(null);

        StockOrder entity = new StockOrder();

        calculateNetQuantityMethod.invoke(stockOrderService, api, entity);

        // grossTareDamaged = 500 - 10 - 5 = 485
        // expected net = 485

        assertEquals(BigDecimal.ZERO, entity.getMoistureWeightDeduction());
        assertEquals(new BigDecimal("485.00"), entity.getNetQuantity().setScale(2, RoundingMode.HALF_UP));
    }

    @Test
    void testCalculateNetQuantity_nullsGracefully() throws Exception {
        ApiStockOrder api = new ApiStockOrder();
        // Leave fields null to test edge case

        StockOrder entity = new StockOrder();

        calculateNetQuantityMethod.invoke(stockOrderService, api, entity);

        // expected fallback to ZERO
        assertEquals(BigDecimal.ZERO, entity.getMoistureWeightDeduction());
        assertEquals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), entity.getNetQuantity().setScale(2, RoundingMode.HALF_UP));
    }
}
