package com.marketplace.product.Controller;

import com.marketplace.product.Entity.ProceedOrder;
import com.marketplace.product.Service.BlockchainService;
import com.marketplace.product.Service.EmailService;
import com.marketplace.product.Service.ProceedOrderService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

@Path("/internal/orders")
public class OrderNotificationController {

    @Inject
    ProceedOrderService proceedOrderService;

    @Inject
    BlockchainService blockchainService;

    @Inject
    EmailService emailService;


    @POST
    @Path("/{orderId}/payment-confirmed")
    public Response onPaymentConfirmed(
            @PathParam("orderId") String mongoOrderId,
            @QueryParam("txId") String stripeTransactionId) {

        System.out.println("✅ REAL FLOW: Received payment confirmation for Mongo order ID: " + mongoOrderId);

        boolean dbUpdateSuccess = proceedOrderService.updateOrderStatusToPaid(mongoOrderId, stripeTransactionId);
        if (!dbUpdateSuccess) {
            return Response.status(Response.Status.NOT_FOUND).entity("Order not found in DB: " + mongoOrderId).build();
        }
        System.out.println("✅ MongoDB status updated to PAID for order " + mongoOrderId);

        ProceedOrder order = proceedOrderService.findByMongoId(mongoOrderId);
        if (order == null) {
            System.err.println("Could not retrieve full order details after update. Skipping email and blockchain steps.");
            return Response.ok("{\"status\":\"payment_processed_db_only\"}").build();
        }

        System.out.println("   -> Preparing to send confirmation email...");
        CompletableFuture.runAsync(() -> emailService.sendOrderConfirmationEmail(order))
                .exceptionally(ex -> {
                    System.err.println("FAILED to send email for order " + mongoOrderId + ": " + ex.getMessage());
                    return null;
                });


        try {
            if (order.blockchainOrderId == null) {
                throw new IllegalStateException("Order is missing a blockchainOrderId.");
            }

            long blockchainOrderId = order.blockchainOrderId;
            System.out.println("Triggering blockchain update using pre-generated numeric ID [" + blockchainOrderId + "]");

            String buyerAddress = "0xf39Fd6e51aad88F6F4ce6aB8827279cffFb92266";
            String sellerAddress = "0x70997970C51812dc3A010C7d01b50e0d17dc79C8";
            String itemId = order.products.isEmpty() ? "default-item" : order.products.get(0).getProduct().getId().toString();

            CompletableFuture<TransactionReceipt> futureReceipt = blockchainService.createAndPayOrderOnBlockchain(
                    BigInteger.valueOf(blockchainOrderId),
                    buyerAddress,
                    sellerAddress,
                    itemId,
                    stripeTransactionId
            );

            futureReceipt.thenAccept(receipt -> {
                System.out.println("✅ Blockchain transaction 'createAndPayOrder' confirmed! TxHash: " + receipt.getTransactionHash());
                proceedOrderService.updateBlockchainInfo(mongoOrderId, blockchainOrderId, receipt.getTransactionHash());
            }).exceptionally(ex -> {
                System.err.println("CRITICAL: Blockchain transaction 'createAndPayOrder' FAILED for order " + mongoOrderId + " !!!!!!!!!!");
                ex.printStackTrace();
                return null;
            });

            System.out.println("✅ REAL FLOW: 'createAndPayOrder' transaction has been sent (asynchronously). Responding OK.");
            return Response.ok("{\"status\":\"db_updated_email_and_blockchain_tx_sent\"}").build();

        } catch (Exception e) {
            System.err.println("CRITICAL: DB was updated, but failed to send blockchain transaction for order: " + mongoOrderId);
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
