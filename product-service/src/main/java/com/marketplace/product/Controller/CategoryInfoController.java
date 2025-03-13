package com.marketplace.product.Controller;

import com.marketplace.product.Entity.CategoryInfo;
import com.marketplace.product.Service.CategoryInfoService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoryInfoController {

    @Inject
    CategoryInfoService categoryInfoService;

    // Create or Update CategoryInfo
    @POST

    public Response createOrUpdateCategoryInfo(CategoryInfo categoryInfo) {
        CategoryInfo savedCategory = categoryInfoService.createOrUpdateCategoryInfo(categoryInfo);
        return Response.status(Response.Status.CREATED).entity(savedCategory).build();
    }

    // Get CategoryInfo by ID
    @GET
    @Path("/{id}")
    public Response getCategoryInfoById(@PathParam("id") String id) {
        Optional<CategoryInfo> categoryInfo = categoryInfoService.getCategoryInfoById(id);
        if (categoryInfo.isPresent()) {
            return Response.ok(categoryInfo.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    // Get all CategoryInfo
    @GET
    public List<CategoryInfo> getAllCategoryInfo() {
        return categoryInfoService.getAllCategoryInfo();
    }

    // Delete CategoryInfo by ID
    @DELETE
    @Path("/{id}")

    public Response deleteCategoryInfo(@PathParam("id") String id) {
        boolean deleted = categoryInfoService.deleteCategoryInfo(id);
        if (deleted) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
