package com.marketplace.product.Service;

import com.marketplace.productservice.contracts.OrderStatusTracker;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class BlockchainService {

    @ConfigProperty(name = "blockchain.rpc.url")
    String rpcUrl;

    @ConfigProperty(name = "blockchain.oracle.private-key")
    String privateKey;

    @ConfigProperty(name = "marketplace.contract.address")
    String contractAddress;

    private OrderStatusTracker contract;

    @PostConstruct
    void init() {
        System.out.println("======================================================");
        System.out.println("=== INITIALIZING BlockchainService... ===");
        System.out.println("RPC URL: " + this.rpcUrl);
        System.out.println("Contract Address: " + this.contractAddress);
        System.out.println("======================================================");

        // Crée la connexion au noeud Hardhat
        Web3j web3j = Web3j.build(new HttpService(this.rpcUrl));

        // Charge le portefeuille de notre Oracle à partir de sa clé privée
        Credentials credentials = Credentials.create(this.privateKey);

        // Utilise le fournisseur de "gas" par défaut, qui estime le coût automatiquement
        DefaultGasProvider gasProvider = new DefaultGasProvider();

        // Charge le contrat
        this.contract = OrderStatusTracker.load(this.contractAddress, web3j, credentials, gasProvider);

        // On vérifie que le contrat est bien chargé et qu'on peut communiquer avec lui
        try {
            boolean isValid = this.contract.isValid();
            if (isValid) {
                System.out.println("======> BlockchainService INITIALIZED. Contract connection is valid.");
            } else {
                System.err.println("!!!!!!!!!! CRITICAL: BlockchainService failed to connect to a valid contract at " + this.contractAddress);
            }
        } catch (Exception e) {
            System.err.println("!!!!!!!!!! CRITICAL: Exception during contract validation: " + e.getMessage());
        }
    }

    public CompletableFuture<TransactionReceipt> markOrderAsPaid(BigInteger numericOrderId) {

        System.out.println("===> [BlockchainService] Calling smart contract function 'markAsPaid' for numeric ID: " + numericOrderId);
        return this.contract.markAsPaid(numericOrderId).sendAsync();
    }

    public CompletableFuture<TransactionReceipt> markOrderAsShipped(BigInteger numericOrderId, String trackingNumber) {

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
}
