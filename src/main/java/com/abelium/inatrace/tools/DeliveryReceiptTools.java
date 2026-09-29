package com.abelium.inatrace.tools;

import java.util.Map;

/**
 * Utilidades para comprobantes de entrega y configuraciones de lotes por empresa.
 *
 * El correlativo de entrega ("0001", "0002", ...) y la simplificación de semiproducto
 * a "Cacao" se leen de la columna jsonb {@code company.configuration}:
 * - {@code enableDeliveryReceipt} (o {@code deliveryReceiptSequence}): comprobante secuencial
 * - {@code simplifySemiProductToCacao} (o {@code genericCacaoDisplay}): mostrar "Cacao"
 *
 * Ambas opciones están DESACTIVADAS por defecto (ausente o false = inactivo).
 * Para UNOCACE se activan vía configuración, y para Fortaleza del Valle (FV)
 * u otras organizaciones quedan desactivadas salvo que un usuario funcional
 * las habilite expresamente desde la configuración de la organización.
 */
public final class DeliveryReceiptTools {

    public static final String DELIVERY_RECEIPT_CONFIG_KEY = "enableDeliveryReceipt";
    public static final String DELIVERY_RECEIPT_ALT_KEY = "deliveryReceiptSequence";
    public static final String SIMPLIFY_SEMI_PRODUCT_KEY = "simplifySemiProductToCacao";
    public static final String SIMPLIFY_SEMI_PRODUCT_ALT_KEY = "genericCacaoDisplay";
    public static final String QUOTA_BALANCE_CONFIG_KEY = "enableQuotaBalance";

    private DeliveryReceiptTools() {
    }

    private static boolean isTruthy(Object val) {
        if (val == null) {
            return false;
        }
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        if (val instanceof Number) {
            return ((Number) val).intValue() == 1;
        }
        if (val instanceof String) {
            String s = ((String) val).trim().toLowerCase();
            return "true".equals(s) || "1".equals(s);
        }
        return false;
    }

    /**
     * Determina si la empresa tiene habilitada la generación y visualización de
     * comprobantes de entrega secuenciales ("0001", "0002", ...).
     */
    public static boolean deliveryReceiptEnabled(Map<String, Object> companyConfiguration) {
        if (companyConfiguration == null) {
            return false;
        }
        return isTruthy(companyConfiguration.get(DELIVERY_RECEIPT_CONFIG_KEY))
                || isTruthy(companyConfiguration.get(DELIVERY_RECEIPT_ALT_KEY));
    }

    /**
     * Determina si la empresa tiene habilitada la sustitución del nombre técnico
     * del semiproducto por la palabra "Cacao" en la vista básica de historial.
     */
    public static boolean simplifySemiProductEnabled(Map<String, Object> companyConfiguration) {
        if (companyConfiguration == null) {
            return false;
        }
        return isTruthy(companyConfiguration.get(SIMPLIFY_SEMI_PRODUCT_KEY))
                || isTruthy(companyConfiguration.get(SIMPLIFY_SEMI_PRODUCT_ALT_KEY));
    }

    /**
     * Determina si la empresa tiene habilitado el control y visualización de
     * saldo de cupo en entregas (enableQuotaBalance).
     */
    public static boolean quotaBalanceEnabled(Map<String, Object> companyConfiguration) {
        if (companyConfiguration == null) {
            return false;
        }
        return isTruthy(companyConfiguration.get(QUOTA_BALANCE_CONFIG_KEY));
    }

    /**
     * Formatea un número secuencial con relleno de ceros a 4 dígitos ("0001", "0002", ...).
     */
    public static String formatSequence(long sequence) {
        if (sequence <= 0) {
            return null;
        }
        return String.format("%04d", sequence);
    }
}
