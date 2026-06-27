package com.example.auth_service.service;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.RefreshTokenRequest;
import com.example.auth_service.dto.UserCreatedEvent;
import com.example.auth_service.entity.OutboxEvent;
import com.example.auth_service.entity.OutboxStatus;
import com.example.auth_service.entity.RefreshToken;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.BusinessException;
import com.example.auth_service.exception.ErrorCode;
import com.example.auth_service.repository.OutboxEventRepository;
import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.observation.annotation.Observed;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;
    private final OpenTelemetry openTelemetry;
    private static final TextMapSetter<Map<String, String>> MAP_SETTER =
            new TextMapSetter<>() {
                @Override
                public void set(Map<String, String> carrier, String key, String value) {
                    if (carrier != null && key != null && value != null) {
                        carrier.put(key, value);
                    }
                }
            };

    @Transactional
    @Observed(name = "register-user")
    public User registerUser(User user) {

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(List.of("USER"));

        User savedUser = userRepository.save(user);

        try {

            UserCreatedEvent event =
                    UserCreatedEvent.builder()
                            .eventId(UUID.randomUUID().toString())
                            .userId(savedUser.getId())
                            .username(savedUser.getUsername())
                            .email(savedUser.getEmail())
                            .build();

            String payload = objectMapper.writeValueAsString(event);

            // ===== Capture Current Trace Context =====
            Map<String, String> headers = new HashMap<>();

            openTelemetry
                    .getPropagators()
                    .getTextMapPropagator()
                    .inject(
                            Context.current(),
                            headers,
                            MAP_SETTER
                    );

            String traceparent = headers.get("traceparent");

            log.info("Captured Traceparent => {}", traceparent);

            OutboxEvent outboxEvent =
                    OutboxEvent.builder()
                            .eventId(event.getEventId())
                            .aggregateType("USER")
                            .aggregateId(savedUser.getId().toString())
                            .eventType("USER_CREATED")
                            .payload(payload)
                            .traceparent(traceparent)
                            .status(OutboxStatus.PENDING.name())
                            .retryCount(0)
                            .createdAt(LocalDateTime.now())
                            .build();

            outboxEventRepository.save(outboxEvent);

            log.info(
                    "Outbox Event Created Successfully : {}",
                    outboxEvent.getEventId()
            );

        } catch (Exception e) {

            log.error(
                    "Failed to create Outbox Event for user : {}",
                    user.getUsername(),
                    e
            );

            throw new RuntimeException(
                    "Failed to create outbox event",
                    e
            );
        }

        return savedUser;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        boolean isPasswordMatch =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );

        if (!isPasswordMatch) {
            throw new BusinessException(
                    ErrorCode.INVALID_PASSWORD
            );
        }

        String accessToken =
                jwtUtil.generateAccessToken(
                        user.getUsername(),
                        user.getRoles()
                );

        String refreshToken =
                jwtUtil.generateRefreshToken(
                        user.getUsername()
                );

        RefreshToken refreshTokenEntity =
                refreshTokenRepository
                        .findByUser(user)
                        .orElse(new RefreshToken());

        refreshTokenEntity.setToken(refreshToken);

        refreshTokenEntity.setExpiryDate(
                LocalDateTime.now().plusDays(7)
        );

        refreshTokenEntity.setUser(user);

        refreshTokenRepository.save(
                refreshTokenEntity
        );

        return new AuthResponse(
                accessToken,
                refreshToken
        );
    }

    public AuthResponse refreshToken(
            RefreshTokenRequest request) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(request.getRefreshToken())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.INVALID_REFRESH_TOKEN
                                )
                        );

        if (refreshToken.getExpiryDate()
                .isBefore(LocalDateTime.now())) {

            throw new BusinessException(
                    ErrorCode.REFRESH_TOKEN_EXPIRED
            );
        }

        String accessToken =
                jwtUtil.generateAccessToken(
                        refreshToken.getUser().getUsername(),
                        refreshToken.getUser().getRoles()
                );

        return new AuthResponse(
                accessToken,
                refreshToken.getToken()
        );
    }

    public void logout(
            RefreshTokenRequest request) {

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(request.getRefreshToken())
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.INVALID_REFRESH_TOKEN
                                )
                        );

        refreshTokenRepository.delete(refreshToken);
    }

    @Transactional
    public User updateRoles(
            Long userId,
            List<String> roles) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        user.setRoles(roles);

        return userRepository.save(user);
    }
}