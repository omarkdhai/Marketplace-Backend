package com.marketplace.product.Service;

import com.marketplace.product.DTO.ProceedOrderDTO;
import com.marketplace.product.Entity.CartItem;
import com.marketplace.product.Entity.ProceedOrder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ProceedOrderService {

    @Inject
    CartItemService cartItemService;

    public void save(ProceedOrderDTO dto) {
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

        CartItem cartItem = cartItemService.getCartByUserId(dto.userId);

        if (cartItem != null) {
            order.products = new ArrayList<>(cartItem.getProducts());
            order.totalPrice = cartItem.getTotalPrice();
        }
        
        order.persist();
        cartItemService.clearCart(dto.userId);
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
}
