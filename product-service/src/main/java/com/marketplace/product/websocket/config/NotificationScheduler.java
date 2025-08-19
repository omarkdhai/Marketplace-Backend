package com.marketplace.product.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class NotificationScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationScheduler.class);

    @Inject
    NotificationWebSocket notificationWebSocket;

    @Inject
    NotificationRepository notificationRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(every = "5m", identity = "favorites-reminder-job")
    void sendFavoritesReminder() {
        try {
            String action = "REMINDER_FAVORITES";
            String title = "Don't miss out!";
            String messageText = "Check your favorite products for new updates or special offers.";

            Notification dbNotification = new Notification(action, title, messageText);
            notificationRepository.persist(dbNotification);

            Map<String, String> frontendPayload = new HashMap<>();
            frontendPayload.put("action", action);
            frontendPayload.put("title", title);
            frontendPayload.put("message", messageText);
            frontendPayload.put("timestamp", dbNotification.timestamp.toString());

            String messageJson = objectMapper.writeValueAsString(frontendPayload);

            notificationWebSocket.broadcast(messageJson);

            LOGGER.info("Successfully broadcasted and persisted 'favorites reminder' notification.");

        } catch (Exception e) {
            LOGGER.error("Failed to send favorites reminder notification", e);
        }
    }
}
