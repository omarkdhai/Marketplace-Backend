package com.marketplace.product.Controller;

import com.marketplace.product.Entity.Transaction;
import com.marketplace.product.Repository.TransactionRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/v1/transactions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionController {

    @Inject
    TransactionRepository transactionRepository;

    @POST
    public Response create(Transaction transaction) {
        transactionRepository.persist(transaction);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/by-user")
    public List<Transaction> getByUser(@QueryParam("email") String email) {
        return transactionRepository.findByUserEmail(email);
    }



}
