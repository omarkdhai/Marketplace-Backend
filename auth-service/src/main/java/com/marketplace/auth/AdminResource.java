package com.marketplace.auth;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/admin")
@RolesAllowed("Admin")
public class AdminResource {
    @GET
    public String getAdminData() {
        return "Admin data";
    }
}
