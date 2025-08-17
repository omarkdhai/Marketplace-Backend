package com.marketplace.product.websocket.controller;

import com.marketplace.product.Entity.ProceedOrder;
import com.marketplace.product.Entity.Product;
import com.marketplace.product.websocket.config.NotificationWebSocket;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;

@Path("/notify")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

  @Inject
  NotificationWebSocket notificationWebSocket;

  @POST
  public Response addProduct(Product product) throws IOException {
    notificationWebSocket.broadcastProductNotification(product, "PRODUCT_CREATED");
    return Response.ok(product).build();
  }

  @POST
  @Path("/shipped")
  public Response shipOrder(ProceedOrder order) throws IOException {
    notificationWebSocket.broadcastOrderNotification(order, "SHIP_ORDER");
    return Response.ok(order).build();
  }
}