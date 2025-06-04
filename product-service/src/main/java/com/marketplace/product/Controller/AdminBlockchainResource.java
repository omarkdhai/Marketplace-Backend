package com.marketplace.product.Controller;

import com.marketplace.product.DTO.AdminBlockchainTransactionDto;
import com.marketplace.product.Entity.ProceedOrder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Path("/api/v1/admin/blockchain")
public class AdminBlockchainResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminBlockchainResource.class);

    @GET
    @Path("/transactions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<AdminBlockchainTransactionDto> getAllBlockchainTrackedOrders() {
        LOGGER.info("ADMIN: Fetching all orders with blockchain transaction information.");

        List<ProceedOrder> allOrders = ProceedOrder.listAll();

        LOGGER.info("ADMIN: Found {} total orders.", allOrders.size());

        return allOrders.stream()
                .map(AdminBlockchainTransactionDto::new)
                .collect(Collectors.toList());
    }
}
