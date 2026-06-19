package com.example.auth_service.handler;

import com.example.auth_service.dto.ErrorResponse;
import com.example.auth_service.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex){

        ErrorResponse response =
                new ErrorResponse(
                        ex.getErrorCode().name(),
                        ex.getMessage(),
                        LocalDateTime.now()
                );

        return ResponseEntity
                .status(
                        ex.getErrorCode().getStatus()
                )
                .body(response);
    }
}