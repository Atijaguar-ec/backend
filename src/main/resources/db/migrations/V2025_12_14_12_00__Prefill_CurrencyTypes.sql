-- ═══════════════════════════════════════════════════════════════
-- Prefill CurrencyType table with common currencies
-- Idempotent: only inserts if table is empty
-- ═══════════════════════════════════════════════════════════════

-- Check if table is empty before inserting
INSERT INTO CurrencyType (code, label, enabled)
SELECT 'USD', 'US Dollar', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'USD');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'EUR', 'Euro', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'EUR');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'COP', 'Colombian Peso', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'COP');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'PEN', 'Peruvian Sol', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'PEN');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'BRL', 'Brazilian Real', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'BRL');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'MXN', 'Mexican Peso', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'MXN');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'GTQ', 'Guatemalan Quetzal', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'GTQ');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'HNL', 'Honduran Lempira', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'HNL');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'NIO', 'Nicaraguan Córdoba', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'NIO');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'CRC', 'Costa Rican Colón', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'CRC');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'RWF', 'Rwandan Franc', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'RWF');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'UGX', 'Ugandan Shilling', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'UGX');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'ETB', 'Ethiopian Birr', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'ETB');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'KES', 'Kenyan Shilling', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'KES');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'TZS', 'Tanzanian Shilling', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'TZS');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'XOF', 'CFA Franc BCEAO', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'XOF');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'GHS', 'Ghanaian Cedi', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'GHS');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'NGN', 'Nigerian Naira', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'NGN');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'IDR', 'Indonesian Rupiah', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'IDR');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'VND', 'Vietnamese Dong', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'VND');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'PHP', 'Philippine Peso', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'PHP');

INSERT INTO CurrencyType (code, label, enabled)
SELECT 'INR', 'Indian Rupee', 1
WHERE NOT EXISTS (SELECT 1 FROM CurrencyType WHERE code = 'INR');
