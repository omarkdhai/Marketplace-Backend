package com.marketplace.support.Entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DisputeTicket extends PanacheMongoEntity {
    public String mongoOrderId;
    public String userEmail;
    public String subject;
    public String message;
    public LocalDateTime createdAt = LocalDateTime.now();
    public String status = "OPEN";
}
