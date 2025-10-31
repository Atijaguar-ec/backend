-- Recalculate net_quantity for existing stock orders
-- This updates all existing orders to have the correct net quantity based on their deductions

-- First, populate totalGrossQuantity for orders that don't have it
-- For orders with tare or damaged weight, we need to reverse-calculate the gross
UPDATE StockOrder
SET totalGrossQuantity = totalQuantity + COALESCE(tare, 0) + COALESCE(damagedWeightDeduction, 0)
WHERE totalGrossQuantity IS NULL
  AND orderType = 'PURCHASE_ORDER'
  AND (tare IS NOT NULL OR damagedWeightDeduction IS NOT NULL);

-- For orders without deductions, gross = total
UPDATE StockOrder
SET totalGrossQuantity = totalQuantity
WHERE totalGrossQuantity IS NULL
  AND orderType = 'PURCHASE_ORDER';

-- Now calculate net_quantity for all orders
UPDATE StockOrder
SET netQuantity = CASE
    -- If moisture percentage exists, apply it to the base weight
    WHEN moisturePercentage IS NOT NULL AND moisturePercentage > 0 THEN
        GREATEST(0, 
            (COALESCE(totalGrossQuantity, totalQuantity, 0) 
             - COALESCE(tare, 0) 
             - COALESCE(damagedWeightDeduction, 0))
            * (moisturePercentage / 100)
        )
    -- If no moisture, net = gross - tare - damaged
    ELSE
        GREATEST(0,
            COALESCE(totalGrossQuantity, totalQuantity, 0) 
            - COALESCE(tare, 0) 
            - COALESCE(damagedWeightDeduction, 0)
        )
END
WHERE netQuantity IS NULL
  AND orderType = 'PURCHASE_ORDER';
