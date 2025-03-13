package org.marketplace.notification.Controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/notifications/ws/{userId}")
@ApplicationScoped
public class NotificationWebSocket {

    private Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        sessions.put(userId, session);
        session.getAsyncRemote().sendText("Connected to notification service");
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        sessions.remove(userId);
    }

    @OnError
    public void onError(Session session, @PathParam("userId") String userId, Throwable throwable) {
        sessions.remove(userId);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("userId") String userId) {
        // Handle incoming messages
        Session session = sessions.get(userId);
        if (session != null) {
            session.getAsyncRemote().sendText("Received: " + message);
        }
    }

    public void sendNotification(String userId, String message) {
        Session session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        }
    }
}
