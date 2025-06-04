package com.marketplace.product.Entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends PanacheMongoEntity {

    public ObjectId id;
    public String blockchainTransactionId;
    public LocalDateTime date;
    public Double amount;
    public String fiatCurrency;
    public String buyerEthAddress;
    public String sellerEthAddress;
    public String state;
    public String email;
    public String fistName;
    public String lastName;
}
