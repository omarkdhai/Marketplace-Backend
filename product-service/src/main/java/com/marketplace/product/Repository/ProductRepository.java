package com.marketplace.product.Repository;

import com.marketplace.product.Entity.Product;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductRepository implements PanacheMongoRepository<Product> {

}
