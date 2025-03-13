package org.marketplace.notification.Service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.marketplace.notification.Controller.NotificationWebSocket;

@ApplicationScoped
public class WebSocketService {

    @Inject
    NotificationWebSocket webSocket;

    public void sendNotification(String userId, String message) {
        webSocket.sendNotification(userId, message);
    }
}
