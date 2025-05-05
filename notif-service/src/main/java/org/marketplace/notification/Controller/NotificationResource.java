package org.marketplace.notification.Controller;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.marketplace.notification.Entity.NotificationPreferences;
import org.marketplace.notification.Entity.NotificationRequest;
import org.marketplace.notification.Service.KeycloakUserService;
import org.marketplace.notification.Service.NotificationService;

@Path("/api/v1/notifs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NotificationResource {

    private static final Logger LOG = Logger.getLogger(NotificationResource.class);

    @Inject
    NotificationService notificationService;

    @Inject
    KeycloakUserService keycloakUserService;

    @POST
    @Path("/send/{userId}")
    public Response sendNotification(
            @PathParam("userId") String userId,
            @Valid NotificationRequest request) {
        LOG.debugf("Received API request to send notification to user %s", userId);
        try {
            notificationService.sendNotification(userId, request);
            return Response.accepted().entity("Notification request accepted for user " + userId).build();
        } catch (NotFoundException e) {
            LOG.warnf("Cannot send notification, user not found: %s", userId);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.errorf(e, "Error processing notification request for user %s", userId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to process notification request").build();
        }
    }

    @GET
    @Path("/preferences/{userId}")
    public Response getPreferences(@PathParam("userId") String userId) {
        LOG.debugf("Received API request to get preferences for user %s", userId);
        try {
            NotificationPreferences prefs = notificationService.getUserPreferences(userId);
            keycloakUserService.findUserById(userId).orElseThrow(() -> new NotFoundException("User not found: " + userId));
            return Response.ok(prefs).build();
        } catch (NotFoundException e) {
            LOG.warnf("Cannot get preferences, user not found: %s", userId);
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            LOG.errorf(e, "Error getting preferences for user %s", userId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to get preferences").build();
        }
    }

    @PUT
    @Path("/preferences/{userId}")
    public Response updatePreferences(
            @PathParam("userId") String userId,
            NotificationPreferences preferences) {
        LOG.debugf("Received API request to update preferences for user %s", userId);
        try {
            boolean updated = notificationService.updateUserPreferences(userId, preferences);
            if (updated) {
                return Response.ok(preferences).entity("Preferences updated successfully for user " + userId).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found or update failed for user " + userId).build();
            }
        } catch (Exception e) {
            LOG.errorf(e, "Error updating preferences for user %s", userId);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to update preferences").build();
        }
    }
}
