package com.marketplace.product.Controller;

import com.marketplace.product.DTO.ProceedOrderDTO;
import com.marketplace.product.Service.ProceedOrderService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("api/v1/orders")
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
}
