package com.marketplace.product.Controller;


import com.marketplace.product.DTO.ProductForm;
import com.marketplace.product.Entity.Product;
import com.marketplace.product.Enum.ProductStatus;
import com.marketplace.product.Service.ProductService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Path("/products")
public class ProductController {

    @Inject
    ProductService productService;
    private static final Logger LOGGER = Logger.getLogger(ProductController.class);
    private static final String UPLOAD_DIR = "C:/Users/21628/Documents/PFE Documents/Marketplace Project/uploaded-images";

    @GET
    @Produces("application/json")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getProductById(@PathParam("id") String id) {
        Optional<Product> product = productService.getProductById(id);
        return product.map(Response::ok)
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addProduct(@MultipartForm ProductForm productForm) {
        try {
            LOGGER.info("Received request to add product.");

            // Log all the form data to check the inputs
            LOGGER.info("Product Details: " + productForm.toString());

            // Convert the status string to ProductStatus enum
            ProductStatus status = ProductStatus.valueOf(productForm.getStatus().toUpperCase());

            // Check if photo is being received correctly
            if (productForm.getPhoto() != null) {
                // Prepare the file path for saving the photo
                String fileName = productForm.getName() + ".png";
                String filePath = UPLOAD_DIR + "/" + fileName;


                LOGGER.info("Saving file to: " + filePath);

                // Save the photo file
                saveFile(productForm.getPhoto(), filePath);

                LOGGER.info("Product photo uploaded successfully.");

                // Create the product object with the correct status enum
                Product product = new Product(productForm.getName(), productForm.getDescription(), productForm.getPrice(), status);
                product.setPhoto(filePath); // Save the file path in the photo field
                Product savedProduct = productService.addProduct(product); // Assuming productService.addProduct saves to MongoDB

                return Response.status(Response.Status.CREATED)
                        .entity(savedProduct)
                        .build();
            }
            return null;/*else {
                LOGGER.warn("No photo received for 'photo' field.");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("No photo received for 'photo' field.")
                        .build();
            }*/
        } catch (Exception e) {
            LOGGER.error("Error while processing the request", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while uploading the product photo.")
                    .build();
        }
    }

    private void saveFile(InputStream uploadedInputStream, String filePath) throws IOException {
        try {
            LOGGER.info("Attempting to save file: " + filePath);
            Files.copy(uploadedInputStream, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("File saved successfully at: " + filePath);
        } catch (IOException e) {
            LOGGER.error("Error saving the file", e);
            throw new IOException("Error saving file", e);
        }
    }

    @PUT
    @Path("/{id}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateProduct(@PathParam("id") String id, Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        if (updatedProduct != null) {
            return Response.ok(updatedProduct).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
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

}
