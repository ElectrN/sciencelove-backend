package ru.sciencelove.controller;

import ru.sciencelove.dto.response.MentorResponse;
import ru.sciencelove.dto.response.RequestResponse;
import ru.sciencelove.entity.MentorProfile;
import ru.sciencelove.entity.Request;
import ru.sciencelove.repository.MentorProfileRepository;
import ru.sciencelove.repository.RequestRepository;
import ru.sciencelove.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MatchingController {

    private final MatchingService matchingService;
    private final RequestRepository requestRepository;
    private final MentorProfileRepository mentorProfileRepository;

    /**
     * GET /api/matching/requests/{id}/suggest
     * Получить список подходящих менторов для заявки
     */
    @GetMapping("/requests/{id}/suggest")
    public ResponseEntity<List<MatchingService.MentorSuggestion>> getSuggestedMentors(@PathVariable UUID id) {
        List<MatchingService.MentorSuggestion> suggestions = matchingService.getSuggestedMentors(id);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * GET /api/matching/mentors/{id}/requests
     * Получить заявки, подходящие ментору
     */
    @GetMapping("/mentors/{id}/requests")
    public ResponseEntity<List<MatchingService.RequestSuggestion>> getSuggestedRequests(@PathVariable UUID id) {
        List<MatchingService.RequestSuggestion> suggestions = matchingService.getSuggestedRequests(id);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * POST /api/matching/score
     * Рассчитать скор совпадения между заявкой и ментором
     */
    @PostMapping("/score")
    public ResponseEntity<MatchScoreResponse> calculateScore(@RequestBody MatchScoreRequest request) {
        // Загружаем сущности из БД
        Request requestEntity = requestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        MentorProfile mentorEntity = mentorProfileRepository.findById(request.getMentorId())
                .orElseThrow(() -> new RuntimeException("Ментор не найден"));

        double score = matchingService.calculateMatchScore(requestEntity, mentorEntity);
        return ResponseEntity.ok(new MatchScoreResponse(score));
    }

    /**
     * DTO для запроса расчёта скорса
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class MatchScoreRequest {
        private UUID requestId;
        private UUID mentorId;
    }

    /**
     * DTO для ответа расчёта скорса
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class MatchScoreResponse {
        private double score;
        private String recommendation;

        public MatchScoreResponse(double score) {
            this.score = Math.round(score * 100.0) / 100.0;
            this.recommendation = getRecommendation(this.score);
        }

        private String getRecommendation(double score) {
            if (score >= 0.8) return "Отличное совпадение!";
            if (score >= 0.6) return "Хорошее совпадение";
            if (score >= 0.4) return "Среднее совпадение";
            return "Слабое совпадение";
        }
    }
}