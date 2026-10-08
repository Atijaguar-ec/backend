package com.abelium.inatrace.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryReceiptToolsTest {

    @Test
    @DisplayName("deliveryReceiptEnabled: false by default for null or empty config (Fortaleza del Valle behavior)")
    void testDeliveryReceiptDefaultDisabled() {
        assertFalse(DeliveryReceiptTools.deliveryReceiptEnabled(null), "Null config must be disabled");
        assertFalse(DeliveryReceiptTools.deliveryReceiptEnabled(Collections.emptyMap()), "Empty config must be disabled");

        Map<String, Object> fvConfig = new HashMap<>();
        fvConfig.put("onlyOrganicProduction", true);
        assertFalse(DeliveryReceiptTools.deliveryReceiptEnabled(fvConfig), "FV config without flag must be disabled");

        fvConfig.put(DeliveryReceiptTools.DELIVERY_RECEIPT_CONFIG_KEY, false);
        assertFalse(DeliveryReceiptTools.deliveryReceiptEnabled(fvConfig), "Explicitly false must be disabled");

        fvConfig.put(DeliveryReceiptTools.DELIVERY_RECEIPT_CONFIG_KEY, "false");
        assertFalse(DeliveryReceiptTools.deliveryReceiptEnabled(fvConfig), "String 'false' must be disabled");

        fvConfig.put(DeliveryReceiptTools.DELIVERY_RECEIPT_CONFIG_KEY, 0);
        assertFalse(DeliveryReceiptTools.deliveryReceiptEnabled(fvConfig), "Number 0 must be disabled");
    }

    @Test
    @DisplayName("deliveryReceiptEnabled: true when enableDeliveryReceipt or deliveryReceiptSequence is true (boolean, string, or number)")
    void testDeliveryReceiptEnabled() {
        Map<String, Object> config = new HashMap<>();
        config.put(DeliveryReceiptTools.DELIVERY_RECEIPT_CONFIG_KEY, true);
        assertTrue(DeliveryReceiptTools.deliveryReceiptEnabled(config), "enableDeliveryReceipt=true must be enabled");

        config.put(DeliveryReceiptTools.DELIVERY_RECEIPT_CONFIG_KEY, "true");
        assertTrue(DeliveryReceiptTools.deliveryReceiptEnabled(config), "enableDeliveryReceipt='true' string must be enabled");

        config.put(DeliveryReceiptTools.DELIVERY_RECEIPT_CONFIG_KEY, 1);
        assertTrue(DeliveryReceiptTools.deliveryReceiptEnabled(config), "enableDeliveryReceipt=1 number must be enabled");

        Map<String, Object> altConfig = new HashMap<>();
        altConfig.put(DeliveryReceiptTools.DELIVERY_RECEIPT_ALT_KEY, true);
        assertTrue(DeliveryReceiptTools.deliveryReceiptEnabled(altConfig), "deliveryReceiptSequence=true must be enabled");

        altConfig.put(DeliveryReceiptTools.DELIVERY_RECEIPT_ALT_KEY, "true");
        assertTrue(DeliveryReceiptTools.deliveryReceiptEnabled(altConfig), "deliveryReceiptSequence='true' must be enabled");
    }

    @Test
    @DisplayName("simplifySemiProductEnabled: false by default for null, empty or false config")
    void testSimplifySemiProductDefaultDisabled() {
        assertFalse(DeliveryReceiptTools.simplifySemiProductEnabled(null), "Null config must be disabled");
        assertFalse(DeliveryReceiptTools.simplifySemiProductEnabled(Collections.emptyMap()), "Empty config must be disabled");

        Map<String, Object> config = new HashMap<>();
        config.put(DeliveryReceiptTools.SIMPLIFY_SEMI_PRODUCT_KEY, false);
        assertFalse(DeliveryReceiptTools.simplifySemiProductEnabled(config), "Explicitly false must be disabled");
    }

    @Test
    @DisplayName("simplifySemiProductEnabled: true when simplifySemiProductToCacao or genericCacaoDisplay is true")
    void testSimplifySemiProductEnabled() {
        Map<String, Object> config = new HashMap<>();
        config.put(DeliveryReceiptTools.SIMPLIFY_SEMI_PRODUCT_KEY, true);
        assertTrue(DeliveryReceiptTools.simplifySemiProductEnabled(config), "simplifySemiProductToCacao=true must be enabled");

        Map<String, Object> altConfig = new HashMap<>();
        altConfig.put(DeliveryReceiptTools.SIMPLIFY_SEMI_PRODUCT_ALT_KEY, true);
        assertTrue(DeliveryReceiptTools.simplifySemiProductEnabled(altConfig), "genericCacaoDisplay=true must be enabled");
    }

    @Test
    @DisplayName("formatSequence: formats properly with 4 digits padding")
    void testFormatSequence() {
        assertNull(DeliveryReceiptTools.formatSequence(0));
        assertNull(DeliveryReceiptTools.formatSequence(-1));
        assertEquals("0001", DeliveryReceiptTools.formatSequence(1));
        assertEquals("0002", DeliveryReceiptTools.formatSequence(2));
        assertEquals("0010", DeliveryReceiptTools.formatSequence(10));
        assertEquals("0123", DeliveryReceiptTools.formatSequence(123));
        assertEquals("1234", DeliveryReceiptTools.formatSequence(1234));
        assertEquals("10005", DeliveryReceiptTools.formatSequence(10005));
    }

    @Test
    @DisplayName("isPublicDeliveryReceiptEnabled: false by default, true when enablePublicDeliveryReceipt is true")
    void testPublicDeliveryReceiptEnabled() {
        assertFalse(DeliveryReceiptTools.isPublicDeliveryReceiptEnabled(null), "Null config must be false");
        assertFalse(DeliveryReceiptTools.isPublicDeliveryReceiptEnabled(Collections.emptyMap()), "Empty config must be false");

        Map<String, Object> config = new HashMap<>();
        config.put(DeliveryReceiptTools.PUBLIC_DELIVERY_RECEIPT_CONFIG_KEY, false);
        assertFalse(DeliveryReceiptTools.isPublicDeliveryReceiptEnabled(config), "Explicitly false must be false");

        config.put(DeliveryReceiptTools.PUBLIC_DELIVERY_RECEIPT_CONFIG_KEY, "false");
        assertFalse(DeliveryReceiptTools.isPublicDeliveryReceiptEnabled(config), "String false must be false");

        config.put(DeliveryReceiptTools.PUBLIC_DELIVERY_RECEIPT_CONFIG_KEY, true);
        assertTrue(DeliveryReceiptTools.isPublicDeliveryReceiptEnabled(config), "Boolean true must be true");

        config.put(DeliveryReceiptTools.PUBLIC_DELIVERY_RECEIPT_CONFIG_KEY, "true");
        assertTrue(DeliveryReceiptTools.isPublicDeliveryReceiptEnabled(config), "String true must be true");

        config.put(DeliveryReceiptTools.PUBLIC_DELIVERY_RECEIPT_CONFIG_KEY, 1);
        assertTrue(DeliveryReceiptTools.isPublicDeliveryReceiptEnabled(config), "Number 1 must be true");
    }
}
