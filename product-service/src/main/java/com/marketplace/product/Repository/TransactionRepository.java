package com.marketplace.product.Repository;

import com.marketplace.product.Entity.Transaction;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class TransactionRepository implements PanacheMongoRepository<Transaction> {

    public List<Transaction> findByUserEmail(String email) {
        return list("userEmail", email);
    }
}
