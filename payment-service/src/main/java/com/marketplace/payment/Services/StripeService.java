package com.marketplace.payment.Services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class StripeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StripeService.class);

    @ConfigProperty(name = "stripe.secret.key")
    String secretKey;

    @PostConstruct
    void init() {
        if (secretKey == null || secretKey.trim().isEmpty()) {
            LOGGER.error("Stripe secret key is not configured. Stripe integration will not work.");
            return;
        }
        Stripe.apiKey = secretKey;
        LOGGER.info("Stripe API Key initialized.");
    }

    public PaymentIntent createPaymentIntent(long amountInSmallestUnit, String currency, String orderId, String customerEmail) throws StripeException {
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(amountInSmallestUnit)
                        .setCurrency(currency.toLowerCase())
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        // Optional: Add metadata
                        .putMetadata("order_id", orderId)
                        .putMetadata("customer_email", customerEmail)
                        .setDescription("Marketplace Order Payment for " + orderId)
                        .build();

        LOGGER.info("Creating PaymentIntent for orderId: {}, amount: {}, currency: {}", orderId, amountInSmallestUnit, currency);
        PaymentIntent paymentIntent = PaymentIntent.create(params);
        LOGGER.info("PaymentIntent created successfully: {}", paymentIntent.getId());
        return paymentIntent;
    }
}
