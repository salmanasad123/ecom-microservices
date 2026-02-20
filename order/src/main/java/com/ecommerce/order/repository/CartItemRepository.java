package com.ecommerce.order.repository;

import com.ecommerce.order.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Transactional
    @Modifying
    void deleteByUserId(String userId);

    CartItem findByUserIdAndProductId(String userId, String productId);

    @Transactional
    @Modifying
    void deleteByUserIdAndProductId(String userId, String productId);

    List<CartItem> findByUserId(String userId);
}
