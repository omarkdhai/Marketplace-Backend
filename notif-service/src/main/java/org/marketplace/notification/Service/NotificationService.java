package org.marketplace.notification.Service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.marketplace.notification.Entity.NotificationPreferences;
import org.marketplace.notification.Entity.NotificationRequest;
import org.marketplace.notification.Entity.NotificationType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ApplicationScoped
public class NotificationService {

    private static final Logger LOG = Logger.getLogger(NotificationService.class);

    @Inject
    KeycloakUserService keycloakUserService;

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "quarkus.mailer.from")
    String mailFrom;

    public NotificationPreferences getUserPreferences(String userId) {
        Map<String, List<String>> attributes = keycloakUserService.getUserAttributes(userId);
        return NotificationPreferences.fromAttributes(attributes);
    }

    public boolean updateUserPreferences(String userId, NotificationPreferences preferences) {
        Map<String, List<String>> attributes = preferences.toAttributes();
        return keycloakUserService.updateUserAttributes(userId, attributes);
    }

    public void sendNotification(String userId, NotificationRequest request) {
        LOG.infof("Received notification request for user %s, type %s", userId, request.getType());

        // Get User Preferences
        NotificationPreferences prefs = getUserPreferences(userId);

        // Check if user exists and get email (only if needed)
        Optional<String> userEmailOpt = Optional.empty();
        boolean requiresEmail = shouldSendEmail(prefs, request.getType());

        if (requiresEmail) {
            userEmailOpt = keycloakUserService.getUserEmail(userId);
            if (userEmailOpt.isEmpty()) {
                LOG.warnf("Cannot send email. User %s not found or has no email.", userId);
                requiresEmail = false; // Skip email if user/email not found
            }
        }

        // Send Email if applicable
        if (requiresEmail) {
            sendEmailNotification(userEmailOpt.get(), request.getSubject(), request.getBody());
        } else {
            LOG.debugf("Skipping email for user %s, type %s based on preferences or missing email.", userId, request.getType());
        }


        // Send Push Notification if applicable
        if (shouldSendPush(prefs, request.getType())) {
            sendPushNotification(userId, request.getSubject(), request.getBody(), request.getData());
        } else {
            LOG.debugf("Skipping push notification for user %s, type %s based on preferences.", userId, request.getType());
        }
    }

    private boolean shouldSendEmail(NotificationPreferences prefs, NotificationType type) {
        if (!prefs.isEmailEnabled()) return false;

        return switch (type) {
            case ORDER_STATUS -> prefs.isEmailOrderStatus();
            case LOW_STOCK -> prefs.isEmailLowStock();
            case PROMOTION -> prefs.isEmailPromotions();
            case GENERAL -> true; // Assuming general emails are always sent if email is enabled
        };
    }

    private boolean shouldSendPush(NotificationPreferences prefs, NotificationType type) {
        if (!prefs.isPushEnabled()) return false;

        return switch (type) {
            case ORDER_STATUS -> prefs.isPushOrderStatus();
            case LOW_STOCK -> prefs.isPushLowStock();
            case PROMOTION -> prefs.isPushPromotions();
            case GENERAL -> true; // Assuming general push are always sent if push is enabled
        };
    }

    private void sendEmailNotification(String toEmail, String subject, String body) {
        try {
            LOG.infof("Attempting to send email to %s with subject: %s", toEmail, subject);
            mailer.send(Mail.withText(toEmail, subject, body).setFrom(mailFrom));
            LOG.infof("Successfully sent email to %s", toEmail);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to send email to %s", toEmail);
        }
    }

    private void sendPushNotification(String userId, String title, String body, Map<String, String> data) {
        // --- Placeholder for Push Notification Logic ---
        // This is where you would integrate with FCM, APNS, etc.
        // You would typically need:
        // 1. A way to retrieve the user's device registration token(s) (often stored in a separate DB).
        // 2. A client library for your chosen push service (e.g., Firebase Admin SDK).
        // 3. Logic to construct the push message payload (title, body, data).
        // 4. Sending the message via the push service client.

        LOG.infof("--- PUSH NOTIFICATION ---");
        LOG.infof("To User ID: %s", userId);
        LOG.infof("Title: %s", title);
        LOG.infof("Body: %s", body);
        LOG.infof("Data: %s", data != null ? data.toString() : "null");
        LOG.infof("--- END PUSH (Placeholder) ---");

        // Example: In a real scenario you might do:
        // List<String> deviceTokens = getDeviceTokensForUser(userId);
        // for (String token : deviceTokens) {
        //     firebaseMessagingClient.send(token, title, body, data);
        // }
    }
}
