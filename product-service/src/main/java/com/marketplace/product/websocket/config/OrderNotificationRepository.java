package com.marketplace.product.websocket.config;

import com.marketplace.product.Entity.Transaction;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class OrderNotificationRepository implements PanacheMongoRepository<OrderNotification> {

}
