package org.marketplace.notification.Interface;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.marketplace.notification.Entity.NotificationRequest;

@Path("/api/v1/notifs")
@RegisterRestClient(configKey="notif-service")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface NotificationServiceClient {

    @POST
    @Path("/send/{userId}")
    Response sendNotification(@PathParam("userId") String userId, NotificationRequest request);
}
