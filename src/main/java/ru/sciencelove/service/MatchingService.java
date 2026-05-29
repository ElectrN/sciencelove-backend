package ru.sciencelove.service;

import ru.sciencelove.dto.response.MentorResponse;
import ru.sciencelove.dto.response.RequestResponse;
import ru.sciencelove.entity.MentorProfile;
import ru.sciencelove.entity.Request;
import ru.sciencelove.entity.ScientificField;
import ru.sciencelove.repository.MentorProfileRepository;
import ru.sciencelove.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchingService {

    private final MentorProfileRepository mentorProfileRepository;
    private final RequestRepository requestRepository;

    /**
     * Получить список подходящих менторов для заявки
     */
    public List<MentorSuggestion> getSuggestedMentors(UUID requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        if (request.getStatus() != Request.Status.OPEN) {
            throw new RuntimeException("Нельзя подобрать ментора для заявки в статусе " + request.getStatus());
        }

        // Получаем всех активных менторов
        List<MentorProfile> allMentors = mentorProfileRepository.findAllActive();

        List<MentorSuggestion> suggestions = new ArrayList<>();

        for (MentorProfile mentor : allMentors) {
            // Расчёт скорса совпадения
            double score = calculateMatchScore(request, mentor);

            // Фильтруем по минимальному порогу (0.3 = 30% совпадения)
            if (score >= 0.3) {
                suggestions.add(new MentorSuggestion(mentor, score));
            }
        }

        // Сортируем по убыванию скорса
        suggestions.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return suggestions;
    }

    /**
     * Получить заявки, подходящие ментору
     */
    public List<RequestSuggestion> getSuggestedRequests(UUID mentorId) {
        MentorProfile mentor = mentorProfileRepository.findById(mentorId)
                .orElseThrow(() -> new RuntimeException("Ментор не найден"));

        // Получаем все открытые заявки
        List<Request> allRequests = requestRepository.findByStatus(Request.Status.OPEN);

        List<RequestSuggestion> suggestions = new ArrayList<>();

        for (Request request : allRequests) {
            double score = calculateMatchScore(request, mentor);

            if (score >= 0.3) {
                suggestions.add(new RequestSuggestion(request, score));
            }
        }

        suggestions.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        return suggestions;
    }

    /**
     * Рассчитать скор совпадения между заявкой и ментором
     * @return значение от 0.0 до 1.0
     */
    public double calculateMatchScore(Request request, MentorProfile mentor) {
        double score = 0.0;

        // 1. Научные области (вес: 50%)
        double fieldScore = calculateFieldScore(request, mentor);
        score += fieldScore * 0.5;

        // 2. Бюджет (вес: 30%)
        double budgetScore = calculateBudgetScore(request, mentor);
        score += budgetScore * 0.3;

        // 3. Доступность (вес: 20%) - упрощённая версия
        double availabilityScore = 1.0; // Пока считаем, что все доступны
        score += availabilityScore * 0.2;

        return Math.min(1.0, Math.max(0.0, score)); // Ограничиваем диапазон [0, 1]
    }

    /**
     * Расчёт скорса по научным областям
     */
    private double calculateFieldScore(Request request, MentorProfile mentor) {
        List<UUID> requestFieldIds = request.getFields() != null ?
                request.getFields().stream().map(ScientificField::getId).collect(Collectors.toList()) : List.of();

        List<UUID> mentorFieldIds = mentor.getInterests() != null ?
                mentor.getInterests().stream().map(ScientificField::getId).collect(Collectors.toList()) : List.of();

        if (requestFieldIds.isEmpty() || mentorFieldIds.isEmpty()) {
            return 0.5; // Нейтральная оценка, если нет данных
        }

        // Считаем пересечение
        long commonFields = requestFieldIds.stream()
                .filter(mentorFieldIds::contains)
                .count();

        // Нормализуем относительно количества полей в заявке
        return (double) commonFields / requestFieldIds.size();
    }

    /**
     * Расчёт скорса по бюджету
     */
    private double calculateBudgetScore(Request request, MentorProfile mentor) {
        BigDecimal budget = request.getBudget();
        BigDecimal hourlyRate = mentor.getHourlyRate();

        if (budget == null || hourlyRate == null) {
            return 0.5; // Нейтральная оценка
        }

        if (hourlyRate.compareTo(budget) > 0) {
            return 0.0; // Ментор дороже бюджета — не подходит
        }

        // Чем больше "запас" бюджета, тем выше скор (но с насыщением)
        BigDecimal remaining = budget.subtract(hourlyRate);
        BigDecimal ratio = remaining.divide(budget, 2, BigDecimal.ROUND_HALF_UP);

        return Math.min(1.0, ratio.doubleValue() + 0.5);
    }

    /**
     * Внутренний класс для результата подбора ментора
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class MentorSuggestion {
        private MentorResponse mentor;
        private double score;

        public MentorSuggestion(MentorProfile mentorProfile, double score) {
            this.mentor = toMentorResponse(mentorProfile);
            this.score = Math.round(score * 100.0) / 100.0; // Округляем до 2 знаков
        }

        private MentorResponse toMentorResponse(MentorProfile mentor) {
            // Упрощённая конвертация — в реальном проекте использовать общий сервис
            return MentorResponse.builder()
                    .id(mentor.getId())
                    .degree(mentor.getDegree())
                    .university(mentor.getUniversity())
                    .bio(mentor.getBio())
                    .rating(mentor.getRating())
                    .hourlyRate(mentor.getHourlyRate())
                    .isPremium(mentor.getIsPremium())
                    .expertise(mentor.getExpertise())
                    .build();
        }
    }

    /**
     * Внутренний класс для результата подбора заявки
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class RequestSuggestion {
        private RequestResponse request;
        private double score;

        public RequestSuggestion(Request requestEntity, double score) {
            // Упрощённая конвертация
            this.request = RequestResponse.builder()
                    .id(requestEntity.getId())
                    .title(requestEntity.getTitle())
                    .topic(requestEntity.getTopic())
                    .discipline(requestEntity.getDiscipline())
                    .budget(requestEntity.getBudget())
                    .urgency(requestEntity.getUrgency() != null ? requestEntity.getUrgency().name() : null)
                    .status(requestEntity.getStatus() != null ? requestEntity.getStatus().name() : null)
                    .build();
            this.score = Math.round(score * 100.0) / 100.0;
        }
    }
}