package com.marketplace.product.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryConfirmationRequest {

    private String signerAddress;
    private String signature;
}
