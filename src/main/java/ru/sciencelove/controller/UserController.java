package ru.sciencelove.controller;

import ru.sciencelove.dto.response.UserResponse;
import ru.sciencelove.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(Authentication auth) {
        UserResponse user = userService.getCurrentUser(auth);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMyProfile(
            Authentication auth,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String avatarUrl,
            @RequestParam(required = false) String phone) {

        UserResponse user = userService.updateProfile(auth, fullName, avatarUrl, phone);
        return ResponseEntity.ok(user);
    }
}