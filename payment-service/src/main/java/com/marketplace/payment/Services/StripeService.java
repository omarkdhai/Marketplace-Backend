package com.marketplace.payment.Services;

import com.marketplace.payment.Services.Client.OrderServiceClient;
import com.marketplace.payment.Services.DTO.CreateCustomerRequest;
import com.marketplace.payment.Services.DTO.CreatePaymentRequest;
import com.marketplace.payment.Services.Model.FailedFulfillment;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerListParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class StripeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StripeService.class);

    @Inject
    @RestClient
    OrderServiceClient orderServiceClient;

    @Inject
    @ConfigProperty(name = "stripe.secret.key")
    String secretKey;

    @Inject
    @ConfigProperty(name = "stripe.webhook.secret")
    String webhookSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        LOGGER.info("Stripe service initialized.");
    }

    public Customer createCustomer(CreateCustomerRequest request) {
        try {
            PaymentMethodAttachParams attachParams = PaymentMethodAttachParams.builder()
                    .setCustomer(request.getEmail())
                    .build();

            CustomerCreateParams customerParams = CustomerCreateParams.builder()
                    .setEmail(request.getEmail())
                    .setPaymentMethod(request.getPaymentMethodId())
                    .setInvoiceSettings(
                            CustomerCreateParams.InvoiceSettings.builder()
                                    .setDefaultPaymentMethod(request.getPaymentMethodId())
                                    .build()
                    )
                    .build();

            Customer customer = Customer.create(customerParams);
            LOGGER.info("Successfully created Stripe Customer with ID: {}", customer.getId());
            return customer;
        } catch (StripeException e) {
            LOGGER.error("Stripe error while creating customer: {}", e.getMessage());
            throw new RuntimeException("Error creating Stripe customer: " + e.getMessage(), e);
        }
    }

    public Customer getOrCreateCustomer(String email, String paymentMethodId) {
        try {
            // Search for existing customers with this email
            CustomerListParams listParams = CustomerListParams.builder().setEmail(email).setLimit(1L).build();
            CustomerCollection customers = Customer.list(listParams);

            if (!customers.getData().isEmpty()) {
                // Customer exists, return the first one found
                Customer existingCustomer = customers.getData().get(0);
                LOGGER.info("Found existing Stripe Customer with ID: {}", existingCustomer.getId());
                return existingCustomer;
            } else {
                // Customer does not exist, create a new one
                LOGGER.info("No existing customer found for email: {}. Creating new one.", email);
                CustomerCreateParams createParams = CustomerCreateParams.builder()
                        .setEmail(email)
                        .setName(email)
                        .build();
                Customer newCustomer = Customer.create(createParams);
                LOGGER.info("Successfully created new Stripe Customer with ID: {}", newCustomer.getId());
                return newCustomer;
            }
        } catch (StripeException e) {
            LOGGER.error("Stripe error while getting or creating customer: {}", e.getMessage());
            throw new RuntimeException("Error getting or creating Stripe customer: " + e.getMessage(), e);
        }
    }

    public PaymentIntent createPaymentIntent(CreatePaymentRequest request) {
        try {
            PaymentIntentCreateParams.AutomaticPaymentMethods automaticPaymentMethods =
                    PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                            .build();

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(request.getAmount())
                    .setCurrency(request.getCurrency())
                    .setCustomer(request.getCustomerId())
                    .putMetadata("order_id", request.getOrderId())
                    .setAutomaticPaymentMethods(automaticPaymentMethods)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);
            LOGGER.info("Successfully created PaymentIntent with ID: {}", paymentIntent.getId());
            return paymentIntent;
        } catch (StripeException e) {
            LOGGER.error("Stripe error while creating payment intent: {}", e.getMessage());
            throw new RuntimeException("Error creating payment intent: " + e.getMessage(), e);
        }
    }

    public void handleWebhookEvent(String payload, String sigHeader) {
        if (sigHeader == null || payload == null) {
            throw new BadRequestException("Missing signature or payload");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

        } catch (SignatureVerificationException e) {
            LOGGER.error("Webhook error: Invalid signature.", e);
            throw new BadRequestException("Invalid Stripe signature.");
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = dataObjectDeserializer.getObject().orElse(null);

        if (stripeObject == null) {
            LOGGER.warn("Webhook event data could not be deserialized. Event ID: {}", event.getId());
            return;
        }

        LOGGER.info("Received Stripe event: type='{}', id='{}'", event.getType(), event.getId());

        // Handle the event
        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                LOGGER.info("✅ Payment succeeded for PaymentIntent: {}", paymentIntent.getId());

                String orderId = paymentIntent.getMetadata().get("order_id");
                if (orderId == null || orderId.isEmpty()) {
                    LOGGER.error("CRITICAL: PaymentIntent {} succeeded but has no order_id in metadata!", paymentIntent.getId());
                    return;
                }

                LOGGER.info("Payment for order_id: {} has succeeded. Calling order-service to fulfill.", orderId);

                try {
                    Response response = orderServiceClient.fulfillOrder(orderId, paymentIntent.getId());

                    if (response.getStatus() >= 300) {
                        String errorMessage = "Order service responded with status " + response.getStatus();
                        LOGGER.error("Failed to fulfill order {}. {}", orderId, errorMessage);
                        saveFailedFulfillment(orderId, paymentIntent.getId(), errorMessage);
                    } else {
                        LOGGER.info("Successfully notified order-service for order {}. Status: {}", orderId, response.getStatus());
                    }
                } catch (Exception e) {
                    String errorMessage = "Error calling order-service: " + e.getMessage();
                    LOGGER.error("CRITICAL: {} for order {}", errorMessage, orderId);
                    saveFailedFulfillment(orderId, paymentIntent.getId(), errorMessage);
                }
                break;

            case "payment_intent.payment_failed":
                PaymentIntent failedPaymentIntent = (PaymentIntent) stripeObject;
                LOGGER.error("❌ Payment failed for PaymentIntent: {}. Reason: {}", failedPaymentIntent.getId(), failedPaymentIntent.getLastPaymentError().getMessage());
                break;

            default:
                LOGGER.warn("Unhandled event type: {}", event.getType());
        }
    }

    private void saveFailedFulfillment(String orderId, String paymentIntentId, String errorMessage) {
        LOGGER.info("Persisting failed fulfillment for orderId: {}", orderId);
        FailedFulfillment failedJob = new FailedFulfillment(orderId, paymentIntentId, errorMessage);
        failedJob.persist();
    }
}
