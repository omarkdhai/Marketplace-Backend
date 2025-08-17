package com.marketplace.product.websocket.config;

import com.marketplace.product.Entity.CartProduct;
import com.marketplace.product.Entity.ProceedOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class OrderNotification {
  private String action;
  private String mongoId;
  public LocalDateTime createdAt = LocalDateTime.now();
  public List<CartProduct> products = new ArrayList<>();
  private String latestStatus;
}
