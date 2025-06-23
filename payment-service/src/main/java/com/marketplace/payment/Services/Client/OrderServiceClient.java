package com.marketplace.payment.Services.Client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/internal/orders")
@RegisterRestClient(configKey="order-service-api")
public interface OrderServiceClient {

    @POST
    @Path("/{orderId}/payment-confirmed")
    Response fulfillOrder(@PathParam("orderId") String orderId, @QueryParam("txId") String stripeTransactionId);

}
