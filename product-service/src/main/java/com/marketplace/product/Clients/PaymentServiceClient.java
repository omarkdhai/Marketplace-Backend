package com.marketplace.product.Clients;

import com.marketplace.product.Clients.beans.Customer;
import com.marketplace.product.Clients.beans.StripCustomerCreateRequest;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v1/payments")
@RegisterRestClient(configKey="payment-service-api")
public interface PaymentServiceClient {

    @POST
    @Path("/create-payment-intent")
    @Produces(MediaType.APPLICATION_JSON)
    CreatePaymentResponse createPaymentIntent(CreatePaymentRequest request);

    @POST
    @Path("/get-or-create-customer")
    @Produces(MediaType.APPLICATION_JSON)
    Customer getOrCreateCustomerStrip(StripCustomerCreateRequest request);
}
