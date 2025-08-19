package com.marketplace.product.websocket.config;

import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/v1/notifications")
@Produces(MediaType.APPLICATION_JSON)
public class NotificationController {

    @Inject
    NotificationRepository notificationRepository;

    @GET
    public Response getRecentNotifications() {
        List<Notification> notifications = notificationRepository.findAll(Sort.by("timestamp").descending())
                .page(Page.of(0, 50))
                .list();

        return Response.ok(notifications).build();
    }

    @DELETE
    public Response clearAllNotifications() {
        try {
            notificationRepository.deleteAll();
            return Response.noContent().build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Failed to clear notifications.\"}")
                    .build();
        }
    }
}
