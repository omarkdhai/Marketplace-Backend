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

    private ObjectId id;

    private List<CartProduct> products = new ArrayList<>();

    @NotNull
    public String userId;

    public double totalPrice;

    public BigInteger blockchainTransactionId; // The ID from the smart contract's TransactionCreated event
    public String buyerEthAddress;
    public String sellerEthAddress;
    public BigInteger blockchainRegisteredAmount; // The amount (fiat reference) registered in the smart contract
    public String blockchainState; // "CREATED", "FUNDED_SYMBOLICALLY", "ITEM_SENT"
    public String paymentStatus; // "PENDING_BLOCKCHAIN_CONFIRMATION", "PAYMENT_AUTHORIZED_OFFCHAIN"


    public void updateTotalPrice() {
        this.totalPrice = products.stream()
                .mapToDouble(cartProduct -> cartProduct.getProduct().getPrice() * cartProduct.getQuantity())
                .sum();
    }
}
