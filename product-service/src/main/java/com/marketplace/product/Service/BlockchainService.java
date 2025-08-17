package com.marketplace.product.Service;

import com.marketplace.productservice.contracts.OrderStatusTracker;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.web3j.abi.EventEncoder;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@ApplicationScoped
public class BlockchainService {

    @ConfigProperty(name = "blockchain.rpc.url")
    String rpcUrl;

    @ConfigProperty(name = "blockchain.oracle.private-key")
    String privateKey;

    @ConfigProperty(name = "marketplace.contract.address")
    String contractAddress;

    private OrderStatusTracker contract;
    private Web3j web3j; // Gardons une référence à web3j si nécessaire

    @PostConstruct
    void init() {
        System.out.println("======================================================");
        System.out.println("=== INITIALIZING BlockchainService... ===");
        System.out.println("RPC URL: " + this.rpcUrl);
        System.out.println("Contract Address: " + this.contractAddress);
        System.out.println("======================================================");

        // Crée la connexion au noeud Hardhat
        this.web3j = Web3j.build(new HttpService(this.rpcUrl));

        // Charge le portefeuille de notre Oracle à partir de sa clé privée
        Credentials credentials = Credentials.create(this.privateKey);

        System.out.println("   -> [DEBUG] Address derived from private key: " + credentials.getAddress());

        DefaultGasProvider gasProvider = new DefaultGasProvider();

        // Charge le contrat avec les credentials qui seront utilisés pour signer
        this.contract = OrderStatusTracker.load(this.contractAddress, this.web3j, credentials, gasProvider);

        System.out.println("======> BlockchainService INITIALIZED.");
    }

    public CompletableFuture<TransactionReceipt> createAndPayOrderOnBlockchain(
            BigInteger numericOrderId, String buyerAddress, String sellerAddress, String itemId, String stripeId) {

        System.out.println("===> [BlockchainService] Calling 'createAndPayOrder'...");
        return this.contract.createAndPayOrder(numericOrderId, buyerAddress, sellerAddress, itemId, stripeId).sendAsync();
    }

    public CompletableFuture<TransactionReceipt> markAsShipped(BigInteger numericOrderId, String trackingNumber) {
        System.out.println("===> [BlockchainService] Calling smart contract function 'markAsShipped' for numeric ID: " + numericOrderId);
        return this.contract.markAsShipped(numericOrderId, trackingNumber).sendAsync();
    }

    public CompletableFuture<TransactionReceipt> confirmOrderDelivered(BigInteger numericOrderId) {
        System.out.println("===> [BlockchainService] Calling smart contract function 'confirmDelivered' for numeric ID: " + numericOrderId);
        return this.contract.confirmDelivered(numericOrderId).sendAsync();
    }

    public CompletableFuture<OrderStatusTracker.Order> getOrderStateFromBlockchain(BigInteger numericOrderId) {
        System.out.println("===> [BlockchainService] Calling READ-ONLY function 'getOrder' for numeric ID: " + numericOrderId);
        return this.contract.getOrder(numericOrderId).sendAsync();
    }

    public CompletableFuture<TransactionReceipt> openDispute(BigInteger numericOrderId) {
        System.out.println("===> [BlockchainService] Calling 'openDispute' for numeric ID: " + numericOrderId);
        return this.contract.openDispute(numericOrderId).sendAsync();
    }

    public CompletableFuture<TransactionReceipt> resolveDispute(BigInteger numericOrderId, boolean wasRefunded) {
        System.out.println("===> [BlockchainService] Calling 'resolveDispute' for numeric ID: " + numericOrderId + " with refund: " + wasRefunded);
        return this.contract.resolveDispute(numericOrderId, wasRefunded).sendAsync();
    }

    public List<OrderStatusTracker.OrderCreatedEventResponse> getAllOrdersFromBlockchainEvents() throws Exception {
        System.out.println("===> [BlockchainService] Scanning blockchain for 'OrderCreated' events (SYNCHRONOUS)...");

        BigInteger latestBlockNumber = this.web3j.ethBlockNumber().send().getBlockNumber();
        BigInteger startBlock = latestBlockNumber.subtract(BigInteger.valueOf(10000));
        if (startBlock.compareTo(BigInteger.ZERO) < 0) {
            startBlock = BigInteger.ZERO;
        }
        DefaultBlockParameter fromBlock = DefaultBlockParameter.valueOf(startBlock);
        DefaultBlockParameter toBlock = DefaultBlockParameter.valueOf(latestBlockNumber);
        EthFilter filter = new EthFilter(fromBlock, toBlock, this.contract.getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OrderStatusTracker.ORDERCREATED_EVENT));

        List<EthLog.LogResult> rawLogs = this.web3j.ethGetLogs(filter).send().getLogs();
        System.out.println("   -> Found " + rawLogs.size() + " raw event logs.");

        List<Log> logs = rawLogs.stream()
                .map(logResult -> (Log) logResult.get())
                .collect(Collectors.toList());

        List<OrderStatusTracker.OrderCreatedEventResponse> decodedEvents = new ArrayList<>();

        for (Log log : logs) {
            decodedEvents.add(OrderStatusTracker.getOrderCreatedEventFromLog(log));
        }

        return decodedEvents;
    }

    public List<OrderStatusTracker.OrderStatusChangedEventResponse> getAllOrderStatusChanges() throws Exception {
        System.out.println("===> [BlockchainService] Scanning blockchain for all 'OrderStatusChanged' events...");

        BigInteger latestBlockNumber = this.web3j.ethBlockNumber().send().getBlockNumber();
        BigInteger startBlock = latestBlockNumber.subtract(BigInteger.valueOf(10000));
        if (startBlock.compareTo(BigInteger.ZERO) < 0) {
            startBlock = BigInteger.ZERO;
        }
        DefaultBlockParameter fromBlock = DefaultBlockParameter.valueOf(startBlock);
        DefaultBlockParameter toBlock = DefaultBlockParameter.valueOf(latestBlockNumber);

        EthFilter filter = new EthFilter(fromBlock, toBlock, this.contract.getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OrderStatusTracker.ORDERSTATUSCHANGED_EVENT)); // On utilise le bon événement

        List<Log> logs = this.web3j.ethGetLogs(filter).send().getLogs().stream()
                .map(logResult -> (Log) logResult.get())
                .collect(Collectors.toList());
        System.out.println("   -> Found " + logs.size() + " status change event logs.");

        List<OrderStatusTracker.OrderStatusChangedEventResponse> decodedEvents = new ArrayList<>();
        for (Log log : logs) {
            decodedEvents.add(OrderStatusTracker.getOrderStatusChangedEventFromLog(log)); // On utilise la bonne méthode de décodage
        }

        return decodedEvents;
    }
}
