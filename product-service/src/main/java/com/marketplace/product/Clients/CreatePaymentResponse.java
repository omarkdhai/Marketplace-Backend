package com.marketplace.product.Clients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentResponse {

    public String clientSecret;
    public String status;
    private String orderId;
}
