-- ═══════════════════════════════════════════════════════════════
-- V1: Initial Schema - Base de datos INATrace
-- ═══════════════════════════════════════════════════════════════
-- Este archivo contiene el esquema base de la aplicación.
-- Generado automáticamente desde la base de desarrollo.
-- 
-- IMPORTANTE: Este archivo debe ejecutarse en una base de datos vacía.
-- Flyway se encarga de aplicarlo automáticamente en instalaciones limpias.
-- ═══════════════════════════════════════════════════════════════

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `ActivityProof`;
CREATE TABLE `ActivityProof` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `formalCreationDate` datetime(6) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `validUntil` datetime(6) DEFAULT NULL,
  `document_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKemqkaop1au8l83r0ea5dw2ixw` (`document_id`),
  CONSTRAINT `FKemqkaop1au8l83r0ea5dw2ixw` FOREIGN KEY (`document_id`) REFERENCES `Document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `AnalyticsAggregate`;
CREATE TABLE `AnalyticsAggregate` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `intValue` int DEFAULT NULL,
  `key1` varchar(64) DEFAULT NULL,
  `key2` varchar(64) DEFAULT NULL,
  `timestampValue` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXnfy1t9sfvtgde8eni5wg5efsp` (`key1`),
  KEY `IDX1w9f3cf0e5jftmj3v4mmck54m` (`key2`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `AnalyticsAggregateItem`;
CREATE TABLE `AnalyticsAggregateItem` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `intValue` int DEFAULT NULL,
  `itemKey` varchar(64) DEFAULT NULL,
  `aggregate_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXp63bdsep5v1j21l5r5i3207in` (`itemKey`),
  KEY `FKa2apmrpkp3o44bqd3bukch4hk` (`aggregate_id`),
  CONSTRAINT `FKa2apmrpkp3o44bqd3bukch4hk` FOREIGN KEY (`aggregate_id`) REFERENCES `AnalyticsAggregate` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `AuthenticationToken`;
CREATE TABLE `AuthenticationToken` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `entityVersion` bigint NOT NULL,
  `expiration` datetime(6) NOT NULL,
  `status` enum('ACTIVE','DISABLED') NOT NULL,
  `token` varchar(64) NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpoc991v9pdc6h0sp0ebpqwnao` (`token`),
  KEY `IDXpoc991v9pdc6h0sp0ebpqwnao` (`token`),
  KEY `FKg35jtk8ndjjt0w0ft0tsrnd6d` (`user_id`),
  CONSTRAINT `FKg35jtk8ndjjt0w0ft0tsrnd6d` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `BatchLocation`;
CREATE TABLE `BatchLocation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address_address` varchar(255) DEFAULT NULL,
  `address_cell` varchar(255) DEFAULT NULL,
  `address_city` varchar(255) DEFAULT NULL,
  `address_hondurasDepartment` varchar(255) DEFAULT NULL,
  `address_hondurasFarm` varchar(255) DEFAULT NULL,
  `address_hondurasMunicipality` varchar(255) DEFAULT NULL,
  `address_hondurasVillage` varchar(255) DEFAULT NULL,
  `address_otherAddress` varchar(1000) DEFAULT NULL,
  `address_sector` varchar(255) DEFAULT NULL,
  `address_state` varchar(255) DEFAULT NULL,
  `address_village` varchar(255) DEFAULT NULL,
  `address_zip` varchar(50) DEFAULT NULL,
  `entityVersion` bigint NOT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `numberOfFarmers` int DEFAULT NULL,
  `pinName` varchar(255) DEFAULT NULL,
  `address_country_id` bigint DEFAULT NULL,
  `batch_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgw3r933r1vehpp3x2wtkwbkby` (`address_country_id`),
  KEY `FK7tn1lq3epx217kgwlr2ew1c2m` (`batch_id`),
  CONSTRAINT `FK7tn1lq3epx217kgwlr2ew1c2m` FOREIGN KEY (`batch_id`) REFERENCES `ProductLabelBatch` (`id`),
  CONSTRAINT `FKgw3r933r1vehpp3x2wtkwbkby` FOREIGN KEY (`address_country_id`) REFERENCES `Country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `BulkPayment`;
CREATE TABLE `BulkPayment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `additionalCost` decimal(38,2) DEFAULT NULL,
  `additionalCostDescription` varchar(255) DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `formalCreationTime` datetime(6) DEFAULT NULL,
  `paymentDescription` longtext,
  `paymentPurposeType` enum('ADVANCE_PAYMENT','FIRST_INSTALLMENT','INVOICE_PAYMENT','SECOND_INSTALLMENT','WOMEN_PREMIUM') DEFAULT NULL,
  `receiptNumber` varchar(255) DEFAULT NULL,
  `totalAmount` decimal(38,2) DEFAULT NULL,
  `createdBy_id` bigint NOT NULL,
  `payingCompany_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9tdn94o09x6sc2ym2thtc34v5` (`createdBy_id`),
  KEY `FK4kj37nte48242lehysge1ewlc` (`payingCompany_id`),
  CONSTRAINT `FK4kj37nte48242lehysge1ewlc` FOREIGN KEY (`payingCompany_id`) REFERENCES `Company` (`id`),
  CONSTRAINT `FK9tdn94o09x6sc2ym2thtc34v5` FOREIGN KEY (`createdBy_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `BulkPaymentActivityProof`;
CREATE TABLE `BulkPaymentActivityProof` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activityProof_id` bigint DEFAULT NULL,
  `bulkPayment_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK45r9pwy6rvn3vc39pqnmqcksb` (`activityProof_id`),
  KEY `FKak33ydw04ciktu4x5f0kalw3j` (`bulkPayment_id`),
  CONSTRAINT `FK3uo0yxwowclwdl8ibwo1im1m1` FOREIGN KEY (`activityProof_id`) REFERENCES `ActivityProof` (`id`),
  CONSTRAINT `FKak33ydw04ciktu4x5f0kalw3j` FOREIGN KEY (`bulkPayment_id`) REFERENCES `BulkPayment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `BusinessToCustomerSettings`;
CREATE TABLE `BusinessToCustomerSettings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `average_region_farm_gate_price` decimal(38,2) DEFAULT NULL,
  `container_size` decimal(38,2) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `fair_trade` decimal(38,2) DEFAULT NULL,
  `graphic_fair_prices` bit(1) DEFAULT NULL,
  `graphic_farm_gate_price` enum('DISABLED','PERCENT_VALUE','PER_CONTAINER','PER_KG') DEFAULT NULL,
  `graphic_increase_of_income` bit(1) DEFAULT NULL,
  `graphic_price_to_producer` enum('DISABLED','PERCENT_VALUE','PER_CONTAINER','PER_KG') DEFAULT NULL,
  `graphic_quality` bit(1) DEFAULT NULL,
  `heading_color` varchar(7) DEFAULT NULL,
  `manual_farm_gate_price` decimal(38,2) DEFAULT NULL,
  `manual_producer_price` decimal(38,2) DEFAULT NULL,
  `order_fair_prices` bigint DEFAULT NULL,
  `order_feedback` bigint DEFAULT NULL,
  `order_producers` bigint DEFAULT NULL,
  `order_quality` bigint DEFAULT NULL,
  `primary_color` varchar(7) DEFAULT NULL,
  `product_title_color` varchar(7) DEFAULT NULL,
  `quaternary_color` varchar(7) DEFAULT NULL,
  `secondary_color` varchar(7) DEFAULT NULL,
  `tab_fair_prices` bit(1) DEFAULT NULL,
  `tab_feedback` bit(1) DEFAULT NULL,
  `tab_producers` bit(1) DEFAULT NULL,
  `tab_quality` bit(1) DEFAULT NULL,
  `tertiary_color` varchar(7) DEFAULT NULL,
  `text_color` varchar(7) DEFAULT NULL,
  `world_market` decimal(38,2) DEFAULT NULL,
  `headerBackgroundImage_id` bigint DEFAULT NULL,
  `landingPageBackgroundImage_id` bigint DEFAULT NULL,
  `landingPageImage_id` bigint DEFAULT NULL,
  `productFont_id` bigint DEFAULT NULL,
  `textFont_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKs11is424x4p32acwm0qieuf34` (`headerBackgroundImage_id`),
  UNIQUE KEY `UK1vu4j2a1yy0g7ty2tpnfh1e90` (`landingPageBackgroundImage_id`),
  UNIQUE KEY `UK5c1yy0uaoitqx5nku6t9kixed` (`landingPageImage_id`),
  UNIQUE KEY `UKdowqv65y7bem3bb0cnyg2pqgv` (`productFont_id`),
  UNIQUE KEY `UK3unh40md1gwovdw63ghqsfntp` (`textFont_id`),
  CONSTRAINT `FK62u27uf5v9nrfmc7oirw5a37j` FOREIGN KEY (`textFont_id`) REFERENCES `Document` (`id`),
  CONSTRAINT `FK7ugqrlx5w2t5vi106l0jj9qw9` FOREIGN KEY (`headerBackgroundImage_id`) REFERENCES `Document` (`id`),
  CONSTRAINT `FKjnq4oasl0pntc9g98bsy23ek9` FOREIGN KEY (`productFont_id`) REFERENCES `Document` (`id`),
  CONSTRAINT `FKringgjfrq2ocejmx3nk6mkyeo` FOREIGN KEY (`landingPageImage_id`) REFERENCES `Document` (`id`),
  CONSTRAINT `FKs2w0ruaupuejwhiylil5m01r7` FOREIGN KEY (`landingPageBackgroundImage_id`) REFERENCES `Document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `Certification`;
CREATE TABLE `Certification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `validity` datetime(6) DEFAULT NULL,
  `certificate_id` bigint DEFAULT NULL,
  `stockOrder_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhlt8qkk24nkdwc4rb3ulnjqcs` (`certificate_id`),
  KEY `FK2r634dsqa5p95j8jo59yn2f8x` (`stockOrder_id`),
  CONSTRAINT `FK2r634dsqa5p95j8jo59yn2f8x` FOREIGN KEY (`stockOrder_id`) REFERENCES `StockOrder` (`id`),
  CONSTRAINT `FKaoo6433h2o8lb174so5kyb7xr` FOREIGN KEY (`certificate_id`) REFERENCES `Document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `Company`;
CREATE TABLE `Company` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `abbreviation` varchar(255) DEFAULT NULL,
  `about` longtext,
  `allowBeycoIntegration` bit(1) DEFAULT NULL,
  `displayPrefferedWayOfPayment` bit(1) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `entityVersion` bigint NOT NULL,
  `headquarters_address` varchar(255) DEFAULT NULL,
  `headquarters_cell` varchar(255) DEFAULT NULL,
  `headquarters_city` varchar(255) DEFAULT NULL,
  `headquarters_hondurasDepartment` varchar(255) DEFAULT NULL,
  `headquarters_hondurasFarm` varchar(255) DEFAULT NULL,
  `headquarters_hondurasMunicipality` varchar(255) DEFAULT NULL,
  `headquarters_hondurasVillage` varchar(255) DEFAULT NULL,
  `headquarters_otherAddress` varchar(1000) DEFAULT NULL,
  `headquarters_sector` varchar(255) DEFAULT NULL,
  `headquarters_state` varchar(255) DEFAULT NULL,
  `headquarters_village` varchar(255) DEFAULT NULL,
  `headquarters_zip` varchar(50) DEFAULT NULL,
  `interview` longtext,
  `manager` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `purchaseProofDocumentMultipleFarmers` bit(1) DEFAULT NULL,
  `status` enum('ACTIVE','DEACTIVATED','REGISTERED') NOT NULL,
  `webPage` varchar(255) DEFAULT NULL,
  `currency_id` bigint DEFAULT NULL,
  `headquarters_country_id` bigint DEFAULT NULL,
  `logo_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6ke2b52benai2fmyy88hnnjlg` (`logo_id`),
  KEY `IDXck8wy9n707u5ictdeeg36f3na` (`name`),
  KEY `FKbbghpoldo0lie5e285hny3sr8` (`currency_id`),
  KEY `FKqy4m2cx39jbat8b1iwc2b9y02` (`headquarters_country_id`),
  CONSTRAINT `FK5owxc6ljlrw1e5ir3w5afr7c5` FOREIGN KEY (`logo_id`) REFERENCES `Document` (`id`),
  CONSTRAINT `FKbbghpoldo0lie5e285hny3sr8` FOREIGN KEY (`currency_id`) REFERENCES `CurrencyType` (`id`),
  CONSTRAINT `FKqy4m2cx39jbat8b1iwc2b9y02` FOREIGN KEY (`headquarters_country_id`) REFERENCES `Country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `CompanyCertification`;
CREATE TABLE `CompanyCertification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `type` varchar(255) DEFAULT NULL,
  `validity` date DEFAULT NULL,
  `certificate_id` bigint DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  `companyTranslation_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn6v3ytd1je8o9oeb3w1tadvxp` (`certificate_id`),
  KEY `FKhr33i4m4lct9gtf2hs1ghmc1f` (`company_id`),
  KEY `FK7840sse63j43vc3rh7uh9ysky` (`companyTranslation_id`),
  CONSTRAINT `FK7840sse63j43vc3rh7uh9ysky` FOREIGN KEY (`companyTranslation_id`) REFERENCES `CompanyTranslation` (`id`),
  CONSTRAINT `FKhr33i4m4lct9gtf2hs1ghmc1f` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`),
  CONSTRAINT `FKn6v3ytd1je8o9oeb3w1tadvxp` FOREIGN KEY (`certificate_id`) REFERENCES `Document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `CompanyCustomer`;
CREATE TABLE `CompanyCustomer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `contact` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `location_latitude` double DEFAULT NULL,
  `location_longitude` double DEFAULT NULL,
  `location_address` varchar(255) DEFAULT NULL,
  `location_cell` varchar(255) DEFAULT NULL,
  `location_city` varchar(255) DEFAULT NULL,
  `location_hondurasDepartment` varchar(255) DEFAULT NULL,
  `location_hondurasFarm` varchar(255) DEFAULT NULL,
  `location_hondurasMunicipality` varchar(255) DEFAULT NULL,
  `location_hondurasVillage` varchar(255) DEFAULT NULL,
  `location_otherAddress` varchar(1000) DEFAULT NULL,
  `location_sector` varchar(255) DEFAULT NULL,
  `location_state` varchar(255) DEFAULT NULL,
  `location_village` varchar(255) DEFAULT NULL,
  `location_zip` varchar(50) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `officialCompanyName` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `vatId` varchar(40) DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  `location_country_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgce6xf8xk0ca9ngsch6gy8rf8` (`company_id`),
  KEY `FKnftn8qjehp9ylkyqsr8xog93a` (`location_country_id`),
  KEY `FKqpkh66ddnr85q2xcmlmx5c3co` (`product_id`),
  CONSTRAINT `FKgce6xf8xk0ca9ngsch6gy8rf8` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`),
  CONSTRAINT `FKnftn8qjehp9ylkyqsr8xog93a` FOREIGN KEY (`location_country_id`) REFERENCES `Country` (`id`),
  CONSTRAINT `FKqpkh66ddnr85q2xcmlmx5c3co` FOREIGN KEY (`product_id`) REFERENCES `Product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `CompanyDocument`;
CREATE TABLE `CompanyDocument` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `category` enum('MEET_THE_FARMER','PRODUCTION_RECORD','VIDEO') DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `link` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `quote` longtext,
  `type` enum('FILE','LINK') DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  `companyTranslation_id` bigint DEFAULT NULL,
  `document_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1lkncmvq1t6poo1lecqhgqplf` (`company_id`),
  KEY `FKjwpwtvfp6biu6gaogybxqa8y3` (`companyTranslation_id`),
  KEY `FKmclnbhregkbbug3250l50qxjx` (`document_id`),
  CONSTRAINT `FK1lkncmvq1t6poo1lecqhgqplf` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`),
  CONSTRAINT `FKjwpwtvfp6biu6gaogybxqa8y3` FOREIGN KEY (`companyTranslation_id`) REFERENCES `CompanyTranslation` (`id`),
  CONSTRAINT `FKmclnbhregkbbug3250l50qxjx` FOREIGN KEY (`document_id`) REFERENCES `Document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `CompanyProcessingAction`;
CREATE TABLE `CompanyProcessingAction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `alias_label` varchar(255) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `order_override` int DEFAULT NULL,
  `company_id` bigint NOT NULL,
  `processing_action_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_company_processing_action_company_enabled` (`company_id`,`enabled`,`order_override`),
  KEY `idx_company_processing_action_processing_action` (`processing_action_id`),
  CONSTRAINT `FK5ujuuiy2p38ig2yi15ex7q4yg` FOREIGN KEY (`processing_action_id`) REFERENCES `ProcessingAction` (`id`),
  CONSTRAINT `FKeavl8s3cs7g2iaguvabt7kr2p` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `CompanyTranslation`;
CREATE TABLE `CompanyTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `language` enum('EN','ES') NOT NULL,
  `abbreviation` varchar(255) DEFAULT NULL,
  `about` longtext,
  `interview` longtext,
  `name` varchar(255) DEFAULT NULL,
  `webPage` varchar(255) DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKhg3olp2ynrkn4htdvmn7va2qs` (`company_id`,`language`),
  CONSTRAINT `FK38hkfpu1v49g48bpsh0yn6jo4` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `CompanyTranslation_mediaLinks`;
CREATE TABLE `CompanyTranslation_mediaLinks` (
  `CompanyTranslation_id` bigint NOT NULL,
  `link` varchar(255) DEFAULT NULL,
  `medium` varchar(255) NOT NULL,
  PRIMARY KEY (`CompanyTranslation_id`,`medium`),
  CONSTRAINT `FKkckdgt4lveapow6uvh93hl20f` FOREIGN KEY (`CompanyTranslation_id`) REFERENCES `CompanyTranslation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `CompanyUser`;
CREATE TABLE `CompanyUser` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `entityVersion` bigint NOT NULL,
  `role` enum('ACCOUNTANT','COMPANY_ADMIN','COMPANY_USER','MANAGER') DEFAULT NULL,
  `company_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKooc3pd761luongqc14ep6jgfd` (`company_id`),
  KEY `FKh37pevau45fgnmyrq7o6nasyi` (`user_id`),
  CONSTRAINT `FKh37pevau45fgnmyrq7o6nasyi` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FKooc3pd761luongqc14ep6jgfd` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `CompanyValueChain`;
CREATE TABLE `CompanyValueChain` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NOT NULL,
  `valueChain_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKbbly17yyupinf6ryi9o4vjnta` (`valueChain_id`,`company_id`),
  KEY `FKnyye6j73kq2nib0lq76n27kq8` (`company_id`),
  CONSTRAINT `FK7k8lpq37srd9rj5iyfhi33gv` FOREIGN KEY (`valueChain_id`) REFERENCES `ValueChain` (`id`),
  CONSTRAINT `FKnyye6j73kq2nib0lq76n27kq8` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `Company_mediaLinks`;
CREATE TABLE `Company_mediaLinks` (
  `Company_id` bigint NOT NULL,
  `link` varchar(255) DEFAULT NULL,
  `medium` varchar(255) NOT NULL,
  PRIMARY KEY (`Company_id`,`medium`),
  CONSTRAINT `FKev48m1qovkhuxk6xttmmfhu29` FOREIGN KEY (`Company_id`) REFERENCES `Company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ConfirmationToken`;
CREATE TABLE `ConfirmationToken` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `entityVersion` bigint NOT NULL,
  `status` enum('ACTIVE','DISABLED') NOT NULL,
  `token` varchar(64) NOT NULL,
  `type` enum('CONFIRM_EMAIL','PASSWORD_RESET') DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK5yeek6arig5t6anh1oo2s5sar` (`token`),
  KEY `IDX5yeek6arig5t6anh1oo2s5sar` (`token`),
  KEY `FKsjwvpslamugnwqhxsp09noep2` (`user_id`),
  CONSTRAINT `FKsjwvpslamugnwqhxsp09noep2` FOREIGN KEY (`user_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `Country`;
CREATE TABLE `Country` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(3) NOT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqyh4l70f9l5k5jcv876rb4j89` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `CurrencyPair`;
CREATE TABLE `CurrencyPair` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `date` date DEFAULT NULL,
  `value` decimal(38,2) DEFAULT NULL,
  `from_id` bigint DEFAULT NULL,
  `to_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1t5j4yvhljgom8thrjual998g` (`from_id`),
  KEY `FKlm15bj6g8nrg74i4xlfxs6lbi` (`to_id`),
  CONSTRAINT `FK1t5j4yvhljgom8thrjual998g` FOREIGN KEY (`from_id`) REFERENCES `CurrencyType` (`id`),
  CONSTRAINT `FKlm15bj6g8nrg74i4xlfxs6lbi` FOREIGN KEY (`to_id`) REFERENCES `CurrencyType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `CurrencyType`;
CREATE TABLE `CurrencyType` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `label` varchar(255) NOT NULL,
  `enabled` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `Document`;
CREATE TABLE `Document` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `contentType` varchar(128) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `size` bigint DEFAULT NULL,
  `storageKey` varchar(255) DEFAULT NULL,
  `type` enum('GENERAL','IMAGE') DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKlkixmvkl0tunyy6v0jbo1tsf8` (`storageKey`),
  KEY `IDXlkixmvkl0tunyy6v0jbo1tsf8` (`storageKey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `Facility`;
CREATE TABLE `Facility` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `displayMayInvolveCollectors` bit(1) DEFAULT NULL,
  `displayOrganic` bit(1) DEFAULT NULL,
  `displayPriceDeductionDamage` bit(1) DEFAULT NULL,
  `displayPriceDeterminedLater` bit(1) DEFAULT NULL,
  `displayTare` bit(1) DEFAULT NULL,
  `displayWeightDeductionDamage` bit(1) DEFAULT NULL,
  `displayWomenOnly` bit(1) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `isCollectionFacility` bit(1) DEFAULT NULL,
  `isDeactivated` bit(1) DEFAULT NULL,
  `isPublic` bit(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  `facilityLocation_id` bigint DEFAULT NULL,
  `facilityType_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpaf4sgn0ox8xenm74vka2aegy` (`facilityLocation_id`),
  KEY `FKmwbprmjxk6737yes985o85cmi` (`company_id`),
  KEY `FKcumgxtgyjr9fvqegw9vvtwdj6` (`facilityType_id`),
  CONSTRAINT `FKbydfckdpevkicna5tbl1dmtvq` FOREIGN KEY (`facilityLocation_id`) REFERENCES `FacilityLocation` (`id`),
  CONSTRAINT `FKcumgxtgyjr9fvqegw9vvtwdj6` FOREIGN KEY (`facilityType_id`) REFERENCES `FacilityType` (`id`),
  CONSTRAINT `FKmwbprmjxk6737yes985o85cmi` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `FacilityFinalProduct`;
CREATE TABLE `FacilityFinalProduct` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `facility_id` bigint NOT NULL,
  `finalProduct_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKc0nexm9vp5my189hg058pnpty` (`facility_id`),
  KEY `FKjg15plyreq6lcmhr95kqw630e` (`finalProduct_id`),
  CONSTRAINT `FKc0nexm9vp5my189hg058pnpty` FOREIGN KEY (`facility_id`) REFERENCES `Facility` (`id`),
  CONSTRAINT `FKjg15plyreq6lcmhr95kqw630e` FOREIGN KEY (`finalProduct_id`) REFERENCES `FinalProduct` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `FacilityLocation`;
CREATE TABLE `FacilityLocation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address_address` varchar(255) DEFAULT NULL,
  `address_cell` varchar(255) DEFAULT NULL,
  `address_city` varchar(255) DEFAULT NULL,
  `address_hondurasDepartment` varchar(255) DEFAULT NULL,
  `address_hondurasFarm` varchar(255) DEFAULT NULL,
  `address_hondurasMunicipality` varchar(255) DEFAULT NULL,
  `address_hondurasVillage` varchar(255) DEFAULT NULL,
  `address_otherAddress` varchar(1000) DEFAULT NULL,
  `address_sector` varchar(255) DEFAULT NULL,
  `address_state` varchar(255) DEFAULT NULL,
  `address_village` varchar(255) DEFAULT NULL,
  `address_zip` varchar(50) DEFAULT NULL,
  `entityVersion` bigint NOT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `numberOfFarmers` int DEFAULT NULL,
  `pinName` varchar(255) DEFAULT NULL,
  `isPubliclyVisible` bit(1) DEFAULT NULL,
  `address_country_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4gx3rd9bs6efgswmxgj9gf3x4` (`address_country_id`),
  CONSTRAINT `FK4gx3rd9bs6efgswmxgj9gf3x4` FOREIGN KEY (`address_country_id`) REFERENCES `Country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `FacilitySemiProduct`;
CREATE TABLE `FacilitySemiProduct` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `entityVersion` bigint NOT NULL,
  `facility_id` bigint NOT NULL,
  `semiProduct_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK790gxtge166vqt962rp196vn` (`facility_id`),
  KEY `FKdg7h24mnlambgii7dir3g5sga` (`semiProduct_id`),
  CONSTRAINT `FK790gxtge166vqt962rp196vn` FOREIGN KEY (`facility_id`) REFERENCES `Facility` (`id`),
  CONSTRAINT `FKdg7h24mnlambgii7dir3g5sga` FOREIGN KEY (`semiProduct_id`) REFERENCES `SemiProduct` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `FacilityTranslation`;
CREATE TABLE `FacilityTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `language` enum('EN','ES') NOT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `facility_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6ed4v4q3ustuj2ub9d2nltcpe` (`facility_id`),
  CONSTRAINT `FK6ed4v4q3ustuj2ub9d2nltcpe` FOREIGN KEY (`facility_id`) REFERENCES `Facility` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `FacilityType`;
CREATE TABLE `FacilityType` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `label` varchar(255) NOT NULL,
  `order` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_facility_type_order` (`order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `FacilityValueChain`;
CREATE TABLE `FacilityValueChain` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `facility_id` bigint NOT NULL,
  `valueChain_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq76hy2x2xa23oueptkxvnaw7q` (`facility_id`),
  KEY `FK1k8016c8hnoybrqutbojdsgon` (`valueChain_id`),
  CONSTRAINT `FK1k8016c8hnoybrqutbojdsgon` FOREIGN KEY (`valueChain_id`) REFERENCES `ValueChain` (`id`),
  CONSTRAINT `FKq76hy2x2xa23oueptkxvnaw7q` FOREIGN KEY (`facility_id`) REFERENCES `Facility` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `FarmPlantInformation`;
CREATE TABLE `FarmPlantInformation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `numberOfPlants` int DEFAULT NULL,
  `plantCultivatedArea` decimal(38,2) DEFAULT NULL,
  `productType_id` bigint DEFAULT NULL,
  `userCustomer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7go67a5u1nxudtpuj2u0hree` (`productType_id`),
  KEY `FKdxp7vd7t2vk0ddkl6svhvm3hj` (`userCustomer_id`),
  CONSTRAINT `FK7go67a5u1nxudtpuj2u0hree` FOREIGN KEY (`productType_id`) REFERENCES `ProductType` (`id`),
  CONSTRAINT `FKdxp7vd7t2vk0ddkl6svhvm3hj` FOREIGN KEY (`userCustomer_id`) REFERENCES `UserCustomer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `FinalProduct`;
CREATE TABLE `FinalProduct` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `description` varchar(255) NOT NULL,
  `entityVersion` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `measurementUnitType_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdhis2r5ykqmbn5851bit0k9m2` (`measurementUnitType_id`),
  KEY `FKsgf9nmb63egkc3wt0e27ndsqc` (`product_id`),
  CONSTRAINT `FKdhis2r5ykqmbn5851bit0k9m2` FOREIGN KEY (`measurementUnitType_id`) REFERENCES `MeasureUnitType` (`id`),
  CONSTRAINT `FKsgf9nmb63egkc3wt0e27ndsqc` FOREIGN KEY (`product_id`) REFERENCES `Product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `FinalProductLabel`;
CREATE TABLE `FinalProductLabel` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `finalProduct_id` bigint NOT NULL,
  `productLabel_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKonq6k76jrkw0nfci1r5jh29h6` (`finalProduct_id`),
  KEY `FKabpxmf2jga7s5jkqxl0v8bx5c` (`productLabel_id`),
  CONSTRAINT `FKabpxmf2jga7s5jkqxl0v8bx5c` FOREIGN KEY (`productLabel_id`) REFERENCES `ProductLabel` (`id`),
  CONSTRAINT `FKonq6k76jrkw0nfci1r5jh29h6` FOREIGN KEY (`finalProduct_id`) REFERENCES `FinalProduct` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `GlobalSettings`;
CREATE TABLE `GlobalSettings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `isPublic` bit(1) DEFAULT NULL,
  `name` varchar(128) NOT NULL,
  `value` longtext,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK893h0n4eip1a71mlo73qarst1` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `KnowledgeBlog`;
CREATE TABLE `KnowledgeBlog` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `content` longtext,
  `date` date DEFAULT NULL,
  `summary` longtext,
  `title` varchar(255) DEFAULT NULL,
  `type` enum('FAIRNESS','PROVENANCE','QUALITY') NOT NULL,
  `youtubeUrl` varchar(255) DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXj2wseaneec8w3jk29pdyakhu3` (`type`),
  KEY `FKdp3obm3682few5err1t9pilgo` (`product_id`),
  CONSTRAINT `FKdp3obm3682few5err1t9pilgo` FOREIGN KEY (`product_id`) REFERENCES `Product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `KnowledgeBlog_Document`;
CREATE TABLE `KnowledgeBlog_Document` (
  `KnowledgeBlog_id` bigint NOT NULL,
  `documents_id` bigint NOT NULL,
  KEY `FKmh4bqfxnxtte4o7mxu5ae5y00` (`documents_id`),
  KEY `FKdj09xtee48258tlupd90p0nm7` (`KnowledgeBlog_id`),
  CONSTRAINT `FKdj09xtee48258tlupd90p0nm7` FOREIGN KEY (`KnowledgeBlog_id`) REFERENCES `KnowledgeBlog` (`id`),
  CONSTRAINT `FKmh4bqfxnxtte4o7mxu5ae5y00` FOREIGN KEY (`documents_id`) REFERENCES `Document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `MeasureUnitType`;
CREATE TABLE `MeasureUnitType` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) NOT NULL,
  `label` varchar(255) NOT NULL,
  `weight` decimal(38,2) DEFAULT NULL,
  `underlyingMeasurementUnitType_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKss34jj4jan6qmexcyhgvccmfq` (`underlyingMeasurementUnitType_id`),
  CONSTRAINT `FKss34jj4jan6qmexcyhgvccmfq` FOREIGN KEY (`underlyingMeasurementUnitType_id`) REFERENCES `MeasureUnitType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `Payment`;
CREATE TABLE `Payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `amount` decimal(38,2) DEFAULT NULL,
  `amountPaidToTheCollector` decimal(38,2) DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `formalCreationTime` date DEFAULT NULL,
  `orderReference` varchar(255) DEFAULT NULL,
  `paymentConfirmedAtTime` datetime(6) DEFAULT NULL,
  `paymentPurposeType` enum('ADVANCE_PAYMENT','FIRST_INSTALLMENT','INVOICE_PAYMENT','SECOND_INSTALLMENT','WOMEN_PREMIUM') DEFAULT NULL,
  `paymentStatus` enum('CONFIRMED','UNCONFIRMED') DEFAULT NULL,
  `paymentType` enum('BANK_TRANSFER','CASH','CASH_VIA_COLLECTOR','CHEQUE','OFFSETTING') DEFAULT NULL,
  `preferredWayOfPayment` enum('BANK_TRANSFER','CASH','CASH_VIA_COLLECTOR','CHEQUE','OFFSETTING','UNKNOWN') DEFAULT NULL,
  `productionDate` date DEFAULT NULL,
  `purchased` decimal(38,2) DEFAULT NULL,
  `receiptDocumentType` enum('PURCHASE_SHEET','RECEIPT') DEFAULT NULL,
  `receiptNumber` varchar(255) DEFAULT NULL,
  `recipientType` enum('COMPANY','USER_CUSTOMER') DEFAULT NULL,
  `totalPaid` decimal(38,2) DEFAULT NULL,
  `bulkPayment_id` bigint DEFAULT NULL,
  `createdBy_id` bigint NOT NULL,
  `payingCompany_id` bigint DEFAULT NULL,
  `paymentConfirmedByCompany_id` bigint DEFAULT NULL,
  `paymentConfirmedByUser_id` bigint DEFAULT NULL,
  `receiptDocument_id` bigint DEFAULT NULL,
  `recipientCompany_id` bigint DEFAULT NULL,
  `recipientUserCustomer_id` bigint DEFAULT NULL,
  `representativeOfRecipientUserCustomer_id` bigint DEFAULT NULL,
  `stockOrder_id` bigint DEFAULT NULL,
  `updatedBy_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4ashldgu7vtv6do3g9bn8593j` (`receiptDocument_id`),
  KEY `FK3m9wbqfo12rn3mytqixccxta2` (`bulkPayment_id`),
  KEY `FKddubxt0xleyftt9jtl2umarko` (`createdBy_id`),
  KEY `FKmt37cugduilusyin97l7apajt` (`payingCompany_id`),
  KEY `FK2knh6y378q3wmqjrwpmqcv4ib` (`paymentConfirmedByCompany_id`),
  KEY `FK6rkw6qfvbpeqawd39n40etbd2` (`paymentConfirmedByUser_id`),
  KEY `FKi8y43p75khktv5au5pa12cxwl` (`recipientCompany_id`),
  KEY `FKgd557mftr3sbwakyhn8m8xhds` (`recipientUserCustomer_id`),
  KEY `FKkoipvqhpe48t9lnt7vkxw0jhv` (`representativeOfRecipientUserCustomer_id`),
  KEY `FK115u0lb664tkh1mx8ar66v04j` (`stockOrder_id`),
  KEY `FKglvtlmye2i8ap6yslve1ak7hj` (`updatedBy_id`),
  KEY `idx_payment_recipient_type` (`recipientType`),
  CONSTRAINT `FK115u0lb664tkh1mx8ar66v04j` FOREIGN KEY (`stockOrder_id`) REFERENCES `StockOrder` (`id`),
  CONSTRAINT `FK2knh6y378q3wmqjrwpmqcv4ib` FOREIGN KEY (`paymentConfirmedByCompany_id`) REFERENCES `Company` (`id`),
  CONSTRAINT `FK3m9wbqfo12rn3mytqixccxta2` FOREIGN KEY (`bulkPayment_id`) REFERENCES `BulkPayment` (`id`),
  CONSTRAINT `FK6rkw6qfvbpeqawd39n40etbd2` FOREIGN KEY (`paymentConfirmedByUser_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FK781d0x6ftnxfjmvls8lds91gq` FOREIGN KEY (`receiptDocument_id`) REFERENCES `Document` (`id`),
  CONSTRAINT `FKddubxt0xleyftt9jtl2umarko` FOREIGN KEY (`createdBy_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FKgd557mftr3sbwakyhn8m8xhds` FOREIGN KEY (`recipientUserCustomer_id`) REFERENCES `UserCustomer` (`id`),
  CONSTRAINT `FKglvtlmye2i8ap6yslve1ak7hj` FOREIGN KEY (`updatedBy_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FKi8y43p75khktv5au5pa12cxwl` FOREIGN KEY (`recipientCompany_id`) REFERENCES `Company` (`id`),
  CONSTRAINT `FKkoipvqhpe48t9lnt7vkxw0jhv` FOREIGN KEY (`representativeOfRecipientUserCustomer_id`) REFERENCES `UserCustomer` (`id`),
  CONSTRAINT `FKmt37cugduilusyin97l7apajt` FOREIGN KEY (`payingCompany_id`) REFERENCES `Company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `Plot`;
CREATE TABLE `Plot` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `geoId` varchar(255) DEFAULT NULL,
  `lastUpdated` datetime(6) DEFAULT NULL,
  `numberOfPlants` int DEFAULT NULL,
  `organicStartOfTransition` datetime(6) DEFAULT NULL,
  `plotName` varchar(255) DEFAULT NULL,
  `size` double DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `crop_id` bigint DEFAULT NULL,
  `farmer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKp866yyb8jfs2fawyol9hc0xq4` (`crop_id`),
  KEY `FKmckxho0qyajs99kvv1hstk7de` (`farmer_id`),
  CONSTRAINT `FKmckxho0qyajs99kvv1hstk7de` FOREIGN KEY (`farmer_id`) REFERENCES `UserCustomer` (`id`),
  CONSTRAINT `FKp866yyb8jfs2fawyol9hc0xq4` FOREIGN KEY (`crop_id`) REFERENCES `ProductType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `PlotCoordinate`;
CREATE TABLE `PlotCoordinate` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `plot_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKaajjvpqh3l69s73fo95qs99sk` (`plot_id`),
  CONSTRAINT `FKaajjvpqh3l69s73fo95qs99sk` FOREIGN KEY (`plot_id`) REFERENCES `Plot` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `Process`;
CREATE TABLE `Process` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `production` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProcessingAction`;
CREATE TABLE `ProcessingAction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `estimatedOutputQuantityPerUnit` decimal(38,2) DEFAULT NULL,
  `finalProductAction` bit(1) DEFAULT NULL,
  `maxOutputWeight` decimal(38,2) DEFAULT NULL,
  `prefix` varchar(255) DEFAULT NULL,
  `publicTimelineIconType` enum('LEAF','OTHER','QRCODE','SHIP','WAREHOUSE') DEFAULT NULL,
  `publicTimelineLabel` varchar(255) DEFAULT NULL,
  `publicTimelineLocation` varchar(255) DEFAULT NULL,
  `repackedOutputFinalProducts` bit(1) DEFAULT NULL,
  `sortOrder` bigint DEFAULT NULL,
  `type` enum('FINAL_PROCESSING','GENERATE_QR_CODE','PROCESSING','SHIPMENT','TRANSFER') DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  `inputFinalProduct_id` bigint DEFAULT NULL,
  `inputSemiProduct_id` bigint DEFAULT NULL,
  `outputFinalProduct_id` bigint DEFAULT NULL,
  `qrCodeForFinalProduct_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6xslu9314yj51oq60nvfv59ja` (`company_id`),
  KEY `FK93id1dxrtncnhhlp37cbg4uka` (`inputFinalProduct_id`),
  KEY `FKhj2ps3oosqhrsp3jpt8twtbnt` (`inputSemiProduct_id`),
  KEY `FKjcb11b4o4wnrdrvgyjs8q0vlp` (`outputFinalProduct_id`),
  KEY `FKp80ufrwi1nwpouird3hfl15e7` (`qrCodeForFinalProduct_id`),
  CONSTRAINT `FK6xslu9314yj51oq60nvfv59ja` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`),
  CONSTRAINT `FK93id1dxrtncnhhlp37cbg4uka` FOREIGN KEY (`inputFinalProduct_id`) REFERENCES `FinalProduct` (`id`),
  CONSTRAINT `FKhj2ps3oosqhrsp3jpt8twtbnt` FOREIGN KEY (`inputSemiProduct_id`) REFERENCES `SemiProduct` (`id`),
  CONSTRAINT `FKjcb11b4o4wnrdrvgyjs8q0vlp` FOREIGN KEY (`outputFinalProduct_id`) REFERENCES `FinalProduct` (`id`),
  CONSTRAINT `FKp80ufrwi1nwpouird3hfl15e7` FOREIGN KEY (`qrCodeForFinalProduct_id`) REFERENCES `FinalProduct` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProcessingActionFacility`;
CREATE TABLE `ProcessingActionFacility` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `facility_id` bigint NOT NULL,
  `processingAction_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKna81ra301vittgk2tpkdeytd6` (`facility_id`),
  KEY `FKloyqqfoerxmr52nogdragqho4` (`processingAction_id`),
  CONSTRAINT `FKloyqqfoerxmr52nogdragqho4` FOREIGN KEY (`processingAction_id`) REFERENCES `ProcessingAction` (`id`),
  CONSTRAINT `FKna81ra301vittgk2tpkdeytd6` FOREIGN KEY (`facility_id`) REFERENCES `Facility` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProcessingActionOutputSemiProduct`;
CREATE TABLE `ProcessingActionOutputSemiProduct` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `maxOutputWeight` decimal(38,2) DEFAULT NULL,
  `repackedOutput` bit(1) DEFAULT NULL,
  `outputSemiProduct_id` bigint NOT NULL,
  `processingAction_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKc4gp5jqufrbob7yfyxffhpxpq` (`outputSemiProduct_id`),
  KEY `FKp68rslba7686lvhswfpkhgf3f` (`processingAction_id`),
  CONSTRAINT `FKc4gp5jqufrbob7yfyxffhpxpq` FOREIGN KEY (`outputSemiProduct_id`) REFERENCES `SemiProduct` (`id`),
  CONSTRAINT `FKp68rslba7686lvhswfpkhgf3f` FOREIGN KEY (`processingAction_id`) REFERENCES `ProcessingAction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProcessingActionPEF`;
CREATE TABLE `ProcessingActionPEF` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mandatory` bit(1) DEFAULT NULL,
  `requiredOnQuote` bit(1) DEFAULT NULL,
  `processingAction_id` bigint NOT NULL,
  `processingEvidenceField_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkvffte8ib2rstrrfov5h1q300` (`processingAction_id`),
  KEY `FKdg5qwnv5t6ismr544k2odqi07` (`processingEvidenceField_id`),
  CONSTRAINT `FKdg5qwnv5t6ismr544k2odqi07` FOREIGN KEY (`processingEvidenceField_id`) REFERENCES `ProcessingEvidenceField` (`id`),
  CONSTRAINT `FKkvffte8ib2rstrrfov5h1q300` FOREIGN KEY (`processingAction_id`) REFERENCES `ProcessingAction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProcessingActionPET`;
CREATE TABLE `ProcessingActionPET` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mandatory` bit(1) DEFAULT NULL,
  `requiredOnQuote` bit(1) DEFAULT NULL,
  `requiredOneOfGroupIdForQuote` varchar(255) DEFAULT NULL,
  `processingAction_id` bigint NOT NULL,
  `processingEvidenceType_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcm7qejvmwwudu002b7114rji0` (`processingAction_id`),
  KEY `FKt7i15u2grqh93v3w16fegkcd0` (`processingEvidenceType_id`),
  CONSTRAINT `FKcm7qejvmwwudu002b7114rji0` FOREIGN KEY (`processingAction_id`) REFERENCES `ProcessingAction` (`id`),
  CONSTRAINT `FKt7i15u2grqh93v3w16fegkcd0` FOREIGN KEY (`processingEvidenceType_id`) REFERENCES `ProcessingEvidenceType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProcessingActionTranslation`;
CREATE TABLE `ProcessingActionTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `language` enum('EN','ES') NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `processingAction_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXaas8e9sv5ikwlbjpskfbpa7ds` (`processingAction_id`,`language`,`name`),
  CONSTRAINT `FKcbbfjxd6hbggu5b718siionqg` FOREIGN KEY (`processingAction_id`) REFERENCES `ProcessingAction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProcessingActionValueChain`;
CREATE TABLE `ProcessingActionValueChain` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `processingAction_id` bigint NOT NULL,
  `valueChain_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbmpejepywtnf6wrr56mqhn4vt` (`processingAction_id`),
  KEY `FKiuxvh53qg2h5x4kr0i8ln40v9` (`valueChain_id`),
  CONSTRAINT `FKbmpejepywtnf6wrr56mqhn4vt` FOREIGN KEY (`processingAction_id`) REFERENCES `ProcessingAction` (`id`),
  CONSTRAINT `FKiuxvh53qg2h5x4kr0i8ln40v9` FOREIGN KEY (`valueChain_id`) REFERENCES `ValueChain` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProcessingEvidenceField`;
CREATE TABLE `ProcessingEvidenceField` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `fieldName` varchar(255) NOT NULL,
  `label` varchar(255) DEFAULT NULL,
  `type` enum('DATE','EXCHANGE_RATE','INTEGER','NUMBER','OBJECT','PRICE','STRING','TEXT','TIMESTAMP') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX3tvjox6jnv10x6cs6gycqp0op` (`label`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProcessingEvidenceFieldTranslation`;
CREATE TABLE `ProcessingEvidenceFieldTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `language` enum('EN','ES') NOT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  `processingEvidenceField_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKp5f7itfssgxk9o473ppken09e` (`processingEvidenceField_id`),
  CONSTRAINT `FKp5f7itfssgxk9o473ppken09e` FOREIGN KEY (`processingEvidenceField_id`) REFERENCES `ProcessingEvidenceField` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProcessingEvidenceType`;
CREATE TABLE `ProcessingEvidenceType` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `code` varchar(255) NOT NULL,
  `fairness` bit(1) DEFAULT NULL,
  `label` varchar(255) NOT NULL,
  `provenance` bit(1) DEFAULT NULL,
  `quality` bit(1) DEFAULT NULL,
  `type` enum('CALCULATED','DOCUMENT','FIELD') DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProcessingEvidenceTypeTranslation`;
CREATE TABLE `ProcessingEvidenceTypeTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `language` enum('EN','ES') NOT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `label` varchar(255) DEFAULT NULL,
  `processingEvidenceType_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjbfjeqv07ytvw1894v45kpo7k` (`processingEvidenceType_id`),
  CONSTRAINT `FKjbfjeqv07ytvw1894v45kpo7k` FOREIGN KEY (`processingEvidenceType_id`) REFERENCES `ProcessingEvidenceType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProcessingOrder`;
CREATE TABLE `ProcessingOrder` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `initiatorUserId` bigint DEFAULT NULL,
  `processingDate` date DEFAULT NULL,
  `processingAction_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKehm7yh2vt9pyyt84nps0y5ht1` (`processingAction_id`),
  CONSTRAINT `FKehm7yh2vt9pyyt84nps0y5ht1` FOREIGN KEY (`processingAction_id`) REFERENCES `ProcessingAction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `Product`;
CREATE TABLE `Product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `name` varchar(255) DEFAULT NULL,
  `originText` longtext,
  `entityVersion` bigint NOT NULL,
  `status` enum('ACTIVE','DISABLED') NOT NULL,
  `businessToCustomerSettings_id` bigint DEFAULT NULL,
  `journey_id` bigint DEFAULT NULL,
  `photo_id` bigint DEFAULT NULL,
  `process_id` bigint DEFAULT NULL,
  `responsibility_id` bigint DEFAULT NULL,
  `settings_id` bigint DEFAULT NULL,
  `sustainability_id` bigint DEFAULT NULL,
  `company_id` bigint NOT NULL,
  `valueChain_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKj2ukxung52qt9x25f7k3qw2ab` (`businessToCustomerSettings_id`),
  UNIQUE KEY `UKksf2l80gug6r236qvxnwhvfet` (`journey_id`),
  UNIQUE KEY `UK5laxscreq8nov9p5p8luj2dxw` (`process_id`),
  UNIQUE KEY `UKbfr0y0lplx807m7wogpf56m7w` (`responsibility_id`),
  UNIQUE KEY `UKfhwn6nrc1vkix4tyqfk45n4xv` (`settings_id`),
  UNIQUE KEY `UKj6k82yi4ovg5nof5jfdvj9or8` (`sustainability_id`),
  KEY `IDXgxubutkbk5o2a6aakbe7q9kww` (`name`),
  KEY `FKe2e5kw40uhrd3109q8hoj5crn` (`photo_id`),
  KEY `FKgqmlqsdx3a92mn4f5fr0c7mm6` (`company_id`),
  KEY `FKonsi6jg1xteevfxoi6n62wwnd` (`valueChain_id`),
  CONSTRAINT `FKcn4o73emn0h52j2nva4rteiby` FOREIGN KEY (`sustainability_id`) REFERENCES `Sustainability` (`id`),
  CONSTRAINT `FKe2e5kw40uhrd3109q8hoj5crn` FOREIGN KEY (`photo_id`) REFERENCES `Document` (`id`),
  CONSTRAINT `FKe3y2jt0njpw25mbqpexhgakfp` FOREIGN KEY (`businessToCustomerSettings_id`) REFERENCES `BusinessToCustomerSettings` (`id`),
  CONSTRAINT `FKgqmlqsdx3a92mn4f5fr0c7mm6` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`),
  CONSTRAINT `FKluwsxgwrwwkbbkqsm4d850ven` FOREIGN KEY (`journey_id`) REFERENCES `ProductJourney` (`id`),
  CONSTRAINT `FKn4yklv6n1rgy673t8mvrnrxem` FOREIGN KEY (`responsibility_id`) REFERENCES `Responsibility` (`id`),
  CONSTRAINT `FKonsi6jg1xteevfxoi6n62wwnd` FOREIGN KEY (`valueChain_id`) REFERENCES `ValueChain` (`id`),
  CONSTRAINT `FKsq8mv5bkm7u234ex7j18fxaqu` FOREIGN KEY (`settings_id`) REFERENCES `ProductSettings` (`id`),
  CONSTRAINT `FKsylrjatbji2vvpivyswu3nv0r` FOREIGN KEY (`process_id`) REFERENCES `Process` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductCompany`;
CREATE TABLE `ProductCompany` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` enum('ASSOCIATION','BUYER','EXPORTER','IMPORTER','OWNER','PROCESSOR','PRODUCER','ROASTER','TRADER') DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl8wtvs6muyjxu24kb75ybylx5` (`company_id`),
  KEY `FK1sth1qq1mlpr9172k30h937m2` (`product_id`),
  CONSTRAINT `FK1sth1qq1mlpr9172k30h937m2` FOREIGN KEY (`product_id`) REFERENCES `Product` (`id`),
  CONSTRAINT `FKl8wtvs6muyjxu24kb75ybylx5` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductDataSharingAgreement`;
CREATE TABLE `ProductDataSharingAgreement` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` varchar(255) DEFAULT NULL,
  `document_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbrv32h6qygwrkph15fitbot9w` (`document_id`),
  KEY `FKbutcvvnswgue59dtlyw4o8e3m` (`product_id`),
  CONSTRAINT `FKbrv32h6qygwrkph15fitbot9w` FOREIGN KEY (`document_id`) REFERENCES `Document` (`id`),
  CONSTRAINT `FKbutcvvnswgue59dtlyw4o8e3m` FOREIGN KEY (`product_id`) REFERENCES `Product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductJourney`;
CREATE TABLE `ProductJourney` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `markers` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductLabel`;
CREATE TABLE `ProductLabel` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `entityVersion` bigint NOT NULL,
  `language` enum('EN','ES') NOT NULL,
  `status` enum('PUBLISHED','UNPUBLISHED') NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `uuid` varchar(64) DEFAULT NULL,
  `content_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK9vtfk5t5n8kjs631k8thdxkai` (`uuid`),
  KEY `FKdjk0mx1d2s1580qiody246qvw` (`content_id`),
  KEY `FKn3ulw2udilgadyve3d182tew8` (`product_id`),
  CONSTRAINT `FKdjk0mx1d2s1580qiody246qvw` FOREIGN KEY (`content_id`) REFERENCES `ProductLabelContent` (`id`),
  CONSTRAINT `FKn3ulw2udilgadyve3d182tew8` FOREIGN KEY (`product_id`) REFERENCES `Product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductLabelBatch`;
CREATE TABLE `ProductLabelBatch` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `checkAuthenticity` bit(1) DEFAULT NULL,
  `entityVersion` bigint NOT NULL,
  `expiryDate` date DEFAULT NULL,
  `number` varchar(64) DEFAULT NULL,
  `productionDate` date DEFAULT NULL,
  `traceOrigin` bit(1) DEFAULT NULL,
  `label_id` bigint DEFAULT NULL,
  `photo_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKndev6922kprich7xrl7j7d652` (`label_id`,`number`),
  KEY `IDXetkl7cyo38glfu40vefrbfqmo` (`number`),
  KEY `FKj8oxrgt7slr9f2hkrr6qdl10p` (`photo_id`),
  CONSTRAINT `FK1csjhtfu983nw13gg93b63hvu` FOREIGN KEY (`label_id`) REFERENCES `ProductLabel` (`id`),
  CONSTRAINT `FKj8oxrgt7slr9f2hkrr6qdl10p` FOREIGN KEY (`photo_id`) REFERENCES `Document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductLabelCompanyDocument`;
CREATE TABLE `ProductLabelCompanyDocument` (
  `productLabelId` bigint NOT NULL,
  `companyDocumentId` bigint NOT NULL,
  PRIMARY KEY (`companyDocumentId`,`productLabelId`),
  KEY `FKptwas3tk4j0wqjrsfrwgetnwl` (`productLabelId`),
  CONSTRAINT `FKhm53uks8p24yrf5mv0qjvm432` FOREIGN KEY (`companyDocumentId`) REFERENCES `CompanyDocument` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKptwas3tk4j0wqjrsfrwgetnwl` FOREIGN KEY (`productLabelId`) REFERENCES `ProductLabel` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductLabelContent`;
CREATE TABLE `ProductLabelContent` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `name` varchar(255) DEFAULT NULL,
  `originText` longtext,
  `entityVersion` bigint NOT NULL,
  `businessToCustomerSettings_id` bigint DEFAULT NULL,
  `journey_id` bigint DEFAULT NULL,
  `photo_id` bigint DEFAULT NULL,
  `process_id` bigint DEFAULT NULL,
  `responsibility_id` bigint DEFAULT NULL,
  `settings_id` bigint DEFAULT NULL,
  `sustainability_id` bigint DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK61owam154pflopm6ypg2dh3ug` (`businessToCustomerSettings_id`),
  UNIQUE KEY `UKakbr1ygl1ch28dvvuaxoo6jn` (`journey_id`),
  UNIQUE KEY `UK9w0m7e8bhgksn81cxnbd2yna7` (`process_id`),
  UNIQUE KEY `UK6tmagq9250qqp5mgafe4tblfq` (`responsibility_id`),
  UNIQUE KEY `UKtlf54hv0vc8xobq8s44rqptk0` (`settings_id`),
  UNIQUE KEY `UKey0y4pa8xyd36ictevk20wfwk` (`sustainability_id`),
  KEY `FK4g938rxyuixeug0e5o0fgcevq` (`photo_id`),
  KEY `FKbdfylsr63yjdq22gjr5fk9cqt` (`company_id`),
  CONSTRAINT `FK1o4cf92nboed8jyamu3ifgvdh` FOREIGN KEY (`businessToCustomerSettings_id`) REFERENCES `BusinessToCustomerSettings` (`id`),
  CONSTRAINT `FK4g938rxyuixeug0e5o0fgcevq` FOREIGN KEY (`photo_id`) REFERENCES `Document` (`id`),
  CONSTRAINT `FK7hg994ujr4lt4qmk2i7ohqkke` FOREIGN KEY (`settings_id`) REFERENCES `ProductSettings` (`id`),
  CONSTRAINT `FKbdfylsr63yjdq22gjr5fk9cqt` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`),
  CONSTRAINT `FKhs7mjvu01745rkr4xwovsoequ` FOREIGN KEY (`responsibility_id`) REFERENCES `Responsibility` (`id`),
  CONSTRAINT `FKl3q28anaul4acukdgm7x1kjxd` FOREIGN KEY (`journey_id`) REFERENCES `ProductJourney` (`id`),
  CONSTRAINT `FKok98gsnbogntx0j5gavny1q35` FOREIGN KEY (`sustainability_id`) REFERENCES `Sustainability` (`id`),
  CONSTRAINT `FKrd70apnoah0q9utmjoo390m7g` FOREIGN KEY (`process_id`) REFERENCES `Process` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductLabelFeedback`;
CREATE TABLE `ProductLabelFeedback` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `feedback` longtext,
  `gdprConsent` bit(1) DEFAULT NULL,
  `privacyPolicyConsent` bit(1) DEFAULT NULL,
  `termsOfUseConsent` bit(1) DEFAULT NULL,
  `type` enum('COMPLAINT','PRAISE','PROPOSAL') DEFAULT NULL,
  `label_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKij8bq5eplvc8ly8ky5wef25ba` (`label_id`),
  CONSTRAINT `FKij8bq5eplvc8ly8ky5wef25ba` FOREIGN KEY (`label_id`) REFERENCES `ProductLabel` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductLabelFeedback_questionnaireAnswers`;
CREATE TABLE `ProductLabelFeedback_questionnaireAnswers` (
  `ProductLabelFeedback_id` bigint NOT NULL,
  `answer` varchar(255) DEFAULT NULL,
  `questionKey` varchar(255) NOT NULL,
  PRIMARY KEY (`ProductLabelFeedback_id`,`questionKey`),
  CONSTRAINT `FK9183rb3ifck3t3k56o6s44llo` FOREIGN KEY (`ProductLabelFeedback_id`) REFERENCES `ProductLabelFeedback` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductLabel_fields`;
CREATE TABLE `ProductLabel_fields` (
  `ProductLabel_id` bigint NOT NULL,
  `fields_name` varchar(255) DEFAULT NULL,
  `fields_section` varchar(255) DEFAULT NULL,
  `fields_visible` bit(1) DEFAULT NULL,
  `fields_ORDER` int NOT NULL,
  PRIMARY KEY (`ProductLabel_id`,`fields_ORDER`),
  CONSTRAINT `FK1yy8s1hb30og30a9fg7ap8gbk` FOREIGN KEY (`ProductLabel_id`) REFERENCES `ProductLabel` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductLocation`;
CREATE TABLE `ProductLocation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address_address` varchar(255) DEFAULT NULL,
  `address_cell` varchar(255) DEFAULT NULL,
  `address_city` varchar(255) DEFAULT NULL,
  `address_hondurasDepartment` varchar(255) DEFAULT NULL,
  `address_hondurasFarm` varchar(255) DEFAULT NULL,
  `address_hondurasMunicipality` varchar(255) DEFAULT NULL,
  `address_hondurasVillage` varchar(255) DEFAULT NULL,
  `address_otherAddress` varchar(1000) DEFAULT NULL,
  `address_sector` varchar(255) DEFAULT NULL,
  `address_state` varchar(255) DEFAULT NULL,
  `address_village` varchar(255) DEFAULT NULL,
  `address_zip` varchar(50) DEFAULT NULL,
  `entityVersion` bigint NOT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `numberOfFarmers` int DEFAULT NULL,
  `pinName` varchar(255) DEFAULT NULL,
  `address_country_id` bigint DEFAULT NULL,
  `content_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkirfhk5mx4ak0hs7wc74ntwl4` (`address_country_id`),
  KEY `FK8tao8cqd43mgghwrqsklj36yn` (`content_id`),
  KEY `FK4dpke2vdl5qsf0bk4htef6gd7` (`product_id`),
  CONSTRAINT `FK4dpke2vdl5qsf0bk4htef6gd7` FOREIGN KEY (`product_id`) REFERENCES `Product` (`id`),
  CONSTRAINT `FK8tao8cqd43mgghwrqsklj36yn` FOREIGN KEY (`content_id`) REFERENCES `ProductLabelContent` (`id`),
  CONSTRAINT `FKkirfhk5mx4ak0hs7wc74ntwl4` FOREIGN KEY (`address_country_id`) REFERENCES `Country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductOrder`;
CREATE TABLE `ProductOrder` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `deliveryDeadline` date NOT NULL,
  `orderId` varchar(255) DEFAULT NULL,
  `requiredOrganic` bit(1) DEFAULT NULL,
  `requiredWomensOnly` bit(1) DEFAULT NULL,
  `customer_id` bigint DEFAULT NULL,
  `facility_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK5bws3cmf2p0we5tvg2k961vow` (`customer_id`),
  KEY `FKncu91af8haiwa7h4nap56m8dc` (`facility_id`),
  CONSTRAINT `FK5bws3cmf2p0we5tvg2k961vow` FOREIGN KEY (`customer_id`) REFERENCES `CompanyCustomer` (`id`),
  CONSTRAINT `FKncu91af8haiwa7h4nap56m8dc` FOREIGN KEY (`facility_id`) REFERENCES `Facility` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductSettings`;
CREATE TABLE `ProductSettings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `costBreakdown` bit(1) DEFAULT NULL,
  `gdprText` longtext,
  `incomeIncreaseDescription` longtext,
  `language` enum('EN','ES') NOT NULL,
  `privacyPolicyText` longtext,
  `termsOfUseText` longtext,
  `incomeIncreaseDocument_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKe79ndlekldujaawt2chbqo0mf` (`incomeIncreaseDocument_id`),
  CONSTRAINT `FKe79ndlekldujaawt2chbqo0mf` FOREIGN KEY (`incomeIncreaseDocument_id`) REFERENCES `Document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductSettings_pricingTransparency`;
CREATE TABLE `ProductSettings_pricingTransparency` (
  `ProductSettings_id` bigint NOT NULL,
  `ptValue` double DEFAULT NULL,
  `ptKey` varchar(255) NOT NULL,
  PRIMARY KEY (`ProductSettings_id`,`ptKey`),
  CONSTRAINT `FKs8opebx2q1e5mmxsqlxgbahjd` FOREIGN KEY (`ProductSettings_id`) REFERENCES `ProductSettings` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductType`;
CREATE TABLE `ProductType` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ProductTypeTranslation`;
CREATE TABLE `ProductTypeTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `language` enum('EN','ES') NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `productType_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK12qoky74bjlnyc0jif8aq6yag` (`productType_id`),
  CONSTRAINT `FK12qoky74bjlnyc0jif8aq6yag` FOREIGN KEY (`productType_id`) REFERENCES `ProductType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `REVINFO`;
CREATE TABLE `REVINFO` (
  `REV` int NOT NULL AUTO_INCREMENT,
  `REVTSTMP` bigint DEFAULT NULL,
  PRIMARY KEY (`REV`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `RequestLog`;
CREATE TABLE `RequestLog` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `geoLocation_city` varchar(255) DEFAULT NULL,
  `geoLocation_country` varchar(100) DEFAULT NULL,
  `geoLocation_latitude` double DEFAULT NULL,
  `geoLocation_longitude` double DEFAULT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `logKey` varchar(64) DEFAULT NULL,
  `type` enum('CLICK_CERT_STD','CLICK_COMPANY_PAGE','CLICK_PROD_REC','CLICK_SOCIAL_MEDIA','CLICK_VERIFY_BATCH','CLICK_VERIFY_BATCH_ORIGIN','LANDING_PAGE','VERIFY_BATCH','VERIFY_BATCH_ORIGIN','VISIT_QR') DEFAULT NULL,
  `value1` varchar(255) DEFAULT NULL,
  `value2` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDXin6vvqt8h9o3tv14g7wc23o5x` (`creationTimestamp`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `Responsibility`;
CREATE TABLE `Responsibility` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `laborPolicies` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `SemiProduct`;
CREATE TABLE `SemiProduct` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `isBuyable` bit(1) DEFAULT NULL,
  `isSKU` bit(1) DEFAULT NULL,
  `isSKUEndCustomer` bit(1) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `measurementUnitType_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKppde4op6gkof0g1ux590e4qq5` (`measurementUnitType_id`),
  CONSTRAINT `FKppde4op6gkof0g1ux590e4qq5` FOREIGN KEY (`measurementUnitType_id`) REFERENCES `MeasureUnitType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `SemiProductTranslation`;
CREATE TABLE `SemiProductTranslation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `language` enum('EN','ES') NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `semiProduct_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKg7ujk5wv9ob8yikydyfufrpu9` (`semiProduct_id`),
  CONSTRAINT `FKg7ujk5wv9ob8yikydyfufrpu9` FOREIGN KEY (`semiProduct_id`) REFERENCES `SemiProduct` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `StockOrder`;
CREATE TABLE `StockOrder` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `availableQuantity` decimal(38,2) DEFAULT NULL,
  `balance` decimal(38,2) DEFAULT NULL,
  `comments` varchar(255) DEFAULT NULL,
  `cost` decimal(38,2) DEFAULT NULL,
  `creatorId` bigint DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `currencyForEndCustomer` varchar(255) DEFAULT NULL,
  `damagedPriceDeduction` decimal(38,2) DEFAULT NULL,
  `damagedWeightDeduction` decimal(38,2) DEFAULT NULL,
  `deliveryTime` date DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `fulfilledQuantity` decimal(38,2) DEFAULT NULL,
  `identifier` varchar(255) DEFAULT NULL,
  `internalLotNumber` varchar(255) DEFAULT NULL,
  `isAvailable` bit(1) DEFAULT NULL,
  `isOpenOrder` bit(1) DEFAULT NULL,
  `isPurchaseOrder` bit(1) DEFAULT NULL,
  `lotPrefix` varchar(255) DEFAULT NULL,
  `orderId` varchar(255) DEFAULT NULL,
  `orderType` enum('GENERAL_ORDER','PROCESSING_ORDER','PURCHASE_ORDER','TRANSFER_ORDER') DEFAULT NULL,
  `organic` bit(1) DEFAULT NULL,
  `outQuantityNotInRange` bit(1) DEFAULT NULL,
  `paid` decimal(38,2) DEFAULT NULL,
  `preferredWayOfPayment` enum('BANK_TRANSFER','CASH','CASH_VIA_COLLECTOR','CHEQUE','OFFSETTING','UNKNOWN') DEFAULT NULL,
  `priceDeterminedLater` bit(1) DEFAULT NULL,
  `pricePerUnit` decimal(38,2) DEFAULT NULL,
  `pricePerUnitForEndCustomer` decimal(38,2) DEFAULT NULL,
  `productionDate` date DEFAULT NULL,
  `qrCodeTag` varchar(255) DEFAULT NULL,
  `repackedOriginStockOrderId` varchar(255) DEFAULT NULL,
  `requiredWomensCoffee` bit(1) DEFAULT NULL,
  `sacNumber` int DEFAULT NULL,
  `tare` decimal(38,2) DEFAULT NULL,
  `totalGrossQuantity` decimal(38,2) DEFAULT NULL,
  `totalQuantity` decimal(38,2) DEFAULT NULL,
  `womenShare` bit(1) DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  `consumerCompanyCustomer_id` bigint DEFAULT NULL,
  `createdBy_id` bigint NOT NULL,
  `facility_id` bigint DEFAULT NULL,
  `finalProduct_id` bigint DEFAULT NULL,
  `measurementUnitType_id` bigint DEFAULT NULL,
  `processingOrder_id` bigint DEFAULT NULL,
  `producerUserCustomer_id` bigint DEFAULT NULL,
  `productOrder_id` bigint DEFAULT NULL,
  `productionLocation_id` bigint DEFAULT NULL,
  `qrCodeTagFinalProduct_id` bigint DEFAULT NULL,
  `quoteCompany_id` bigint DEFAULT NULL,
  `quoteFacility_id` bigint DEFAULT NULL,
  `representativeOfProducerUserCustomer_id` bigint DEFAULT NULL,
  `semiProduct_id` bigint DEFAULT NULL,
  `updatedBy_id` bigint DEFAULT NULL,
  `weekNumber` int DEFAULT NULL COMMENT 'Week number for cacao deliveries (1-53)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKlae52wbqdt21qdpy0h3igwi4g` (`consumerCompanyCustomer_id`),
  UNIQUE KEY `UK9cp009brrdyr1i1afpaac40kq` (`productionLocation_id`),
  KEY `FKagwb3fo2tj9eut9mm291q6xf8` (`company_id`),
  KEY `FKichgxiwjfyw2oyt9n3p2togm7` (`createdBy_id`),
  KEY `FK8qviw6rhyt2prrhli1xvtx9jm` (`facility_id`),
  KEY `FK1w8xtfkxsjiompkr6ef0qj1er` (`finalProduct_id`),
  KEY `FKhml4tgblny1qllfs52iihwlv7` (`measurementUnitType_id`),
  KEY `FKnmdr1vvvpt0plaqkmeoi4p4c0` (`processingOrder_id`),
  KEY `FKi94c6srcmf4e24xh3egn5xu8m` (`producerUserCustomer_id`),
  KEY `FKf17kfi346gs25kvh0bgeqdm4h` (`productOrder_id`),
  KEY `FKtcmi4vhn99kmw7ck8r4idau59` (`qrCodeTagFinalProduct_id`),
  KEY `FK8uqjby0ch6ur40bf0qvoi89yo` (`quoteCompany_id`),
  KEY `FKptkjcd0enpouqqqghx51qb3jh` (`quoteFacility_id`),
  KEY `FKq3miflleer2mjagvxoi4rc9sb` (`representativeOfProducerUserCustomer_id`),
  KEY `FK2nx2nl0nc3t1j0y2a7lcri77r` (`semiProduct_id`),
  KEY `FK8rnxf717vsgx5d2mbqkbjknsn` (`updatedBy_id`),
  KEY `idx_stock_order_week_number` (`weekNumber`),
  CONSTRAINT `FK1w8xtfkxsjiompkr6ef0qj1er` FOREIGN KEY (`finalProduct_id`) REFERENCES `FinalProduct` (`id`),
  CONSTRAINT `FK2nx2nl0nc3t1j0y2a7lcri77r` FOREIGN KEY (`semiProduct_id`) REFERENCES `SemiProduct` (`id`),
  CONSTRAINT `FK8qviw6rhyt2prrhli1xvtx9jm` FOREIGN KEY (`facility_id`) REFERENCES `Facility` (`id`),
  CONSTRAINT `FK8rnxf717vsgx5d2mbqkbjknsn` FOREIGN KEY (`updatedBy_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FK8uqjby0ch6ur40bf0qvoi89yo` FOREIGN KEY (`quoteCompany_id`) REFERENCES `Company` (`id`),
  CONSTRAINT `FKagwb3fo2tj9eut9mm291q6xf8` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`),
  CONSTRAINT `FKf17kfi346gs25kvh0bgeqdm4h` FOREIGN KEY (`productOrder_id`) REFERENCES `ProductOrder` (`id`),
  CONSTRAINT `FKhml4tgblny1qllfs52iihwlv7` FOREIGN KEY (`measurementUnitType_id`) REFERENCES `MeasureUnitType` (`id`),
  CONSTRAINT `FKi94c6srcmf4e24xh3egn5xu8m` FOREIGN KEY (`producerUserCustomer_id`) REFERENCES `UserCustomer` (`id`),
  CONSTRAINT `FKichgxiwjfyw2oyt9n3p2togm7` FOREIGN KEY (`createdBy_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FKn0wr1kysi3kksc68rgc2x64vm` FOREIGN KEY (`productionLocation_id`) REFERENCES `StockOrderLocation` (`id`),
  CONSTRAINT `FKnmdr1vvvpt0plaqkmeoi4p4c0` FOREIGN KEY (`processingOrder_id`) REFERENCES `ProcessingOrder` (`id`),
  CONSTRAINT `FKnyqxerfktpasiojdgb8f2d806` FOREIGN KEY (`consumerCompanyCustomer_id`) REFERENCES `CompanyCustomer` (`id`),
  CONSTRAINT `FKptkjcd0enpouqqqghx51qb3jh` FOREIGN KEY (`quoteFacility_id`) REFERENCES `Facility` (`id`),
  CONSTRAINT `FKq3miflleer2mjagvxoi4rc9sb` FOREIGN KEY (`representativeOfProducerUserCustomer_id`) REFERENCES `UserCustomer` (`id`),
  CONSTRAINT `FKtcmi4vhn99kmw7ck8r4idau59` FOREIGN KEY (`qrCodeTagFinalProduct_id`) REFERENCES `FinalProduct` (`id`),
  CONSTRAINT `chk_stock_order_week_number` CHECK (((`weekNumber` is null) or ((`weekNumber` >= 1) and (`weekNumber` <= 53))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `StockOrderActivityProof`;
CREATE TABLE `StockOrderActivityProof` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activityProof_id` bigint DEFAULT NULL,
  `stockOrder_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6n4519sebyu3h9nwpcxaubhmx` (`activityProof_id`),
  KEY `FKbnds9x997664q6unbjg7rx2gh` (`stockOrder_id`),
  CONSTRAINT `FKbnds9x997664q6unbjg7rx2gh` FOREIGN KEY (`stockOrder_id`) REFERENCES `StockOrder` (`id`),
  CONSTRAINT `FKmu82ets1wmxbf3h9bl0im1ych` FOREIGN KEY (`activityProof_id`) REFERENCES `ActivityProof` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `StockOrderLocation`;
CREATE TABLE `StockOrderLocation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address_address` varchar(255) DEFAULT NULL,
  `address_cell` varchar(255) DEFAULT NULL,
  `address_city` varchar(255) DEFAULT NULL,
  `address_hondurasDepartment` varchar(255) DEFAULT NULL,
  `address_hondurasFarm` varchar(255) DEFAULT NULL,
  `address_hondurasMunicipality` varchar(255) DEFAULT NULL,
  `address_hondurasVillage` varchar(255) DEFAULT NULL,
  `address_otherAddress` varchar(1000) DEFAULT NULL,
  `address_sector` varchar(255) DEFAULT NULL,
  `address_state` varchar(255) DEFAULT NULL,
  `address_village` varchar(255) DEFAULT NULL,
  `address_zip` varchar(50) DEFAULT NULL,
  `entityVersion` bigint NOT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `numberOfFarmers` int DEFAULT NULL,
  `pinName` varchar(255) DEFAULT NULL,
  `address_country_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfeklgqo1sjfn3snkpu9kb6v7p` (`address_country_id`),
  CONSTRAINT `FKfeklgqo1sjfn3snkpu9kb6v7p` FOREIGN KEY (`address_country_id`) REFERENCES `Country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `StockOrderPEFieldValue`;
CREATE TABLE `StockOrderPEFieldValue` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `booleanValue` bit(1) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `instantValue` datetime(6) DEFAULT NULL,
  `numericValue` decimal(38,2) DEFAULT NULL,
  `stringValue` varchar(255) DEFAULT NULL,
  `processingEvidenceField_id` bigint NOT NULL,
  `stockOrder_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsu77nf9u6gn6elvpluy71sys2` (`processingEvidenceField_id`),
  KEY `FK6jctmm1v20kvejkovp12u21wj` (`stockOrder_id`),
  CONSTRAINT `FK6jctmm1v20kvejkovp12u21wj` FOREIGN KEY (`stockOrder_id`) REFERENCES `StockOrder` (`id`),
  CONSTRAINT `FKsu77nf9u6gn6elvpluy71sys2` FOREIGN KEY (`processingEvidenceField_id`) REFERENCES `ProcessingEvidenceField` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `StockOrderPETypeValue`;
CREATE TABLE `StockOrderPETypeValue` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `date` datetime(6) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `otherEvidence` bit(1) DEFAULT NULL,
  `document_id` bigint DEFAULT NULL,
  `processingEvidenceType_id` bigint NOT NULL,
  `stockOrder_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnyfscqqrgy0g3g89fu52boexk` (`document_id`),
  KEY `FKkmqbs3wb284k8s7on8e5ffacm` (`processingEvidenceType_id`),
  KEY `FK4jk9t70quohsreomd57g2wn4o` (`stockOrder_id`),
  CONSTRAINT `FK4jk9t70quohsreomd57g2wn4o` FOREIGN KEY (`stockOrder_id`) REFERENCES `StockOrder` (`id`),
  CONSTRAINT `FKkmqbs3wb284k8s7on8e5ffacm` FOREIGN KEY (`processingEvidenceType_id`) REFERENCES `ProcessingEvidenceType` (`id`),
  CONSTRAINT `FKnyfscqqrgy0g3g89fu52boexk` FOREIGN KEY (`document_id`) REFERENCES `Document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `Sustainability`;
CREATE TABLE `Sustainability` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `co2Footprint` varchar(255) DEFAULT NULL,
  `packaging` longtext,
  `production` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `Transaction`;
CREATE TABLE `Transaction` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `currency` varchar(255) DEFAULT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `initiationUserId` bigint DEFAULT NULL,
  `inputQuantity` decimal(38,2) DEFAULT NULL,
  `isProcessing` bit(1) DEFAULT NULL,
  `outputQuantity` decimal(38,2) DEFAULT NULL,
  `pricePerUnit` decimal(38,2) DEFAULT NULL,
  `rejectComment` varchar(255) DEFAULT NULL,
  `shipmentId` bigint DEFAULT NULL,
  `status` enum('CANCELED','EXECUTED','PENDING') DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  `finalProduct_id` bigint DEFAULT NULL,
  `inputMeasureUnitType_id` bigint DEFAULT NULL,
  `semiProduct_id` bigint DEFAULT NULL,
  `sourceFacility_id` bigint DEFAULT NULL,
  `sourceStockOrder_id` bigint DEFAULT NULL,
  `targetProcessingOrder_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKssfgs8d8bq29ioykwx3t233dj` (`company_id`),
  KEY `FK9cyn45y43ch7xl21yicklihxf` (`finalProduct_id`),
  KEY `FK36oii80imeh1kq9gvpp605ncw` (`semiProduct_id`),
  KEY `FKcieuhct8rhk3xjpkrxt4syaci` (`sourceFacility_id`),
  KEY `FKpeijim9773wvffefx1t5pn9x1` (`targetProcessingOrder_id`),
  KEY `idx_transaction_input_measure_unit_type` (`inputMeasureUnitType_id`),
  KEY `idx_transaction_source_stock_order` (`sourceStockOrder_id`),
  CONSTRAINT `FK36oii80imeh1kq9gvpp605ncw` FOREIGN KEY (`semiProduct_id`) REFERENCES `SemiProduct` (`id`),
  CONSTRAINT `FK9cyn45y43ch7xl21yicklihxf` FOREIGN KEY (`finalProduct_id`) REFERENCES `FinalProduct` (`id`),
  CONSTRAINT `FK_transaction_input_measure_unit_type` FOREIGN KEY (`inputMeasureUnitType_id`) REFERENCES `MeasureUnitType` (`id`),
  CONSTRAINT `FK_transaction_source_stock_order` FOREIGN KEY (`sourceStockOrder_id`) REFERENCES `StockOrder` (`id`),
  CONSTRAINT `FKcieuhct8rhk3xjpkrxt4syaci` FOREIGN KEY (`sourceFacility_id`) REFERENCES `Facility` (`id`),
  CONSTRAINT `FKpeijim9773wvffefx1t5pn9x1` FOREIGN KEY (`targetProcessingOrder_id`) REFERENCES `ProcessingOrder` (`id`),
  CONSTRAINT `FKssfgs8d8bq29ioykwx3t233dj` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `User`;
CREATE TABLE `User` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `entityVersion` bigint NOT NULL,
  `language` enum('EN','ES') NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `role` enum('REGIONAL_ADMIN','SYSTEM_ADMIN','USER') DEFAULT NULL,
  `status` enum('ACTIVE','CONFIRMED_EMAIL','DEACTIVATED','UNCONFIRMED') NOT NULL,
  `surname` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKe6gkqunxajvyxl5uctpl2vl2p` (`email`),
  KEY `IDXl0h6hvscrlrnctuk3r39iax10` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `UserCustomer`;
CREATE TABLE `UserCustomer` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `bank_accountHolderName` varchar(255) DEFAULT NULL,
  `bank_accountNumber` varchar(255) DEFAULT NULL,
  `bank_additionalInformation` varchar(255) DEFAULT NULL,
  `bank_bankName` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `farm_areaOrganicCertified` decimal(38,2) DEFAULT NULL,
  `farm_areaUnit` varchar(255) DEFAULT NULL,
  `farm_organic` bit(1) DEFAULT NULL,
  `farm_startTransitionToOrganic` datetime(6) DEFAULT NULL,
  `farm_totalCultivatedArea` decimal(38,2) DEFAULT NULL,
  `farmerCompanyInternalId` varchar(255) DEFAULT NULL,
  `gender` enum('DIVERSE','FEMALE','MALE','N_A') DEFAULT NULL,
  `hasSmartphone` bit(1) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `surname` varchar(255) DEFAULT NULL,
  `type` enum('COLLECTOR','FARMER') DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  `userCustomerLocation_id` bigint DEFAULT NULL,
  `farm_maxProductionQuantity` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKqwylf4p2xfrh7c8xhge7evju0` (`userCustomerLocation_id`),
  KEY `FK1lypepdlf8t1bp36ux8gjx1sb` (`company_id`),
  KEY `FKh10e8fei3oj1ns2egwk9mjb9v` (`product_id`),
  CONSTRAINT `FK1lypepdlf8t1bp36ux8gjx1sb` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`),
  CONSTRAINT `FKh10e8fei3oj1ns2egwk9mjb9v` FOREIGN KEY (`product_id`) REFERENCES `Product` (`id`),
  CONSTRAINT `FKo4999sgsqb35aanjay9qbyw99` FOREIGN KEY (`userCustomerLocation_id`) REFERENCES `UserCustomerLocation` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `UserCustomerAssociation`;
CREATE TABLE `UserCustomerAssociation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint DEFAULT NULL,
  `userCustomer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK66kikp7i5erki6wwqcpa54eqw` (`company_id`),
  KEY `FK1h9bies0dad9uehx69f8eus0k` (`userCustomer_id`),
  CONSTRAINT `FK1h9bies0dad9uehx69f8eus0k` FOREIGN KEY (`userCustomer_id`) REFERENCES `UserCustomer` (`id`),
  CONSTRAINT `FK66kikp7i5erki6wwqcpa54eqw` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `UserCustomerCertification`;
CREATE TABLE `UserCustomerCertification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `description` longtext,
  `type` varchar(255) DEFAULT NULL,
  `validity` date DEFAULT NULL,
  `certificate_id` bigint DEFAULT NULL,
  `userCustomer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKonaa6lryyxoeyncw3oc00etdl` (`certificate_id`),
  KEY `FKhnrfyo1pt9pcap2kbnqjrway8` (`userCustomer_id`),
  CONSTRAINT `FKhnrfyo1pt9pcap2kbnqjrway8` FOREIGN KEY (`userCustomer_id`) REFERENCES `UserCustomer` (`id`),
  CONSTRAINT `FKonaa6lryyxoeyncw3oc00etdl` FOREIGN KEY (`certificate_id`) REFERENCES `Document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `UserCustomerCooperative`;
CREATE TABLE `UserCustomerCooperative` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role` tinyint DEFAULT NULL,
  `company_id` bigint DEFAULT NULL,
  `userCustomer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKra3lrhwa6cwto03lqdei43i3q` (`company_id`),
  KEY `FKmgbf31ad862648ycv82hn9plb` (`userCustomer_id`),
  CONSTRAINT `FKmgbf31ad862648ycv82hn9plb` FOREIGN KEY (`userCustomer_id`) REFERENCES `UserCustomer` (`id`),
  CONSTRAINT `FKra3lrhwa6cwto03lqdei43i3q` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `UserCustomerLocation`;
CREATE TABLE `UserCustomerLocation` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address_address` varchar(255) DEFAULT NULL,
  `address_cell` varchar(255) DEFAULT NULL,
  `address_city` varchar(255) DEFAULT NULL,
  `address_hondurasDepartment` varchar(255) DEFAULT NULL,
  `address_hondurasFarm` varchar(255) DEFAULT NULL,
  `address_hondurasMunicipality` varchar(255) DEFAULT NULL,
  `address_hondurasVillage` varchar(255) DEFAULT NULL,
  `address_otherAddress` varchar(1000) DEFAULT NULL,
  `address_sector` varchar(255) DEFAULT NULL,
  `address_state` varchar(255) DEFAULT NULL,
  `address_village` varchar(255) DEFAULT NULL,
  `address_zip` varchar(50) DEFAULT NULL,
  `entityVersion` bigint NOT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `numberOfFarmers` int DEFAULT NULL,
  `pinName` varchar(255) DEFAULT NULL,
  `isPubliclyVisible` bit(1) DEFAULT NULL,
  `address_country_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgqxj6jkxwi3gk0v917cv0ml67` (`address_country_id`),
  CONSTRAINT `FKgqxj6jkxwi3gk0v917cv0ml67` FOREIGN KEY (`address_country_id`) REFERENCES `Country` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `UserCustomerProductType`;
CREATE TABLE `UserCustomerProductType` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `productType_id` bigint DEFAULT NULL,
  `userCustomer_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKasr1pd8ve7udq5yqy1y1ytvnu` (`productType_id`),
  KEY `FKf2s52ftyav5sko9qnsdm02w5o` (`userCustomer_id`),
  CONSTRAINT `FKasr1pd8ve7udq5yqy1y1ytvnu` FOREIGN KEY (`productType_id`) REFERENCES `ProductType` (`id`),
  CONSTRAINT `FKf2s52ftyav5sko9qnsdm02w5o` FOREIGN KEY (`userCustomer_id`) REFERENCES `UserCustomer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `User_AUD`;
CREATE TABLE `User_AUD` (
  `id` bigint NOT NULL,
  `REV` int NOT NULL,
  `REVTYPE` tinyint DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `language` enum('EN','ES') DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `role` enum('REGIONAL_ADMIN','SYSTEM_ADMIN','USER') DEFAULT NULL,
  `status` enum('ACTIVE','CONFIRMED_EMAIL','DEACTIVATED','UNCONFIRMED') DEFAULT NULL,
  `surname` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`REV`,`id`),
  CONSTRAINT `FKilft2rdosb65jocpcoan7xnjq` FOREIGN KEY (`REV`) REFERENCES `REVINFO` (`REV`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ValueChain`;
CREATE TABLE `ValueChain` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `description` longtext NOT NULL,
  `entityVersion` bigint NOT NULL,
  `name` varchar(255) NOT NULL,
  `valueChainStatus` enum('DISABLED','ENABLED') NOT NULL,
  `createdBy_id` bigint NOT NULL,
  `productType_id` bigint NOT NULL,
  `updatedBy_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKikaj76lt1196kmhqat2nbtofy` (`createdBy_id`),
  KEY `FKbmtpdrafucwdq2y7cplshwvov` (`productType_id`),
  KEY `FK1952c58mq1ier9f3ndr71obp4` (`updatedBy_id`),
  CONSTRAINT `FK1952c58mq1ier9f3ndr71obp4` FOREIGN KEY (`updatedBy_id`) REFERENCES `User` (`id`),
  CONSTRAINT `FKbmtpdrafucwdq2y7cplshwvov` FOREIGN KEY (`productType_id`) REFERENCES `ProductType` (`id`),
  CONSTRAINT `FKikaj76lt1196kmhqat2nbtofy` FOREIGN KEY (`createdBy_id`) REFERENCES `User` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ValueChainFacilityType`;
CREATE TABLE `ValueChainFacilityType` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `facilityType_id` bigint NOT NULL,
  `valueChain_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKp4nh3wfjqp8grrrny232u2jkc` (`valueChain_id`,`facilityType_id`),
  KEY `FK8rw55gunmmjto4yhk6ukghx68` (`facilityType_id`),
  CONSTRAINT `FK8rw55gunmmjto4yhk6ukghx68` FOREIGN KEY (`facilityType_id`) REFERENCES `FacilityType` (`id`),
  CONSTRAINT `FKputjpv9rra9ysqy7d99irpbk7` FOREIGN KEY (`valueChain_id`) REFERENCES `ValueChain` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ValueChainMeasureUnitType`;
CREATE TABLE `ValueChainMeasureUnitType` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `measureUnitType_id` bigint NOT NULL,
  `valueChain_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKbkw7lhamgim4k6ef78x7nh50q` (`valueChain_id`,`measureUnitType_id`),
  KEY `FKarstmfdm297s71hby08y03x9r` (`measureUnitType_id`),
  CONSTRAINT `FKagwkk2ipq0u8mo96n2ghopby` FOREIGN KEY (`valueChain_id`) REFERENCES `ValueChain` (`id`),
  CONSTRAINT `FKarstmfdm297s71hby08y03x9r` FOREIGN KEY (`measureUnitType_id`) REFERENCES `MeasureUnitType` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ValueChainProcEvidenceType`;
CREATE TABLE `ValueChainProcEvidenceType` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `processingEvidenceType_id` bigint NOT NULL,
  `valueChain_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6odw4g6gvmlt1xvbjqyf0bh51` (`valueChain_id`,`processingEvidenceType_id`),
  KEY `FKg2tq35skluy6funye0gvskn1q` (`processingEvidenceType_id`),
  CONSTRAINT `FKg2tq35skluy6funye0gvskn1q` FOREIGN KEY (`processingEvidenceType_id`) REFERENCES `ProcessingEvidenceType` (`id`),
  CONSTRAINT `FKm8ahpk3a1gonwspwf8khsnpd7` FOREIGN KEY (`valueChain_id`) REFERENCES `ValueChain` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ValueChainProcessingEvidenceField`;
CREATE TABLE `ValueChainProcessingEvidenceField` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `processingEvidenceField_id` bigint NOT NULL,
  `valueChain_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6o5a8306ppost0mnskon3x9ox` (`processingEvidenceField_id`),
  KEY `FKa0ak0a42jvjf7jphuo1912i6s` (`valueChain_id`),
  CONSTRAINT `FK6o5a8306ppost0mnskon3x9ox` FOREIGN KEY (`processingEvidenceField_id`) REFERENCES `ProcessingEvidenceField` (`id`),
  CONSTRAINT `FKa0ak0a42jvjf7jphuo1912i6s` FOREIGN KEY (`valueChain_id`) REFERENCES `ValueChain` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `ValueChainSemiProduct`;
CREATE TABLE `ValueChainSemiProduct` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `semiProduct_id` bigint NOT NULL,
  `valueChain_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKlsuycp13s1d417cikemfdp0b7` (`valueChain_id`,`semiProduct_id`),
  KEY `FK509vur6rrf79cbr7tosmurh4l` (`semiProduct_id`),
  CONSTRAINT `FK509vur6rrf79cbr7tosmurh4l` FOREIGN KEY (`semiProduct_id`) REFERENCES `SemiProduct` (`id`),
  CONSTRAINT `FKqrjd1cghp33g1ndq85x1qi3a0` FOREIGN KEY (`valueChain_id`) REFERENCES `ValueChain` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
DROP TABLE IF EXISTS `company_processing_action`;
CREATE TABLE `company_processing_action` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `creationTimestamp` datetime(6) DEFAULT NULL,
  `updateTimestamp` datetime(6) DEFAULT NULL,
  `alias_label` varchar(255) DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `entityVersion` bigint DEFAULT NULL,
  `order_override` int DEFAULT NULL,
  `company_id` bigint NOT NULL,
  `processing_action_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_company_processing_action_company_enabled_order` (`company_id`,`enabled`,`order_override`),
  KEY `idx_company_processing_action_processing_action` (`processing_action_id`),
  CONSTRAINT `FKn7e1kocg7cvkeu24kcm1w0yn7` FOREIGN KEY (`company_id`) REFERENCES `Company` (`id`),
  CONSTRAINT `FKoe1c0yuuuyhfyfnfhwcgwoowa` FOREIGN KEY (`processing_action_id`) REFERENCES `ProcessingAction` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ═══════════════════════════════════════════════════════════════
-- Fin de V1__Initial_schema.sql
-- ═══════════════════════════════════════════════════════════════
