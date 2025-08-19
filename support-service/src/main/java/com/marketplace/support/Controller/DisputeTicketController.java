package com.marketplace.support.Controller;

import com.marketplace.support.Entity.DisputeTicket;
import com.marketplace.support.Service.DisputeTicketService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/api/v1/disputes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DisputeTicketController {
    @Inject
    DisputeTicketService disputeTicketService;

    @POST
    public Response createDisputeTicket(DisputeTicket ticket) {
        disputeTicketService.createTicket(ticket);
        return Response.status(Response.Status.CREATED).entity(Map.of("message", "Support ticket created successfully.")).build();
    }

    @GET
    public List<DisputeTicket> getAllContacts() {
        return disputeTicketService.getAll();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteMessage(@PathParam("id") String id) {
        try {
            disputeTicketService.deleteTicketById(id);
            return Response.noContent().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}
