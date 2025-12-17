-- Create CertificationType table for managing certifications and environmental seals catalog

CREATE TABLE IF NOT EXISTS CertificationType (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL DEFAULT 'ACTIVE',
    createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_certification_category CHECK (category IN ('CERTIFICATE', 'SEAL')),
    CONSTRAINT chk_certification_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create translation table for multi-language support
CREATE TABLE IF NOT EXISTS CertificationTypeTranslation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    certification_type_id BIGINT NOT NULL,
    language VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    createdAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedAt DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cert_type_translation_cert_type FOREIGN KEY (certification_type_id) REFERENCES CertificationType(id) ON DELETE CASCADE,
    CONSTRAINT uk_cert_type_translation UNIQUE (certification_type_id, language)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert initial seed data (common certifications)
INSERT INTO CertificationType (code, name, category, status) VALUES
('FAIRTRADE', 'Fair Trade', 'SEAL', 'ACTIVE'),
('FAIRTRADE_SPP', 'Fairtrade / SPP', 'CERTIFICATE', 'ACTIVE'),
('BIOSUISSE_FT_SPP', 'Biosuisse / Fairtrade / SPP', 'CERTIFICATE', 'ACTIVE'),
('TRANSICION_FT_SPP', 'Transition / Fairtrade / SPP', 'CERTIFICATE', 'ACTIVE')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    category = VALUES(category),
    status = VALUES(status);

-- Insert Spanish translations
INSERT INTO CertificationTypeTranslation (certification_type_id, language, name)
SELECT id, 'ES', 
    CASE code
        WHEN 'FAIRTRADE' THEN 'Comercio Justo'
        WHEN 'FAIRTRADE_SPP' THEN 'Fairtrade / SPP'
        WHEN 'BIOSUISSE_FT_SPP' THEN 'Biosuisse / Fairtrade / SPP'
        WHEN 'TRANSICION_FT_SPP' THEN 'Transición / Fairtrade / SPP'
    END
FROM CertificationType
WHERE code IN ('FAIRTRADE', 'FAIRTRADE_SPP', 'BIOSUISSE_FT_SPP', 'TRANSICION_FT_SPP')
ON DUPLICATE KEY UPDATE
    name = VALUES(name);
