package com.marketplace.product.websocket.config;

import com.marketplace.product.Entity.CartProduct;
import com.marketplace.product.Entity.ProceedOrder;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class OrderNotification extends PanacheMongoEntity {
  private String action;
  private String orderId;
  private java.time.Instant timestamp;


  public OrderNotification(String action, String orderId) {
    this.action = action;
    this.orderId = orderId;
    this.timestamp = java.time.Instant.now();
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getOrderId() {
    return orderId;
  }

  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }
}
