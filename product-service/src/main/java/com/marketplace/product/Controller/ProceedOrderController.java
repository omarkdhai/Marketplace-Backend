package com.marketplace.product.Controller;

import com.marketplace.product.Clients.CreatePaymentResponse;
import com.marketplace.product.DTO.BlockchainOrderStateDTO;
import com.marketplace.product.DTO.PaymentConfirmationRequest;
import com.marketplace.product.DTO.ProceedOrderDTO;
import com.marketplace.product.Entity.ProceedOrder;
import com.marketplace.product.Service.BlockchainService;
import com.marketplace.product.Service.ProceedOrderService;
import com.marketplace.productservice.contracts.OrderStatusTracker;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Path("/api/v1/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProceedOrderController {

    @Inject
    ProceedOrderService service;

    @Inject
    BlockchainService blockchainService;


    @POST
    public Response submitOrder(ProceedOrderDTO dto) {
        try {
            CreatePaymentResponse paymentResponse = service.saveAndInitiatePayment(dto);
            return Response.status(Response.Status.CREATED).entity(paymentResponse).build();
        } catch (Exception e) {
            System.err.println("Error during order submission and payment initiation: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PATCH
    @Path("/{id}/fulfill")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_PLAIN)
    public Response fulfillOrder(@PathParam("id") String orderId) {
        try {
            service.fulfillOrder(orderId);
            return Response.ok("Order " + orderId + " marked as fulfilled.").build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to fulfill order.").build();
        }
    }

    @GET
    public List<ProceedOrder> getAllOrders() {
        return service.getAllOrders();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteOrder(@PathParam("id") String id) {
        boolean deleted = service.deleteOrder(id);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Order not found").build();
        }
    }


    @POST
    @Path("/{orderId}/confirm-payment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmOrderPayment(
            @PathParam("orderId") String orderId,
            PaymentConfirmationRequest paymentDetails) {
        if (orderId == null || orderId.trim().isEmpty() || paymentDetails == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Missing orderId or payment details."))
                    .build();
        }

        boolean success = service.updatePaymentStatus(
                orderId,
                paymentDetails.paymentMethod,
                paymentDetails.paymentGatewayTransactionId,
                paymentDetails.paymentStatus
        );

        if (success) {
            return Response.ok(Map.of("message", "Payment status updated for order " + orderId)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND) // Or INTERNAL_SERVER_ERROR if update failed for other reasons
                    .entity(Map.of("error", "Order not found or failed to update payment status."))
                    .build();
        }
    }

    @POST
    @Path("/{mongoOrderId}/ship")
    public Response shipOrder(@PathParam("mongoOrderId") String mongoOrderId) {

        System.out.println("✅ Received request to ship order: " + mongoOrderId);

        String trackingNumber = "TRK-" + mongoOrderId.substring(mongoOrderId.length() - 6).toUpperCase() + "-" + System.currentTimeMillis();
        System.out.println("   -> Automatically generated tracking number: " + trackingNumber);

        // Get the order to find its numeric blockchain ID (this logic remains the same).
        ProceedOrder order = service.getOrderWithNumericId(mongoOrderId);
        if (order == null || order.blockchainOrderId == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Order or its blockchain ID not found.").build();
        }
        BigInteger blockchainOrderId = BigInteger.valueOf(order.blockchainOrderId);

        // Call the BlockchainService
        try {
            // We now pass the GENERATED trackingNumber to the service.
            CompletableFuture<TransactionReceipt> futureReceipt = blockchainService.markAsShipped(blockchainOrderId, trackingNumber);

            // Handle the confirmation (this logic remains the same, but now uses the generated number).
            futureReceipt.thenAccept(receipt -> {
                System.out.println("✅ 'markAsShipped' transaction confirmed! TxHash: " + receipt.getTransactionHash());
                // Update the DB with the new status, hash, and the GENERATED tracking number.
                service.updateOrderStatusToShipped(mongoOrderId, trackingNumber, receipt.getTransactionHash());
            }).exceptionally(ex -> {
                System.err.println("!!!!!!!!!! CRITICAL: 'markAsShipped' transaction FAILED for order " + mongoOrderId + " !!!!!!!!!!");
                ex.printStackTrace();
                return null;
            });

            System.out.println("✅ 'markAsShipped' transaction has been sent asynchronously.");
            // We can return the generated tracking number to the frontend so it can be displayed immediately.
            return Response.accepted("{\"status\":\"shipping_transaction_sent\", \"trackingNumber\":\"" + trackingNumber + "\"}").build();

        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to send 'markAsShipped' transaction for order: " + mongoOrderId);
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{mongoOrderId}/confirm-delivery")
    public Response confirmDelivery(@PathParam("mongoOrderId") String mongoOrderId) {

        System.out.println("✅ Received request to confirm delivery for order: " + mongoOrderId);

        ProceedOrder order = service.getOrderWithNumericId(mongoOrderId);
        if (order == null || order.blockchainOrderId == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Order or its blockchain ID not found.").build();
        }
        BigInteger blockchainOrderId = BigInteger.valueOf(order.blockchainOrderId);

        try {
            CompletableFuture<TransactionReceipt> futureReceipt = blockchainService.confirmOrderDelivered(blockchainOrderId);

            futureReceipt.thenAccept(receipt -> {
                System.out.println("✅ 'confirmDelivered' transaction confirmed! TxHash: " + receipt.getTransactionHash());
                service.updateOrderStatusToCompleted(mongoOrderId, receipt.getTransactionHash());
            }).exceptionally(ex -> {
                System.err.println("CRITICAL: 'confirmDelivered' transaction FAILED for order " + mongoOrderId + " !!!!!!!!!!");
                ex.printStackTrace();
                return null;
            });

            System.out.println("✅ 'confirmDelivered' transaction has been sent asynchronously.");
            return Response.accepted("{\"status\":\"delivery_confirmation_sent\"}").build();

        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to send 'confirmDelivered' transaction for order: " + mongoOrderId);
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{mongoOrderId}/blockchain-status")
    public Response getBlockchainOrderStatus(@PathParam("mongoOrderId") String mongoOrderId) {
        System.out.println("✅ Received request to get blockchain status for order: " + mongoOrderId);

        ProceedOrder order = service.getOrderWithNumericId(mongoOrderId);
        if (order == null || order.blockchainOrderId == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Order or its blockchain ID not found in local DB.").build();
        }
        BigInteger blockchainOrderId = BigInteger.valueOf(order.blockchainOrderId);

        try {
            OrderStatusTracker.Order contractOrder = blockchainService.getOrderStateFromBlockchain(blockchainOrderId).get();

            if (contractOrder == null || contractOrder.id.equals(BigInteger.ZERO)) {
                System.out.println("   -> Blockchain returned an empty order. The order does not exist at this ID on-chain.");
                return Response.status(Response.Status.NOT_FOUND).entity("Order not found on the blockchain for ID: " + blockchainOrderId).build();
            }

            System.out.println("   -> Blockchain returned a valid order. Mapping to DTO.");
            BlockchainOrderStateDTO responseDto = BlockchainOrderStateDTO.fromContractOrder(contractOrder);
            return Response.ok(responseDto).build();

        } catch (Exception e) {
            System.err.println("CRITICAL: An unexpected exception occurred while reading order state from blockchain for order: " + mongoOrderId);
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve data from the blockchain.").build();
        }
    }

    @GET
    @Path("/by-blockchain-id/{blockchainOrderId}")
    public Response getBlockchainStatusByNumericId(@PathParam("blockchainOrderId") Long blockchainOrderId) {
        System.out.println("✅ Received request for blockchain status using NUMERIC ID: " + blockchainOrderId);

        return queryBlockchainAndBuildResponse(BigInteger.valueOf(blockchainOrderId));
    }

    private Response queryBlockchainAndBuildResponse(BigInteger numericId) {
        try {
            OrderStatusTracker.Order contractOrder = blockchainService.getOrderStateFromBlockchain(numericId).get();

            if (contractOrder == null || contractOrder.id.equals(BigInteger.ZERO)) {
                return Response.status(Response.Status.NOT_FOUND).entity("Order not found on the blockchain for ID: " + numericId).build();
            }

            BlockchainOrderStateDTO responseDto = BlockchainOrderStateDTO.fromContractOrder(contractOrder);
            return Response.ok(responseDto).build();

        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to read order state from blockchain for numeric ID: " + numericId);
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve data from the blockchain.").build();
        }
    }

    @POST
    @Path("/{mongoOrderId}/dispute")
    public Response openDispute(@PathParam("mongoOrderId") String mongoOrderId) {
        System.out.println("✅ Received request to open a dispute for order: " + mongoOrderId);

        ProceedOrder order = service.findByMongoId(mongoOrderId);
        if (order == null || order.blockchainOrderId == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        BigInteger blockchainOrderId = BigInteger.valueOf(order.blockchainOrderId);

        try {
            CompletableFuture<TransactionReceipt> futureReceipt = blockchainService.openDispute(blockchainOrderId);

            futureReceipt.thenAccept(receipt -> {
                System.out.println("✅ 'openDispute' transaction confirmed! TxHash: " + receipt.getTransactionHash());
                service.updateOrderStatusToDisputed(mongoOrderId, receipt.getTransactionHash());
            }).exceptionally(ex -> {
                System.err.println("!CRITICAL: 'openDispute' transaction FAILED for order " + mongoOrderId);
                ex.printStackTrace();
                return null;
            });

            return Response.accepted("{\"status\":\"dispute_opening_sent\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/{mongoOrderId}/resolve-dispute")
    public Response resolveDispute(@PathParam("mongoOrderId") String mongoOrderId, @QueryParam("refund") boolean wasRefunded) {
        System.out.println("✅ Received admin request to resolve dispute for order: " + mongoOrderId + ", with refund: " + wasRefunded);

        ProceedOrder order = service.findByMongoId(mongoOrderId);
        if (order == null || order.blockchainOrderId == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        BigInteger blockchainOrderId = BigInteger.valueOf(order.blockchainOrderId);

        try {
            CompletableFuture<TransactionReceipt> futureReceipt = blockchainService.resolveDispute(blockchainOrderId, wasRefunded);

            futureReceipt.thenAccept(receipt -> {
                System.out.println("✅ 'resolveDispute' transaction confirmed! TxHash: " + receipt.getTransactionHash());
                service.updateOrderStatusAfterDispute(mongoOrderId, wasRefunded, receipt.getTransactionHash());
            }).exceptionally(ex -> {
                System.err.println("CRITICAL: 'resolveDispute' transaction FAILED for order " + mongoOrderId);
                ex.printStackTrace();
                return null;
            });

            return Response.accepted("{\"status\":\"dispute_resolution_sent\"}").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

}
