package com.marketplace.product.Service;

import com.marketplace.product.Entity.CartItem;
import com.marketplace.product.Entity.CartProduct;
import com.marketplace.product.Entity.Product;
import com.marketplace.product.Repository.CartItemRepository;
import com.marketplace.product.Repository.ProductRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class CartItemService {

    @Inject
    CartItemRepository cartItemRepository;

    @Inject
    ProductRepository productRepository;

    public void addToCart(String userId, List<String> productIds) {
        List<ObjectId> ids = productIds.stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        List<Product> products = productRepository.find("_id in ?1", ids).list();

        CartItem existingCart = cartItemRepository.find("userId", userId).firstResult();

        if (existingCart == null) {
            existingCart = new CartItem();
            existingCart.setUserId(userId);
            existingCart.setProducts(new ArrayList<>());
        }

        for (Product product : products) {
            boolean found = false;
            for (CartProduct cartProduct : existingCart.getProducts()) {
                if (cartProduct.getProduct() != null &&
                        cartProduct.getProduct().getId().equals(product.getId())) {
                    cartProduct.setQuantity(cartProduct.getQuantity() + 1);
                    found = true;
                    break;
                }
            }

            if (!found) {
                existingCart.getProducts().add(new CartProduct(product, 1));
            }
        }

        existingCart.setTotalPrice(calculateTotalPrice(existingCart.getProducts()));
        cartItemRepository.persistOrUpdate(existingCart);
    }

    private double calculateTotalPrice(List<CartProduct> cartProducts) {
        double total = 0.0;

        for (CartProduct cartProduct : cartProducts) {
            Product product = cartProduct.getProduct();
            if (product != null) {
                total += product.getPrice() * cartProduct.getQuantity();
            }
        }

        return total;
    }

    public List<CartItem> getCartItems(String userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public void removeProductFromCart(String userId, String productId) {
        CartItem cart = CartItem.find("userId", userId).firstResult();
        if (cart == null) {
            throw new NotFoundException("Cart not found for user");
        }

        ObjectId objectId;
        try {
            objectId = new ObjectId(productId);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid product ID format");
        }

        boolean removed = cart.getProducts().removeIf(cp ->
                cp.getProduct() != null &&
                        cp.getProduct().getId() != null &&
                        cp.getProduct().getId().equals(objectId)
        );

        if (!removed) {
            throw new NotFoundException("Product not found in cart");
        }

        cart.updateTotalPrice();
        cart.update();
    }

    public CartItem increaseProductQuantity(String userId, String productId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart not found for user: " + userId);
        }

        CartItem cartItem = cartItems.get(0); // Assuming one cart per user
        boolean found = false;

        for (CartProduct cp : cartItem.getProducts()) {
            if (cp.getProduct().getId().toString().equals(productId)) {
                cp.setQuantity(cp.getQuantity() + 1);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new RuntimeException("Product not found in cart: " + productId);
        }

        cartItem.setTotalPrice(calculateTotalPrice(cartItem.getProducts()));
        cartItem.update();
        return cartItem;
    }

    public CartItem decreaseProductQuantity(String userId, String productId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart not found for user: " + userId);
        }

        CartItem cartItem = cartItems.get(0);
        Iterator<CartProduct> iterator = cartItem.getProducts().iterator();
        boolean found = false;

        while (iterator.hasNext()) {
            CartProduct cp = iterator.next();
            if (cp.getProduct().getId().toString().equals(productId)) {
                int newQty = cp.getQuantity() - 1;
                if (newQty <= 0) {
                    iterator.remove(); // remove from cart if quantity <= 0
                } else {
                    cp.setQuantity(newQty);
                }
                found = true;
                break;
            }
        }
        if (!found) {
            throw new RuntimeException("Product not found in cart: " + productId);
        }

        cartItem.setTotalPrice(calculateTotalPrice(cartItem.getProducts()));
        cartItem.update();
        return cartItem;
    }

}
