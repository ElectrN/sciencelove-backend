package ru.sciencelove.controller;

import ru.sciencelove.dto.request.CreateStudentProfileRequest;
import ru.sciencelove.dto.response.StudentResponse;
import ru.sciencelove.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentController {

    private final StudentService studentService;

    @GetMapping("/me")
    public ResponseEntity<StudentResponse> getMyProfile(Authentication auth) {
        StudentResponse student = studentService.getCurrentStudentProfile(auth);
        return ResponseEntity.ok(student);
    }

    @PostMapping("/me")
    public ResponseEntity<StudentResponse> createMyProfile(
            Authentication auth,
            @Valid @RequestBody CreateStudentProfileRequest request) {

        StudentResponse student = studentService.createStudentProfile(auth, request);
        return ResponseEntity.ok(student);
    }

    @PutMapping("/me")
    public ResponseEntity<StudentResponse> updateMyProfile(
            Authentication auth,
            @Valid @RequestBody CreateStudentProfileRequest request) {

        StudentResponse student = studentService.updateStudentProfile(auth, request);
        return ResponseEntity.ok(student);
    }
}