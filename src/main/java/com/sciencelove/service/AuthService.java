package com.sciencelove.service;

import com.sciencelove.dto.request.RegisterRequest;
import com.sciencelove.dto.request.AuthRequest;
import com.sciencelove.dto.response.AuthResponse;
import com.sciencelove.dto.response.UserResponse;
import com.sciencelove.entity.User;
import com.sciencelove.repository.UserRepository;
import com.sciencelove.security.JwtTokenProvider;
import com.sciencelove.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email уже зарегистрирован");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.Role.valueOf(request.getRole()))
                .isActive(true)
                .emailVerified(false)
                .build();

        userRepository.save(user);

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = tokenProvider.generateToken(auth);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(86400000L)
                .user(toUserResponse(user))
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Неверный пароль");
        }

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String token = tokenProvider.generateToken(auth);
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        return AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(86400000L)
                .user(toUserResponse(user))
                .build();
    }

    public UserResponse getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof UserDetailsImpl)) {
            throw new RuntimeException("Неверный тип principal");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole().name())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .build();
    }
}