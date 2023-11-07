package com.itskool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
        return http
                .authorizeExchange(authorizeRequests ->
                        authorizeRequests.pathMatchers("/openapi/**")
                                .permitAll()
                                .pathMatchers("/webjars/**")
                                .permitAll()
                                .pathMatchers("/actuator/**")
                                .permitAll()
                                .pathMatchers(POST, "/product-composite/**")
                                .hasAuthority("SCOPE_product:write")
                                .pathMatchers(DELETE, "/product-composite/**")
                                .hasAuthority("SCOPE_product:write")
                                .pathMatchers(GET, "/product-composite/**")
                                .hasAuthority("SCOPE_product:read")
                                .anyExchange()
                                .authenticated()
                )
                .oauth2ResourceServer(oAuth2ResourceServerConfigurer ->
                        oAuth2ResourceServerConfigurer.jwt(withDefaults()))
                .build();
    }
}
