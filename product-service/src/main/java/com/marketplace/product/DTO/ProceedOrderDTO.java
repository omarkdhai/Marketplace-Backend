package com.marketplace.product.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ProceedOrderDTO {

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
    public String paymentStatus;
    private String stripeCustomerId;
    public String blockchainTransactionId;
    public String buyerEthAddress;
    public String sellerEthAddress;
    public String blockchainRegisteredAmount;
    public String blockchainState;
    public Date lastBlockchainUpdate;
    public Boolean orderStatus = false;
}
