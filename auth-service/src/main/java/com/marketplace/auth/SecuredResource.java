package com.marketplace.auth;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/secured")
public class SecuredResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @RolesAllowed("user")  // Only accessible by users with the 'user' role
    public String hello() {
        return "Hello, you are authenticated!";
    }
}
