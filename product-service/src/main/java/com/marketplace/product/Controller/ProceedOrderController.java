package com.marketplace.product.Controller;

import com.marketplace.product.DTO.AdminBlockchainTransactionDto;
import com.marketplace.product.DTO.ProceedOrderDTO;
import com.marketplace.product.Entity.ProceedOrder;
import com.marketplace.product.Service.ProceedOrderService;
import com.marketplace.product.contracts.Web3jClientProducer;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.web3j.protocol.core.methods.response.Transaction;

import java.io.IOException;
import java.util.List;

@Path("/api/v1/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProceedOrderController {

    @Inject
    ProceedOrderService service;
    @Inject
    Web3jClientProducer web3jClientProducer;

    @POST
    public Response submitOrder(ProceedOrderDTO dto) throws IOException {
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
