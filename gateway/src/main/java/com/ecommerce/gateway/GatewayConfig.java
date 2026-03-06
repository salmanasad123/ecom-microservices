package com.ecommerce.gateway;

import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Code yaml configuration for routes that we did in properties file.
// I also implemented circuitBreaker at Gateway level as follows:
// Steps:
// -
@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {

        // if there is any path that is not /products/**, then it will be forwarded to product-service,
        // and the path will be rewritten to /api/products/**.
        // lb://PRODUCT-SERVICE means it will use service discovery to find the instance of product-service.
        // rewritePath is a filter that will rewrite the path of the request.
        // ${segment} is a placeholder for the segment of the path that is captured by the regex.
        // segment will capture the path after /products, and it will be appended to /api/products.
        // ?<segment>/?.* is a regex that will capture the path after /products.

        /**
         *  .filters((GatewayFilterSpec f) -> {
         *   return f.circuitBreaker((SpringCloudCircuitBreakerFilterFactory.Config config) -> {
         *   config.setName("ecomBreaker");
         *  });
         *  This code is to enable circuit breaking at gateway level, ecomBreaker is the name we have
         *  set in the gateway service properties file.
         */
        RouteLocator routeLocator = routeLocatorBuilder.routes()
                .route("product-service", (PredicateSpec r) -> {
                    return r.path("/api/products/**")
//                            .filters(f -> f.rewritePath("/products(?<segment>/?.*)",
//                                    "/api/products${segment}"))
                            .filters((GatewayFilterSpec f) -> {
                                return f.circuitBreaker((SpringCloudCircuitBreakerFilterFactory.Config config) -> {
                                    config.setName("ecomBreaker");
                                    config.setFallbackUri("forward:/fallback/products");
                                });
                            })
                            .uri("lb://PRODUCT-SERVICE");
                })
                .route("user-service", (PredicateSpec r) -> {
                    return r.path("/api/users/**")
                            .uri("lb://USER-SERVICE");
                })
                .route("order-service", (PredicateSpec r) -> {
                    return r.path("/api/orders/**", "/api/cart/**")
                            .uri("lb://ORDER-SERVICE");
                })
                .route("eureka-server", (PredicateSpec r) -> {
                    return r.path("/eureka/main")
                            .filters((GatewayFilterSpec f) -> {
                                return f.rewritePath("eureka/main", "");
                            })
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
