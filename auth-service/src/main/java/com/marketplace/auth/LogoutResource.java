package com.marketplace.auth;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/logout")
public class LogoutResource {

    @GET
    public Response logout() {

        String keycloakLogoutUrl = "http://localhost:8180/auth/realms/pfe-realm/protocol/openid-connect/logout?redirect_uri=http://localhost:8081";
        return Response.seeOther(URI.create(keycloakLogoutUrl)).build();
    }
}
