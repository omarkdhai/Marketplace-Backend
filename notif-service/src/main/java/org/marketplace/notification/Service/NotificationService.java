package org.marketplace.notification.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.marketplace.notification.Entity.Notification;
import org.marketplace.notification.Enum.NotifType;
import org.marketplace.notification.Repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class NotificationService {
    @Inject
    NotificationRepository repository;

    @Inject
    Mailer mailer;

    @Inject
    WebSocketSessionManager webSocketManager;

    @Inject
    ObjectMapper objectMapper;

    public void sendEmail(String to, String subject, String content) {
        mailer.send(Mail.withText(to, subject, content));

        Notification notification = new Notification();
        notification.setUserId(to);
        notification.setTitle(subject);
        notification.setMessage(content);
        notification.setType(NotifType.EMAIL);
        notification.setCreatedAt(LocalDateTime.now());

        repository.persist(notification);
    }

    public void sendInAppNotification(String userId, String title, String message) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(NotifType.IN_APP);
        notification.setCreatedAt(LocalDateTime.now());

        repository.persist(notification);
    }

    public List<Notification> getUserNotifications(String userId) {
        return repository.findByUserId(userId);
    }

    public List<Notification> getUnreadNotifications(String userId) {
        return repository.findUnreadByUserId(userId);
    }

    public void markAsRead(String notificationId) {
        Notification notification = repository.findById(new ObjectId(notificationId));
        if (notification != null) {
            notification.setRead(true);
            repository.update(notification);
        }
    }

    public void sendWebSocketNotification(String userId, String title, String message) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(NotifType.WEBSOCKET);
            notification.setCreatedAt(LocalDateTime.now());

            repository.persist(notification);

            Map<String, String> wsMessage = new HashMap<>();
            wsMessage.put("type", "NOTIFICATION");
            wsMessage.put("title", title);
            wsMessage.put("message", message);

            String jsonMessage = objectMapper.writeValueAsString(wsMessage);
            webSocketManager.sendToUser(userId, jsonMessage);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
