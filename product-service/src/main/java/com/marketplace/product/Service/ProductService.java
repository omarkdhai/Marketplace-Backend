package com.marketplace.product.Service;

import com.marketplace.product.Entity.Product;
import com.marketplace.product.Repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProductService {
    @Inject
    ProductRepository productRepository;

    @Transactional
    public Product addProduct(Product product) {
        try {
            if (product != null) {
                product.persist();
                return product;
            } else {
                throw new IllegalArgumentException("Product cannot be null");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while saving the product", e);
        }
    }




    // Get products
    public List<Product> getAllProducts() {
        return productRepository.listAll();
    }

    // Get Product by ID
    public Optional<Product> getProductById(String id) {
        return Product.findByIdOptional(new ObjectId(id));
    }

    // Update Product

    public Product updateProduct(String id, Product product) {
        Product existingProduct = Product.findById(id);
        if (existingProduct != null) {
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setStatus(product.getStatus());
            existingProduct.setCategories(product.getCategories());
            existingProduct.setPhoto(product.getPhoto());
            existingProduct.setMedias(product.getMedias());
            existingProduct.setKeywords(product.getKeywords());

            existingProduct.persist();
            return existingProduct;
        } else {
            return null;
        }
    }

    // Delete Product

    public boolean deleteProduct(String id) {
        return Product.deleteById(new ObjectId(id));
    }

    // Get Product by Keywords
    public List<Product> searchByKeyword(String keyword) {
        return Product.find("keywords like ?1", "%" + keyword + "%").list();
    }

    // Get product by Name
    public List<Product> searchByName(String name) {
        return Product.find("name", name).list();
    }

    // Filter Products by Category
    public List<Product> findByCategory(String categoryName) {
        return Product.list("categories.name", categoryName);
    }

}
