package org.marketplace.notification.Controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.marketplace.notification.Service.WebSocketService;

@Path("/notify")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WebSocketResource {

    @Inject
    WebSocketService webSocketService;

    @POST
    @Path("/{userId}")
    public Response sendNotification(
            @PathParam("userId") String userId,
            String message) {
        webSocketService.sendNotification(userId, message);
        return Response.ok().build();
    }
}
