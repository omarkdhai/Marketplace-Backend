package com.marketplace.auth.Controller;

import com.marketplace.auth.DTO.LoginRequest;
import com.marketplace.auth.Service.KeycloakService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.keycloak.representations.AccessTokenResponse;
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
        try {
            String id = keycloakService.createUser(
                    data.get("firstName"),
                    data.get("lastName"),
                    data.get("email"),
                    data.get("password"),
                    data.get("birthdate")
            );
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of(
                            "userId", id,
                            "message", "User created successfully. Verification email sent."
                    ))
                    .build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of(
                            "error", "User creation failed",
                            "details", e.getMessage()
                    ))
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of(
                            "error", "Unexpected error during user creation",
                            "details", e.getMessage()
                    ))
                    .build();
        }
    }

    @PUT
    @Path("/{userId}/resend-verification-email")
    public Response resendVerificationEmail(@PathParam("userId") String userId) {
        try {
            keycloakService.sendVerificationEmail(userId);
            return Response.ok(Map.of("message", "Verification email sent successfully.")).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of(
                            "error", "Failed to send verification email",
                            "details", e.getMessage()
                    ))
                    .build();
        }
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest loginRequest) {
        try {
            AccessTokenResponse tokenResponse = keycloakService.login(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );
            return Response.ok(tokenResponse).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of(
                            "error", "Login failed",
                            "details", e.getMessage()
                    ))
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of(
                            "error", "Unexpected error during login",
                            "details", e.getMessage()
                    ))
                    .build();
        }
    }

    @GET
    public Response getUsers() {
        try {
            List<UserRepresentation> users = keycloakService.getAllUsers();
            return Response.ok(users).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of(
                            "error", "Failed to fetch users",
                            "details", e.getMessage()
                    ))
                    .build();
        }
    }

    @PUT
    @Path("/{userId}/update-attributes")
    public Response updateUserAttributes(@PathParam("userId") String userId, Map<String, String> newAttributes) {
        try {
            keycloakService.updateUserAttributes(userId, newAttributes);
            return Response.ok(Map.of("message", "User attributes updated successfully.")).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of(
                            "error", "Failed to update user attributes",
                            "details", e.getMessage()
                    ))
                    .build();
        }
    }

    @DELETE
    @Path("/{userId}")
    public Response deleteUser(@PathParam("userId") String userId) {
        try {
            keycloakService.deleteUser(userId);
            return Response.ok(Map.of("message", "User deleted successfully.")).build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of(
                            "error", "Failed to delete user",
                            "details", e.getMessage()
                    ))
                    .build();
        }
    }

    @POST
    @Path("/code-email")
    public Response sendCodeEmail(Map<String, String> data) {
        String email = data.get("email");

        if (email == null || email.isEmpty()) {
            throw new WebApplicationException("Email is required", Response.Status.BAD_REQUEST);
        }

        keycloakService.codeEmailVerif(email);

        return Response.ok(Map.of(
                "message", "Verification email sent successfully",
                "email", email
        )).build();
    }

}
