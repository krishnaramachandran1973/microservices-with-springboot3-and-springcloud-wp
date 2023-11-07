package com.itskool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) throws Exception {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(authorizeExchangeSpec ->
                        authorizeExchangeSpec.pathMatchers("/headerrouting/**")
                                .permitAll()
                                .pathMatchers("/actuator/**")
                                .permitAll()
                                .pathMatchers("/eureka/**")
                                .permitAll()
                                .pathMatchers("/oauth2/**")
                                .permitAll()
                                .pathMatchers("/login/**")
                                .permitAll()
                                .pathMatchers("/error/**")
                                .permitAll()
                                .pathMatchers("/openapi/**")
                                .permitAll()
                                .pathMatchers("/webjars/**")
                                .permitAll()
                                .anyExchange()
                                .authenticated()
                )
                .oauth2ResourceServer(resourceServerConfigurer -> resourceServerConfigurer.jwt(withDefaults()));
        return http.build();
    }
}
