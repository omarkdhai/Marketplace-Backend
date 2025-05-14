package com.marketplace.product.Service;

import com.marketplace.product.DTO.ProceedOrderDTO;
import com.marketplace.product.Entity.CartItem;
import com.marketplace.product.Entity.ProceedOrder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

        order.persist();

        cartItemService.clearCart(dto.userId);
    }

    public List<ProceedOrder> getAllOrders() {
        return ProceedOrder.listAll();
    }

    public boolean deleteOrder(String id) {
        return ProceedOrder.deleteById(new org.bson.types.ObjectId(id));
    }
}
