package com.marketplace.product.websocket.config;

import com.marketplace.product.Entity.Transaction;
import com.marketplace.product.Repository.TransactionRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/v1/orderNotification")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrderNotificationController {

    @Inject
    OrderNotificationRepository orderNotificationRepository;

    @GET
    public Response findAllOrderNotification() {
        return Response.status(Response.Status.OK)
                .entity(orderNotificationRepository.findAll())
                .build();
    }


}
