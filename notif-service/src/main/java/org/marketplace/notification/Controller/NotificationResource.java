package org.marketplace.notification.Controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.marketplace.notification.Entity.Notification;
import org.marketplace.notification.DTOs.EmailRequest;
import org.marketplace.notification.DTOs.InAppNotificationRequest;
import org.marketplace.notification.DTOs.WebSocketNotificationRequest;
import org.marketplace.notification.Service.NotificationService;

import java.util.List;

@Path("/api/v1/notifs")
public class NotificationResource {
    @Inject
    NotificationService notificationService;

    @GET
    @Produces("application/json")
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @POST
    @Path("/email")
    @Consumes("application/json")
    @Produces("application/json")
    public Response sendEmail(EmailRequest request) {
        notificationService.sendEmail(request.getTo(), request.getSubject(), request.getContent());
        return Response.ok().build();
    }

    @GET
    @Path("/emails")
    @Produces("application/json")
    public Response getAllEmails() {
        List<Notification> emails = notificationService.getAllEmails();
        return Response.ok(emails).build();
    }


    @POST
    @Path("/in-app")
    @Produces("application/json")
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
    @Produces("application/json")
    public Response getUserNotifications(@PathParam("userId") String userId) {
        return Response.ok(notificationService.getUserNotifications(userId)).build();
    }

    @GET
    @Path("/user/{userId}/unread")
    @Produces("application/json")
    public Response getUnreadNotifications(@PathParam("userId") String userId) {
        return Response.ok(notificationService.getUnreadNotifications(userId)).build();
    }

    @PUT
    @Path("/{id}/read")
    @Produces("application/json")
    public Response markAsRead(@PathParam("id") String id) {
        notificationService.markAsRead(id);
        return Response.ok().build();
    }

    @POST
    @Path("/websocket")
    @Produces("application/json")
    public Response sendWebSocketNotification(WebSocketNotificationRequest request) {
        notificationService.sendWebSocketNotification(
                request.getUserId(),
                request.getTitle(),
                request.getMessage()
        );
        return Response.ok().build();
    }
}
