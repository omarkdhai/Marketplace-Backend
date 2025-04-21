package com.marketplace.product.Entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.smallrye.common.constraint.NotNull;
import lombok.*;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItem extends PanacheMongoEntity {

    private ObjectId id;

    private List<CartProduct> products = new ArrayList<>();

    @NotNull
    public String userId;

    public double totalPrice;

    public void updateTotalPrice() {
        this.totalPrice = products.stream()
                .mapToDouble(cartProduct -> cartProduct.getProduct().getPrice() * cartProduct.getQuantity())
                .sum();
    }
}
