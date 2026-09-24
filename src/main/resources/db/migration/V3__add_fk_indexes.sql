-- ============================================================================
-- V3__add_fk_indexes.sql — Foreign Key Indexes for Performance (ADR-014, HU-08)
-- Eradicate sequential scans on high-traffic tables:
-- plotcoordinate, stockorder, transaction, usercustomercertification, plot, payment
-- ============================================================================

-- Critical High-Traffic FK Indexes
CREATE INDEX IF NOT EXISTS idx_plotcoordinate_plot_id ON public.plotcoordinate USING btree (plot_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_company_id ON public.stockorder USING btree (company_id);
CREATE INDEX IF NOT EXISTS idx_transaction_company_id ON public.transaction USING btree (company_id);
CREATE INDEX IF NOT EXISTS idx_usercustomercertification_usercustomer_id ON public.usercustomercertification USING btree (usercustomer_id);
CREATE INDEX IF NOT EXISTS idx_usercustomercertification_certificate_id ON public.usercustomercertification USING btree (certificate_id);

-- Plot FK Indexes
CREATE INDEX IF NOT EXISTS idx_plot_farmer_id ON public.plot USING btree (farmer_id);
CREATE INDEX IF NOT EXISTS idx_plot_crop_id ON public.plot USING btree (crop_id);
CREATE INDEX IF NOT EXISTS idx_plot_certificationtype_id ON public.plot USING btree (certificationtype_id);

-- StockOrder FK Indexes
CREATE INDEX IF NOT EXISTS idx_stockorder_createdby_id ON public.stockorder USING btree (createdby_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_updatedby_id ON public.stockorder USING btree (updatedby_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_producerusercustomer_id ON public.stockorder USING btree (producerusercustomer_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_rep_producerusercustomer_id ON public.stockorder USING btree (representativeofproducerusercustomer_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_productionlocation_id ON public.stockorder USING btree (productionlocation_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_consumercompanycustomer_id ON public.stockorder USING btree (consumercompanycustomer_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_semiproduct_id ON public.stockorder USING btree (semiproduct_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_finalproduct_id ON public.stockorder USING btree (finalproduct_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_facility_id ON public.stockorder USING btree (facility_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_quotefacility_id ON public.stockorder USING btree (quotefacility_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_quotecompany_id ON public.stockorder USING btree (quotecompany_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_measurementunittype_id ON public.stockorder USING btree (measurementunittype_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_productorder_id ON public.stockorder USING btree (productorder_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_processingorder_id ON public.stockorder USING btree (processingorder_id);
CREATE INDEX IF NOT EXISTS idx_stockorder_qrcodetagfinalproduct_id ON public.stockorder USING btree (qrcodetagfinalproduct_id);

-- Payment FK Indexes
CREATE INDEX IF NOT EXISTS idx_payment_stockorder_id ON public.payment USING btree (stockorder_id);
CREATE INDEX IF NOT EXISTS idx_payment_createdby_id ON public.payment USING btree (createdby_id);
CREATE INDEX IF NOT EXISTS idx_payment_updatedby_id ON public.payment USING btree (updatedby_id);
CREATE INDEX IF NOT EXISTS idx_payment_payingcompany_id ON public.payment USING btree (payingcompany_id);
CREATE INDEX IF NOT EXISTS idx_payment_recipientcompany_id ON public.payment USING btree (recipientcompany_id);
CREATE INDEX IF NOT EXISTS idx_payment_recipientusercustomer_id ON public.payment USING btree (recipientusercustomer_id);
CREATE INDEX IF NOT EXISTS idx_payment_rep_recipientusercustomer_id ON public.payment USING btree (representativeofrecipientusercustomer_id);
CREATE INDEX IF NOT EXISTS idx_payment_receiptdocument_id ON public.payment USING btree (receiptdocument_id);
CREATE INDEX IF NOT EXISTS idx_payment_bulkpayment_id ON public.payment USING btree (bulkpayment_id);
CREATE INDEX IF NOT EXISTS idx_payment_confirmedbyuser_id ON public.payment USING btree (paymentconfirmedbyuser_id);
CREATE INDEX IF NOT EXISTS idx_payment_confirmedbycompany_id ON public.payment USING btree (paymentconfirmedbycompany_id);

-- Transaction FK Indexes
CREATE INDEX IF NOT EXISTS idx_transaction_sourcestockorder_id ON public.transaction USING btree (sourcestockorder_id);
CREATE INDEX IF NOT EXISTS idx_transaction_targetprocessingorder_id ON public.transaction USING btree (targetprocessingorder_id);
CREATE INDEX IF NOT EXISTS idx_transaction_sourcefacility_id ON public.transaction USING btree (sourcefacility_id);
CREATE INDEX IF NOT EXISTS idx_transaction_semiproduct_id ON public.transaction USING btree (semiproduct_id);
CREATE INDEX IF NOT EXISTS idx_transaction_finalproduct_id ON public.transaction USING btree (finalproduct_id);
CREATE INDEX IF NOT EXISTS idx_transaction_inputmeasureunittype_id ON public.transaction USING btree (inputmeasureunittype_id);

-- UserCustomer FK Indexes
CREATE INDEX IF NOT EXISTS idx_usercustomer_company_id ON public.usercustomer USING btree (company_id);
CREATE INDEX IF NOT EXISTS idx_usercustomer_product_id ON public.usercustomer USING btree (product_id);
CREATE INDEX IF NOT EXISTS idx_usercustomer_location_id ON public.usercustomer USING btree (usercustomerlocation_id);
CREATE INDEX IF NOT EXISTS idx_usercustomer_statusupdatedby_id ON public.usercustomer USING btree (statusupdatedby_id);

-- UserCustomer Relationships FK Indexes
CREATE INDEX IF NOT EXISTS idx_usercustomerassociation_usercustomer_id ON public.usercustomerassociation USING btree (usercustomer_id);
CREATE INDEX IF NOT EXISTS idx_usercustomerassociation_association_id ON public.usercustomerassociation USING btree (association_id);
CREATE INDEX IF NOT EXISTS idx_usercustomercooperative_usercustomer_id ON public.usercustomercooperative USING btree (usercustomer_id);
CREATE INDEX IF NOT EXISTS idx_usercustomercooperative_cooperative_id ON public.usercustomercooperative USING btree (cooperative_id);
CREATE INDEX IF NOT EXISTS idx_usercustomerproducttype_usercustomer_id ON public.usercustomerproducttype USING btree (usercustomer_id);
CREATE INDEX IF NOT EXISTS idx_usercustomerproducttype_producttype_id ON public.usercustomerproducttype USING btree (producttype_id);

-- Plot Deforestation Analysis
CREATE INDEX IF NOT EXISTS idx_plotdeforestationanalysis_plot ON public.plotdeforestationanalysis USING btree (plot_id);
