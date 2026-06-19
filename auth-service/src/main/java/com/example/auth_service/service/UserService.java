package com.example.auth_service.service;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.RefreshTokenRequest;
import com.example.auth_service.entity.RefreshToken;
import com.example.auth_service.entity.User;
import com.example.auth_service.exception.BusinessException;
import com.example.auth_service.exception.ErrorCode;
import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public User registerUser(User user) {

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        user.setRoles(List.of("USER"));

        return userRepository.save(user);
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