package com.ecommerce.order.clients;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    // if the dependency is not found the application wont crash
    @Autowired(required = false)
    private ObservationRegistry observationRegistry;

    @Autowired(required = false)
    private Tracer tracer;

    @Autowired(required = false)
    private Propagator propagator;

    // moved to a common file so we don't get bean already defined exception
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        RestClient.Builder builder = RestClient.builder();

        if (observationRegistry != null) {
            builder.requestInterceptor(createTracingInterceptor());
        }
        return builder;
    }

    /*
    Ye function outgoing HTTP calls ke liye tracing headers add karta hai.
👉  Matlab:
    Tumhari service kisi dusri service ko HTTP call kare
    To same trace / request ID us call ke saath bhej di jaye
    Taake logs + tracing tools me end-to-end flow dikhe.

    Spring ka RestTemplate is interceptor ko call karta hai before request send hoti hai.
    */
    private ClientHttpRequestInterceptor createTracingInterceptor() {
        return (((HttpRequest request, byte[] body, ClientHttpRequestExecution execution) -> {
            if (tracer != null && propagator != null && tracer.currentSpan() != null) {
                propagator.inject(
                        tracer.currentTraceContext().context(), request.getHeaders(),
                        (HttpHeaders headers, String key, String value) -> {
                            headers.add(key, value);
                        }
                );
            }
            return execution.execute(request, body);
        }));
    }

}
