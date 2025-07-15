package com.marketplace.product.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipmentConfirmationRequest {

    private String signerAddress;
    private String signature;
    private String trackingNumber;
}
