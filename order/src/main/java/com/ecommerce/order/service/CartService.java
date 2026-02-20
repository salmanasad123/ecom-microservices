package com.ecommerce.order.service;

import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.models.CartItem;
import com.ecommerce.order.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;

    @Autowired
    public CartService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }


    public boolean addToCart(String userId, CartItemRequest cartItemRequest) {
        // Look for product
//        Optional<Product> productOptional = productRepository.findById(cartItemRequest.getProductId());
//        if (productOptional.isEmpty()) {
//            return false;
//        }
//        // Check for item stock
//        Product product = productOptional.get();
//        if (product.getStockQuantity() < cartItemRequest.getQuantity()) {
//            return false;
//        }
//        // Check for user
//        Optional<User> userOptional = userRepository.findById(Long.valueOf(userId));
//        if (userOptional.isEmpty()) {
//            return false;
//        }
//        User user = userOptional.get();

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

        CartItem cartItem= cartItemRepository.findByUserIdAndProductId(userId, productId);

        if (cartItem!=null) {
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
