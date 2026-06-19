package com.example.auth_service.dto;

import java.time.LocalDateTime;

public record ErrorResponse(

        String errorCode,
        String message,
        LocalDateTime timestamp

) {}