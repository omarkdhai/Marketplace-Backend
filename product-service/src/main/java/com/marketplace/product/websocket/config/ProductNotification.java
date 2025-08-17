package com.marketplace.product.websocket.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ProductNotification {
  private String action;
  private String productId;
  private String name;
  private String description;
  private double price;
  private String status;
  private java.time.Instant creationDate;
  private String imgUrl;
  private java.time.Instant timestamp;

  public ProductNotification() {
    this.timestamp = java.time.Instant.now();
  }

  public ProductNotification(String action, String productId, String name,
                             String description, double price, String status,
                             java.time.Instant creationDate, String imgUrl) {
    this.action = action;
    this.productId = productId;
    this.name = name;
    this.description = description;
    this.price = price;
    this.status = status;
    this.creationDate = creationDate;
    this.imgUrl = imgUrl;
    this.timestamp = java.time.Instant.now();
  }

  // Getters and setters
  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }

  public String getProductId() { return productId; }
  public void setProductId(String productId) { this.productId = productId; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }

  public double getPrice() { return price; }
  public void setPrice(double price) { this.price = price; }

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }

  public java.time.Instant getCreationDate() { return creationDate; }
  public void setCreationDate(java.time.Instant creationDate) { this.creationDate = creationDate; }

  public String getImgUrl() { return imgUrl; }
  public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }

  public java.time.Instant getTimestamp() { return timestamp; }
  public void setTimestamp(java.time.Instant timestamp) { this.timestamp = timestamp; }
}
