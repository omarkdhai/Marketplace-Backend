package com.marketplace.product.Service;

import com.marketplace.product.Clients.CreatePaymentRequest;
import com.marketplace.product.Clients.CreatePaymentResponse;
import com.marketplace.product.Clients.PaymentServiceClient;
import com.marketplace.product.Clients.beans.Customer;
import com.marketplace.product.Clients.beans.StripCustomerCreateRequest;
import com.marketplace.product.DTO.ProceedOrderDTO;
import com.marketplace.product.Entity.CartItem;
import com.marketplace.product.Entity.ProceedOrder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class ProceedOrderService {

    @Inject
    CartItemService cartItemService;

    @Inject
    @RestClient
    PaymentServiceClient paymentServiceClient;


    public String save(ProceedOrderDTO dto) {
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

        CartItem cartItem = cartItemService.getCartByUserId(dto.userId);

        if (cartItem != null) {
            order.products = new ArrayList<>(cartItem.getProducts());
            order.totalPrice = cartItem.getTotalPrice();
        } else {
            throw new IllegalStateException("Cannot create an order with an empty or non-existent cart.");
        }
        
        order.persist();

        cartItemService.clearCart(dto.userId);

        return order.id.toString();
    }

    public boolean updateOrderStatusToPaid(String orderId, String gatewayTransactionId) {
        ProceedOrder order = ProceedOrder.findById(new ObjectId(orderId));
        if (order == null) {
            System.err.println("Attempted to update a non-existent order: " + orderId);
            return false;
        }

        // Verify the webhook
        if ("PAID".equals(order.paymentStatus)) {
            System.out.println("Order " + orderId + " is already marked as PAID. Ignoring event.");
            return true;
        }

        System.out.println("Updating order " + orderId + " status to PAID in MongoDB.");
        order.paymentStatus = "PAID";
        order.setOrderStatus(true);
        order.paymentGatewayTransactionId = gatewayTransactionId;
        order.lastPaymentUpdate = new Date();

        order.update();
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

    public boolean toggleOrderStatus(String orderId) {
        ProceedOrder order = ProceedOrder.findById(new ObjectId(orderId));
        if (order != null) {
            order.setOrderStatus(!Boolean.TRUE.equals(order.getOrderStatus())); // toggle
            order.persistOrUpdate();
            return true;
        }
        return false;
    }

    @Transactional
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
}
