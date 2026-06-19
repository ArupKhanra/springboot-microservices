package com.example.apigateway.validator;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    private static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/refresh"
    );

    public final Predicate<ServerHttpRequest> isSecured =
            request -> OPEN_API_ENDPOINTS.stream()
                    .noneMatch(endpoint ->
                            request.getURI()
                                    .getPath()
                                    .contains(endpoint));
}