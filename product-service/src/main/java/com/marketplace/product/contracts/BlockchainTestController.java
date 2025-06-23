package com.marketplace.product.contracts;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.apache.camel.FluentProducerTemplate;

import java.math.BigInteger;

@Path("/internal/test/blockchain")
public class BlockchainTestController {

    @Inject
    FluentProducerTemplate producerTemplate;

    @POST
    @Path("/mark-paid/{orderId}")
    public Response testMarkAsPaid(@PathParam("orderId") Long orderId) {
        if (orderId == null || orderId <= 0) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Order ID").build();
        }

        try {
            System.out.println("TEST ENDPOINT: Triggering blockchain update for order: " + orderId);

            producerTemplate
                    .to("direct:markAsPaid")
                    .withBody(BigInteger.valueOf(orderId))
                    .send();

            String successMessage = "TEST ENDPOINT: Successfully queued blockchain update for order " + orderId;
            System.out.println(successMessage);
            return Response.ok(successMessage).build();

        } catch (Exception e) {
            String errorMessage = "TEST ENDPOINT: Error triggering Camel route: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        }
    }
}
