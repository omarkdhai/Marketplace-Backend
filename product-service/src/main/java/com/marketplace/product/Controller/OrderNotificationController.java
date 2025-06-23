package com.marketplace.product.Controller;

import com.marketplace.product.Service.ProceedOrderService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.apache.camel.FluentProducerTemplate;

import java.math.BigInteger;

@Path("/internal/orders")
public class OrderNotificationController {

    @Inject
    FluentProducerTemplate producerTemplate;

    @Inject
    ProceedOrderService proceedOrderService;

    @POST
    @Path("/{orderId}/payment-confirmed")
    public Response onPaymentConfirmed(
            @PathParam("orderId") String orderId,
            @QueryParam("txId") String stripeTransactionId) {

        System.out.println("REAL FLOW: Received payment confirmation for order ID: " + orderId);

        // Update db
        boolean dbUpdateSuccess = proceedOrderService.updateOrderStatusToPaid(orderId, stripeTransactionId);

        if (!dbUpdateSuccess) {
            return Response.status(Response.Status.NOT_FOUND).entity("Order not found in DB: " + orderId).build();
        }

        System.out.println("MongoDB status updated to PAID for order " + orderId);

        // Update Blockchain
        try {
            Long orderIdAsLong = Long.parseLong(orderId);
            System.out.println("Triggering blockchain update for order: " + orderId);
            producerTemplate
                    .to("direct:markAsPaid")
                    .withBody(BigInteger.valueOf(orderIdAsLong))
                    .send();
            System.out.println("REAL FLOW: Blockchain update successfully queued for order: " + orderId);

            return Response.ok().entity("{\"status\":\"db_and_blockchain_updates_queued\"}").build();

        } catch (NumberFormatException e) {
            System.err.println("CRITICAL: Order ID " + orderId + " is not a valid number for blockchain processing.");
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid Order ID format for blockchain.").build();
        } catch (Exception e) {
            String errorMessage = "CRITICAL: DB was updated, but failed to queue blockchain update for order: " + orderId;
            System.err.println(errorMessage + ". Error: " + e.getMessage());

            //Alert
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errorMessage).build();
        }
    }
}
