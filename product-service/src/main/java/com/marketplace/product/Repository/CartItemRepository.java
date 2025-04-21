package com.marketplace.product.Repository;

import com.marketplace.product.Entity.CartItem;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.List;

@ApplicationScoped
public class CartItemRepository implements PanacheMongoRepository<CartItem> {

    public List<CartItem> findByUserId(String userId) {
        return list("userId", userId);
    }

    public CartItem findByUserIdAndProduct(String userId, ObjectId productId) {
        return find("userId = ?1 and products.id = ?2", userId, productId).firstResult();
    }
}
