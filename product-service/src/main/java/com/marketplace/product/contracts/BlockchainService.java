package com.marketplace.product.contracts;

import com.marketplace.product.Entity.CartItem;
import com.marketplace.product.contracts.MarketplaceEscrow;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import io.quarkus.runtime.StartupEvent;
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
import java.math.BigInteger;
@ApplicationScoped
public class BlockchainService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockchainService.class);

    @Inject
    Web3jClientProducer web3jClientProducer;

    @ConfigProperty(name = "marketplace.contract.address")
    String contractAddress;

    private MarketplaceEscrow marketplaceEscrowContract;

    private static final BigInteger GAS_PRICE = BigInteger.valueOf(20_000_000_000L);
    private static final BigInteger GAS_LIMIT = BigInteger.valueOf(6_721_975L);
    private ContractGasProvider gasProvider = new StaticGasProvider(GAS_PRICE, GAS_LIMIT);

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("BlockchainService starting...");
        Web3j web3j = web3jClientProducer.getWeb3j();

        if (web3j != null && contractAddress != null && !contractAddress.isEmpty()) {
            TransactionManager transactionManager = new ClientTransactionManager(web3j, null);
            try {
                this.marketplaceEscrowContract = MarketplaceEscrow.load(
                        contractAddress,
                        web3j,
                        transactionManager,
                        gasProvider
                );
                LOGGER.info("MarketplaceEscrow contract loaded at address: {}", this.marketplaceEscrowContract.getContractAddress());
                subscribeToAllEvents();
            } catch (Exception e) {
                LOGGER.error("Failed to load MarketplaceEscrow contract: {}", e.getMessage(), e);
            }
        } else {
            LOGGER.error("Cannot load contract: Web3j instance or contract address is not available.");
        }
    }

    @Transactional
    protected void updateCartItemBlockchainState(String cartItemId, BigInteger blockchainTxId, String newState, String buyer, String seller, BigInteger blockchainAmount) {
        CartItem cartItem = CartItem.findById(new org.bson.types.ObjectId(cartItemId));
        if (cartItem != null) {
            if (blockchainTxId != null) cartItem.blockchainTransactionId = blockchainTxId;
            if (newState != null) cartItem.blockchainState = newState;
            if (buyer != null) cartItem.buyerEthAddress = buyer;
            if (seller != null) cartItem.sellerEthAddress = seller;
            if (blockchainAmount != null) cartItem.blockchainRegisteredAmount = blockchainAmount;
            cartItem.persistOrUpdate();
            LOGGER.info("CartItem {} (Blockchain TxId: {}) updated with state: {}", cartItemId, cartItem.blockchainTransactionId, newState);
        } else {
            LOGGER.warn("CartItem with ID {} not found for blockchain update (TxId: {}).", cartItemId, blockchainTxId);
        }
    }

    @Transactional
    protected void updateCartItemBlockchainState(BigInteger blockchainTxId, String newState, String paymentStatusUpdate) {
        CartItem cartItem = CartItem.find("blockchainTransactionId", blockchainTxId).firstResult();
        if (cartItem != null) {
            if (newState != null) cartItem.blockchainState = newState;
            if (paymentStatusUpdate != null) cartItem.paymentStatus = paymentStatusUpdate; // For payment related updates
            cartItem.persistOrUpdate();
            LOGGER.info("CartItem for Blockchain TxId {} updated with state: {}{}", blockchainTxId, newState, (paymentStatusUpdate != null ? " and payment status: " + paymentStatusUpdate : ""));
        } else {
            LOGGER.warn("CartItem with Blockchain TxId {} not found for update.", blockchainTxId);
        }
    }


    private void subscribeToAllEvents() {
        if (this.marketplaceEscrowContract == null) {
            LOGGER.warn("Contract not loaded, cannot subscribe to events.");
            return;
        }
        LOGGER.info("Subscribing to MarketplaceEscrow events...");

        marketplaceEscrowContract.transactionCreatedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: TransactionCreated");
                    LOGGER.info("  ItemID (CartItem ID): {}", event.itemId); // This is your CartItem.id.toString()
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Buyer: {}", event.buyer);
                    LOGGER.info("  Seller: {}", event.seller);
                    LOGGER.info("  Amount (Fiat Reference): {}", event.amount);

                    updateCartItemBlockchainState(event.itemId, event.transactionId, "CREATED", event.buyer, event.seller, event.amount);
                    // Potentially send notification to buyer/seller
                }, throwable -> LOGGER.error("Error processing TransactionCreated event: {}", throwable.getMessage(), throwable));

        marketplaceEscrowContract.fundsDepositedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: FundsDeposited (Symbolic Confirmation)");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Amount from Event (Fiat Reference or 0): {}", event.amount);

                    updateCartItemBlockchainState(event.transactionId, "FUNDED_SYMBOLICALLY", null);
                    // Notify seller: "Buyer has committed to the order."
                }, throwable -> LOGGER.error("Error processing FundsDeposited event: {}", throwable.getMessage(), throwable));

        marketplaceEscrowContract.itemSentEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: ItemSent");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Seller: {}", event.seller);

                    updateCartItemBlockchainState(event.transactionId, "ITEM_SENT", null);
                    // Notify buyer: "Your order has been shipped."
                }, throwable -> LOGGER.error("Error processing ItemSent event: {}", throwable.getMessage(), throwable));

        marketplaceEscrowContract.itemConfirmedByBuyerEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: ItemConfirmedByBuyer");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);

                    updateCartItemBlockchainState(event.transactionId, "COMPLETED", "PAYMENT_AUTHORIZED_OFFCHAIN");
                    // Notify seller: "Buyer confirmed receipt. Payment is authorized."
                    // Trigger off-chain payout to seller for the fiat amount.
                }, throwable -> LOGGER.error("Error processing ItemConfirmedByBuyer event: {}", throwable.getMessage(), throwable));

        marketplaceEscrowContract.fundsReleasedToSellerEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: FundsReleasedToSeller (Authorization Signal)");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Seller: {}", event.seller);
                    LOGGER.info("  Amount (Fiat Reference): {}", event.amount);
                    // This event is a bit redundant if ItemConfirmedByBuyer already triggers payout.
                    // But good for logging that the contract "thinks" funds were released.
                    // You might just log this or update a specific flag if needed.
                    // updateCartItemPaymentStatus(event.transactionId, "SELLER_PAYOUT_CONFIRMED_ONCHAIN_LOGIC");
                }, throwable -> LOGGER.error("Error processing FundsReleasedToSeller event: {}", throwable.getMessage(), throwable));

        marketplaceEscrowContract.platformFeeCollectedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: PlatformFeeCollected (Authorization Signal)");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Platform (Owner): {}", event.platform);
                    LOGGER.info("  Amount (Fiat Reference): {}", event.amount);
                    // Record this fiat fee amount for accounting.
                    // Example: feeLedgerService.recordPlatformFee(event.transactionId, event.amount);
                }, throwable -> LOGGER.error("Error processing PlatformFeeCollected event: {}", throwable.getMessage(), throwable));

        marketplaceEscrowContract.fundsRefundedToBuyerEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: FundsRefundedToBuyer (Authorization Signal)");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Buyer: {}", event.buyer);
                    LOGGER.info("  Amount (Fiat Reference): {}", event.amount);

                    updateCartItemBlockchainState(event.transactionId, "REFUNDED_AUTHORIZED", "REFUND_AUTHORIZED_OFFCHAIN");
                    // Trigger off-chain refund process to the buyer for the fiat amount.
                }, throwable -> LOGGER.error("Error processing FundsRefundedToBuyer event: {}", throwable.getMessage(), throwable));

        marketplaceEscrowContract.disputeOpenedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: DisputeOpened");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Requester (Buyer): {}", event.requester);

                    updateCartItemBlockchainState(event.transactionId, "DISPUTED", null);
                    // Notify admin/customer support.
                }, throwable -> LOGGER.error("Error processing DisputeOpened event: {}", throwable.getMessage(), throwable));

        marketplaceEscrowContract.disputeResolvedEventFlowable(DefaultBlockParameterName.LATEST, DefaultBlockParameterName.LATEST)
                .subscribe(event -> {
                    LOGGER.info("EVENT Received: DisputeResolved");
                    LOGGER.info("  Tx ID (Smart Contract): {}", event.transactionId);
                    LOGGER.info("  Refunded to Buyer (bool): {}", event.refundedToBuyer);
                    LOGGER.info("  Amount to Seller (Fiat Ref): {}", event.amountToSeller);
                    LOGGER.info("  Amount to Buyer (Fiat Ref): {}", event.amountToBuyer);

                    if (event.refundedToBuyer) {
                        updateCartItemBlockchainState(event.transactionId, "REFUNDED_AUTHORIZED", "REFUND_AUTHORIZED_OFFCHAIN_DISPUTE");
                        // Trigger off-chain refund process for event.amountToBuyer (fiat).
                    } else {
                        updateCartItemBlockchainState(event.transactionId, "COMPLETED", "PAYMENT_AUTHORIZED_OFFCHAIN_DISPUTE");
                        // Trigger off-chain payout to seller for event.amountToSeller (fiat).
                    }
                    // Notify relevant parties.
                }, throwable -> LOGGER.error("Error processing DisputeResolved event: {}", throwable.getMessage(), throwable));

        LOGGER.info("Successfully subscribed to all relevant MarketplaceEscrow events.");
    }

    // Your getTransactionTupleById and getTransactionInfoPojo methods remain the same
    // Just ensure they correctly interpret the amount/platformFee from the tuple as fiat.
    public Tuple7<BigInteger, String, String, BigInteger, BigInteger, BigInteger, String> getTransactionTupleById(BigInteger transactionId) throws Exception {
        if (this.marketplaceEscrowContract == null) {
            throw new IllegalStateException("Contract not loaded.");
        }
        return this.marketplaceEscrowContract.transactions(transactionId).send();
    }

    // Your TransactionInfo POJO remains the same
    public static class TransactionInfo {
        public BigInteger id;
        public String buyer;
        public String seller;
        public BigInteger amount; // Represents fiat value (e.g., cents)
        public BigInteger platformFee; // Represents fiat value (e.g., cents)
        public BigInteger state;
        public String itemId; // Corresponds to CartItem.id.toString()

        public TransactionInfo(BigInteger id, String buyer, String seller, BigInteger amount, BigInteger platformFee, BigInteger state, String itemId) {
            this.id = id; this.buyer = buyer; this.seller = seller; this.amount = amount;
            this.platformFee = platformFee; this.state = state; this.itemId = itemId;
        }
    }

    public TransactionInfo getTransactionInfoPojo(BigInteger transactionId) throws Exception {
        Tuple7<BigInteger, String, String, BigInteger, BigInteger, BigInteger, String> txTuple = getTransactionTupleById(transactionId);
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
