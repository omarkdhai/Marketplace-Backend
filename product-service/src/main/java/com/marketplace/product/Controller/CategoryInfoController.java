package com.marketplace.product.Controller;

import com.marketplace.product.DTO.CategoryInfoForm;
import com.marketplace.product.Entity.CategoryInfo;
import com.marketplace.product.Entity.Product;
import com.marketplace.product.Enum.ProductStatus;
import com.marketplace.product.Service.CategoryInfoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Optional;


@Path("/api/v1/categories")
public class CategoryInfoController {

    @Inject
    CategoryInfoService categoryInfoService;

    private static final Logger LOGGER = Logger.getLogger(CategoryInfoController.class);

    @GET
    @Produces("application/json")
    public List<CategoryInfo> getAllCategories() {
        return categoryInfoService.getAllCategoryInfos();
    }

    @GET
    @Path("/{id}")
    @Produces("application/json")
    public Response getCategoryById(@PathParam("id") String id) {
        Optional<CategoryInfo> category = categoryInfoService.getCategoryInfoById(id);
        return category.map(Response::ok)
                .orElseGet(() -> Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addCategory(@MultipartForm CategoryInfoForm categoryForm) {
        try {
            LOGGER.info("Received request to add category.");

            LOGGER.info("Category Details: " + categoryForm.toString());

            // Create the CategoryInfo object
            CategoryInfo category = new CategoryInfo();
            category.name = categoryForm.getName();
            category.description = categoryForm.getDescription();

            // Convert miniPhoto InputPart to Base64 string
            if (categoryForm.getMiniPhoto() != null) {
                byte[] miniPhotoBytes = convertInputPartToByteArray(categoryForm.getMiniPhoto());
                category.setMiniPhoto(Base64.getEncoder().encodeToString(miniPhotoBytes));
            }

            // Convert maxiPhoto InputPart to Base64 string
            if (categoryForm.getMaxiPhoto() != null) {
                byte[] maxiPhotoBytes = convertInputPartToByteArray(categoryForm.getMaxiPhoto());
                category.setMaxiPhoto(Base64.getEncoder().encodeToString(maxiPhotoBytes));
            }

            // Save the category to MongoDB
            CategoryInfo savedCategory = categoryInfoService.addCategoryInfo(category);

            return Response.status(Response.Status.CREATED)
                    .entity(savedCategory)
                    .build();

        } catch (Exception e) {
            LOGGER.error("Error while processing the request", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error while uploading the category.")
                    .build();
        }
    }

    // Helper method to convert InputPart to byte[]
    public byte[] convertInputPartToByteArray(InputPart inputPart) throws IOException {
        InputStream inputStream = inputPart.getBody(InputStream.class, null);
        return inputStream.readAllBytes();
    }

    @PUT
    @Path("/{id}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateCategory(@PathParam("id") String id, @MultipartForm CategoryInfoForm categoryForm) {
        try {
            LOGGER.info("Received request to update category with ID: " + id);

            Optional<CategoryInfo> existingCategory = categoryInfoService.getCategoryInfoById(id);
            if (existingCategory.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).entity("Category not found").build();
            }

            CategoryInfo category = existingCategory.get();
            category.setName(categoryForm.getName());
            category.setDescription(categoryForm.getDescription());

            if (categoryForm.getMiniPhoto() != null) {
                byte[] miniPhotoBytes = convertInputPartToByteArray(categoryForm.getMiniPhoto());
                category.setMiniPhoto(Base64.getEncoder().encodeToString(miniPhotoBytes));
            }

            if (categoryForm.getMaxiPhoto() != null) {
                byte[] maxiPhotoBytes = convertInputPartToByteArray(categoryForm.getMaxiPhoto());
                category.setMaxiPhoto(Base64.getEncoder().encodeToString(maxiPhotoBytes));
            }

            CategoryInfo updatedCategory = categoryInfoService.updateCategoryInfo(id, category);
            return Response.ok(updatedCategory).build();
        } catch (Exception e) {
            LOGGER.error("Error updating category", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating category").build();
        }
    }


    @DELETE
    @Path("/{id}")
    public Response deleteCategory(@PathParam("id") String id) {
        boolean deleted = categoryInfoService.deleteCategoryInfo(id);
        return deleted ? Response.ok().build() : Response.status(Response.Status.NOT_FOUND).build();
    }
}


