package ru.sciencelove.controller;

import ru.sciencelove.dto.request.CreateMentorProfileRequest;
import ru.sciencelove.dto.response.MentorResponse;
import ru.sciencelove.service.MentorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MentorController {

    private final MentorService mentorService;

    @GetMapping
    public ResponseEntity<List<MentorResponse>> getAllMentors() {
        List<MentorResponse> mentors = mentorService.getAllActiveMentors();
        return ResponseEntity.ok(mentors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MentorResponse> getMentorById(@PathVariable UUID id) {
        MentorResponse mentor = mentorService.getMentorById(id);
        return ResponseEntity.ok(mentor);
    }

    @GetMapping("/me")
    public ResponseEntity<MentorResponse> getMyProfile(Authentication auth) {
        MentorResponse mentor = mentorService.getCurrentMentorProfile(auth);
        return ResponseEntity.ok(mentor);
    }

    @PostMapping("/me")
    public ResponseEntity<MentorResponse> createMyProfile(
            Authentication auth,
            @Valid @RequestBody CreateMentorProfileRequest request) {

        MentorResponse mentor = mentorService.createMentorProfile(auth, request);
        return ResponseEntity.ok(mentor);
    }

    @PutMapping("/me")
    public ResponseEntity<MentorResponse> updateMyProfile(
            Authentication auth,
            @Valid @RequestBody CreateMentorProfileRequest request) {

        MentorResponse mentor = mentorService.updateMentorProfile(auth, request);
        return ResponseEntity.ok(mentor);
    }

    @GetMapping("/search")
    public ResponseEntity<List<MentorResponse>> searchMentors(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<UUID> fieldIds,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minRating) {

        List<MentorResponse> mentors = mentorService.searchMentors(keyword, fieldIds, maxPrice, minRating);
        return ResponseEntity.ok(mentors);
    }
}