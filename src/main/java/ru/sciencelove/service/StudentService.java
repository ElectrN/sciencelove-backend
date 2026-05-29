package ru.sciencelove.service;

import ru.sciencelove.dto.request.CreateStudentProfileRequest;
import ru.sciencelove.dto.response.StudentResponse;
import ru.sciencelove.entity.StudentProfile;
import ru.sciencelove.entity.User;
import ru.sciencelove.repository.StudentProfileRepository;
import ru.sciencelove.repository.UserRepository;
import ru.sciencelove.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserRepository userRepository;

    public StudentResponse getCurrentStudentProfile(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof UserDetailsImpl)) {
            throw new RuntimeException("Неверный тип principal");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;

        StudentProfile student = studentProfileRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Профиль студента не найден"));

        return toStudentResponse(student);
    }

    @Transactional
    public StudentResponse createStudentProfile(Authentication auth, CreateStudentProfileRequest request) {
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

        if (user.getRole() != User.Role.STUDENT) {
            throw new RuntimeException("Только студенты могут создавать профиль студента");
        }

        if (studentProfileRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("Профиль студента уже существует");
        }

        StudentProfile student = StudentProfile.builder()
                .user(user)
                .university(request.getUniversity())
                .faculty(request.getFaculty())
                .course(request.getCourse())
                .degreeType(request.getDegreeType() != null ?
                        StudentProfile.DegreeType.valueOf(request.getDegreeType()) : null)
                .interests(request.getInterests())
                .goals(request.getGoals())
                .totalRequests(0)
                .totalConsultations(0)
                .build();

        studentProfileRepository.save(student);

        return toStudentResponse(student);
    }

    @Transactional
    public StudentResponse updateStudentProfile(Authentication auth, CreateStudentProfileRequest request) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof UserDetailsImpl)) {
            throw new RuntimeException("Неверный тип principal");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;

        StudentProfile student = studentProfileRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Профиль студента не найден"));

        if (request.getUniversity() != null) {
            student.setUniversity(request.getUniversity());
        }
        if (request.getFaculty() != null) {
            student.setFaculty(request.getFaculty());
        }
        if (request.getCourse() != null) {
            student.setCourse(request.getCourse());
        }
        if (request.getDegreeType() != null) {
            student.setDegreeType(StudentProfile.DegreeType.valueOf(request.getDegreeType()));
        }
        if (request.getInterests() != null) {
            student.setInterests(request.getInterests());
        }
        if (request.getGoals() != null) {
            student.setGoals(request.getGoals());
        }

        studentProfileRepository.save(student);

        return toStudentResponse(student);
    }

    private StudentResponse toStudentResponse(StudentProfile student) {
        return StudentResponse.builder()
                .id(student.getId())
                .university(student.getUniversity())
                .faculty(student.getFaculty())
                .course(student.getCourse())
                .degreeType(student.getDegreeType() != null ? student.getDegreeType().name() : null)
                .interests(student.getInterests())
                .goals(student.getGoals())
                .build();
    }
}