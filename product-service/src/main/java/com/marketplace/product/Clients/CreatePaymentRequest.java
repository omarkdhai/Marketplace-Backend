package com.marketplace.product.Clients;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentRequest {

    public Long amount;
    public String currency;
    public String customerId;
    public String orderId;
}
