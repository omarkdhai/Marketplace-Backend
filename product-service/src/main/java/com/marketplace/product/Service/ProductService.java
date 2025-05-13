package com.marketplace.product.Service;

import com.marketplace.product.DTO.ProductForm;
import com.marketplace.product.Entity.CategoryInfo;
import com.marketplace.product.Entity.Product;
import com.marketplace.product.Enum.ProductStatus;
import com.marketplace.product.Repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ProductService {
    @Inject
    ProductRepository productRepository;


    //Add Product
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

    //Toggle Favorite Product
    public Product toggleFavorite(String productId) {
        Product product = productRepository.findById(new ObjectId(productId));
        if (product == null) {
            throw new IllegalArgumentException("Product not found");
        }
        product.toggleFavorite();
        productRepository.update(product);
        return product;
    }


    // Get products
    public List<Product> getAllProducts() {
        return productRepository.listAll();
    }

    // Get Product by ID
    public Response getProductById(String id) {
        Optional<Product> product = Product.findByIdOptional(new ObjectId(id));
        return product.map(Response::ok)
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    // Update Product
    public Product updateProduct(String id, ProductForm form) throws IOException {
        if (!ObjectId.isValid(id)) {
            throw new WebApplicationException("Invalid product ID", Response.Status.BAD_REQUEST);
        }

        Product existingProduct = Product.findById(new ObjectId(id));
        if (existingProduct == null) {
            throw new WebApplicationException("Product not found", Response.Status.NOT_FOUND);
        }

        existingProduct.setName(form.getName());
        existingProduct.setDescription(form.getDescription());
        existingProduct.setPrice(form.getPrice());

        // Enum conversion with safety
        try {
            existingProduct.setStatus(ProductStatus.valueOf(form.getStatus().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Invalid product status", Response.Status.BAD_REQUEST);
        }

        // Parse categories
        if (form.getCategories() != null && !form.getCategories().isEmpty()) {
            List<CategoryInfo> categories = Arrays.stream(form.getCategories().split(","))
                    .map(String::trim)
                    .map(name -> {
                        CategoryInfo cat = new CategoryInfo();
                        cat.name = name;
                        cat.description = "Updated description";
                        return cat;
                    })
                    .collect(Collectors.toList());
            existingProduct.setCategories(categories);
        }

        // Parse keywords
        if (form.getKeywords() != null) {
            List<String> keywords = Arrays.stream(form.getKeywords().split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            existingProduct.setKeywords(keywords);
        }

        // Update photo if provided
        if (form.getPhoto() != null) {
            byte[] photoBytes = convertInputPartToByteArray(form.getPhoto());
            existingProduct.setPhoto(Base64.getEncoder().encodeToString(photoBytes));
        }

        // Update medias if provided
        if (form.getMedias() != null && !form.getMedias().isEmpty()) {
            List<byte[]> mediaList = new ArrayList<>();
            for (InputPart media : form.getMedias()) {
                mediaList.add(convertInputPartToByteArray(media));
            }
            existingProduct.setMedias(mediaList);
        }

        existingProduct.persistOrUpdate();
        return existingProduct;
    }

    public byte[] convertInputPartToByteArray(InputPart inputPart) throws IOException {
        InputStream inputStream = inputPart.getBody(InputStream.class, null);
        return inputStream.readAllBytes();
    }


    // Delete Product
    public boolean deleteProduct(String id) {
        return Product.deleteById(new ObjectId(id));
    }

    // Get Product by Keywords
    public List<Product> searchByKeyword(String keyword) {
        return Product.find("{ keywords: { $regex: ?1, $options: 'i' } }", keyword).list();
    }

    // Get product by Name
    public List<Product> searchByName(String name) {
        return Product.find("name", name).list();
    }

    // Get product by Category Name
    public List<Product> getProductsByCategoryName(String categoryName) {
        return Product.find("categories.name", categoryName).list();
    }

    // Filter By Prices Asc Or Desc
    public List<Product> getProductsSortedByPriceAsc() {
        return Product.find("ORDER BY price ASC").list();
    }

    public List<Product> getProductsSortedByPriceDesc() {
        return Product.find("ORDER BY price DESC").list();
    }

}
