-- ============================================================================
-- V2__envers_audit_tables.sql — Hibernate Envers Audit Schema (ADR-008, HU-14)
-- Core Business Entities: StockOrder, Payment, Plot, Transaction, UserCustomer
-- ============================================================================

-- 1. Revision Information Sequence and Table
CREATE SEQUENCE IF NOT EXISTS public.revinfo_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS public.revinfo (
    rev integer NOT NULL,
    revtstmp bigint,
    username character varying(255),
    CONSTRAINT revinfo_pkey PRIMARY KEY (rev)
);

-- Ensure username column exists if revinfo was already present
ALTER TABLE public.revinfo ADD COLUMN IF NOT EXISTS username character varying(255);

-- 2. user_aud (ensure it exists and conforms to standard)
CREATE TABLE IF NOT EXISTS public.user_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    email character varying(255),
    language character varying(40),
    name character varying(255),
    role character varying(40),
    status character varying(40),
    surname character varying(255),
    CONSTRAINT user_aud_pkey PRIMARY KEY (rev, id),
    CONSTRAINT fk_user_aud_rev FOREIGN KEY (rev) REFERENCES public.revinfo(rev)
);
CREATE INDEX IF NOT EXISTS idx_user_aud_rev ON public.user_aud USING btree (rev);

-- 3. stockorder_aud
CREATE TABLE IF NOT EXISTS public.stockorder_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    creationtimestamp timestamp(6) with time zone,
    updatetimestamp timestamp(6) with time zone,
    availablequantity numeric(38,2),
    balance numeric(38,2),
    comments character varying(255),
    cost numeric(38,2),
    creatorid bigint,
    currency character varying(255),
    currencyforendcustomer character varying(255),
    damagedpricededuction numeric(38,2),
    damagedweightdeduction numeric(38,2),
    deliverytime date,
    finalpricediscount numeric(38,2),
    fulfilledquantity numeric(38,2),
    identifier character varying(255),
    internallotnumber character varying(255),
    isavailable boolean,
    isopenorder boolean,
    ispurchaseorder boolean,
    lotprefix character varying(255),
    moisturepercentage numeric(38,2),
    moistureweightdeduction numeric(38,2),
    netquantity numeric(38,2),
    orderid character varying(255),
    ordertype character varying(40),
    organic boolean,
    organiccertification character varying(255),
    outquantitynotinrange boolean,
    paid numeric(38,2),
    parcellot character varying(255),
    preferredwayofpayment character varying(40),
    pricedeterminedlater boolean,
    priceperunit numeric(38,2),
    priceperunitforendcustomer numeric(38,2),
    productiondate date,
    qrcodetag character varying(255),
    repackedoriginstockorderid character varying(255),
    requiredwomenscoffee boolean,
    sacnumber integer,
    tare numeric(38,2),
    totalgrossquantity numeric(38,2),
    totalquantity numeric(38,2),
    variety character varying(255),
    weeknumber integer,
    womenshare boolean,
    company_id bigint,
    consumercompanycustomer_id bigint,
    createdby_id bigint,
    facility_id bigint,
    finalproduct_id bigint,
    measurementunittype_id bigint,
    processingorder_id bigint,
    producerusercustomer_id bigint,
    productorder_id bigint,
    productionlocation_id bigint,
    qrcodetagfinalproduct_id bigint,
    quotecompany_id bigint,
    quotefacility_id bigint,
    representativeofproducerusercustomer_id bigint,
    semiproduct_id bigint,
    updatedby_id bigint,
    CONSTRAINT stockorder_aud_pkey PRIMARY KEY (rev, id),
    CONSTRAINT fk_stockorder_aud_rev FOREIGN KEY (rev) REFERENCES public.revinfo(rev)
);
CREATE INDEX IF NOT EXISTS idx_stockorder_aud_rev ON public.stockorder_aud USING btree (rev);

-- 4. payment_aud
CREATE TABLE IF NOT EXISTS public.payment_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    creationtimestamp timestamp(6) with time zone,
    updatetimestamp timestamp(6) with time zone,
    amount numeric(38,2),
    amountpaidtothecollector numeric(38,2),
    currency character varying(255),
    formalcreationtime date,
    orderreference character varying(255),
    paymentconfirmedattime timestamp(6) with time zone,
    paymentpurposetype character varying(40),
    paymentstatus character varying(40),
    paymenttype character varying(40),
    preferredwayofpayment character varying(40),
    productiondate date,
    purchased numeric(38,2),
    receiptdocumenttype character varying(40),
    receiptnumber character varying(255),
    recipienttype character varying(40),
    totalpaid numeric(38,2),
    bulkpayment_id bigint,
    createdby_id bigint,
    payingcompany_id bigint,
    paymentconfirmedbycompany_id bigint,
    paymentconfirmedbyuser_id bigint,
    receiptdocument_id bigint,
    recipientcompany_id bigint,
    recipientusercustomer_id bigint,
    representativeofrecipientusercustomer_id bigint,
    stockorder_id bigint,
    updatedby_id bigint,
    CONSTRAINT payment_aud_pkey PRIMARY KEY (rev, id),
    CONSTRAINT fk_payment_aud_rev FOREIGN KEY (rev) REFERENCES public.revinfo(rev)
);
CREATE INDEX IF NOT EXISTS idx_payment_aud_rev ON public.payment_aud USING btree (rev);

-- 5. plot_aud
CREATE TABLE IF NOT EXISTS public.plot_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    geoid character varying(255),
    lastupdated timestamp(6) without time zone,
    numberofplants integer,
    organicstartoftransition timestamp(6) without time zone,
    plotname character varying(255),
    productionestimate numeric(38,2),
    cocoavariety character varying(40),
    size double precision,
    unit character varying(255),
    certificationtype_id bigint,
    crop_id bigint,
    farmer_id bigint,
    CONSTRAINT plot_aud_pkey PRIMARY KEY (rev, id),
    CONSTRAINT fk_plot_aud_rev FOREIGN KEY (rev) REFERENCES public.revinfo(rev)
);
CREATE INDEX IF NOT EXISTS idx_plot_aud_rev ON public.plot_aud USING btree (rev);

-- 6. transaction_aud
CREATE TABLE IF NOT EXISTS public.transaction_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    creationtimestamp timestamp(6) with time zone,
    updatetimestamp timestamp(6) with time zone,
    currency character varying(255),
    initiationuserid bigint,
    inputquantity numeric(38,2),
    isprocessing boolean,
    outputquantity numeric(38,2),
    priceperunit numeric(38,2),
    rejectcomment character varying(255),
    shipmentid bigint,
    status character varying(40),
    company_id bigint,
    finalproduct_id bigint,
    inputmeasureunittype_id bigint,
    semiproduct_id bigint,
    sourcefacility_id bigint,
    sourcestockorder_id bigint,
    targetprocessingorder_id bigint,
    CONSTRAINT transaction_aud_pkey PRIMARY KEY (rev, id),
    CONSTRAINT fk_transaction_aud_rev FOREIGN KEY (rev) REFERENCES public.revinfo(rev)
);
CREATE INDEX IF NOT EXISTS idx_transaction_aud_rev ON public.transaction_aud USING btree (rev);

-- 7. usercustomer_aud
CREATE TABLE IF NOT EXISTS public.usercustomer_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    bank_accountholdername character varying(255),
    bank_accountnumber character varying(255),
    bank_additionalinformation character varying(255),
    bank_bankname character varying(255),
    companyname character varying(255),
    email character varying(255),
    farm_areaorganiccertified numeric(38,2),
    farm_areaunit character varying(255),
    farm_maxproductionquantity numeric(38,2),
    farm_organic boolean,
    farm_starttransitiontoorganic timestamp(6) without time zone,
    farm_totalcultivatedarea numeric(38,2),
    farmercompanyinternalid character varying(255),
    gender character varying(40),
    hassmartphone boolean,
    legalrepresentative character varying(255),
    location character varying(255),
    name character varying(255),
    persontype character varying(20),
    phone character varying(100),
    status character varying(40),
    statusreason character varying(255),
    statusupdatetimestamp timestamp(6) with time zone,
    surname character varying(255),
    type character varying(40),
    company_id bigint,
    product_id bigint,
    statusupdatedby_id bigint,
    usercustomerlocation_id bigint,
    CONSTRAINT usercustomer_aud_pkey PRIMARY KEY (rev, id),
    CONSTRAINT fk_usercustomer_aud_rev FOREIGN KEY (rev) REFERENCES public.revinfo(rev)
);
CREATE INDEX IF NOT EXISTS idx_usercustomer_aud_rev ON public.usercustomer_aud USING btree (rev);
