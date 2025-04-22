package com.marketplace.support.Repository;

import com.marketplace.support.Entity.ContactMessage;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContactRepository implements PanacheMongoRepository<ContactMessage> {
}
