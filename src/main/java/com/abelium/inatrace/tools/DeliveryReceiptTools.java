package com.abelium.inatrace.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Utilidades para comprobantes de entrega y configuraciones de lotes por empresa.
 *
 * El correlativo de entrega ("0001", "0002", ...) y la simplificación de semiproducto
 * a "Cacao" se leen de la columna jsonb {@code company.configuration}:
 * - {@code enableDeliveryReceipt} (o {@code deliveryReceiptSequence}): comprobante secuencial
 * - {@code simplifySemiProductToCacao} (o {@code genericCacaoDisplay}): mostrar "Cacao"
 * - {@code enableQuotaBalance}: control y visualización de cupo
 * - {@code quotaAlertThresholdPercent}: umbral de alerta de cupo (default: 80%)
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
    public static final String QUOTA_ALERT_THRESHOLD_KEY = "quotaAlertThresholdPercent";
    public static final String PUBLIC_DELIVERY_RECEIPT_CONFIG_KEY = "enablePublicDeliveryReceipt";
    public static final BigDecimal DEFAULT_QUOTA_ALERT_THRESHOLD_PERCENT = new BigDecimal("80.00");

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
     * Obtiene el umbral porcentual de alerta de cupo configurado para la empresa
     * (por defecto 80.00%).
     */
    public static BigDecimal getQuotaAlertThresholdPercent(Map<String, Object> companyConfiguration) {
        if (companyConfiguration == null) {
            return DEFAULT_QUOTA_ALERT_THRESHOLD_PERCENT;
        }
        Object val = companyConfiguration.get(QUOTA_ALERT_THRESHOLD_KEY);
        if (val == null) {
            return DEFAULT_QUOTA_ALERT_THRESHOLD_PERCENT;
        }
        try {
            BigDecimal parsed;
            if (val instanceof Number) {
                parsed = new BigDecimal(val.toString()).setScale(2, RoundingMode.HALF_UP);
            } else if (val instanceof String) {
                String s = ((String) val).trim();
                if (s.isEmpty()) {
                    return DEFAULT_QUOTA_ALERT_THRESHOLD_PERCENT;
                }
                parsed = new BigDecimal(s).setScale(2, RoundingMode.HALF_UP);
            } else {
                return DEFAULT_QUOTA_ALERT_THRESHOLD_PERCENT;
            }
            if (parsed.compareTo(BigDecimal.ZERO) > 0 && parsed.compareTo(new BigDecimal("100.00")) <= 0) {
                return parsed;
            }
        } catch (Exception ignored) {
        }
        return DEFAULT_QUOTA_ALERT_THRESHOLD_PERCENT;
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

    /**
     * Determina si la empresa tiene habilitada la consulta pública del comprobante
     * de entrega vía código QR sin requerir inicio de sesión.
     */
    public static boolean isPublicDeliveryReceiptEnabled(Map<String, Object> companyConfiguration) {
        if (companyConfiguration == null) {
            return false;
        }
        return isTruthy(companyConfiguration.get(PUBLIC_DELIVERY_RECEIPT_CONFIG_KEY));
    }
}
