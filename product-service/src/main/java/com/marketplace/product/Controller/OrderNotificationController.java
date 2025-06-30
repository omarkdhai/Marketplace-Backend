package com.marketplace.product.Controller;

import com.marketplace.product.Service.BlockchainService;
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

    @POST
    @Path("/{orderId}/payment-confirmed")
    public Response onPaymentConfirmed(
            @PathParam("orderId") String mongoOrderId,
            @QueryParam("txId") String stripeTransactionId) {

        System.out.println("✅ REAL FLOW: Received payment confirmation for Mongo order ID: " + mongoOrderId);

        // Étape 1: Mettre à jour la base de données interne.
        boolean dbUpdateSuccess = proceedOrderService.updateOrderStatusToPaid(mongoOrderId, stripeTransactionId);
        if (!dbUpdateSuccess) {
            return Response.status(Response.Status.NOT_FOUND).entity("Order not found in DB: " + mongoOrderId).build();
        }
        System.out.println("✅ MongoDB status updated to PAID for order " + mongoOrderId);


        // Étape 2: Déclencher la mise à jour de la blockchain.
        try {
            long numericRepresentation = Math.abs((long) mongoOrderId.hashCode());
            System.out.println("Triggering blockchain update for numeric representation [" + numericRepresentation + "]");

            CompletableFuture<TransactionReceipt> futureReceipt = blockchainService.markOrderAsPaid(BigInteger.valueOf(numericRepresentation));

            // Étape 3 (Asynchrone): Une fois que la transaction est confirmée, on exécute ce code.
            futureReceipt.thenAccept(receipt -> {
                // Ce code s'exécute dans un autre thread, une fois que la blockchain a répondu.
                System.out.println("✅ Blockchain transaction confirmed! TxHash: " + receipt.getTransactionHash());
                // On sauvegarde le hash de la transaction dans notre base de données.
                proceedOrderService.updateBlockchainInfo(mongoOrderId, numericRepresentation, receipt.getTransactionHash());
            }).exceptionally(ex -> {
                // Ce code s'exécute si l'envoi à la blockchain échoue.
                System.err.println("!!!!!!!!!! CRITICAL: Blockchain transaction FAILED for order " + mongoOrderId + " !!!!!!!!!!");
                ex.printStackTrace();
                return null; // Il faut retourner une valeur par défaut dans exceptionally.
            });

            // IMPORTANT: On répond IMMÉDIATEMENT au payment-service.
            // On ne bloque pas la réponse en attendant que la transaction soit minée.
            System.out.println("✅ REAL FLOW: Blockchain update has been sent (asynchronously). Responding OK to payment-service.");
            return Response.ok("{\"status\":\"db_updated_and_blockchain_tx_sent\"}").build();

        } catch (Exception e) {
            System.err.println("CRITICAL: DB was updated, but failed to send blockchain transaction for order: " + mongoOrderId);
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
