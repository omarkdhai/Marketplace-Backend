package com.marketplace.product.websocket.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marketplace.product.Entity.ProceedOrder;
import com.marketplace.product.Entity.Product;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/websocket/notifications")
@ApplicationScoped
public class NotificationWebSocket {

  @Inject
  OrderNotificationRepository orderNotificationRepository;

  @Inject
  NotificationRepository notificationRepository;
  private final Logger Log  = LoggerFactory.getLogger(NotificationWebSocket.class.getName());
  private static final Map<String, Session> sessions = new ConcurrentHashMap<>();
  private final ObjectMapper objectMapper;

  public NotificationWebSocket() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());
  }

  @OnOpen
  public void onOpen(Session session) {
    sessions.put(session.getId(), session);
    Log.info("WebSocket connection opened: " + session.getId());
    Log.info("Total active sessions: " + sessions.size());
  }

  @OnClose
  public void onClose(Session session) {
    sessions.remove(session.getId());
    Log.info("WebSocket connection closed: " + session.getId());
    Log.info("Total active sessions: " + sessions.size());
  }

  @OnError
  public void onError(Session session, Throwable throwable) {
    sessions.remove(session.getId());
    Log.error("WebSocket error for session " + session.getId(), throwable);
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    Log.info("Received message from " + session.getId() + ": " + message);
    if ("ping".equals(message)) {
      sendToSession(session, "pong");
    }
  }

  public void broadcastProductNotification(Product product, String action) {
    try {
      Map<String, Object> frontendPayload = Map.of(
              "action", action,
              "productId", product.getId().toString(),
              "name", product.getName(),
              "description", product.getDescription(),
              "price", product.getPrice(),
              "photo", product.getPhoto(),
              "timestamp", Instant.now().toString()
      );

      Notification dbNotification = new Notification(action, frontendPayload);
      notificationRepository.persist(dbNotification);

      String message = objectMapper.writeValueAsString(frontendPayload);
      broadcast(message);
      Log.info("Broadcasted and persisted product notification: " + action + " - " + product.getName());
    } catch (Exception e) {
      Log.error("Error broadcasting product notification", e);
    }
  }


  public void broadcastOrderNotification(ProceedOrder proceedOrder, String action) {
    try {
      Map<String, Object> frontendPayload = Map.of(
              "action", action,
              "orderId", proceedOrder.getId().toString(),
              "timestamp", Instant.now().toString()
      );
      Notification dbNotification = new Notification(action, frontendPayload);
      notificationRepository.persist(dbNotification);
      String message = objectMapper.writeValueAsString(frontendPayload);
      broadcast(message);
      Log.info("Broadcasted and persisted order notification: " + action + " - " + proceedOrder.getId().toString());
    } catch (Exception e) {
      Log.error("Error broadcasting order notification", e);
    }
  }

  public void broadcast(String message) {
    sessions.values().forEach(session -> sendToSession(session, message));
  }

  private void sendToSession(Session session, String message) {
    try {
      if (session.isOpen()) {
        session.getAsyncRemote().sendText(message);
      } else {
        sessions.remove(session.getId());
      }
    } catch (Exception e) {
      Log.error("Error sending message to session " + session.getId(), e);
      sessions.remove(session.getId());
    }
  }
}