package com.marketplace.product.Entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProceedOrder extends PanacheMongoEntity {

    public String userId;
    public String firstName;
    public String lastName;
    public String email;
    public String phone;
    public String streetAddress;
    public String city;
    public String postalCode;
    public String tvaNumber;
    public int satisfaction;
    public String paymentMethod;
    public LocalDateTime createdAt = LocalDateTime.now();
    public Boolean orderStatus = false;
    public String trackingNumber;
    public Date shippedAt;

    public List<CartProduct> products = new ArrayList<>();
    public double totalPrice;
    private String currency;

    public String paymentStatus;
    public String paymentGatewayTransactionId;
    public Date lastPaymentUpdate;

    public Long blockchainOrderId;
    public String blockchainTransactionHash;

    public String signature;
    public String signerAddress;

    public String blockchainTransactionId;
    public String buyerEthAddress;
    public String sellerEthAddress;
    public String blockchainRegisteredAmount;
    public String blockchainState;
    public Date lastBlockchainUpdate;
}
