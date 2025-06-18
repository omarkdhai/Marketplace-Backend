package com.marketplace.payment.Services.Scheduler;

import com.marketplace.payment.Services.Client.OrderServiceClient;
import com.marketplace.payment.Services.Model.FailedFulfillment;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class FulfillmentRetryScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FulfillmentRetryScheduler.class);
    private static final int MAX_ATTEMPTS = 5;

    @Inject
    @RestClient
    OrderServiceClient orderServiceClient;

    @Scheduled(every = "60s")
    void retryFailedFulfillments() {
        LOGGER.info("Running scheduled job: Retrying failed fulfillments...");

        // Find all jobs that haven't exceeded the max attempt count
        List<FailedFulfillment> jobsToRetry = FailedFulfillment.list("attemptCount < ?1", MAX_ATTEMPTS);

        if (jobsToRetry.isEmpty()) {
            LOGGER.info("No failed fulfillments to retry.");
            return;
        }

        for (FailedFulfillment job : jobsToRetry) {
            LOGGER.info("Retrying fulfillment for orderId: {}. Attempt #{}", job.orderId, job.attemptCount + 1);
            try {
                Response response = orderServiceClient.fulfillOrder(job.orderId);

                if (response.getStatus() < 300) {
                    LOGGER.info("SUCCESS on retry for orderId: {}. Deleting job.", job.orderId);
                    job.delete(); // Success! Remove it from the retry queue.
                } else {
                    handleRetryFailure(job, "Order service responded with status " + response.getStatus());
                }
            } catch (Exception e) {
                handleRetryFailure(job, "Error calling order-service: " + e.getMessage());
            }
        }
    }

    private void handleRetryFailure(FailedFulfillment job, String errorMessage) {
        LOGGER.warn("Retry FAILED for orderId: {}. Reason: {}", job.orderId, errorMessage);
        job.attemptCount++;
        job.lastAttemptAt = LocalDateTime.now();
        job.lastErrorMessage = errorMessage;

        if (job.attemptCount >= MAX_ATTEMPTS) {
            LOGGER.error("CRITICAL: Fulfillment for orderId {} has failed {} times and will not be retried. Please investigate manually.", job.orderId, MAX_ATTEMPTS);
        }

        job.update();
    }
}
