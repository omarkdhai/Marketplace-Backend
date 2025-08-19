package com.marketplace.product.websocket.config;

import io.quarkus.mongodb.panache.PanacheMongoEntity;

import java.time.Instant;
import java.util.Map;

public class Notification extends PanacheMongoEntity {

    public String action;
    public String title;
    public String message;
    public Instant timestamp;

    public Map<String, Object> details;

    public Notification() {
        this.timestamp = Instant.now();
    }

    public Notification(String action, String title, String message) {
        this.action = action;
        this.title = title;
        this.message = message;
        this.timestamp = Instant.now();
    }

    public Notification(String action, Map<String, Object> details) {
        this.action = action;
        this.details = details;
        this.timestamp = Instant.now();
    }
}
