package com.marketplace.support.Service;

import com.marketplace.support.Entity.ContactMessage;
import com.marketplace.support.Repository.ContactRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
}
