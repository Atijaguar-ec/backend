package com.abelium.inatrace.components.currencies;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CurrencyServiceFallbackTest {

    @Autowired
    private CurrencyService currencyService;

    @Test
    void testConvertFromEurGracefulFallbackWhenRateMissing() {
        // Unknown currency with no rates in DB
        BigDecimal input = new BigDecimal("150.00");
        BigDecimal result = currencyService.convertFromEur("NON_EXISTENT_CURRENCY_XYZ", input);
        assertNotNull(result);
        assertEquals(input, result, "Should fall back gracefully to 1:1 conversion without IndexOutOfBoundsException");
    }

    @Test
    void testConvertToEurGracefulFallbackWhenRateMissing() {
        BigDecimal input = new BigDecimal("200.00");
        BigDecimal result = currencyService.convertToEur("NON_EXISTENT_CURRENCY_XYZ", input);
        assertNotNull(result);
        assertEquals(input, result, "Should fall back gracefully to 1:1 conversion without IndexOutOfBoundsException");
    }

    @Test
    void testConvertSameCurrencyIdentity() {
        BigDecimal input = new BigDecimal("55.50");
        BigDecimal result = currencyService.convert("USD", "USD", input);
        assertEquals(input, result);
    }

    @Test
    void testFetchRatesGracefulOnMissingOrInvalidApiKey() {
        // fetchRates should not throw unhandled exception even if API key is dummy/missing
        assertDoesNotThrow(() -> currencyService.fetchRates(new Date()));
    }
}
