package com.marketplace.support.Service;

import com.marketplace.support.Entity.ContactMessage;
import com.marketplace.support.Repository.ContactRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class ContactService {

    @Inject
    ContactRepository repository;

    public void save(ContactMessage contactMessage) {
        contactMessage.setCreationDate(Instant.now());
        contactMessage.persist();
    }

    public List<ContactMessage> getAll() {
        return ContactMessage.listAll();
    }

    public void deleteById(String id) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Invalid ID format");
        }

        ContactMessage message = ContactMessage.findById(objectId);
        if (message != null) {
            message.delete();
        } else {
            throw new NotFoundException("Contact message not found");
        }
    }

}
