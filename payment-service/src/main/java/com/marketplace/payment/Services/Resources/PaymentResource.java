package com.marketplace.payment.Services.Resources;

import com.marketplace.payment.Services.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

@Path("/api/v1/payments")
public class PaymentResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentResource.class);

    @Inject
    StripeService stripeService;

    // DTO for the request body from the frontend
    public static class CreatePaymentIntentRequest {
        public String orderId;
        public long amount;
        public String currency;
        public String customerEmail;
    }

    @POST
    @Path("/create-payment-intent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPaymentIntent(CreatePaymentIntentRequest request) {
        if (request == null || request.orderId == null || request.amount <= 0 || request.currency == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Collections.singletonMap("error", "Missing required payment details: orderId, amount, currency."))
                    .build();
        }

        LOGGER.info("Received request to create PaymentIntent for orderId: {}", request.orderId);
        try {
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                    request.amount,
                    request.currency,
                    request.orderId,
                    request.customerEmail
            );

            // Return only the client_secret to the frontend
            Map<String, String> responseData = Collections.singletonMap("clientSecret", paymentIntent.getClientSecret());
            return Response.ok(responseData).build();
        } catch (StripeException e) {
            LOGGER.error("StripeException while creating PaymentIntent for orderId {}: {}", request.orderId, e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Collections.singletonMap("error", "Failed to create payment intent: " + e.getMessage()))
                    .build();
        } catch (Exception e) {
            LOGGER.error("Unexpected error while creating PaymentIntent for orderId {}: {}", request.orderId, e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Collections.singletonMap("error", "An unexpected error occurred while creating the payment intent."))
                    .build();
        }
    }
}
