package com.example.apigateway.filter;

import com.example.apigateway.util.JwtUtil;
import com.example.apigateway.validator.RouteValidator;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             GatewayFilterChain chain) {

        if (!routeValidator.isSecured.test(exchange.getRequest())) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            exchange.getResponse()
                    .setStatusCode(HttpStatus.UNAUTHORIZED);

            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {

            jwtUtil.validateToken(token);

            Claims claims =
                    jwtUtil.extractClaims(token);

            List<String> roles =
                    claims.get("roles", List.class);

            String path =
                    exchange.getRequest()
                            .getURI()
                            .getPath();

            // Admin APIs
            if (path.startsWith("/admin")) {

                if (roles == null ||
                        !roles.contains("ADMIN")) {

                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FORBIDDEN);

                    return exchange.getResponse().setComplete();
                }
            }

            return chain.filter(exchange);

        } catch (Exception e) {

            exchange.getResponse()
                    .setStatusCode(HttpStatus.UNAUTHORIZED);

            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}