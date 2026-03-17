package com.ecommerce.order.service;


import com.ecommerce.order.dto.OrderCreatedEvent;
import com.ecommerce.order.dto.OrderItemDTO;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.models.*;
import com.ecommerce.order.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    // We will now use Stream instead of rabbitTemplate
    private final StreamBridge streamBridge;

    @Autowired
    public OrderService(CartService cartService, OrderRepository orderRepository,
                        RabbitTemplate rabbitTemplate, StreamBridge streamBridge) {
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.streamBridge = streamBridge;
    }


    public Optional<OrderResponse> createOrder(String userId) {

        // validate for cart items. User should have items in the cart
        List<CartItem> cartItemList = cartService.getCartForUser(userId);
        if (cartItemList.isEmpty()) {
            return Optional.empty();
        }

        // Validate for user, userId has to be valid
//        Optional<User> userOptional = userRepository.findById(Long.valueOf(userId));
//        if (userOptional.isEmpty()) {
//            return Optional.empty();
//        }
//        User user = userOptional.get();

        // Calculate total price
        BigDecimal totalPrice = cartItemList.stream()
                .map((CartItem cartItem) -> {
                    // picking the price of each cartItem, at this point we have stream of bigDecimal
                    return cartItem.getPrice();
                }).reduce(BigDecimal.ZERO, (bigDecimal, augend) -> {
                    // reduce combines the value of stream so we reduce it into a single value.
                    return bigDecimal.add(augend);
                });

        // Create order
        Order order = new Order();
        order.setUserId(Long.valueOf(userId));
        order.setOrderStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(totalPrice);

        // converting cartItems to orderItems
        List<OrderItem> orderItemList = cartItemList.stream()
                .map((CartItem cartItem) -> {
                    OrderItem orderItem = new OrderItem(null, cartItem.getProductId(),
                            cartItem.getQuantity(), cartItem.getPrice(), order);
                    return orderItem;
                }).collect(Collectors.toList());

        order.setItems(orderItemList);
        // save the order
        Order savedOrder = orderRepository.save(order);

        // Clear the cart, when the order is placed. Remove all the cart items for a particular user.
        cartService.clearCart(userId);

        // publish order created event
        OrderCreatedEvent event = new OrderCreatedEvent(savedOrder.getId(), savedOrder.getUserId(),
                savedOrder.getOrderStatus(), savedOrder.getTotalAmount(), mapToOrderItemDTOs(savedOrder.getItems()),
                savedOrder.getCreatedAt());

        // publish message to rabbitmq using the exchange and routing key
        // rabbitTemplate.convertAndSend(exchangeName, routingKey, event);

        // publish message to rabbitmq using stream
        streamBridge.send("createOrder-out-0", event);

        OrderResponse orderResponse = mapOrderToOrderResponse(savedOrder);
        return Optional.of(orderResponse);
    }

    private List<OrderItemDTO> mapToOrderItemDTOs(List<OrderItem> orderItems) {

        return orderItems.stream()
                .map((OrderItem item) -> {
                    return new OrderItemDTO(item.getId(), item.getProductId(),
                            item.getQuantity(), item.getPrice(),
                            item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                })
                .collect(Collectors.toList());
    }

    private OrderResponse mapOrderToOrderResponse(Order savedOrder) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(savedOrder.getId());
        orderResponse.setStatus(savedOrder.getOrderStatus());
        orderResponse.setTotalAmount(savedOrder.getTotalAmount());
        orderResponse.setCreatedAt(savedOrder.getCreatedAt());

        // convert orderItem to orderItemDTO because orderResponse has orderItemDTO
        List<OrderItemDTO> orderItemDTOList = savedOrder.getItems().stream()
                .map((OrderItem orderItem) -> {
                    OrderItemDTO orderItemDTO = new OrderItemDTO();
                    orderItemDTO.setId(orderItem.getId());
                    orderItemDTO.setPrice(orderItem.getPrice());
                    orderItemDTO.setQuantity(orderItem.getQuantity());
                    orderItemDTO.setProductId(String.valueOf(orderItem.getProductId()));
                    orderItemDTO.setSubTotal(orderItem.getPrice().multiply(new BigDecimal(orderItem.getQuantity())));
                    return orderItemDTO;
                }).collect(Collectors.toList());

        orderResponse.setOrderItemDTOList(orderItemDTOList);

        return orderResponse;
    }
}
