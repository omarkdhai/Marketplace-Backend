package com.marketplace.product.Controller;

import com.marketplace.product.DTO.ProceedOrderDTO;
import com.marketplace.product.Entity.ProceedOrder;
import com.marketplace.product.Service.ProceedOrderService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/v1/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProceedOrderController {

    @Inject
    ProceedOrderService service;

    @POST
    public Response submitOrder(ProceedOrderDTO dto) {
        service.save(dto);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    public List<ProceedOrder> getAllOrders() {
        return service.getAllOrders();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteOrder(@PathParam("id") String id) {
        boolean deleted = service.deleteOrder(id);
        if (deleted) {
            return Response.noContent().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Order not found").build();
        }
    }

    @PUT
    @Path("/{id}/toggle-status")
    public Response toggleOrderStatus(@PathParam("id") String id) {
        boolean success = service.toggleOrderStatus(id);
        if (success) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Order not found")
                    .build();
        }
    }
}
