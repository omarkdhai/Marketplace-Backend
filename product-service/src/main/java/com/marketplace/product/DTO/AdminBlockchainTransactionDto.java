package com.marketplace.product.DTO;

import com.marketplace.product.Entity.ProceedOrder;
import java.util.Date;

public class AdminBlockchainTransactionDto {

    public String id;
    public String userId;
    public String firstName;
    public String lastName;
    public String email;
    public String phone;
    public double totalPrice;
    public String paymentMethod;
    public Date createdAt;

    // Blockchain specific fields
    public String blockchainTransactionId;
    public String buyerEthAddress;
    public String sellerEthAddress;
    public String blockchainRegisteredAmount;
    public String blockchainState;
    public Date lastBlockchainUpdate;

    public AdminBlockchainTransactionDto() {}

    // Constructor to map from ProceedOrder entity
    public AdminBlockchainTransactionDto(ProceedOrder order) {
        this.id = order.id != null ? order.id.toString() : null;
        this.userId = order.userId;
        this.firstName = order.firstName;
        this.lastName = order.lastName;
        this.email = order.email;
        this.phone = order.phone;
        this.totalPrice = order.totalPrice;
        this.paymentMethod = order.paymentMethod;
        this.createdAt = order.createdAt != null ? java.sql.Timestamp.valueOf(order.createdAt) : null;
        this.blockchainTransactionId = order.blockchainTransactionId;
        this.buyerEthAddress = order.buyerEthAddress;
        this.sellerEthAddress = order.sellerEthAddress;
        this.blockchainRegisteredAmount = order.blockchainRegisteredAmount;
        this.blockchainState = order.blockchainState;
        this.lastBlockchainUpdate = order.lastBlockchainUpdate;
    }
}
