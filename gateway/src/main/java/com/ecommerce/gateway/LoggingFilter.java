package com.ecommerce.gateway;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// Filter to just log the on-going request,
// GlobalFilter is Spring Cloud Gateway ka filter, API Gateway level pe kaam karta hai
@Component
public class LoggingFilter implements GlobalFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        // {} is parameterized log message
        logger.info("Incoming request to : {}", exchange.getRequest().getPath());
        return chain.filter(exchange);
    }
}
