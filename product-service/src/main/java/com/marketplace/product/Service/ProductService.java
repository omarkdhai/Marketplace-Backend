package com.marketplace.product.Service;

import com.marketplace.product.Entity.Product;
import com.marketplace.product.Repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

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
