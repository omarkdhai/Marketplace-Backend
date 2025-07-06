package com.marketplace.product.Service;

import com.marketplace.product.Clients.CreatePaymentRequest;
import com.marketplace.product.Clients.CreatePaymentResponse;
import com.marketplace.product.Clients.PaymentServiceClient;
import com.marketplace.product.DTO.ProceedOrderDTO;
import com.marketplace.product.Entity.CartItem;
import com.marketplace.product.Entity.ProceedOrder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class ProceedOrderService {

    @Inject
    CartItemService cartItemService;

    @Inject
    @RestClient
    PaymentServiceClient paymentServiceClient;

    private static final AtomicLong numericIdCounter = new AtomicLong(System.currentTimeMillis());


    public CreatePaymentResponse saveAndInitiatePayment(ProceedOrderDTO dto) {
        ProceedOrder order = new ProceedOrder();

        order.userId = dto.userId;
        order.firstName = dto.firstName;
        order.lastName = dto.lastName;
        order.email = dto.email;
        order.phone = dto.phone;
        order.streetAddress = dto.streetAddress;
        order.city = dto.city;
        order.postalCode = dto.postalCode;
        order.tvaNumber = dto.tvaNumber;
        order.satisfaction = dto.satisfaction;
        order.paymentMethod = dto.paymentMethod;
        order.paymentStatus = "PENDING_PAYMENT";

        order.blockchainOrderId = numericIdCounter.getAndIncrement();
        System.out.println("Generated new blockchainOrderId: " + order.blockchainOrderId + " for new order.");

        CartItem cartItem = cartItemService.getCartByUserId(dto.userId);
        if (cartItem != null) {
            order.products = new ArrayList<>(cartItem.getProducts());
            order.totalPrice = cartItem.getTotalPrice();
        } else {
            throw new IllegalStateException("Cannot create an order with an empty or non-existent cart.");
        }

        order.persist();

        long amountInCents = (long) (order.totalPrice * 100);
        if (amountInCents < 50) {
            throw new IllegalStateException("Order total is below the minimum chargeable amount of $0.50.");
        }

        CreatePaymentRequest paymentRequest = new CreatePaymentRequest();
        paymentRequest.setOrderId(order.id.toString());

        paymentRequest.setCustomerId(dto.getStripeCustomerId());
        paymentRequest.setAmount(amountInCents);
        paymentRequest.setCurrency("usd");

        System.out.println("Calling payment-service to create PaymentIntent for order: " + order.id.toString());
        CreatePaymentResponse paymentResponse = paymentServiceClient.createPaymentIntent(paymentRequest);
        System.out.println("Received client_secret from payment-service: " + paymentResponse.getClientSecret());
        String mongoOrderId = order.id.toString();
        paymentResponse.setOrderId(mongoOrderId);

        cartItemService.clearCart(dto.userId);

        return paymentResponse;
    }

    public boolean updateOrderStatusToPaid(String mongoOrderId, String gatewayTransactionId) {
        ProceedOrder order = findByMongoId(mongoOrderId); // On utilise la méthode de recherche
        if (order == null) {
            System.err.println("updateOrderStatusToPaid: Order not found with ID: " + mongoOrderId);
            return false;
        }
        order.paymentStatus = "PAID";
        order.paymentGatewayTransactionId = gatewayTransactionId;
        order.lastPaymentUpdate = new Date();
        order.update();
        System.out.println("updateOrderStatusToPaid: Successfully updated status for order " + mongoOrderId);
        return true;
    }

    public void fulfillOrder(String orderId) {
        ProceedOrder order = ProceedOrder.findById(new ObjectId(orderId));
        if (order == null) {
            System.err.println("Webhook tried to fulfill non-existent order: " + orderId);
            throw new NotFoundException("Order not found");
        }

        System.out.println("Fulfilling order via webhook: " + orderId);
        order.paymentStatus = "PAID_VIA_STRIPE";
        order.setOrderStatus(true);
        order.lastPaymentUpdate = new Date();

        order.persistOrUpdate();
    }

    public List<ProceedOrder> getAllOrders() {
        return ProceedOrder.listAll();
    }

    public boolean deleteOrder(String id) {
        return ProceedOrder.deleteById(new org.bson.types.ObjectId(id));
    }

    public void updateBlockchainInfo(String mongoOrderId, Long blockchainOrderId, String txHash) {
        ProceedOrder order = findByMongoId(mongoOrderId);
        if (order != null) {
            System.out.println("updateBlockchainInfo: Found order " + mongoOrderId + ". Setting blockchain info...");

            // On s'assure que l'ID qu'on a généré est bien celui qu'on sauvegarde
            order.blockchainOrderId = blockchainOrderId;
            order.blockchainTransactionHash = txHash;
            order.blockchainState = "Paid";
            order.lastBlockchainUpdate = new Date();

            order.update(); // Sauvegarde les changements
            System.out.println("updateBlockchainInfo: Blockchain info saved to DB for order " + mongoOrderId + ". blockchainOrderId is now: " + order.blockchainOrderId);
        } else {
            System.err.println("updateBlockchainInfo: CRITICAL - Could not find order " + mongoOrderId + " to save blockchain info.");
        }
    }

    public ProceedOrder findByMongoId(String mongoOrderId) {
        try {
            return ProceedOrder.findById(new ObjectId(mongoOrderId));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean updatePaymentStatus(String backendOrderId, String paymentMethodUsed, String gatewayTransactionId, String stripePaymentStatus) {
        ObjectId mongoOrderId;
        try {
            mongoOrderId = new ObjectId(backendOrderId);
        } catch (IllegalArgumentException e) {
            return false;
        }

        ProceedOrder order = ProceedOrder.findById(mongoOrderId);
        if (order != null) {
            order.paymentMethod = paymentMethodUsed;
            if ("succeeded".equalsIgnoreCase(stripePaymentStatus) || "paid".equalsIgnoreCase(stripePaymentStatus)) {
                order.paymentStatus = "PAID_VIA_" + paymentMethodUsed.toUpperCase();
            } else {
                order.paymentStatus = "PAYMENT_" + stripePaymentStatus.toUpperCase() + "_VIA_" + paymentMethodUsed.toUpperCase();
            }
            order.paymentGatewayTransactionId = gatewayTransactionId;
            order.lastPaymentUpdate = new Date();

            order.persistOrUpdate();

            return true;
        }
        return false;
    }

    public ProceedOrder getOrderWithNumericId(String mongoOrderId) {
        try {
            return ProceedOrder.findById(new ObjectId(mongoOrderId));
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid MongoDB ObjectId format: " + mongoOrderId);
            return null;
        }
    }

    public boolean updateOrderStatusToShipped(String mongoOrderId, String trackingNumber, String txHash) {
        ProceedOrder order = ProceedOrder.findById(new ObjectId(mongoOrderId));
        if (order == null) {
            System.err.println("Attempted to ship a non-existent order: " + mongoOrderId);
            return false;
        }

        order.paymentStatus = "SHIPPED";
        order.trackingNumber = trackingNumber;
        order.blockchainTransactionHash = txHash;
        order.blockchainState = "Shipped";
        order.lastBlockchainUpdate = new Date();
        order.update();
        System.out.println("Order " + mongoOrderId + " status updated to SHIPPED in MongoDB.");
        return true;
    }

    public boolean updateOrderStatusToCompleted(String mongoOrderId, String txHash) {
        ProceedOrder order = ProceedOrder.findById(new ObjectId(mongoOrderId));
        if (order == null) {
            System.err.println("Attempted to complete a non-existent order: " + mongoOrderId);
            return false;
        }

        order.paymentStatus = "COMPLETED";
        order.blockchainTransactionHash = txHash;
        order.blockchainState = "Completed";
        order.lastBlockchainUpdate = new Date();
        order.update();
        System.out.println("Order " + mongoOrderId + " status updated to COMPLETED in MongoDB.");
        return true;
    }

    public boolean updateOrderStatusToDisputed(String mongoOrderId, String txHash) {
        ProceedOrder order = findByMongoId(mongoOrderId);
        if (order == null) return false;

        order.paymentStatus = "DISPUTED";
        order.blockchainTransactionHash = txHash;
        order.blockchainState = "Disputed";
        order.lastBlockchainUpdate = new Date();
        order.update();
        System.out.println("Order " + mongoOrderId + " status updated to DISPUTED in MongoDB.");
        return true;
    }

    public boolean updateOrderStatusAfterDispute(String mongoOrderId, boolean wasRefunded, String txHash) {
        ProceedOrder order = findByMongoId(mongoOrderId);
        if (order == null) return false;

        String finalStatus = wasRefunded ? "REFUNDED" : "COMPLETED";
        order.paymentStatus = finalStatus;
        order.blockchainTransactionHash = txHash;
        order.blockchainState = wasRefunded ? "Refunded" : "Completed";
        order.lastBlockchainUpdate = new Date();
        order.update();
        System.out.println("Dispute for order " + mongoOrderId + " resolved. Status set to " + finalStatus + " in MongoDB.");
        return true;
    }
}
