package com.sciencelove.controller;

import com.sciencelove.dto.request.AuthRequest;
import com.sciencelove.dto.request.RegisterRequest;
import com.sciencelove.dto.response.AuthResponse;
import com.sciencelove.dto.response.UserResponse;
import com.sciencelove.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Для разработки, в продакшене ограничь
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/register
     * Регистрация нового пользователя
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/login
     * Вход в систему
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/auth/me
     * Получить текущего пользователя
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication auth) {
        UserResponse user = authService.getCurrentUser(auth);
        return ResponseEntity.ok(user);
    }

    /**
     * GET /api/auth/test
     * Публичный тестовый эндпоинт
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Auth API работает! 🎉");
    }
}