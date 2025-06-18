package com.marketplace.product.Controller;

import com.marketplace.product.Clients.CreatePaymentResponse;
import com.marketplace.product.DTO.PaymentConfirmationRequest;
import com.marketplace.product.DTO.ProceedOrderDTO;
import com.marketplace.product.Entity.ProceedOrder;
import com.marketplace.product.Service.ProceedOrderService;
import com.marketplace.product.contracts.Web3jClientProducer;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/api/v1/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProceedOrderController {

    @Inject
    ProceedOrderService service;
    @Inject
    Web3jClientProducer web3jClientProducer;

    @POST
    public Response submitOrder(ProceedOrderDTO dto) {
        try {
            CreatePaymentResponse paymentResponse = service.save(dto);
            return Response.status(Response.Status.CREATED).entity(paymentResponse).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PATCH
    @Path("/{id}/fulfill")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_PLAIN)
    public Response fulfillOrder(@PathParam("id") String orderId) {
        try {
            service.fulfillOrder(orderId);
            return Response.ok("Order " + orderId + " marked as fulfilled.").build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to fulfill order.").build();
        }
    }

    @GET
    public List<ProceedOrder> getAllOrders() {
        return service.getAllOrders();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteOrder(@PathParam("id") String id) {
        boolean deleted = service.deleteOrder(id);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Order not found").build();
        }
    }

    @PUT
    @Path("/{id}/toggle-status")
    public Response toggleOrderStatus(@PathParam("id") String id) {
        boolean success = service.toggleOrderStatus(id);
        if (success) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Order not found")
                    .build();
        }
    }

    @POST
    @Path("/{orderId}/confirm-payment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmOrderPayment(
            @PathParam("orderId") String orderId,
            PaymentConfirmationRequest paymentDetails) {
        if (orderId == null || orderId.trim().isEmpty() || paymentDetails == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Missing orderId or payment details."))
                    .build();
        }

        boolean success = service.updatePaymentStatus(
                orderId,
                paymentDetails.paymentMethod,
                paymentDetails.paymentGatewayTransactionId,
                paymentDetails.paymentStatus
        );

        if (success) {
            return Response.ok(Map.of("message", "Payment status updated for order " + orderId)).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND) // Or INTERNAL_SERVER_ERROR if update failed for other reasons
                    .entity(Map.of("error", "Order not found or failed to update payment status."))
                    .build();
        }
    }
}
