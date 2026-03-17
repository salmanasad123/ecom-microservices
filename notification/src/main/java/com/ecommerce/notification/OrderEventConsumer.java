package com.ecommerce.notification;

import com.ecommerce.notification.payload.OrderCreatedEvent;
import com.ecommerce.notification.payload.OrderStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

// Consumer for events from rabbitmq (order created events)
@Service
public class OrderEventConsumer {

    // Listener for the queue (it will listen the queue for new messages)
    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void handleOrderEvent(OrderCreatedEvent orderEvent) {
        System.out.println("Received order event: " + orderEvent);

        Long orderId = orderEvent.getOrderId();
        OrderStatus orderStatus = orderEvent.getOrderStatus();

        System.out.println("Order ID: " + orderId);
        System.out.println("Order Status: " + orderStatus);

        // things we can do further:
        // Update database
        // Send notifications
        // Generate invoice

    }
}
