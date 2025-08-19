package com.marketplace.product.Controller;


import com.marketplace.product.DTO.ProductForm;
import com.marketplace.product.Entity.CategoryInfo;
import com.marketplace.product.Entity.Product;
import com.marketplace.product.Enum.ProductStatus;
import com.marketplace.product.Repository.ProductRepository;
import com.marketplace.product.Service.ProductService;
import com.marketplace.product.websocket.config.NotificationWebSocket;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Path("/api/v1/products")
public class ProductController {

    @Inject
    ProductService productService;

    @Inject
    ProductRepository productRepository;

    @Inject
    NotificationWebSocket notificationWebSocket;

    private static final Logger LOGGER = Logger.getLogger(ProductController.class);

    @GET
    @Produces("application/json")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getProductById(@PathParam("id") String id) {
        return productService.getProductById(id);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addProduct(@MultipartForm ProductForm productForm) {
        try {
            LOGGER.info("Received request to add product.");

            LOGGER.info("Product Details: " + productForm.toString());

            // Convert the status string to ProductStatus enum
            ProductStatus status = ProductStatus.valueOf(productForm.getStatus().toUpperCase());

            // Parse keywords
            List<String> keywords = new ArrayList<>();
            if (productForm.getKeywords() != null && !productForm.getKeywords().isEmpty()) {
                keywords = Arrays.stream(productForm.getKeywords().split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());
            }

            // Parse categories
            List<CategoryInfo> categories = new ArrayList<>();
            if (productForm.getCategories() != null && !productForm.getCategories().isEmpty()) {
                List<String> categoryNames = Arrays.stream(productForm.getCategories().split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());

                for (String name : categoryNames) {
                    CategoryInfo category = new CategoryInfo();
                    category.name = name;
                    category.description = "Default description";
                    categories.add(category);
                }
            }

            // Create the product object with the correct status enum
            Product product = new Product(
                    productForm.getName(),
                    productForm.getDescription(),
                    productForm.getPrice(),
                    ProductStatus.valueOf(productForm.getStatus().toUpperCase()),
                    productForm.getCreationDate(),
                    categories,
                    keywords,
                    productForm.getDiscount()
            );

            // Convert the photo InputPart to byte[] and set the photo
            if (productForm.getPhoto() != null) {
                byte[] photoBytes = convertInputPartToByteArray(productForm.getPhoto());
                product.setPhoto(Base64.getEncoder().encodeToString(photoBytes));
            }

            // Convert the List<InputPart> medias to List<byte[]>
            if (productForm.getMedias() != null && !productForm.getMedias().isEmpty()) {
                List<byte[]> mediaBytes = new ArrayList<>();
                for (InputPart media : productForm.getMedias()) {
                    byte[] mediaBytesArray = convertInputPartToByteArray(media);
                    mediaBytes.add(mediaBytesArray);
                }
                product.setMedias(mediaBytes);
            }

            // Save the product to MongoDB
            Product savedProduct = productService.addProduct(product);

            return Response.status(Response.Status.CREATED)
                    .entity(savedProduct)
                    .build();
        } catch (Exception e) {
            LOGGER.error("Error while processing the request", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while uploading the product.")
                    .build();
        }
    }

    // Helper method to convert InputPart to byte[]
    public byte[] convertInputPartToByteArray(InputPart inputPart) throws IOException {
        InputStream inputStream = inputPart.getBody(InputStream.class, null);
        return inputStream.readAllBytes();
    }


    @PUT
    @Path("/favorite/{productId}")
    @Produces("application/json")
    public Response toggleFavorite(@PathParam("productId") String productId) {
        try {
            Product updatedProduct = productService.toggleFavorite(productId);
            return Response.ok(updatedProduct).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/favorites")
    @Produces("application/json")
    public List<Product> getFavoriteProducts() {
        return productRepository.list("favorite", true);
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateProduct(@PathParam("id") String id, @MultipartForm ProductForm form) {
        try {
            if (form.getDiscount() < 0 || form.getDiscount() > 100) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Discount must be between 0 and 100.").build();
            }
            Product updated = productService.updateProduct(id, form);
            if (form.getDiscount() > 0) {
                notificationWebSocket.broadcastProductNotification(updated, "PRODUCT_DISCOUNT");
                LOGGER.info("Broadcasted a discount notification for product: " + updated.getName());
            }
            return Response.ok(updated).build();

        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Failed to update product", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to update product.")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProduct(@PathParam("id") String id) {
        boolean deleted = productService.deleteProduct(id);
        return deleted ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/search")
    @Produces("application/json")
    public Response searchByKeyword(@QueryParam("keyword") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Keyword parameter is required.")
                    .build();
        }
        List<Product> products = productService.searchByKeyword(keyword);
        return Response.ok(products).build();
    }

    @GET
    @Path("/searchByName")
    @Produces("application/json")
    public Response searchByName(@QueryParam("name") String name) {
        List<Product> products = productService.searchByName(name);
        return Response.ok(products).build();
    }

    @GET
    @Path("/byCategory/{name}")
    public List<Product> getByCategoryName(@PathParam("name") String name) {
        return productService.getProductsByCategoryName(name);
    }

    @GET
    @Path("/sort/asc")
    @Produces("application/json")
    public Response sortByPriceAsc() {
        return Response.ok(productService.getProductsSortedByPriceAsc()).build();
    }

    @GET
    @Path("/sort/desc")
    @Produces("application/json")
    public Response sortByPriceDesc() {
        return Response.ok(productService.getProductsSortedByPriceDesc()).build();
    }


}
