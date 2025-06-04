package com.marketplace.product.DTO;

import java.util.Date;

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
    public String blockchainTransactionId;
    public String buyerEthAddress;
    public String sellerEthAddress;
    public String blockchainRegisteredAmount;
    public String blockchainState;
    public Date lastBlockchainUpdate;
    public Boolean orderStatus = false;
}
