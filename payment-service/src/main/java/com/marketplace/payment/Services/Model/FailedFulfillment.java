package com.marketplace.payment.Services.Model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FailedFulfillment extends PanacheMongoEntity {

    public String orderId;
    public String paymentIntentId;
    public int attemptCount;
    public LocalDateTime createdAt;
    public LocalDateTime lastAttemptAt;
    public String lastErrorMessage;

    public FailedFulfillment(String orderId, String paymentIntentId, String errorMessage) {
        this.orderId = orderId;
        this.paymentIntentId = paymentIntentId;
        this.lastErrorMessage = errorMessage;
        this.attemptCount = 1;
        this.createdAt = LocalDateTime.now();
        this.lastAttemptAt = LocalDateTime.now();
    }
}
