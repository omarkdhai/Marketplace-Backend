package com.marketplace.product.Entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


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
}
