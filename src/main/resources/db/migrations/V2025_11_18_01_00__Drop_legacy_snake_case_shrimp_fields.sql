-- ============================================================================
-- Migración: Eliminar columnas legacy en snake_case para campos de camarón
-- Fecha: 2025-11-18
-- Descripción: Limpia columnas antiguas number_of_* y guia_remision_number en StockOrder
--               que quedaron duplicadas frente a las nuevas columnas camelCase.
-- ============================================================================

-- Solo eliminar columnas si existen, para no fallar en bases nuevas
SET @stockOrderTableExists := (
  SELECT COUNT(*)
  FROM information_schema.TABLES
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
);

-- numberOfGavetas / number_of_gavetas
SET @numberOfGavetasSnakeExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'number_of_gavetas'
);

SET @numberOfGavetasCamelExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'numberOfGavetas'
);

SET @numberOfGavetasDdl := IF(
  @stockOrderTableExists = 1 AND @numberOfGavetasSnakeExists = 1 AND @numberOfGavetasCamelExists = 0,
  'ALTER TABLE StockOrder CHANGE COLUMN number_of_gavetas numberOfGavetas INT NULL',
  IF(@stockOrderTableExists = 1 AND @numberOfGavetasSnakeExists = 1 AND @numberOfGavetasCamelExists = 1,
     'ALTER TABLE StockOrder DROP COLUMN number_of_gavetas',
     'SELECT ''Column number_of_gavetas not present or already normalized''')
);

PREPARE numberOfGavetasStmt FROM @numberOfGavetasDdl;
EXECUTE numberOfGavetasStmt;
DEALLOCATE PREPARE numberOfGavetasStmt;

-- numberOfBines / number_of_bines
SET @numberOfBinesSnakeExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'number_of_bines'
);

SET @numberOfBinesCamelExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'numberOfBines'
);

SET @numberOfBinesDdl := IF(
  @stockOrderTableExists = 1 AND @numberOfBinesSnakeExists = 1 AND @numberOfBinesCamelExists = 0,
  'ALTER TABLE StockOrder CHANGE COLUMN number_of_bines numberOfBines VARCHAR(50) NULL',
  IF(@stockOrderTableExists = 1 AND @numberOfBinesSnakeExists = 1 AND @numberOfBinesCamelExists = 1,
     'ALTER TABLE StockOrder DROP COLUMN number_of_bines',
     'SELECT ''Column number_of_bines not present or already normalized''')
);

PREPARE numberOfBinesStmt FROM @numberOfBinesDdl;
EXECUTE numberOfBinesStmt;
DEALLOCATE PREPARE numberOfBinesStmt;

-- numberOfPiscinas / number_of_piscinas
SET @numberOfPiscinasSnakeExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'number_of_piscinas'
);

SET @numberOfPiscinasCamelExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'numberOfPiscinas'
);

SET @numberOfPiscinasDdl := IF(
  @stockOrderTableExists = 1 AND @numberOfPiscinasSnakeExists = 1 AND @numberOfPiscinasCamelExists = 0,
  'ALTER TABLE StockOrder CHANGE COLUMN number_of_piscinas numberOfPiscinas VARCHAR(50) NULL',
  IF(@stockOrderTableExists = 1 AND @numberOfPiscinasSnakeExists = 1 AND @numberOfPiscinasCamelExists = 1,
     'ALTER TABLE StockOrder DROP COLUMN number_of_piscinas',
     'SELECT ''Column number_of_piscinas not present or already normalized''')
);

PREPARE numberOfPiscinasStmt FROM @numberOfPiscinasDdl;
EXECUTE numberOfPiscinasStmt;
DEALLOCATE PREPARE numberOfPiscinasStmt;

-- guiaRemisionNumber / guia_remision_number
SET @guiaRemisionNumberSnakeExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'guia_remision_number'
);

SET @guiaRemisionNumberCamelExists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'StockOrder'
    AND COLUMN_NAME = 'guiaRemisionNumber'
);

SET @guiaRemisionNumberDdl := IF(
  @stockOrderTableExists = 1 AND @guiaRemisionNumberSnakeExists = 1 AND @guiaRemisionNumberCamelExists = 0,
  'ALTER TABLE StockOrder CHANGE COLUMN guia_remision_number guiaRemisionNumber VARCHAR(100) NULL',
  IF(@stockOrderTableExists = 1 AND @guiaRemisionNumberSnakeExists = 1 AND @guiaRemisionNumberCamelExists = 1,
     'ALTER TABLE StockOrder DROP COLUMN guia_remision_number',
     'SELECT ''Column guia_remision_number not present or already normalized''')
);

PREPARE guiaRemisionNumberStmt FROM @guiaRemisionNumberDdl;
EXECUTE guiaRemisionNumberStmt;
DEALLOCATE PREPARE guiaRemisionNumberStmt;
