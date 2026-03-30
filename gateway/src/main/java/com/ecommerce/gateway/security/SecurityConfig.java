package com.ecommerce.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf((ServerHttpSecurity.CsrfSpec csrfSpec) -> {
                    csrfSpec.disable();
                })
                .authorizeExchange((ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchangeSpec) -> {
                    authorizeExchangeSpec.anyExchange().authenticated();
                })
                .oauth2ResourceServer((ServerHttpSecurity.OAuth2ResourceServerSpec oAuth2ResourceServerSpec) -> {
                    oAuth2ResourceServerSpec.jwt(Customizer.withDefaults());
                })
                .build();

    }

}
