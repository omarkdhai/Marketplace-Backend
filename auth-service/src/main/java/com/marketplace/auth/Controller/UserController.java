package com.marketplace.auth.Controller;

import com.marketplace.auth.DTO.LoginRequest;
import com.marketplace.auth.Service.KeycloakService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.HashMap;
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
    @Path("/update-attributes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserAttributesByEmail(Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Email is required"))
                    .build();
        }

        try {
            Map<String, String> attributes = new HashMap<>(body);
            attributes.remove("email");

            keycloakService.updateUserAttributesByEmail(email, attributes);
            return Response.ok(Map.of("message", "User attributes updated successfully.")).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of(
                            "error", "Failed to update user attributes",
                            "details", e.getMessage()
                    ))
                    .build();
        }
    }

    //Get user by Email
    @POST
    @Path("/find-by-email")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getUserByEmail(LoginRequest loginRequest) {
        String email = loginRequest.getEmail();

        if (email == null || email.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Email is required.")
                    .build();
        }

        UserRepresentation user = keycloakService.getUserByEmail(email);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User not found")
                    .build();
        }

        return Response.ok(user).build();
    }

    @DELETE
    @Path("/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUserByEmail(LoginRequest request) {
        try {
            keycloakService.deleteUserByEmail(request.getEmail());
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


    //Send a mail which contains a random verification code with a link to the front end
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

    //Verify the code all along the expiration time
    @POST
    @Path("/verify-code")
    public Response verifyCode(Map<String, String> data) {
        String email = data.get("email");
        String code = data.get("code");

        if (email == null || code == null || email.isEmpty() || code.isEmpty()) {
            throw new WebApplicationException("Email and code are required", Response.Status.BAD_REQUEST);
        }

        keycloakService.verifyEmailCode(email, code);

        return Response.ok(Map.of(
                "message", "Verification successful"
        )).build();
    }

    //Update Password
    @PUT
    @Path("/update-password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePassword(Map<String, String> data) {
        String email = data.get("email");
        String newPassword = data.get("newPassword");

        if (email == null || email.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            keycloakService.updateUserPassword(email, newPassword);
            return Response.ok().build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus()).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @PUT
    @Path("/update-old-password")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePasswordWithOldPassword(Map<String, String> data) {
        String email = data.get("email");
        String oldPassword = data.get("oldPassword");
        String newPassword = data.get("newPassword");

        if (email == null || email.isEmpty() ||
                oldPassword == null || oldPassword.isEmpty() ||
                newPassword == null || newPassword.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        try {
            keycloakService.updateUserPasswordWithOldPassword(email, oldPassword, newPassword);
            return Response.ok().build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus()).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/logout")
    public Response logout(Map<String, String> data) {
        try {
            String email = data.get("email");
            keycloakService.logout(email);
            return Response.ok(Map.of("message", "User logged out successfully")).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("error", "Logout failed", "details", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.serverError()
                    .entity(Map.of("error", "Unexpected error during logout", "details", e.getMessage()))
                    .build();
        }
    }

}
