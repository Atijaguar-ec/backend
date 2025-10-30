
-- EMPRESA
INSERT INTO Company  (id,name,	entityVersion)
VALUES (1, 'FORTALEZA DEL VALLE',1);

INSERT INTO Company  (id,name,	entityVersion)
VALUES (1, 'UNOCACE',1);

INSERT INTO Company  (id,name,	entityVersion)
VALUES (1, 'DUFER',1);


-- Asociar usuario como admin a DUFER (supón id 1 para DUFER y 1 para el usuario)
INSERT INTO CompanyUser (company_id, user_id, role,entityVersion)
VALUES (1, 1, 'COMPANY_ADMIN',1);