package com.marketplace.product.Controller;

import com.marketplace.product.DTO.AddToCartRequest;
import com.marketplace.product.Entity.CartItem;
import com.marketplace.product.Service.CartItemService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;

import java.util.List;

@Path("/api/v1/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CartItemController {

    @Inject
    CartItemService cartService;

    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addToCart(AddToCartRequest request) {
        cartService.addToCart(request.getUserId(), request.getProductIds());
        return Response.ok().entity("{\"message\": \"Added to cart\"}").build();
    }


    // Get all items in the cart
    @GET
    @Path("/{userId}")
    public List<CartItem> getCartItems(@PathParam("userId") String userId) {
        return cartService.getCartItems(userId);
    }

    // Remove a specific cart item

    @DELETE
    @Path("/{userId}/{productId}")
    public Response removeFromCart(@PathParam("userId") String userId, @PathParam("productId") String productId) {
        cartService.removeProductFromCart(userId, productId);
        return Response.noContent().build(); // 204 No Content
    }

    @PUT
    @Path("/increase")
    public Response increaseQuantity(AddToCartRequest addToCartRequest) {
        try {
            var updatedCart = cartService.increaseProductQuantity(addToCartRequest.getUserId(), addToCartRequest.getProductIds().get(0));
            return Response.ok(updatedCart).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    // Decrease product quantity
    @PUT
    @Path("/decrease")
    public Response decreaseQuantity(AddToCartRequest addToCartRequest) {
        try {
            var updatedCart = cartService.decreaseProductQuantity(addToCartRequest.getUserId(), addToCartRequest.getProductIds().get(0));
            return Response.ok(updatedCart).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }
}
