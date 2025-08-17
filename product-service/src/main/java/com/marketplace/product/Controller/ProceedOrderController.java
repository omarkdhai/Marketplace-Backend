package com.marketplace.product.Controller;

import com.marketplace.product.Clients.CreatePaymentResponse;
import com.marketplace.product.DTO.*;
import com.marketplace.product.Entity.ProceedOrder;
import com.marketplace.product.Service.BlockchainService;
import com.marketplace.product.Service.EmailService;
import com.marketplace.product.Service.ProceedOrderService;
import com.marketplace.product.Service.SignatureVerificationService;
import com.marketplace.product.websocket.config.NotificationWebSocket;
import com.marketplace.productservice.contracts.OrderStatusTracker;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Path("/api/v1/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProceedOrderController {

    @Inject
    ProceedOrderService service;

    @Inject
    EmailService emailService;

    @Inject
    BlockchainService blockchainService;

    @Inject
    SignatureVerificationService signatureVerifier;

    @Inject
    NotificationWebSocket orderWebSocket;

    @POST
    public Response submitOrder(ProceedOrderDTO dto) {
        try {
            CreatePaymentResponse paymentResponse = service.saveAndInitiatePayment(dto);
            return Response.status(Response.Status.CREATED).entity(paymentResponse).build();
        } catch (SecurityException e) {
            System.err.println("Signature verification failed: " + e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (IllegalStateException e) {
            System.err.println("Business logic error: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST) // 400 Bad Request
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            System.err.println("Error during order submission and payment initiation: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"An unexpected internal error occurred.\"}")
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
    public Response shipOrder(@PathParam("mongoOrderId") String mongoOrderId, ShipmentConfirmationRequest request) {

        System.out.println("✅ Received request to ship order: " + mongoOrderId);

        String trackingNumber = request.getTrackingNumber();
        if (trackingNumber == null || trackingNumber.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"Tracking number is missing from the request.\"}").build();
        }

        String expectedMessage = "I confirm the shipment of order " + mongoOrderId + " with tracking number " + trackingNumber;

        System.out.println("--- Verifying Signature ---");
        System.out.println("   - Expected Message: " + expectedMessage);
        System.out.println("   - Received Signature: " + request.getSignature());
        System.out.println("   - Received Signer Address: " + request.getSignerAddress());

        boolean isSignatureValid = signatureVerifier.verifySignature(
                request.getSignature(),
                request.getSignerAddress(),
                expectedMessage
        );

        if (!isSignatureValid) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"error\":\"Invalid signature for shipping confirmation.\"}").build();
        }

        System.out.println("✅ Shipping signature verified for order: " + mongoOrderId);

        ProceedOrder order = service.findByMongoId(mongoOrderId);
        if (order == null || order.blockchainOrderId == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Order or its blockchain ID not found.").build();
        }
        BigInteger blockchainOrderId = BigInteger.valueOf(order.blockchainOrderId);

        try {
            CompletableFuture<TransactionReceipt> futureReceipt = blockchainService.markAsShipped(blockchainOrderId, trackingNumber);

            futureReceipt.thenAccept(receipt -> {
                System.out.println("✅ 'markAsShipped' transaction confirmed! TxHash: " + receipt.getTransactionHash());
                service.updateOrderStatusToShipped(mongoOrderId, trackingNumber, receipt.getTransactionHash());

                ProceedOrder updatedOrder = service.findByMongoId(mongoOrderId);
                orderWebSocket.broadcastOrderNotification(updatedOrder, "SHIP_ORDER");
                if (updatedOrder != null) {
                    CompletableFuture.runAsync(() -> emailService.sendOrderShippedEmail(updatedOrder));
                }
            }).exceptionally(ex -> {
                System.err.println("CRITICAL: 'markAsShipped' transaction FAILED for order " + mongoOrderId + " !!!!!!!!!!");
                ex.printStackTrace();
                return null;
            });

            System.out.println("✅ 'markAsShipped' transaction has been sent asynchronously.");
            return Response.accepted("{\"status\":\"shipping_transaction_sent\", \"trackingNumber\":\"" + trackingNumber + "\"}").build();

        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to send 'markAsShipped' transaction for order: " + mongoOrderId);
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/{mongoOrderId}/confirm-delivery")
    public Response confirmDelivery(@PathParam("mongoOrderId") String mongoOrderId, DeliveryConfirmationRequest request) {

        System.out.println("✅ Received request to confirm delivery for order: " + mongoOrderId);

        ProceedOrder order = service.getOrderWithNumericId(mongoOrderId);
        if (order == null || order.blockchainOrderId == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Order or its blockchain ID not found.").build();
        }

        String expectedMessage = "I confirm I have received my order with ID: " + mongoOrderId;

        boolean isSignatureValid = signatureVerifier.verifySignature(
                request.getSignature(),
                request.getSignerAddress(),
                expectedMessage
        );

        if (!isSignatureValid) {

            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid signature.").build();
        }
        System.out.println("✅ Delivery confirmation signature verified for order: " + mongoOrderId);

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
    @Path("/blockchain/all")
    public Response getAllBlockchainOrders() {
        System.out.println("✅ Received request to get all orders from the blockchain.");

        try {
            List<OrderStatusTracker.OrderCreatedEventResponse> events = blockchainService.getAllOrdersFromBlockchainEvents();

            System.out.println("   -> Returning " + events.size() + " orders found on the blockchain.");

            return Response.ok(events).build();

        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to retrieve all orders from blockchain events.");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve data from the blockchain.").build();
        }
    }

    @GET
    @Path("/blockchain/current-states")
    public Response getAllOrderCurrentStates() {
        System.out.println("✅ Received request to get all order current states from the blockchain.");

        try {
            List<OrderStatusTracker.OrderCreatedEventResponse> creationEvents = blockchainService.getAllOrdersFromBlockchainEvents();

            Map<BigInteger, FullOrderStateDTO> ordersMap = new HashMap<>();

            for (OrderStatusTracker.OrderCreatedEventResponse event : creationEvents) {
                FullOrderStateDTO dto = new FullOrderStateDTO();
                dto.blockchainOrderId = event.orderId.toString();
                dto.buyerAddress = event.buyer;
                dto.sellerAddress = event.seller;
                dto.itemId = event.itemId;
                dto.stripePaymentIntentId = event.stripePaymentIntentId;
                dto.latestStatus = "Paid"; // Le statut initial après création est toujours "Paid"

                ordersMap.put(event.orderId, dto);
            }
            System.out.println("   -> Found " + ordersMap.size() + " unique orders.");

            List<OrderStatusTracker.OrderStatusChangedEventResponse> statusChangeEvents = blockchainService.getAllOrderStatusChanges();
            System.out.println("   -> Found " + statusChangeEvents.size() + " status change events to process.");

            for (OrderStatusTracker.OrderStatusChangedEventResponse event : statusChangeEvents) {
                BigInteger orderId = event.orderId;
                if (ordersMap.containsKey(orderId)) {
                    String newStatus = mapStateToString(event.newState);
                    ordersMap.get(orderId).latestStatus = newStatus;
                    System.out.println("      -> Updating order " + orderId + " to status: " + newStatus);
                }
            }

            List<FullOrderStateDTO> finalResponse = new ArrayList<>(ordersMap.values());

            return Response.ok(finalResponse).build();

        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to retrieve aggregated order states.");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/blockchain/all-details")
    public Response getAllBlockchainOrdersWithDetails() {
        System.out.println("✅ Received request to get all order details from the blockchain.");

        try {
            List<OrderStatusTracker.OrderCreatedEventResponse> creationEvents = blockchainService.getAllOrdersFromBlockchainEvents();
            Map<BigInteger, FullOrderStateDTO> ordersMap = new HashMap<>();

            for (OrderStatusTracker.OrderCreatedEventResponse event : creationEvents) {
                FullOrderStateDTO dto = new FullOrderStateDTO();
                dto.blockchainOrderId = event.orderId.toString();
                dto.buyerAddress = event.buyer;
                dto.sellerAddress = event.seller;
                dto.itemId = event.itemId;
                dto.stripePaymentIntentId = event.stripePaymentIntentId;
                dto.latestStatus = "Paid"; // Statut par défaut après création
                ordersMap.put(event.orderId, dto);
            }
            System.out.println("   -> Discovered " + ordersMap.size() + " unique orders.");

            List<OrderStatusTracker.OrderStatusChangedEventResponse> statusChangeEvents = blockchainService.getAllOrderStatusChanges();
            for (OrderStatusTracker.OrderStatusChangedEventResponse event : statusChangeEvents) {
                if (ordersMap.containsKey(event.orderId)) {
                    ordersMap.get(event.orderId).latestStatus = mapStateToString(event.newState);
                }
            }
            System.out.println("   -> Processed " + statusChangeEvents.size() + " status change events.");

            System.out.println("   -> Enriching orders with tracking numbers where applicable...");
            for (FullOrderStateDTO dto : ordersMap.values()) {
                if ("Shipped".equals(dto.latestStatus) || "Delivered".equals(dto.latestStatus) || "Completed".equals(dto.latestStatus)) {
                    System.out.println("      -> Order " + dto.blockchainOrderId + " has been shipped. Fetching tracking number...");

                    try {
                        OrderStatusTracker.Order fullOrderData =
                                blockchainService.getOrderStateFromBlockchain(new BigInteger(dto.getBlockchainOrderId())).get();

                        dto.setTrackingNumber(fullOrderData.trackingNumber);
                        System.out.println("         -> Found tracking number: " + fullOrderData.trackingNumber);

                    } catch (Exception e) {
                        System.err.println("!!!!!! Failed to fetch tracking number for order " + dto.blockchainOrderId);
                    }
                }
            }

            List<FullOrderStateDTO> finalResponse = new ArrayList<>(ordersMap.values());
            return Response.ok(finalResponse).build();

        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to retrieve aggregated order states.");
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String mapStateToString(BigInteger state) {
        int stateValue = state.intValue();
        switch (stateValue) {
            case 0: return "Created";
            case 1: return "Paid";
            case 2: return "Shipped";
            case 3: return "Delivered";
            case 4: return "Disputed";
            case 5: return "Completed";
            case 6: return "Refunded";
            default: return "Unknown";
        }
    }

    @GET
    @Path("/by-blockchain-id/{blockchainOrderId}")
    public Response getOrderByBlockchainId(@PathParam("blockchainOrderId") Long blockchainOrderId) {
        System.out.println("✅ Received request to get blockchain status for NUMERIC ID: " + blockchainOrderId);

        try {
            BigInteger numericId = BigInteger.valueOf(blockchainOrderId);
            OrderStatusTracker.Order contractOrder = blockchainService.getOrderStateFromBlockchain(numericId).get();

            if (contractOrder == null || contractOrder.id.equals(BigInteger.ZERO)) {
                return Response.status(Response.Status.NOT_FOUND).entity("Order not found on the blockchain for this ID.").build();
            }

            return Response.ok().build();

        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to read order state from blockchain for ID: " + blockchainOrderId);
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

    @GET
    @Path("/email-preview/{mongoOrderId}")
    @Produces(MediaType.TEXT_HTML)
    public Response previewEmailTemplate(@PathParam("mongoOrderId") String mongoOrderId) {
        System.out.println("✅ Received request to preview email for order: " + mongoOrderId);

        ProceedOrder order = service.findByMongoId(mongoOrderId);
        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("<h1>Order not found</h1><p>Could not find order with ID: " + mongoOrderId + "</p>")
                    .build();
        }

        try {
            InputStream logoStream = getClass().getResourceAsStream("/marketplacelogo1.png");
            if (logoStream == null) {
                throw new IOException("Could not find marketplacelogo1.png in resources for preview.");
            }
            byte[] logoBytes = logoStream.readAllBytes();
            String logoBase64 = Base64.getEncoder().encodeToString(logoBytes);

            String emailHtml = emailService.buildOrderShippedHtml(order, logoBase64);
            return Response.ok(emailHtml).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("<h1>Error generating template</h1><pre>" + e.getMessage() + "</pre>")
                    .build();
        }
    }

    @GET
    @Path("/email-ship-preview/{mongoOrderId}")
    @Produces(MediaType.TEXT_HTML)
    public Response previewShipEmailTemplate(@PathParam("mongoOrderId") String mongoOrderId) {
        System.out.println("✅ Received request to preview email for order: " + mongoOrderId);

        ProceedOrder order = service.findByMongoId(mongoOrderId);
        if (order == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("<h1>Order not found</h1><p>Could not find order with ID: " + mongoOrderId + "</p>")
                    .build();
        }

        try {
            InputStream logoStream = getClass().getResourceAsStream("/marketplacelogo1.png");
            if (logoStream == null) {
                throw new IOException("Could not find marketplacelogo1.png in resources for preview.");
            }
            byte[] logoBytes = logoStream.readAllBytes();
            String logoBase64 = Base64.getEncoder().encodeToString(logoBytes);

            String emailHtml = emailService.buildOrderShippedHtml(order, logoBase64);
            return Response.ok(emailHtml).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("<h1>Error generating template</h1><pre>" + e.getMessage() + "</pre>")
                    .build();
        }
    }
}
