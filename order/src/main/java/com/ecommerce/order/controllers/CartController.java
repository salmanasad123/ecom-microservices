package com.ecommerce.order.controllers;


import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.models.CartItem;
import com.ecommerce.order.service.CartService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@Transactional
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // adding user-id to know the cart belongs to which user.
    @PostMapping
    public ResponseEntity<String> addToCart(@RequestHeader("X-User-ID") String userId,
                                            @RequestBody CartItemRequest cartItemRequest) {

        if (!cartService.addToCart(userId, cartItemRequest)) {
            return ResponseEntity.badRequest().body("Product out of stock or User not found or Product not found");
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromCart(@RequestHeader("X-User-ID") String userId,
                                               @PathVariable(value = "productId") String productId) {

        boolean result = cartService.deleteItemFromCart(userId, productId);

        return result ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();

    }

    // fetch cart of a particular user
    @GetMapping
    @RateLimiter(name = "productService", fallbackMethod = "retryFallback")
    public ResponseEntity<List<CartItem>> getCartForUser(@RequestHeader("X-User-ID") String userId) {

        List<CartItem> cartItems = cartService.getCartForUser(userId);
        if (cartItems.size() > 0) {
            return ResponseEntity.ok(cartItems);
        } else {
           return ResponseEntity.notFound().build();
        }
    }
    public boolean retryFallback(String userId, Exception exception){
        System.out.println("FALLBACK CALLED");
        return false;
    }
}
