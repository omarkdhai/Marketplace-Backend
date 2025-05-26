package com.marketplace.product.contracts;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Web3jClientProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Web3jClientProducer.class);

    @ConfigProperty(name = "blockchain.rpc.url")
    String rpcUrl;

    private Web3j web3j;

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("Attempting to connect to Ethereum node at: {}", rpcUrl);
        try {
            this.web3j = Web3j.build(new HttpService(rpcUrl));
            String clientVersion = this.web3j.web3ClientVersion().send().getWeb3ClientVersion();
            LOGGER.info("Successfully connected to Ethereum client: {}", clientVersion);
        } catch (Exception e) {
            LOGGER.error("Failed to connect to Ethereum client at {}: {}", rpcUrl, e.getMessage());
        }
    }

    // Method to get the web3j instance (or make web3j field accessible)
    public Web3j getWeb3j() {
        if (this.web3j == null) {
            // Attempt to initialize if not already (e.g., if onStart wasn't called or failed)
            // Or throw an IllegalStateException
            LOGGER.warn("Web3j instance requested but not yet initialized!");
        }
        return this.web3j;
    }
}
