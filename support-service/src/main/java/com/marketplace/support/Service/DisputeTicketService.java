package com.marketplace.support.Service;

import com.marketplace.support.Entity.ContactMessage;
import com.marketplace.support.Entity.DisputeTicket;
import com.marketplace.support.Repository.ContactRepository;
import com.marketplace.support.Repository.DisputeTicketRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.bson.types.ObjectId;

import java.util.List;

@ApplicationScoped
public class DisputeTicketService {
    @Inject
    DisputeTicketRepository disputeTicketRepository;

    public void createTicket(DisputeTicket disputeTicket) {
        disputeTicket.persist();
    }

    public List<DisputeTicket> getAll() {
        return DisputeTicket.listAll();
    }

    public void deleteTicketById(String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Invalid ID format");
        }

        DisputeTicket ticket = DisputeTicket.findById(objectId);
        if (ticket != null) {
            ticket.delete();
        } else {
            throw new NotFoundException("Dispute ticket not found");
        }
    }
}
