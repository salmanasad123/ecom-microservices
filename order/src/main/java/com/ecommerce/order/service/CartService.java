package com.ecommerce.order.service;

import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.clients.UserServiceClient;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.models.CartItem;
import com.ecommerce.order.repository.CartItemRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

    @Autowired
    public CartService(CartItemRepository cartItemRepository, ProductServiceClient productServiceClient,
                       UserServiceClient userServiceClient) {
        this.cartItemRepository = cartItemRepository;
        this.productServiceClient = productServiceClient;
        this.userServiceClient = userServiceClient;
    }

    @CircuitBreaker(name = "productService")
    public boolean addToCart(String userId, CartItemRequest cartItemRequest) {
        // Look for product
        ProductResponse productResponse = productServiceClient.getProductDetails(cartItemRequest.getProductId());
        if (productResponse == null) {
            return false;
        }
//        // Check for item stock
        if (productResponse.getStockQuantity() < cartItemRequest.getQuantity()) {
            return false;
        }
//        // Check for user

        UserResponse userResponse = userServiceClient.getUserDetails(userId);
        if (userResponse == null) {
            return false;
        }

        // if the product already exist in the user cart, we are updating the quantity against that product,
        // if product does not exist we will add that product to the cart.
        CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, cartItemRequest.getProductId());
        if (existingCartItem != null) {
            // update the quantity
            existingCartItem.setQuantity(existingCartItem.getQuantity() + cartItemRequest.getQuantity());
            existingCartItem.setPrice(BigDecimal.valueOf(1000.00));
            cartItemRepository.save(existingCartItem);
        } else {
            // create a new cart item
            CartItem cartItem = new CartItem();
            cartItem.setProductId((cartItemRequest.getProductId()));
            cartItem.setUserId(userId);
            cartItem.setPrice(BigDecimal.valueOf(1000.00));
            cartItem.setQuantity(cartItemRequest.getQuantity());
            cartItemRepository.save(cartItem);
        }
        return true;
    }

    public boolean deleteItemFromCart(String userId, String productId) {

        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId);

        if (cartItem != null) {
            cartItemRepository.delete(cartItem);
            return true;
        }

        return false;
    }

    public List<CartItem> getCartForUser(String userId) {

        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        return cartItems;
    }

    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }
}
