package com.ecommerce.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Code yaml configuration for routes that we did in properties file.
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {

        RouteLocator routeLocator = routeLocatorBuilder.routes()
                .route("product-service", (PredicateSpec r) -> {
                    return r.path("/api/products/**")
                            .uri("lb://PRODUCT-SERVICE");
                })
                .route("user-service", (PredicateSpec r) -> {
                    return r.path("/api/users/**")
                            .uri("lb://USER-SERVICE");
                })
                .route("order-service", (PredicateSpec r) -> {
                    return r.path("/api/orders/**")
                            .uri("lb://ORDER-SERVICE");
                })
                .route("cart-service", (PredicateSpec r) -> {
                    return r.path("/api/cart/**")
                            .uri("lb://CART-SERVICE");
                })
                .route("eureka-server", (PredicateSpec r) -> {
                    return r.path("/eureka/main")
                            .uri("http://localhost:8761");
                })
                .route("eureka-server-static", (PredicateSpec r) -> {
                    return r.path("/eureka/**")
                            .uri("http://localhost:8761");
                })
                .build();

        return routeLocator;
    }
}
