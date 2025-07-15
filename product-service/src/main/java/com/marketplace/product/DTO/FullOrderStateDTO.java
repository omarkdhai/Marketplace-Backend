package com.marketplace.product.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FullOrderStateDTO {

    public String blockchainOrderId;
    public String buyerAddress;
    public String sellerAddress;
    public String stripePaymentIntentId;
    public String itemId;
    public String latestStatus;
    public String trackingNumber;
}
