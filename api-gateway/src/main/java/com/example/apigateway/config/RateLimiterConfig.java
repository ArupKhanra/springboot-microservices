package com.example.apigateway.config;

import com.example.apigateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public KeyResolver userKeyResolver() {

        return exchange -> {

            String authHeader =
                    exchange.getRequest()
                            .getHeaders()
                            .getFirst("Authorization");

            if (authHeader != null &&
                    authHeader.startsWith("Bearer ")) {

                String token =
                        authHeader.substring(7);

                String username =
                        jwtUtil.extractUsername(token);

                return Mono.just(username);
            }

            return Mono.just("anonymous");
        };
    }
}