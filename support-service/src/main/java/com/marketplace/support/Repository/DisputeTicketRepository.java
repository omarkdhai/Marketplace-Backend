package com.marketplace.support.Repository;

import com.marketplace.support.Entity.DisputeTicket;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DisputeTicketRepository implements PanacheMongoRepository<DisputeTicket> {
}
