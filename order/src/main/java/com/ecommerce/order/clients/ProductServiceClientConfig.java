package com.ecommerce.order.clients;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.Optional;

@Configuration
public class ProductServiceClientConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    // url is coming from eureka service. we are making use of service discovery
    // “HttpServiceProxyFactory is responsible for analyzing the client interface, preparing method metadata,
    // and creating a runtime proxy that delegates HTTP calls to the configured RestClient
    @Bean
    public ProductServiceClient productServiceClient(RestClient.Builder builder) {
        RestClient restClient = builder
                .baseUrl("http://product-service")
                .defaultStatusHandler(HttpStatusCode::is4xxClientError,
                        ((request, response) -> Optional.empty()))
                .build();
        RestClientAdapter restClientAdapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory httpServiceProxyFactory =
                HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        ProductServiceClient productServiceClient = httpServiceProxyFactory.createClient(ProductServiceClient.class);

        return productServiceClient;
    }
}
