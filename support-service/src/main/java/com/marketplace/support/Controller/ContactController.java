package com.marketplace.support.Controller;

import com.marketplace.support.Entity.ContactMessage;
import com.marketplace.support.Service.ContactService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/api/v1/contact")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ContactController {

    @Inject
    ContactService service;

    @POST
    public Response submitMessage(ContactMessage message) {
        service.save(message);
        return Response.ok().build();
    }

    @GET
    public List<ContactMessage> getAllContacts() {
        return service.getAll();
    }
}
