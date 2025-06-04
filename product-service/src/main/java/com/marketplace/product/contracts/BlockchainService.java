package com.marketplace.product.contracts;

import com.marketplace.product.Entity.ProceedOrder;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import java.math.BigInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.tuples.generated.Tuple7;
import java.util.Date;

@ApplicationScoped
public class BlockchainService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockchainService.class);

    public BlockchainService() {
        LOGGER.info("BlockchainService: CONSTRUCTOR CALLED");
    }

    @Inject
    Web3jClientProducer web3jClientProducer;

    @ConfigProperty(name = "marketplace.contract.address")
    String contractAddress;

    private MarketplaceEscrow marketplaceEscrowContract;

    private static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L); // 20 Gwei
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(6_721_975L);
    private ContractGasProvider gasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);

    @PostConstruct
    void initializeService() {
        LOGGER.info("BlockchainService: PostConstruct - Initializing service...");
        Web3j web3j = web3jClientProducer.getWeb3j();

        if (contractAddress == null || contractAddress.isEmpty()) {
            LOGGER.error("BlockchainService: Configuration property 'marketplace.contract.address' is not set. Cannot load contract.");
            return;
        }
        if (web3j == null) {
            LOGGER.error("BlockchainService: Web3j instance is null (likely failed to initialize in Web3jClientProducer). Cannot load contract.");
            return;
        }

        // Now web3j should be non-null if producer initialized correctly
        TransactionManager transactionManager = new ClientTransactionManager(web3j, null);
        try {
            this.marketplaceEscrowContract = MarketplaceEscrow.load(
                    contractAddress,
                    web3j,
                    transactionManager,
                    gasProvider
            );
            // Verify contract is callable
            String loadedContractAddress = this.marketplaceEscrowContract.getContractAddress();
            LOGGER.info("BlockchainService: MarketplaceEscrow contract loaded successfully at address: {}", loadedContractAddress);
            if (!loadedContractAddress.equalsIgnoreCase(contractAddress)) {
                LOGGER.warn("BlockchainService: Loaded contract address {} MISMATCHES configured address {}!", loadedContractAddress, contractAddress);
            }
            subscribeToAllEvents();
        } catch (Exception e) {
            LOGGER.error("BlockchainService: Failed to load MarketplaceEscrow contract at address {}: {}", contractAddress, e.getMessage(), e);
        }
    }

    // Helper to update ProceedOrder based on backend Order ID (event.itemId)
    @Transactional
    protected void updateProceedOrderViaItemId(String backendOrderId, BigInteger blockchainTxIdFromEvent, String newState, String buyer, String seller, BigInteger blockchainAmountFromEvent) {
        LOGGER.info("[UPDATE_ITEM_ID] Received data: backendOrderId={}, blockchainTxIdFromEvent={}, newState={}, buyer={}, seller={}, blockchainAmountFromEvent={}",
                backendOrderId, blockchainTxIdFromEvent, newState, buyer, seller, blockchainAmountFromEvent);

        if (backendOrderId == null || backendOrderId.trim().isEmpty()) {
            LOGGER.error("[UPDATE_ITEM_ID] backendOrderId (event.itemId) is null or empty. Cannot find ProceedOrder.");
            return;
        }
        try {
            org.bson.types.ObjectId mongoObjectId = new org.bson.types.ObjectId(backendOrderId);
            LOGGER.info("[UPDATE_ITEM_ID] Attempting to find ProceedOrder with ObjectId: {}", mongoObjectId.toString());
            ProceedOrder order = ProceedOrder.findById(mongoObjectId);

            if (order != null) {
                LOGGER.info("[UPDATE_ITEM_ID] Found ProceedOrder: {}", order.id.toString());
                if (blockchainTxIdFromEvent != null) order.blockchainTransactionId = blockchainTxIdFromEvent.toString();
                if (newState != null) order.blockchainState = newState;
                if (buyer != null) order.buyerEthAddress = buyer;
                if (seller != null) order.sellerEthAddress = seller;
                if (blockchainAmountFromEvent != null) order.blockchainRegisteredAmount = blockchainAmountFromEvent.toString();
                order.lastBlockchainUpdate = new Date();

                LOGGER.info("[UPDATE_ITEM_ID] ProceedOrder before persist: blockchainTxId={}, blockchainState={}", order.blockchainTransactionId, order.blockchainState);
                order.persistOrUpdate();
                LOGGER.info("[UPDATE_ITEM_ID] Order (Backend ID: {}) (Blockchain TxId: {}) updated successfully with state: {}", backendOrderId, order.blockchainTransactionId, newState);
            } else {
                LOGGER.warn("[UPDATE_ITEM_ID] Order with Backend ID {} (ObjectId: {}) not found for blockchain update.", backendOrderId, mongoObjectId.toString());
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("[UPDATE_ITEM_ID] Invalid backendOrderId format for ObjectId: {}. Error: {}", backendOrderId, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("[UPDATE_ITEM_ID] Error updating ProceedOrder via ItemId: {}", e.getMessage(), e);
        }
    }

    // Helper to update ProceedOrder based on blockchainTransactionId
    @Transactional
    protected void updateProceedOrderViaBlockchainTxId(BigInteger blockchainTxId, String newState, String paymentStatusUpdate) {
        ProceedOrder order = ProceedOrder.find("blockchainTransactionId", blockchainTxId.toString()).firstResult();
        if (order != null) {
            if (newState != null) order.blockchainState = newState;
            // Ensure ProceedOrder entity has 'paymentStatus' field if you use this
            // if (paymentStatusUpdate != null) order.paymentStatus = paymentStatusUpdate;
            order.lastBlockchainUpdate = new Date();
            order.persistOrUpdate();
            LOGGER.info("Order for Blockchain TxId {} updated with state: {}{}", blockchainTxId, newState, (paymentStatusUpdate != null ? " and payment status: " + paymentStatusUpdate : ""));
        } else {
            LOGGER.warn("Order with Blockchain TxId {} not found for update.", blockchainTxId);
        }
    }

    private void subscribeToAllEvents() {
        if (this.marketplaceEscrowContract == null) {
            LOGGER.warn("BlockchainService: Contract not loaded, cannot subscribe to events.");
            return;
        }
        LOGGER.info("BlockchainService: Subscribing to MarketplaceEscrow events...");

        // TransactionCreated
        this.marketplaceEscrowContract.transactionCreatedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: TransactionCreated");
                    LOGGER.info("  ItemID (Backend Order ID): {}", event.itemId);
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Buyer: {}", event.buyer);
                    LOGGER.info("  Seller: {}", event.seller);
                    LOGGER.info("  Amount (Fiat Reference): {}", event.amount);
                    updateProceedOrderViaItemId(event.itemId, event.transactionId, "CREATED", event.buyer, event.seller, event.amount);
                }, throwable -> LOGGER.error("Error processing TransactionCreated event: {}", throwable.getMessage(), throwable));

        // FundsDeposited
        this.marketplaceEscrowContract.fundsDepositedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: FundsDeposited (Symbolic Confirmation)");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Amount from Event (Fiat Reference or 0): {}", event.amount);
                    updateProceedOrderViaBlockchainTxId(event.transactionId, "FUNDED_SYMBOLICALLY", null);
                }, throwable -> LOGGER.error("Error processing FundsDeposited event: {}", throwable.getMessage(), throwable));

        // ItemSent
        this.marketplaceEscrowContract.itemSentEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: ItemSent");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Seller: {}", event.seller);
                    updateProceedOrderViaBlockchainTxId(event.transactionId, "ITEM_SENT", null);
                }, throwable -> LOGGER.error("Error processing ItemSent event: {}", throwable.getMessage(), throwable));

        // ItemConfirmedByBuyer
        this.marketplaceEscrowContract.itemConfirmedByBuyerEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: ItemConfirmedByBuyer");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    updateProceedOrderViaBlockchainTxId(event.transactionId, "COMPLETED", "PAYMENT_AUTHORIZED_OFFCHAIN");
                }, throwable -> LOGGER.error("Error processing ItemConfirmedByBuyer event: {}", throwable.getMessage(), throwable));

        // FundsReleasedToSeller
        this.marketplaceEscrowContract.fundsReleasedToSellerEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: FundsReleasedToSeller (Authorization Signal)");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Seller: {}", event.seller);
                    LOGGER.info("  Amount (Fiat Reference): {}", event.amount);
                }, throwable -> LOGGER.error("Error processing FundsReleasedToSeller event: {}", throwable.getMessage(), throwable));

        // PlatformFeeCollected
        this.marketplaceEscrowContract.platformFeeCollectedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: PlatformFeeCollected (Authorization Signal)");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Platform (Owner): {}", event.platform);
                    LOGGER.info("  Amount (Fiat Reference): {}", event.amount);
                }, throwable -> LOGGER.error("Error processing PlatformFeeCollected event: {}", throwable.getMessage(), throwable));

        // FundsRefundedToBuyer
        this.marketplaceEscrowContract.fundsRefundedToBuyerEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: FundsRefundedToBuyer (Authorization Signal)");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Buyer: {}", event.buyer);
                    LOGGER.info("  Amount (Fiat Reference): {}", event.amount);
                    updateProceedOrderViaBlockchainTxId(event.transactionId, "REFUNDED_AUTHORIZED", "REFUND_AUTHORIZED_OFFCHAIN");
                }, throwable -> LOGGER.error("Error processing FundsRefundedToBuyer event: {}", throwable.getMessage(), throwable));

        // DisputeOpened
        this.marketplaceEscrowContract.disputeOpenedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: DisputeOpened");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Requester (Buyer): {}", event.requester);
                    updateProceedOrderViaBlockchainTxId(event.transactionId, "DISPUTED", null);
                }, throwable -> LOGGER.error("Error processing DisputeOpened event: {}", throwable.getMessage(), throwable));

        // DisputeResolved
        this.marketplaceEscrowContract.disputeResolvedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: DisputeResolved");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Refunded to Buyer (bool): {}", event.refundedToBuyer);
                    LOGGER.info("  Amount to Seller (Fiat Ref): {}", event.amountToSeller);
                    LOGGER.info("  Amount to Buyer (Fiat Ref): {}", event.amountToBuyer);
                    if (event.refundedToBuyer) {
                        updateProceedOrderViaBlockchainTxId(event.transactionId, "REFUNDED_AUTHORIZED", "REFUND_AUTHORIZED_OFFCHAIN_DISPUTE");
                    } else {
                        updateProceedOrderViaBlockchainTxId(event.transactionId, "COMPLETED", "PAYMENT_AUTHORIZED_OFFCHAIN_DISPUTE");
                    }
                }, throwable -> LOGGER.error("Error processing DisputeResolved event: {}", throwable.getMessage(), throwable));

        LOGGER.info("BlockchainService: Successfully subscribed to all relevant MarketplaceEscrow events.");
    }


    public Tuple7<BigInteger, String, String, BigInteger, BigInteger, BigInteger, String> getTransactionTupleById(BigInteger transactionId) throws Exception {
        if (this.marketplaceEscrowContract == null) {
            LOGGER.error("getTransactionTupleById called but contract is not loaded.");
            throw new IllegalStateException("Contract not loaded.");
        }
        return this.marketplaceEscrowContract.transactions(transactionId).send();
    }

    public static class TransactionInfo {
        public BigInteger id;
        public String buyer;
        public String seller;
        public BigInteger amount;
        public BigInteger platformFee;
        public BigInteger state;
        public String itemId;

        public TransactionInfo(BigInteger id, String buyer, String seller, BigInteger amount, BigInteger platformFee, BigInteger state, String itemId) {
            this.id = id; this.buyer = buyer; this.seller = seller; this.amount = amount;
            this.platformFee = platformFee; this.state = state; this.itemId = itemId;
        }
    }

    public TransactionInfo getTransactionInfoPojo(BigInteger smartContractTxId) throws Exception {
        Tuple7<BigInteger, String, String, BigInteger, BigInteger, BigInteger, String> txTuple = getTransactionTupleById(smartContractTxId);
        if (txTuple != null) {
            return new TransactionInfo(
                    txTuple.component1(), txTuple.component2(), txTuple.component3(),
                    txTuple.component4(), txTuple.component5(), txTuple.component6(),
                    txTuple.component7()
            );
        }
        return null;
    }
}
