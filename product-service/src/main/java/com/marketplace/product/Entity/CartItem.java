package com.marketplace.product.Entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.smallrye.common.constraint.NotNull;
import lombok.*;
import org.bson.types.ObjectId;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItem extends PanacheMongoEntity {

    public ObjectId id;

    private List<CartProduct> products = new ArrayList<>();

    @NotNull
    public String userId;

    public double totalPrice;

    public BigInteger blockchainTransactionId;
    public String buyerEthAddress;
    public String sellerEthAddress;
    public BigInteger blockchainRegisteredAmount;
    public String blockchainState;
    public String paymentStatus;


    public void updateTotalPrice() {
        this.totalPrice = products.stream()
                .mapToDouble(cartProduct -> cartProduct.getProduct().getPrice() * cartProduct.getQuantity())
                .sum();
    }
}
