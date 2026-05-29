package ru.sciencelove.service;

import ru.sciencelove.dto.request.CreateMentorProfileRequest;
import ru.sciencelove.dto.response.MentorResponse;
import ru.sciencelove.entity.MentorProfile;
import ru.sciencelove.entity.ScientificField;
import ru.sciencelove.entity.User;
import ru.sciencelove.repository.MentorProfileRepository;
import ru.sciencelove.repository.ScientificFieldRepository;
import ru.sciencelove.repository.UserRepository;
import ru.sciencelove.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorService {

    private final MentorProfileRepository mentorProfileRepository;
    private final UserRepository userRepository;
    private final ScientificFieldRepository scientificFieldRepository;

    public List<MentorResponse> getAllActiveMentors() {
        List<MentorProfile> mentors = mentorProfileRepository.findAllActive();
        return mentors.stream()
                .map(this::toMentorResponse)
                .collect(Collectors.toList());
    }

    public MentorResponse getMentorById(UUID mentorId) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Ментор не найден"));

        if (!mentor.getUser().getIsActive()) {
            throw new RuntimeException("Ментор неактивен");
        }

        return toMentorResponse(mentor);
    }

    public MentorResponse getCurrentMentorProfile(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof UserDetailsImpl)) {
            throw new RuntimeException("Неверный тип principal");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;

        MentorProfile mentor = mentorProfileRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Профиль ментора не найден"));

        return toMentorResponse(mentor);
    }

    @Transactional
    public MentorResponse createMentorProfile(Authentication auth, CreateMentorProfileRequest request) {
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

        if (user.getRole() != User.Role.MENTOR) {
            throw new RuntimeException("Только менторы могут создавать профиль ментора");
        }

        if (mentorProfileRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("Профиль ментора уже существует");
        }

        MentorProfile mentor = MentorProfile.builder()
                .user(user)
                .degree(request.getDegree())
                .university(request.getUniversity())
                .department(request.getDepartment())
                .bio(request.getBio())
                .expertise(request.getExpertise())
                .hourlyRate(request.getHourlyRate())
                .minSessionMinutes(request.getMinSessionMinutes() != null ? request.getMinSessionMinutes() : 30)
                .availableDays(request.getAvailableDays())
                .availableHoursStart(request.getAvailableHoursStart())
                .availableHoursEnd(request.getAvailableHoursEnd())
                .isPremium(false)
                .rating(BigDecimal.ZERO)
                .totalConsultations(0)
                .totalReviews(0)
                .build();

        if (request.getInterestFieldIds() != null && !request.getInterestFieldIds().isEmpty()) {
            List<ScientificField> interests = scientificFieldRepository.findAllById(request.getInterestFieldIds());
            mentor.setInterests(interests);
        }

        mentorProfileRepository.save(mentor);

        return toMentorResponse(mentor);
    }

    @Transactional
    public MentorResponse updateMentorProfile(Authentication auth, CreateMentorProfileRequest request) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof UserDetailsImpl)) {
            throw new RuntimeException("Неверный тип principal");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;

        MentorProfile mentor = mentorProfileRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Профиль ментора не найден"));

        if (request.getDegree() != null) {
            mentor.setDegree(request.getDegree());
        }
        if (request.getUniversity() != null) {
            mentor.setUniversity(request.getUniversity());
        }
        if (request.getDepartment() != null) {
            mentor.setDepartment(request.getDepartment());
        }
        if (request.getBio() != null) {
            mentor.setBio(request.getBio());
        }
        if (request.getExpertise() != null) {
            mentor.setExpertise(request.getExpertise());
        }
        if (request.getHourlyRate() != null) {
            mentor.setHourlyRate(request.getHourlyRate());
        }
        if (request.getMinSessionMinutes() != null) {
            mentor.setMinSessionMinutes(request.getMinSessionMinutes());
        }
        if (request.getAvailableDays() != null) {
            mentor.setAvailableDays(request.getAvailableDays());
        }
        if (request.getAvailableHoursStart() != null) {
            mentor.setAvailableHoursStart(request.getAvailableHoursStart());
        }
        if (request.getAvailableHoursEnd() != null) {
            mentor.setAvailableHoursEnd(request.getAvailableHoursEnd());
        }
        if (request.getInterestFieldIds() != null) {
            List<ScientificField> interests = scientificFieldRepository.findAllById(request.getInterestFieldIds());
            mentor.setInterests(interests);
        }

        mentorProfileRepository.save(mentor);

        return toMentorResponse(mentor);
    }

    public List<MentorResponse> searchMentors(String keyword, List<UUID> fieldIds, BigDecimal maxPrice, BigDecimal minRating) {
        List<MentorProfile> mentors;

        if (fieldIds != null && !fieldIds.isEmpty()) {
            mentors = mentorProfileRepository.findByInterestIdsAndUserActive(fieldIds);
        } else if (keyword != null && !keyword.isEmpty()) {
            // ✅ ИСПРАВЛЕНО: добавлены скобки для правильного приоритета
            String lowerKeyword = keyword.toLowerCase();
            mentors = mentorProfileRepository.findAllActive().stream()
                    .filter(m -> {
                        boolean bioMatch = m.getBio() != null && m.getBio().toLowerCase().contains(lowerKeyword);
                        boolean universityMatch = m.getUniversity() != null && m.getUniversity().toLowerCase().contains(lowerKeyword);
                        boolean degreeMatch = m.getDegree() != null && m.getDegree().toLowerCase().contains(lowerKeyword);
                        boolean departmentMatch = m.getDepartment() != null && m.getDepartment().toLowerCase().contains(lowerKeyword);
                        boolean expertiseMatch = m.getExpertise() != null &&
                                m.getExpertise().stream().anyMatch(exp -> exp.toLowerCase().contains(lowerKeyword));

                        return bioMatch || universityMatch || degreeMatch || departmentMatch || expertiseMatch;
                    })
                    .collect(Collectors.toList());
        } else {
            mentors = mentorProfileRepository.findAllActive();
        }

        // Фильтр по максимальной цене
        if (maxPrice != null) {
            mentors = mentors.stream()
                    .filter(m -> m.getHourlyRate() == null || m.getHourlyRate().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());
        }

        // Фильтр по минимальному рейтингу
        if (minRating != null) {
            mentors = mentors.stream()
                    .filter(m -> m.getRating() != null && m.getRating().compareTo(minRating) >= 0)
                    .collect(Collectors.toList());
        }

        return mentors.stream()
                .map(this::toMentorResponse)
                .collect(Collectors.toList());
    }

    private MentorResponse toMentorResponse(MentorProfile mentor) {
        List<MentorResponse.FieldSummary> interests = mentor.getInterests() != null ?
                mentor.getInterests().stream()
                        .map(f -> MentorResponse.FieldSummary.builder()
                                .id(f.getId())
                                .name(f.getName())
                                .slug(f.getSlug())
                                .build())
                        .collect(Collectors.toList()) : List.of();

        return MentorResponse.builder()
                .id(mentor.getId())
                .degree(mentor.getDegree())
                .university(mentor.getUniversity())
                .bio(mentor.getBio())
                .rating(mentor.getRating())
                .totalConsultations(mentor.getTotalConsultations())
                .hourlyRate(mentor.getHourlyRate())
                .isPremium(mentor.getIsPremium())
                .interests(interests)
                .expertise(mentor.getExpertise())
                .build();
    }
}