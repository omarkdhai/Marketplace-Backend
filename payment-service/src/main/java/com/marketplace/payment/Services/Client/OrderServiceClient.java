package com.marketplace.payment.Services.Client;

import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v1/orders")
@RegisterRestClient(configKey="order-service-api")
public interface OrderServiceClient {

    @PATCH
    @Path("/{id}/fulfill")
    @Produces(MediaType.TEXT_PLAIN)
    Response fulfillOrder(@PathParam("id") String orderId);
}
