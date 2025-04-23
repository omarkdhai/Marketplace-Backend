package com.marketplace.auth.Controller;

import com.marketplace.auth.Service.KeycloakService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;

@Path("/v1/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserController {

    @Inject
    KeycloakService keycloakService;

    @POST
    public Response createUser(Map<String, String> data) {
        String id = keycloakService.createUser(
                data.get("firstName"),
                data.get("lastName"),
                data.get("email"),
                data.get("password"),
                data.get("birthdate")
        );
        return Response.status(Response.Status.CREATED).entity(Map.of("userId", id)).build();
    }

    @GET
    public List<UserRepresentation> getUsers() {
        return keycloakService.getAllUsers();
    }

    @PUT
    @Path("/update/{id}")
    public Response updateUserAttributes(@PathParam("id") String userId, Map<String, String> newAttributes) {
        keycloakService.updateUserAttributes(userId, newAttributes);
        return Response.ok().entity(Map.of("message", "User attributes updated")).build();
    }
}
