package com.marketplace.payment.Services.DTO;

public class CreatePaymentResponse {
    private String clientSecret;
    private String status;

    public CreatePaymentResponse(String clientSecret, String status) {
        this.clientSecret = clientSecret;
        this.status = status;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
