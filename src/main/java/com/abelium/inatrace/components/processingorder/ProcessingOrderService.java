package com.abelium.inatrace.components.processingorder;

import com.abelium.inatrace.api.ApiBaseEntity;
import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.api.errors.ApiException;
import com.abelium.inatrace.components.common.BaseService;
import com.abelium.inatrace.components.company.CompanyQueries;
import com.abelium.inatrace.components.processingorder.api.ApiProcessingOrder;
import com.abelium.inatrace.components.processingorder.mappers.ProcessingOrderMapper;
import com.abelium.inatrace.components.stockorder.StockOrderService;
import com.abelium.inatrace.components.stockorder.api.ApiStockOrder;
import com.abelium.inatrace.components.transaction.TransactionService;
import com.abelium.inatrace.components.transaction.api.ApiTransaction;
import com.abelium.inatrace.db.entities.processingaction.ProcessingAction;
import com.abelium.inatrace.components.stockorder.api.ApiClassificationDetail;
import com.abelium.inatrace.db.entities.processingorder.ProcessingClassificationBatch;
import com.abelium.inatrace.db.entities.processingorder.ProcessingClassificationBatchDetail;
import com.abelium.inatrace.db.entities.processingorder.ProcessingOrder;
import com.abelium.inatrace.db.entities.product.FinalProduct;
import com.abelium.inatrace.db.entities.stockorder.StockOrder;
import com.abelium.inatrace.db.entities.stockorder.Transaction;
import com.abelium.inatrace.db.entities.stockorder.enums.OrderType;
import com.abelium.inatrace.db.entities.facility.Facility;
import com.abelium.inatrace.components.facility.api.ApiFacility;
import com.abelium.inatrace.db.entities.laboratory.LaboratoryAnalysis;
import com.abelium.inatrace.db.entities.laboratory.LaboratoryAnalysis.AnalysisType;
import com.abelium.inatrace.db.entities.common.User;
import com.abelium.inatrace.db.repositories.laboratory.LaboratoryAnalysisRepository;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.security.utils.PermissionsUtil;
import com.abelium.inatrace.tools.Queries;
import com.abelium.inatrace.types.Language;
import com.abelium.inatrace.types.ProcessingActionType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Lazy
@Service
public class ProcessingOrderService extends BaseService {

    private final StockOrderService stockOrderService;

    private final TransactionService transactionService;

    private final CompanyQueries companyQueries;

    private final ClassificationExcelService classificationExcelService;

    private final LaboratoryAnalysisRepository laboratoryAnalysisRepository;

    @Autowired
    public ProcessingOrderService(StockOrderService stockOrderService,
                                  TransactionService transactionService,
                                  CompanyQueries companyQueries,
                                  ClassificationExcelService classificationExcelService,
                                  LaboratoryAnalysisRepository laboratoryAnalysisRepository) {
        this.stockOrderService = stockOrderService;
        this.transactionService = transactionService;
        this.companyQueries = companyQueries;
        this.laboratoryAnalysisRepository = laboratoryAnalysisRepository;
        this.classificationExcelService = classificationExcelService;
    }

    public ApiProcessingOrder getProcessingOrder(Long id, CustomUserDetails authUser, Language language) throws ApiException {

        ProcessingOrder processingOrder = fetchEntity(id, ProcessingOrder.class);

        // Check if request user is enrolled in one of the connected companies with the processing order owner company
        PermissionsUtil.checkUserIfConnectedWithProducts(
                companyQueries.fetchCompanyProducts(processingOrder.getProcessingAction().getCompany().getId()),
                authUser);

        ApiProcessingOrder apiProcessingOrder = ProcessingOrderMapper.toApiProcessingOrder(processingOrder, language);
        
        // Populate laboratory analysis data for each target stock order (if exists)
        if (apiProcessingOrder.getTargetStockOrders() != null) {
            apiProcessingOrder.getTargetStockOrders().forEach(apiTargetStockOrder -> {
                if (apiTargetStockOrder.getId() != null) {
                    // Find laboratory analysis for this stock order
                    List<LaboratoryAnalysis> analyses = em.createQuery(
                            "SELECT la FROM LaboratoryAnalysis la WHERE la.stockOrder.id = :stockOrderId",
                            LaboratoryAnalysis.class)
                            .setParameter("stockOrderId", apiTargetStockOrder.getId())
                            .getResultList();
                    
                    if (!analyses.isEmpty()) {
                        // Populate lab analysis fields into the API stock order
                        com.abelium.inatrace.components.stockorder.mappers.StockOrderMapper
                                .populateLaboratoryAnalysisFields(apiTargetStockOrder, analyses.get(0));
                    }
                }
            });
        }
        
        return apiProcessingOrder;
    }

    /**
     * This is temporary method so that everything else stays backward compatible.
     * @param apiProcessingOrder - Processing order request
     * @param user - ID of the user that has requested this action
     * @return Status
     * @throws ApiException - You know... when something goes wrong it's nice to have a feedback
     */
    @Transactional
    public ApiBaseEntity createOrUpdateProcessingOrder(ApiProcessingOrder apiProcessingOrder, CustomUserDetails user, Language language) throws ApiException {

        ProcessingOrder entity = fetchEntityOrElse(apiProcessingOrder.getId(), ProcessingOrder.class, new ProcessingOrder());

        // Custom exceptions for required fields
        if (apiProcessingOrder.getProcessingAction() == null ||
                apiProcessingOrder.getProcessingAction().getId() == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Processing action ID must be provided!");
        }

        if (apiProcessingOrder.getTargetStockOrders() == null || apiProcessingOrder.getTargetStockOrders().isEmpty()) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "At least one target StockOrder has to be provided!");
        }

        ProcessingAction processingAction = fetchEntity(apiProcessingOrder.getProcessingAction().getId(), ProcessingAction.class);

        entity.setProcessingAction(processingAction);
        entity.setInitiatorUserId(apiProcessingOrder.getInitiatorUserId());
        entity.setProcessingDate(apiProcessingOrder.getProcessingDate() != null
                ? apiProcessingOrder.getProcessingDate()
                : LocalDate.now());

        /*
         * --- VALIDATION ---
         */

        // Various validations based on ProcessingActionType
        switch (processingAction.getType()) {

            // SHIPMENT is actually QUOTE
            case SHIPMENT:
                return createOrUpdateQuoteOrder(entity, apiProcessingOrder, user, language);

            case PROCESSING:
            case FINAL_PROCESSING:
            case GENERATE_QR_CODE:

                if (apiProcessingOrder.getInputTransactions() == null || apiProcessingOrder.getInputTransactions().isEmpty()) {
                    throw new ApiException(ApiStatus.INVALID_REQUEST, "At least one input Transaction has to be provided!");
                }

                // Validate that there is no transaction that is referencing current ProcessingOrder (via targetStockOrder)
                // That kind of transaction cannot be part of the same processing
                if (entity.getId() != null) {
                    for (ApiTransaction apiTransaction : apiProcessingOrder.getInputTransactions()) {

                        if (apiTransaction.getId() != null
                                && apiTransaction.getTargetStockOrder() != null
                                && entity.getId().equals(apiTransaction.getTargetStockOrder().getId()))

                            throw new ApiException(ApiStatus.VALIDATION_ERROR, "Transaction with ID '"
                                    + apiTransaction.getId()
                                    + "' cannot be used, because it has been already used in this processing step");
                    }
                }

                // In case we have 'GENERATE_QR_CODE', only one target stock order is permitted
                if (processingAction.getType() == ProcessingActionType.GENERATE_QR_CODE) {
                    if (apiProcessingOrder.getTargetStockOrders().size() > 1) {
                        throw new ApiException(ApiStatus.VALIDATION_ERROR,
                                "Only one target Stock order is permitted when ProcessingActionType is 'Generate QR code'");
                    }
                }

                break;

            case TRANSFER:

                if (apiProcessingOrder.getInputTransactions() == null || apiProcessingOrder.getInputTransactions().isEmpty()) {
                    throw new ApiException(ApiStatus.INVALID_REQUEST, "At least one input Transaction has to be provided!");
                }

                // When processing action is of type TRANSFER, source StockOrders (contained within each input Transaction)
                // will be mapped to target StockOrders, so their size should be the same.
                if (apiProcessingOrder.getInputTransactions().size() != apiProcessingOrder.getTargetStockOrders().size())
                    throw new ApiException(ApiStatus.INVALID_REQUEST, "When ProcessingActionType is 'TRANSFER', " +
                            "size of 'inputTransactions' has to match the size of 'targetStockOrders'!");

                // Source StockOrders from inputTransactions must match with targetStockOrders.
                // Target StockOrders represent new/modified source StockOrder.
                for (int i = 0; i < apiProcessingOrder.getInputTransactions().size(); i++) {

                    ApiStockOrder targetStockOrder = apiProcessingOrder.getTargetStockOrders().get(i);
                    ApiStockOrder sourceStockOrder = apiProcessingOrder.getInputTransactions().get(i).getSourceStockOrder();

                    // Each input Transaction must contain a source StockOrder
                    if (sourceStockOrder == null || sourceStockOrder.getId() == null)
                        throw new ApiException(ApiStatus.VALIDATION_ERROR,
                                "Each input Transaction must contain a valid source StockOrder.");

                    // If target StockOrder contains ID, then there must be matching source StockOrder
                    if (targetStockOrder.getId() != null && !targetStockOrder.getId().equals(sourceStockOrder.getId()))
                        throw new ApiException(ApiStatus.VALIDATION_ERROR,
                                "For each existing target StockOrder there has to be a source StockOrder with the same ID.");
                }
                break;

            default:
                throw new ApiException(ApiStatus.ERROR, "Selected processing action is missing a ProcessingActionType!");
        }

        // Validate transactions. They should all contain:
        // - Company ID
        // - Source StockOrder ID
        // - TransactionStatus
        boolean areTransactionsValid = apiProcessingOrder.getInputTransactions()
                .stream()
                .allMatch(it -> it.getCompany() != null
                        && it.getCompany().getId() != null
                        && it.getSourceStockOrder() != null
                        && it.getSourceStockOrder().getId() != null
                        && it.getStatus() != null
                );

        if (!areTransactionsValid) {
            throw new ApiException(ApiStatus.VALIDATION_ERROR, "At least one of the provided transactions is not valid!");
        }

        // Delete target StockOrder and input Transactions that are not present in the request
        // (applies only for existing ProcessingOrders)
        if (entity.getId() != null) {

            // Remove transactions that are not present in request
            List<Transaction> transactionsToBeDeleted = entity.getInputTransactions()
                    .stream()
                    .filter(transaction -> apiProcessingOrder.getInputTransactions()
                            .stream()
                            .noneMatch(apiTransaction -> transaction.getId().equals(apiTransaction.getId())))
                    .collect(Collectors.toList());

            for (Transaction t : transactionsToBeDeleted) {
                transactionService.deleteTransaction(t.getId(), user, language);
            }

            // Find target StockOrders that are not present in request
            List<StockOrder> targetStockOrdersToBeDeleted = entity.getTargetStockOrders()
                    .stream()
                    .filter(targetStockOrder -> apiProcessingOrder.getTargetStockOrders()
                            .stream()
                            .noneMatch(apiTargetStockOrder -> targetStockOrder.getId().equals(apiTargetStockOrder.getId())))
                    .collect(Collectors.toList());

            // Before deleting, verify that deletion is possible
            for (StockOrder targetStockOrder : targetStockOrdersToBeDeleted) {

                // Used quantity cannot be negative: "fulfilledQuantity - availableQuantity >= 0"
                if (targetStockOrder.getTotalQuantity().subtract(targetStockOrder.getAvailableQuantity())
                        .compareTo(BigDecimal.ZERO) < 0)
                    throw new ApiException(ApiStatus.VALIDATION_ERROR, "Target StockOrder with ID '"
                            + targetStockOrder.getId() + "' cannot be deleted!");
            }

            entity.getTargetStockOrders().removeAll(targetStockOrdersToBeDeleted);
        }

        // Verify that existing target StockOrders (in other words those with ID) can be updated
        for (StockOrder targetStockOrder : entity.getTargetStockOrders()) {
            ApiStockOrder apiTargetStockOrder = apiProcessingOrder.getTargetStockOrders()
                    .stream()
                    .filter(apiTSO -> targetStockOrder.getId().equals(apiTSO.getId()))
                    .findFirst()
                    .orElseThrow(() -> new ApiException(ApiStatus.ERROR,
                            "Inappropriate deletion of target StockOrders that are not present in request!"));

            // Total quantity - used quantity >= 0
            if (apiTargetStockOrder.getTotalQuantity().subtract(targetStockOrder.getTotalQuantity())
                    .compareTo(BigDecimal.ZERO) < 0) {
                BigDecimal usedQuantity = apiTargetStockOrder.getFulfilledQuantity()
                        .subtract(apiTargetStockOrder.getAvailableQuantity());
                if (targetStockOrder.getTotalQuantity().compareTo(usedQuantity) < 0)
                    throw new ApiException(ApiStatus.VALIDATION_ERROR, "Cannot reduce total quantity below already used quantity.");
            }
        }

        if (entity.getId() == null) {
            em.persist(entity);
        }

        Boolean isProcessing = processingAction.getType() != ProcessingActionType.TRANSFER;

        // Get the first input Transaction with generated QR code tag (if present)
        String presentQrCodeTag = findFirstQRCodeTagTransaction(apiProcessingOrder.getInputTransactions());
        FinalProduct qrCodeFinalProduct = null;

        // Create new or update existing input Transactions and validate conditions if there is an input Transaction with generated QR code tag
        for (int i = 0; i < apiProcessingOrder.getInputTransactions().size(); i++) {

            ApiTransaction apiTransaction = apiProcessingOrder.getInputTransactions().get(i);

            ApiBaseEntity insertedApiTransaction = transactionService.createOrUpdateTransaction(apiTransaction, isProcessing);
            Transaction insertedTransaction = fetchEntity(insertedApiTransaction.getId(), Transaction.class);
            insertedTransaction.setTargetProcessingOrder(entity);

            // If we have 'GENERATE_QR_CODE' verify that there no input transaction with generated
            // QR code tag (only transactions without QR code tag are permitted)
            if (processingAction.getType() == ProcessingActionType.GENERATE_QR_CODE &&
                    insertedTransaction.getSourceStockOrder().getQrCodeTag() != null) {
                throw new ApiException(ApiStatus.VALIDATION_ERROR,
                        "Generate QR code action cannot contain input transactions that have already generated QR code tag.");
            }

            // If we have input Transaction which has QR code tag generated, check that we have consistency
            // between all the input Transactions (all shall have the same QR code tag)
            qrCodeFinalProduct = validateQRCodeTag(presentQrCodeTag, insertedTransaction);

            entity.getInputTransactions().remove(insertedTransaction);
            entity.getInputTransactions().add(insertedTransaction);

            // Update source StockOrder
            if (apiTransaction.getId() == null) {
                stockOrderService.calculateQuantities(
                        apiTransaction.getSourceStockOrder(),
                        fetchEntity(apiTransaction.getSourceStockOrder().getId(), StockOrder.class),
                        entity,
                        insertedTransaction.getId()
                );
            }

            // Set targetStockOrders for TRANSFER
            if (processingAction.getType() == ProcessingActionType.TRANSFER) {

                ApiStockOrder apiTargetStockOrder = apiProcessingOrder.getTargetStockOrders().get(i);

                Long targetStockOrderId = stockOrderService.createOrUpdateStockOrder(apiTargetStockOrder, user, entity).getId();
                StockOrder targetStockOrder = fetchEntity(targetStockOrderId, StockOrder.class);
                targetStockOrder.setProcessingOrder(entity);

                // Transfer the QR code tag (if present) to the target Stock order
                targetStockOrder.setQrCodeTag(presentQrCodeTag);
                targetStockOrder.setQrCodeTagFinalProduct(qrCodeFinalProduct);

                // Create or update LaboratoryAnalysis if laboratory data is present
                createOrUpdateLaboratoryAnalysis(apiTargetStockOrder, targetStockOrder, user);

                // For TRANSFER, classification data should be associated with the SOURCE stock order
                // (which is in the classification facility), not the target stock order
                StockOrder sourceStockOrder = insertedTransaction.getSourceStockOrder();
                if (sourceStockOrder != null && sourceStockOrder.getFacility() != null &&
                    Boolean.TRUE.equals(sourceStockOrder.getFacility().getIsClassificationProcess())) {
                    // Associate classification data with the source stock order
                    createOrUpdateClassificationBatch(apiTargetStockOrder, sourceStockOrder);
                } else {
                    // Fallback: try to associate with target if it's a classification facility
                    createOrUpdateClassificationBatch(apiTargetStockOrder, targetStockOrder);
                }

                entity.getTargetStockOrders().add(targetStockOrder);
            }
        }

        // Create new or update existing target stock order for PROCESSING
        System.out.println("🦐 DEBUG: ProcessingAction type = " + processingAction.getType());
        System.out.println("🦐 DEBUG: Number of targetStockOrders = " + apiProcessingOrder.getTargetStockOrders().size());
        
        if (processingAction.getType() != ProcessingActionType.TRANSFER) {

            for (ApiStockOrder apiTargetStockOrder: apiProcessingOrder.getTargetStockOrders()) {
                
                System.out.println("🦐 DEBUG: Processing targetStockOrder - facility: " + 
                    (apiTargetStockOrder.getFacility() != null ? apiTargetStockOrder.getFacility().getName() : "null"));

                Long insertedTargetStockOrderId = stockOrderService.createOrUpdateStockOrder(apiTargetStockOrder, user, entity).getId();
                StockOrder targetStockOrder = fetchEntity(insertedTargetStockOrderId, StockOrder.class);
                targetStockOrder.setProcessingOrder(entity);

                // Transfer the QR code tag (if present) to the target Stock order
                if (presentQrCodeTag != null) {
                    targetStockOrder.setQrCodeTag(presentQrCodeTag);
                    targetStockOrder.setQrCodeTagFinalProduct(qrCodeFinalProduct);
                }

                // Create or update LaboratoryAnalysis if laboratory data is present
                createOrUpdateLaboratoryAnalysis(apiTargetStockOrder, targetStockOrder, user);

                // Create or update ClassificationBatch if classification data is present
                createOrUpdateClassificationBatch(apiTargetStockOrder, targetStockOrder);

                entity.getTargetStockOrders().add(targetStockOrder);

                // 🦐 Auto-create rejected output StockOrder if rejectedWeight > 0 and deheadingFacility is set
                System.out.println("🦐 DEBUG: About to call createRejectedOutputIfNeeded");
                StockOrder rejectedStockOrder = createRejectedOutputIfNeeded(apiTargetStockOrder, targetStockOrder, user, entity);
                if (rejectedStockOrder != null) {
                    System.out.println("🦐 DEBUG: Rejected StockOrder created, adding to entity");
                    entity.getTargetStockOrders().add(rejectedStockOrder);
                } else {
                    System.out.println("🦐 DEBUG: No rejected StockOrder created");
                }
            }
        } else {
            System.out.println("🦐 DEBUG: Skipping - ProcessingAction type is TRANSFER");
        }

        return new ApiBaseEntity(entity);
    }

    private ApiBaseEntity createOrUpdateQuoteOrder(ProcessingOrder entity, ApiProcessingOrder apiProcessingOrder, CustomUserDetails user, Language language) throws ApiException {

        // Validate that there is target Stock order provided
        if (apiProcessingOrder.getTargetStockOrders().size() != 1) {
            throw new ApiException(ApiStatus.INVALID_REQUEST,
                    "One target StockOrder should be present in the request.");
        }

        // Get the target Quote order
        ApiStockOrder apiQuoteStockOrder = apiProcessingOrder.getTargetStockOrders().get(0);

        // Validate that order ID is not provided if we have Product order present
        if (apiQuoteStockOrder.getProductOrder() != null && StringUtils.isNotBlank(apiQuoteStockOrder.getOrderId())) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Order ID cannot be provided when there is a Product ID present");
        }

        // Remove transactions that are not present in request
        if (entity.getId() != null) {
            List<Transaction> transactionsToBeDeleted = entity.getInputTransactions()
                    .stream()
                    .filter(transaction -> apiProcessingOrder.getInputTransactions()
                            .stream()
                            .noneMatch(apiTransaction -> transaction.getId().equals(apiTransaction.getId())))
                    .collect(Collectors.toList());

            entity.getInputTransactions().removeAll(transactionsToBeDeleted);
            for (Transaction t: transactionsToBeDeleted) {
                transactionService.deleteTransaction(t.getId(), user, language);
            }
        }

        // Get the first input Transaction with generated QR code tag (if present)
        String presentQrCodeTag = findFirstQRCodeTagTransaction(apiProcessingOrder.getInputTransactions());
        FinalProduct qrCodeFinalProduct = null;

        // Create new or update existing input Transactions
        for (int i = 0; i < apiProcessingOrder.getInputTransactions().size(); i++) {

            ApiTransaction apiTransaction = apiProcessingOrder.getInputTransactions().get(i);
            Long insertedTransactionId = transactionService.createOrUpdateTransaction(apiTransaction, false).getId();
            Transaction insertedTransaction = fetchEntity(insertedTransactionId, Transaction.class);

            // If we have input Transaction which has QR code tag generated, check that we have consistency
            // between all the input Transactions (all shall have the same QR code tag)
            qrCodeFinalProduct = validateQRCodeTag(presentQrCodeTag, insertedTransaction);

            entity.getInputTransactions().remove(insertedTransaction);
            entity.getInputTransactions().add(insertedTransaction);
            insertedTransaction.setTargetProcessingOrder(entity);

            // Update source StockOrder
            if (apiTransaction.getId() == null) {
                stockOrderService.calculateQuantities(
                        apiTransaction.getSourceStockOrder(),
                        fetchEntity(apiTransaction.getSourceStockOrder().getId(), StockOrder.class),
                        entity,
                        insertedTransaction.getId()
                );
            }
        }

        if (apiQuoteStockOrder.getOrderType() != OrderType.GENERAL_ORDER) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, "Order must be of orderType " +  OrderType.GENERAL_ORDER
                    + " to allow input transactions");
        }

        if (apiQuoteStockOrder.getId() == null) {
            if (apiQuoteStockOrder.getFulfilledQuantity() != null && apiQuoteStockOrder.getFulfilledQuantity().compareTo(BigDecimal.ZERO) != 0) {
                throw new ApiException(ApiStatus.INVALID_REQUEST, "Fulfilled quantity must be 0");
            }
        }

        Long insertedStockOrderId = stockOrderService.createOrUpdateQuoteStockOrder(apiQuoteStockOrder, user, entity).getId();
        StockOrder quoteStockOrder = fetchEntity(insertedStockOrderId, StockOrder.class);

        // Set the back reference for the Processing order
        quoteStockOrder.setProcessingOrder(entity);

        // Set the QR code tag data
        quoteStockOrder.setQrCodeTag(presentQrCodeTag);
        quoteStockOrder.setQrCodeTagFinalProduct(qrCodeFinalProduct);

        // Set the user entered Order ID
        quoteStockOrder.setOrderId(apiQuoteStockOrder.getOrderId());

        entity.setTargetStockOrders(Set.of(quoteStockOrder));

        if (entity.getId() == null) {
            em.persist(entity);
        }

        return new ApiBaseEntity(entity);
    }

    @Transactional
    public void deleteProcessingOrder(Long id, CustomUserDetails user) throws ApiException {

        // Transactions should not be deleted -> May result in inappropriate quantities

        ProcessingOrder entity = fetchEntity(id, ProcessingOrder.class);

        // Check if req. user is enrolled in owner company for this processing order
        PermissionsUtil.checkUserIfCompanyEnrolledAndAdminOrSystemAdmin(entity.getProcessingAction().getCompany().getUsers().stream().toList(), user);

        // Remove connected transactions
        for (Transaction t: entity.getInputTransactions()) {
            em.remove(t);
        }

        // Remove target stock orders
        for (StockOrder so: entity.getTargetStockOrders()) {
            em.remove(so);
        }

        em.remove(entity);
    }

    /**
     * Finds the first input Transaction which has QR code tag generated.
     *
     * @param inputTransactions List of input Transactions from which to find the first one with QR code tag.
     *
     * @return The QR code tag or null if there is no input Transaction with generated one.
     */
    private String findFirstQRCodeTagTransaction(List<ApiTransaction> inputTransactions) {

        return inputTransactions
                .stream()
                .filter(apiTransaction -> apiTransaction.getSourceStockOrder().getQrCodeTag() != null)
                .map(apiTransaction -> apiTransaction.getSourceStockOrder().getQrCodeTag())
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if the provided QR code tag is the same in the provided input Transaction.
     * When we have multiple input transactions and present QR code tag, all the present transaction should have the same QR code tag.
     *
     * @param qrCodeTag The present QR code tag.
     * @param transaction The input transaction to validate.
     *
     * @return The final product for which the QR code tag is generated.
     *
     * @throws ApiException If the input Transaction validation fails.
     */
    private FinalProduct validateQRCodeTag(String qrCodeTag, Transaction transaction) throws ApiException {

        if (qrCodeTag != null) {
            if (!qrCodeTag.equals(transaction.getSourceStockOrder().getQrCodeTag())) {
                throw new ApiException(ApiStatus.VALIDATION_ERROR, "There are input transactions present with different QR code tags.");
            } else {
                return transaction.getSourceStockOrder().getQrCodeTagFinalProduct();
            }
        }

        return null;
    }

    private <E> E fetchEntity(Long id, Class<E> entityClass) throws ApiException {

        E entity = Queries.get(em, entityClass, id);
        if (entity == null) {
            throw new ApiException(ApiStatus.INVALID_REQUEST,
                    entityClass.getSimpleName() + " entity with ID '" + id  + "' not found.");
        }
        return entity;
    }

    private <E> E fetchEntityOrElse(Long id, Class<E> entityClass, E defaultValue) {
        E entity = Queries.get(em, entityClass, id);
        return entity == null ? defaultValue : entity;
    }

    /**
     * Export classification batch as Excel "Liquidación de Pesca".
     * 
     * @param stockOrderId The target stock order ID (output from processing order)
     * @param authUser Authenticated user
     * @return Excel file as byte array
     * @throws ApiException if batch not found or permissions denied
     * @throws IOException if Excel generation fails
     */
    public byte[] exportClassificationLiquidacion(Long stockOrderId, CustomUserDetails authUser) 
            throws ApiException, IOException {

        // Fetch the stock order
        StockOrder stockOrder = fetchEntity(stockOrderId, StockOrder.class);

        // Check permissions
        PermissionsUtil.checkUserIfConnectedWithProducts(
                companyQueries.fetchCompanyProducts(stockOrder.getCompany().getId()),
                authUser);

        // Find the classification batch for this stock order
        TypedQuery<ProcessingClassificationBatch> query = em.createQuery(
                "SELECT b FROM ProcessingClassificationBatch b " +
                "LEFT JOIN FETCH b.details " +
                "WHERE b.targetStockOrder.id = :stockOrderId",
                ProcessingClassificationBatch.class);
        query.setParameter("stockOrderId", stockOrderId);

        ProcessingClassificationBatch batch;
        try {
            batch = query.getSingleResult();
        } catch (NoResultException e) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, 
                    "No classification batch found for stock order ID: " + stockOrderId);
        }

        // Generate Excel
        return classificationExcelService.generateLiquidacionExcel(batch);
    }

    /**
     * Generate Excel file for Purchase Settlement (Liquidación de Compra).
     * Includes pricing information and monetary totals.
     * 
     * @param stockOrderId ID of the target stock order
     * @return Excel file as byte array
     * @throws ApiException if stock order or batch not found
     * @throws IOException if file generation fails
     */
    public byte[] generateLiquidacionCompraExcel(Long stockOrderId) throws ApiException, IOException {
        // Find the classification batch for this stock order
        TypedQuery<ProcessingClassificationBatch> query = em.createQuery(
                "SELECT b FROM ProcessingClassificationBatch b WHERE b.targetStockOrder.id = :stockOrderId",
                ProcessingClassificationBatch.class);
        query.setParameter("stockOrderId", stockOrderId);

        ProcessingClassificationBatch batch;
        try {
            batch = query.getSingleResult();
        } catch (NoResultException e) {
            throw new ApiException(ApiStatus.INVALID_REQUEST, 
                    "No classification batch found for stock order ID: " + stockOrderId);
        }

        // Generate Excel (Purchase Settlement)
        return classificationExcelService.generateLiquidacionCompraExcel(batch);
    }

    /**
     * Create or update LaboratoryAnalysis for a target StockOrder if laboratory analysis data is present.
     * This method extracts sensorial analysis fields from ApiStockOrder and persists them in LaboratoryAnalysis table.
     * 
     * @param apiTargetStockOrder The API stock order containing laboratory analysis data
     * @param targetStockOrder The persisted StockOrder entity
     * @param user The current user performing the action
     */
    private void createOrUpdateLaboratoryAnalysis(ApiStockOrder apiTargetStockOrder, 
                                                   StockOrder targetStockOrder, 
                                                   CustomUserDetails user) {
        
        // Check if any laboratory analysis data is present in the API request
        boolean hasAnalysisData = apiTargetStockOrder.getSensorialRawOdor() != null
                || apiTargetStockOrder.getSensorialRawTaste() != null
                || apiTargetStockOrder.getSensorialRawColor() != null
                || apiTargetStockOrder.getSensorialCookedOdor() != null
                || apiTargetStockOrder.getSensorialCookedTaste() != null
                || apiTargetStockOrder.getSensorialCookedColor() != null
                || apiTargetStockOrder.getQualityNotes() != null
                || apiTargetStockOrder.getMetabisulfiteLevelAcceptable() != null
                || apiTargetStockOrder.getApprovedForPurchase() != null;

        if (!hasAnalysisData) {
            // No laboratory analysis data to persist
            return;
        }

        // Find existing analysis for this stock order or create a new one
        List<LaboratoryAnalysis> existingAnalyses = em.createQuery(
                "SELECT la FROM LaboratoryAnalysis la WHERE la.stockOrder.id = :stockOrderId",
                LaboratoryAnalysis.class)
                .setParameter("stockOrderId", targetStockOrder.getId())
                .getResultList();

        LaboratoryAnalysis analysis;
        if (!existingAnalyses.isEmpty()) {
            // Update existing analysis (take the first one if multiple exist)
            analysis = existingAnalyses.get(0);
            analysis.setUpdatedBy(fetchEntity(user.getUserId(), User.class));
        } else {
            // Create new analysis
            analysis = new LaboratoryAnalysis();
            analysis.setStockOrder(targetStockOrder);
            analysis.setCreatedBy(fetchEntity(user.getUserId(), User.class));
            analysis.setAnalysisType(AnalysisType.SENSORIAL);
            analysis.setAnalysisDate(targetStockOrder.getProductionDate() != null 
                    ? targetStockOrder.getProductionDate().atStartOfDay().toInstant(java.time.ZoneOffset.UTC)
                    : java.time.Instant.now());
        }

        // Map sensorial analysis fields from API to entity
        analysis.setSensorialRawOdor(apiTargetStockOrder.getSensorialRawOdor());
        analysis.setSensorialRawTaste(apiTargetStockOrder.getSensorialRawTaste());
        analysis.setSensorialRawColor(apiTargetStockOrder.getSensorialRawColor());
        analysis.setSensorialCookedOdor(apiTargetStockOrder.getSensorialCookedOdor());
        analysis.setSensorialCookedTaste(apiTargetStockOrder.getSensorialCookedTaste());
        analysis.setSensorialCookedColor(apiTargetStockOrder.getSensorialCookedColor());
        analysis.setQualityNotes(apiTargetStockOrder.getQualityNotes());
        analysis.setMetabisulfiteLevelAcceptable(apiTargetStockOrder.getMetabisulfiteLevelAcceptable());
        analysis.setApprovedForPurchase(apiTargetStockOrder.getApprovedForPurchase());

        // Persist the analysis
        if (existingAnalyses.isEmpty()) {
            em.persist(analysis);
        }
        // If updating, JPA dirty checking will handle the update automatically
    }

    /**
     * Create or update ProcessingClassificationBatch for a target StockOrder if classification data is present.
     * This method extracts classification fields from ApiStockOrder and persists them in ProcessingClassificationBatch 
     * and ProcessingClassificationBatchDetail tables.
     * 
     * @param apiTargetStockOrder The API stock order containing classification data
     * @param targetStockOrder The persisted StockOrder entity
     */
    private void createOrUpdateClassificationBatch(ApiStockOrder apiTargetStockOrder, 
                                                    StockOrder targetStockOrder) {
        
        // Check if facility is classification facility
        if (targetStockOrder.getFacility() == null || 
            !Boolean.TRUE.equals(targetStockOrder.getFacility().getIsClassificationProcess())) {
            // Not a classification facility, skip
            return;
        }

        // Check if any classification data is present in the API request
        boolean hasClassificationData = apiTargetStockOrder.getClassificationStartTime() != null
                || apiTargetStockOrder.getClassificationEndTime() != null
                || apiTargetStockOrder.getProductionOrder() != null
                || apiTargetStockOrder.getFreezingType() != null
                || apiTargetStockOrder.getMachine() != null
                || apiTargetStockOrder.getBrandHeader() != null
                || (apiTargetStockOrder.getClassificationDetails() != null && 
                    !apiTargetStockOrder.getClassificationDetails().isEmpty());

        if (!hasClassificationData) {
            // No classification data to persist
            return;
        }

        // Find existing batch for this stock order or create a new one
        TypedQuery<ProcessingClassificationBatch> query = em.createQuery(
                "SELECT b FROM ProcessingClassificationBatch b " +
                "WHERE b.targetStockOrder.id = :stockOrderId",
                ProcessingClassificationBatch.class);
        query.setParameter("stockOrderId", targetStockOrder.getId());

        ProcessingClassificationBatch batch;
        List<ProcessingClassificationBatch> existingBatches = query.getResultList();
        
        if (!existingBatches.isEmpty()) {
            // Update existing batch
            batch = existingBatches.get(0);
            // Clear existing details to replace with new ones
            batch.getDetails().clear();
        } else {
            // Create new batch
            batch = new ProcessingClassificationBatch();
            batch.setTargetStockOrder(targetStockOrder);
        }

        // Map classification header fields from API to entity
        batch.setStartTime(normalizeDateOrTime(apiTargetStockOrder.getClassificationStartTime()));
        batch.setEndTime(normalizeDateOrTime(apiTargetStockOrder.getClassificationEndTime()));
        batch.setProductionOrder(apiTargetStockOrder.getProductionOrder());
        batch.setFreezingType(apiTargetStockOrder.getFreezingType());
        batch.setMachine(apiTargetStockOrder.getMachine());
        batch.setBrandHeader(apiTargetStockOrder.getBrandHeader());

        // 🦐 Multi-output classification support
        batch.setOutputType(apiTargetStockOrder.getOutputType() != null 
                ? apiTargetStockOrder.getOutputType() 
                : "PROCESSED");
        batch.setPoundsRejected(apiTargetStockOrder.getPoundsRejected());
        // Note: rejectedStockOrder linkage is handled separately after both outputs are saved

        // Map classification details
        if (apiTargetStockOrder.getClassificationDetails() != null) {
            for (ApiClassificationDetail apiDetail : apiTargetStockOrder.getClassificationDetails()) {
                ProcessingClassificationBatchDetail detail = new ProcessingClassificationBatchDetail();
                detail.setBatch(batch);
                detail.setProcessType(apiDetail.getProcessType()); // 🦐 HEAD_ON o SHELL_ON
                detail.setBrandDetail(apiDetail.getBrandDetail());
                detail.setSize(apiDetail.getSize());
                detail.setBoxes(apiDetail.getBoxes());
                detail.setWeightPerBox(apiDetail.getWeightPerBox());
                detail.setWeightFormat(apiDetail.getWeightFormat());
                
                // 🦐 Campos de Liquidación de Pesca/Compra
                detail.setQualityGrade(apiDetail.getQualityGrade());
                detail.setPresentationType(apiDetail.getPresentationType());
                detail.setPricePerPound(apiDetail.getPricePerPound());
                detail.setLineTotal(apiDetail.getLineTotal());
                
                batch.getDetails().add(detail);
            }
        }

        // Persist the batch (cascade will handle details)
        if (existingBatches.isEmpty()) {
            em.persist(batch);
        }
        // If updating, JPA dirty checking will handle the update automatically
    }

    private String normalizeDateOrTime(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > 10) {
            return trimmed.substring(0, 10);
        }
        return trimmed;
    }

    /**
     * 🦐 Auto-create a rejected output StockOrder if rejectedWeight > 0 and deheadingFacility is set.
     * This implements the "Option B" approach where the backend automatically creates the second output
     * for rejected product that goes to the deheading facility.
     * 
     * @param apiPrimaryOutput The primary (PROCESSED) output from the API request
     * @param primaryStockOrder The persisted primary StockOrder entity
     * @param user The current user
     * @param processingOrder The parent ProcessingOrder entity
     * @return The created rejected StockOrder, or null if no rejected output is needed
     */
    private StockOrder createRejectedOutputIfNeeded(ApiStockOrder apiPrimaryOutput, 
                                                     StockOrder primaryStockOrder,
                                                     CustomUserDetails user,
                                                     ProcessingOrder processingOrder) throws ApiException {
        
        // Check if we need to create a rejected output
        BigDecimal rejectedWeight = apiPrimaryOutput.getRejectedWeight();
        ApiFacility deheadingFacility = apiPrimaryOutput.getDeheadingFacility();
        
        // 🦐 Debug log
        System.out.println("🦐 createRejectedOutputIfNeeded called:");
        System.out.println("  - rejectedWeight: " + rejectedWeight);
        System.out.println("  - deheadingFacility: " + (deheadingFacility != null ? deheadingFacility.getId() + " - " + deheadingFacility.getName() : "null"));
        System.out.println("  - primaryStockOrder.totalQuantity: " + primaryStockOrder.getTotalQuantity());
        
        if (rejectedWeight == null || rejectedWeight.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("  - Skipping: rejectedWeight is null or <= 0");
            return null;
        }
        
        if (deheadingFacility == null || deheadingFacility.getId() == null) {
            System.out.println("  - Skipping: deheadingFacility is null or has no ID");
            return null;
        }
        
        // Check if a rejected output already exists for this primary output
        // (to avoid creating duplicates on update)
        TypedQuery<ProcessingClassificationBatch> existingQuery = em.createQuery(
                "SELECT b FROM ProcessingClassificationBatch b " +
                "WHERE b.targetStockOrder.id = :stockOrderId AND b.outputType = 'PROCESSED'",
                ProcessingClassificationBatch.class);
        existingQuery.setParameter("stockOrderId", primaryStockOrder.getId());
        
        List<ProcessingClassificationBatch> existingBatches = existingQuery.getResultList();
        if (!existingBatches.isEmpty()) {
            ProcessingClassificationBatch primaryBatch = existingBatches.get(0);
            if (primaryBatch.getRejectedStockOrder() != null) {
                // Update existing rejected stock order instead of creating new one
                StockOrder existingRejected = primaryBatch.getRejectedStockOrder();
                existingRejected.setTotalQuantity(rejectedWeight);
                existingRejected.setAvailableQuantity(rejectedWeight);
                existingRejected.setFulfilledQuantity(rejectedWeight);
                
                // Update facility if changed
                Facility newFacility = fetchEntity(deheadingFacility.getId(), Facility.class);
                existingRejected.setFacility(newFacility);
                
                return null; // Already exists, no need to add to collection again
            }
        }
        
        // Create the rejected output StockOrder
        StockOrder rejectedStockOrder = new StockOrder();
        
        // Copy basic properties from primary output
        rejectedStockOrder.setCreatorId(primaryStockOrder.getCreatorId());
        rejectedStockOrder.setOrderType(OrderType.PROCESSING_ORDER);
        rejectedStockOrder.setProductionDate(primaryStockOrder.getProductionDate());
        rejectedStockOrder.setSemiProduct(primaryStockOrder.getSemiProduct());
        rejectedStockOrder.setMeasureUnitType(primaryStockOrder.getMeasureUnitType());
        rejectedStockOrder.setCompany(primaryStockOrder.getCompany());
        rejectedStockOrder.setProcessingOrder(processingOrder);
        
        // Set the deheading facility
        Facility facility = fetchEntity(deheadingFacility.getId(), Facility.class);
        rejectedStockOrder.setFacility(facility);
        
        // Set quantities
        rejectedStockOrder.setTotalQuantity(rejectedWeight);
        rejectedStockOrder.setAvailableQuantity(rejectedWeight);
        rejectedStockOrder.setFulfilledQuantity(rejectedWeight);
        
        // Generate internal lot number based on primary output
        String primaryLotNumber = primaryStockOrder.getInternalLotNumber();
        if (primaryLotNumber != null && !primaryLotNumber.isEmpty()) {
            rejectedStockOrder.setInternalLotNumber(primaryLotNumber + "-REJ");
        }
        
        // Persist the rejected stock order
        em.persist(rejectedStockOrder);
        
        // Create classification batch for rejected output
        ProcessingClassificationBatch rejectedBatch = new ProcessingClassificationBatch();
        rejectedBatch.setTargetStockOrder(rejectedStockOrder);
        rejectedBatch.setOutputType("REJECTED");
        rejectedBatch.setStartTime(normalizeDateOrTime(apiPrimaryOutput.getClassificationStartTime()));
        rejectedBatch.setEndTime(normalizeDateOrTime(apiPrimaryOutput.getClassificationEndTime()));
        em.persist(rejectedBatch);
        
        // Link the primary batch to the rejected stock order
        if (!existingBatches.isEmpty()) {
            ProcessingClassificationBatch primaryBatch = existingBatches.get(0);
            primaryBatch.setRejectedStockOrder(rejectedStockOrder);
            primaryBatch.setPoundsRejected(rejectedWeight);
        }
        
        return rejectedStockOrder;
    }

}
