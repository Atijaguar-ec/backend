-- Create CertificationType table for managing certifications and environmental seals catalog

CREATE TABLE IF NOT EXISTS certification_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(500) NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_certification_category CHECK (category IN ('CERTIFICATE', 'SEAL')),
    CONSTRAINT chk_certification_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create translation table for multi-language support
CREATE TABLE IF NOT EXISTS certification_type_translation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    certification_type_id BIGINT NOT NULL,
    language VARCHAR(10) NOT NULL,
    name VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cert_type_translation_cert_type FOREIGN KEY (certification_type_id) REFERENCES certification_type(id) ON DELETE CASCADE,
    CONSTRAINT uk_cert_type_translation UNIQUE (certification_type_id, language)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert initial seed data (common certifications)
INSERT INTO certification_type (code, name, category, status) VALUES
('ORGANIC', 'Organic Certification', 'CERTIFICATE', 'ACTIVE'),
('FAIRTRADE', 'Fair Trade', 'SEAL', 'ACTIVE'),
('RAINFOREST', 'Rainforest Alliance', 'SEAL', 'ACTIVE'),
('UTZ', 'UTZ Certified', 'CERTIFICATE', 'ACTIVE'),
('GLOBAL_GAP', 'GlobalGAP', 'CERTIFICATE', 'ACTIVE');

-- Insert Spanish translations
INSERT INTO certification_type_translation (certification_type_id, language, name)
SELECT id, 'ES', 
    CASE code
        WHEN 'ORGANIC' THEN 'Certificación Orgánica'
        WHEN 'FAIRTRADE' THEN 'Comercio Justo'
        WHEN 'RAINFOREST' THEN 'Rainforest Alliance'
        WHEN 'UTZ' THEN 'UTZ Certificado'
        WHEN 'GLOBAL_GAP' THEN 'GlobalGAP'
    END
FROM certification_type;
