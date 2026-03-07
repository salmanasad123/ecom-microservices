package com.ecommerce.gateway;

import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.GatewayFilterSpec;
import org.springframework.cloud.gateway.route.builder.PredicateSpec;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// Code yaml configuration for routes that we did in properties file.
// I also implemented circuitBreaker at Gateway level as follows:
// Steps:
// -
@Configuration
public class GatewayConfig {

    // To implement rate limiting through api-gateway we need to use redis.
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // replenish rate means the bucket is filled with 10 tokens per second.
        // default burst capacity means bucket can allow 20 requests per second.
        // requested token means one request will consume 1 token.
        return new RedisRateLimiter(10, 20, 1);
    }

    // Gateway ko batata hai ke client ko kaise identify karna hai.
    // Agar rate limiter use karte ho to har client ka unique key banaya jata hai.
    // Har request ke liye client ka hostname use karo as unique key
    // client1.example.com → 10 requests/sec
    // client2.example.com → 10 requests/sec
    public KeyResolver hostNameKeyResolver() {
        return (ServerWebExchange exchange) -> {
            return Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
        };
    }

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
                                        })
                                        .requestRateLimiter((RequestRateLimiterGatewayFilterFactory.Config config) -> {
                                            config.setRateLimiter(redisRateLimiter())
                                                    .setKeyResolver(hostNameKeyResolver());
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
