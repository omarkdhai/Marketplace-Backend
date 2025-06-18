package com.marketplace.payment.Services.DTO;

public class CreateCustomerResponse {
    private String customerId;

    public CreateCustomerResponse(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
