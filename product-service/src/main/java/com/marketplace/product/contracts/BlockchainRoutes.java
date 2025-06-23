package com.marketplace.product.contracts;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.web3j.Web3jConstants;

@ApplicationScoped
public class BlockchainRoutes extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("direct:markAsPaid")
                .routeId("mark-order-as-paid")
                .log("Blockchain Route: Received request to mark order ID [${body}] as PAID.")

                // Call the smart contract
                .setHeader(Web3jConstants.OPERATION, constant("markAsPaid"))

                // Send transaction
                .toD("web3j://{{marketplace.contract.address}}?operation=sendTransaction&gasLimit=500000")
                .log("Blockchain transaction sent successfully for order ID [${body}]. Transaction Receipt: ${body}");

        // Table of routes : [0] = orderId, [1] = trackingNumber
        from("direct:markAsShipped")
                .routeId("mark-order-as-shipped")
                .log("Blockchain Route: Received request to mark order ID [${body[0]}] as SHIPPED with tracking [${body[1]}].")
                .setHeader(Web3jConstants.OPERATION, constant("markAsShipped"))
                .toD("web3j://{{marketplace.contract.address}}?operation=sendTransaction")
                .log("Blockchain transaction for SHIPPED status sent successfully.");

    }
}
