-- Alter ProductTypeTranslation.language column from enum to VARCHAR to support all Language enum values (EN, ES, DE, RW)
-- This is required before V2023_04_12_14_00__Update_Product_Type_Translations migration can insert DE and RW translations

ALTER TABLE ProductTypeTranslation 
MODIFY COLUMN language VARCHAR(40) NOT NULL;
