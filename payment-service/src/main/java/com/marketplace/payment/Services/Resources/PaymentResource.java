package com.marketplace.payment.Services.Resources;

import com.marketplace.payment.Services.DTO.*;
import com.marketplace.payment.Services.StripeService;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


@Path("/api/v1/payments")
@Produces(MediaType.APPLICATION_JSON)
public class PaymentResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentResource.class);

    @Inject
    StripeService stripeService;

    @POST
    @Path("/create-customer")
    public Response createCustomer(CreateCustomerRequest request) {
        LOGGER.info("Received request to create customer for email: {}", request.getEmail());
        if (request.getEmail() == null || request.getPaymentMethodId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"email and paymentMethodId are required.\"}")
                    .build();
        }
        try {
            Customer customer = stripeService.createCustomer(request);
            return Response.ok(new CreateCustomerResponse(customer.getId())).build();
        } catch (Exception e) {
            LOGGER.error("Failed to create customer", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/get-or-create-customer")
    public Response getOrCreateCustomer(Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("{\"error\":\"email is required.\"}").build();
        }
        try {
            // We pass null for paymentMethodId as we are not attaching a card here, just ensuring customer exists
            Customer customer = stripeService.getOrCreateCustomer(email, null);
            return Response.ok(Map.of("customerId", customer.getId())).build();
        } catch (Exception e) {
            LOGGER.error("Failed to get or create customer", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/create-payment-intent")
    public Response createPaymentIntent(CreatePaymentRequest request) {
        LOGGER.info("Received request to create payment intent for customer: {}", request.getCustomerId());
        if (request.getCustomerId() == null || request.getAmount() == null || request.getCurrency() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"customerId, amount, and currency are required.\"}")
                    .build();
        }
        try {
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(request);
            CreatePaymentResponse paymentResponse = new CreatePaymentResponse(paymentIntent.getClientSecret(), paymentIntent.getStatus());
            return Response.ok(paymentResponse).build();
        } catch (Exception e) {
            LOGGER.error("Failed to create payment intent for order: " + request.getOrderId(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/stripe-events")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response handleStripeWebhook(String payload, @HeaderParam("Stripe-Signature") String sigHeader) {
        LOGGER.info("Stripe webhook event received.");
        try {
            stripeService.handleWebhookEvent(payload, sigHeader);
            return Response.ok("Webhook processed").build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal server error").build();
        }
    }
}
