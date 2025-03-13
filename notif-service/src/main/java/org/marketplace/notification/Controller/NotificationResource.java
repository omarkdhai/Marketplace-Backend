package org.marketplace.notification.Controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.marketplace.notification.DTOs.EmailRequest;
import org.marketplace.notification.DTOs.InAppNotificationRequest;
import org.marketplace.notification.DTOs.WebSocketNotificationRequest;
import org.marketplace.notification.Service.NotificationService;

@Path("/notifications")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationResource {
    @Inject
    NotificationService notificationService;

    @POST
    @Path("/email")
    public Response sendEmail(EmailRequest request) {
        notificationService.sendEmail(request.getTo(), request.getSubject(), request.getContent());
        return Response.ok().build();
    }

    @POST
    @Path("/in-app")
    public Response sendInAppNotification(InAppNotificationRequest request) {
        notificationService.sendInAppNotification(
                request.getUserId(),
                request.getTitle(),
                request.getMessage()
        );
        return Response.ok().build();
    }

    @GET
    @Path("/user/{userId}")
    public Response getUserNotifications(@PathParam("userId") String userId) {
        return Response.ok(notificationService.getUserNotifications(userId)).build();
    }

    @GET
    @Path("/user/{userId}/unread")
    public Response getUnreadNotifications(@PathParam("userId") String userId) {
        return Response.ok(notificationService.getUnreadNotifications(userId)).build();
    }

    @PUT
    @Path("/{id}/read")
    public Response markAsRead(@PathParam("id") String id) {
        notificationService.markAsRead(id);
        return Response.ok().build();
    }

    @POST
    @Path("/websocket")
    public Response sendWebSocketNotification(WebSocketNotificationRequest request) {
        notificationService.sendWebSocketNotification(
                request.getUserId(),
                request.getTitle(),
                request.getMessage()
        );
        return Response.ok().build();
    }
}
