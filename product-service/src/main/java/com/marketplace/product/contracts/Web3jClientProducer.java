package com.marketplace.product.contracts;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import okhttp3.OkHttpClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class Web3jClientProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Web3jClientProducer.class);

    public Web3jClientProducer() {
        LOGGER.info("Web3jClientProducer: CONSTRUCTOR CALLED");
    }

    @ConfigProperty(name = "blockchain.rpc.url")
    String rpcUrl;

    private Web3j web3j;

    @PostConstruct
    void initializeClient() {
        LOGGER.info("Web3jClientProducer: @PostConstruct initializeClient() ENTERED.");
        if (rpcUrl == null || rpcUrl.isEmpty()) {
            LOGGER.error("Web3jClientProducer: Configuration property 'blockchain.rpc.url' is not set. Cannot initialize Web3j client.");
            return;
        }

        try {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            HttpService httpService = new HttpService(rpcUrl, okHttpClient, true);
            this.web3j = Web3j.build(httpService);

            String clientVersion = this.web3j.web3ClientVersion().send().getWeb3ClientVersion();
            LOGGER.info("Web3jClientProducer: Successfully connected to Ethereum client [{}]. Client version: {}", rpcUrl, clientVersion);
        } catch (Exception e) {
            LOGGER.error("Web3jClientProducer: Failed to connect to Ethereum client at [{}]: {}", rpcUrl, e.getMessage(), e);
        }
    }
    public Transaction getLastTransaction() throws IOException {
        // Get latest block
        EthBlock latestBlock = web3j.ethGetBlockByNumber(
                org.web3j.protocol.core.DefaultBlockParameterName.LATEST,
                true
        ).send();

        List<EthBlock.TransactionResult> transactions = latestBlock.getBlock().getTransactions();
        if (transactions.isEmpty()) {
            return null;
        }

        // Get the last transaction in the block
        EthBlock.TransactionObject tx = (EthBlock.TransactionObject) transactions.get(transactions.size() - 1).get();
        return tx;
    }
    public Transaction getTransactionByHash(String txHash) throws IOException {
        return web3j.ethGetTransactionByHash(txHash)
                .send()
                .getTransaction()
                .orElse(null);
    }

    public Web3j getWeb3j() {
        try {
            if (this.web3j == null || this.web3j.web3ClientVersion().send().hasError()) {
                LOGGER.warn("Web3jClientProducer: Web3j is null or connection broken. Reinitializing...");
                initializeClient();
            }
        } catch (IOException e) {
            LOGGER.error("Web3jClientProducer: Error checking Web3j status: {}", e.getMessage());
            initializeClient();
        }
        return this.web3j;
    }



    @PreDestroy
    void shutdownClient() {
        LOGGER.info("Web3jClientProducer: Shutting down Web3j client...");
        if (this.web3j != null) {
            this.web3j.shutdown(); // Gracefully close resources used by Web3j
            LOGGER.info("Web3jClientProducer: Web3j client has been shut down.");
        }
    }
}
