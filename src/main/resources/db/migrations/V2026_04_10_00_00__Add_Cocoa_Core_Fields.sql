-- V2026_04_10_00_00__Add_Cocoa_Core_Fields.sql
-- Consolidated PostgreSQL migration for Cacao specific fields

-- 1. Facility changes
ALTER TABLE "Facility" ADD COLUMN IF NOT EXISTS "level" INT;
ALTER TABLE "Facility" ADD COLUMN IF NOT EXISTS "displayFinalPriceDiscount" BOOLEAN DEFAULT FALSE;
ALTER TABLE "Facility" ADD COLUMN IF NOT EXISTS "displayMoisturePercentage" BOOLEAN DEFAULT FALSE;

-- 2. FacilityType changes
ALTER TABLE "FacilityType" ADD COLUMN IF NOT EXISTS "order" INT;

-- 3. UserCustomer (and embedded FarmInformation) changes
ALTER TABLE "UserCustomer" ADD COLUMN IF NOT EXISTS "personType" VARCHAR(20) DEFAULT 'NATURAL';
ALTER TABLE "UserCustomer" ADD COLUMN IF NOT EXISTS "companyName" VARCHAR(255);
ALTER TABLE "UserCustomer" ADD COLUMN IF NOT EXISTS "legalRepresentative" VARCHAR(255);
ALTER TABLE "UserCustomer" ADD COLUMN IF NOT EXISTS "maxProductionQuantity" DECIMAL(19,2);

-- 4. StockOrder changes
ALTER TABLE "StockOrder" ADD COLUMN IF NOT EXISTS "weekNumber" INT;
ALTER TABLE "StockOrder" ADD COLUMN IF NOT EXISTS "parcelLot" VARCHAR(255);
ALTER TABLE "StockOrder" ADD COLUMN IF NOT EXISTS "variety" VARCHAR(255);
ALTER TABLE "StockOrder" ADD COLUMN IF NOT EXISTS "organicCertification" VARCHAR(255);
ALTER TABLE "StockOrder" ADD COLUMN IF NOT EXISTS "moisturePercentage" DECIMAL(19,2);
ALTER TABLE "StockOrder" ADD COLUMN IF NOT EXISTS "moistureWeightDeduction" DECIMAL(19,2);
ALTER TABLE "StockOrder" ADD COLUMN IF NOT EXISTS "netQuantity" DECIMAL(38,2);
ALTER TABLE "StockOrder" ADD COLUMN IF NOT EXISTS "finalPriceDiscount" DECIMAL(38,2);

-- 5. CertificationType and CertificationTypeTranslation tables
CREATE TABLE IF NOT EXISTS "CertificationType" (
    id BIGSERIAL PRIMARY KEY,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    creationTimestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updateTimestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    code VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT uk_certification_type_code UNIQUE (code)
);

CREATE TABLE IF NOT EXISTS "CertificationTypeTranslation" (
    id BIGSERIAL PRIMARY KEY,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    name VARCHAR(255) NOT NULL,
    language VARCHAR(10) NOT NULL,
    certification_type_id BIGINT NOT NULL,
    CONSTRAINT uk_cert_type_trans_lang UNIQUE (certification_type_id, language),
    CONSTRAINT fk_cert_type_trans FOREIGN KEY (certification_type_id) REFERENCES "CertificationType" (id) ON DELETE CASCADE
);
