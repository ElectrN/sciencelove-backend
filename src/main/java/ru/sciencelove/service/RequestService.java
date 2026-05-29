package ru.sciencelove.service;

import ru.sciencelove.dto.request.CreateRequestRequest;
import ru.sciencelove.dto.request.UpdateRequestRequest;
import ru.sciencelove.dto.response.RequestResponse;
import ru.sciencelove.entity.Request;
import ru.sciencelove.entity.ScientificField;
import ru.sciencelove.entity.StudentProfile;
import ru.sciencelove.repository.RequestRepository;
import ru.sciencelove.repository.ScientificFieldRepository;
import ru.sciencelove.repository.StudentProfileRepository;
import ru.sciencelove.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final ScientificFieldRepository scientificFieldRepository;

    public List<RequestResponse> getMyRequests(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof UserDetailsImpl)) {
            throw new RuntimeException("Неверный тип principal");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;

        List<Request> requests = requestRepository.findByStudentUserId(userDetails.getId());

        return requests.stream()
                .map(this::toRequestResponse)
                .collect(Collectors.toList());
    }

    public RequestResponse getRequestById(UUID requestId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof UserDetailsImpl)) {
            throw new RuntimeException("Неверный тип principal");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        // Проверка: только владелец может видеть заявку
        if (!request.getStudent().getUser().getId().equals(userDetails.getId())) {
            throw new RuntimeException("Доступ запрещён");
        }

        return toRequestResponse(request);
    }

    @Transactional
    public RequestResponse createRequest(Authentication auth, CreateRequestRequest requestDto) {
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

        Request request = Request.builder()
                .student(student)
                .title(requestDto.getTitle())
                .topic(requestDto.getTopic())
                .discipline(requestDto.getDiscipline())
                .deadline(requestDto.getDeadline())
                .budget(requestDto.getBudget())
                .urgency(requestDto.getUrgency() != null ?
                        Request.Urgency.valueOf(requestDto.getUrgency()) : Request.Urgency.MEDIUM)
                .status(Request.Status.OPEN)
                .build();

        if (requestDto.getFieldIds() != null && !requestDto.getFieldIds().isEmpty()) {
            List<ScientificField> fields = scientificFieldRepository.findAllById(requestDto.getFieldIds());
            request.setFields(fields);
        }

        requestRepository.save(request);

        // Увеличиваем счётчик заявок у студента
        student.setTotalRequests(student.getTotalRequests() + 1);
        studentProfileRepository.save(student);

        return toRequestResponse(request);
    }

    @Transactional
    public RequestResponse updateRequest(UUID requestId, Authentication auth, UpdateRequestRequest requestDto) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof UserDetailsImpl)) {
            throw new RuntimeException("Неверный тип principal");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        // Проверка: только владелец может редактировать
        if (!request.getStudent().getUser().getId().equals(userDetails.getId())) {
            throw new RuntimeException("Доступ запрещён");
        }

        // Можно редактировать только статус OPEN
        if (request.getStatus() != Request.Status.OPEN) {
            throw new RuntimeException("Нельзя изменить заявку в статусе " + request.getStatus());
        }

        // Обновляем только те поля, которые переданы (частичное обновление)
        if (requestDto.getTitle() != null && !requestDto.getTitle().isEmpty()) {
            request.setTitle(requestDto.getTitle());
        }
        if (requestDto.getTopic() != null) {
            request.setTopic(requestDto.getTopic());
        }
        if (requestDto.getDiscipline() != null && !requestDto.getDiscipline().isEmpty()) {
            request.setDiscipline(requestDto.getDiscipline());
        }
        if (requestDto.getDeadline() != null) {
            request.setDeadline(requestDto.getDeadline());
        }
        if (requestDto.getBudget() != null) {
            request.setBudget(requestDto.getBudget());
        }
        if (requestDto.getUrgency() != null) {
            request.setUrgency(Request.Urgency.valueOf(requestDto.getUrgency()));
        }
        if (requestDto.getFieldIds() != null) {
            List<ScientificField> fields = scientificFieldRepository.findAllById(requestDto.getFieldIds());
            request.setFields(fields);
        }

        requestRepository.save(request);

        return toRequestResponse(request);
    }

    @Transactional
    public void cancelRequest(UUID requestId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        Object principal = auth.getPrincipal();

        if (!(principal instanceof UserDetailsImpl)) {
            throw new RuntimeException("Неверный тип principal");
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена"));

        // Проверка: только владелец может отменить
        if (!request.getStudent().getUser().getId().equals(userDetails.getId())) {
            throw new RuntimeException("Доступ запрещён");
        }

        // Можно отменить только статус OPEN
        if (request.getStatus() != Request.Status.OPEN) {
            throw new RuntimeException("Нельзя отменить заявку в статусе " + request.getStatus());
        }

        request.setStatus(Request.Status.CANCELLED);
        requestRepository.save(request);
    }

    private RequestResponse toRequestResponse(Request request) {
        List<RequestResponse.FieldSummary> fields = request.getFields() != null ?
                request.getFields().stream()
                        .map(f -> RequestResponse.FieldSummary.builder()
                                .id(f.getId())
                                .name(f.getName())
                                .slug(f.getSlug())
                                .build())
                        .collect(Collectors.toList()) : List.of();

        RequestResponse.StudentSummary student = RequestResponse.StudentSummary.builder()
                .id(request.getStudent().getId())
                .fullName(request.getStudent().getUser().getFullName())
                .university(request.getStudent().getUniversity())
                .build();

        return RequestResponse.builder()
                .id(request.getId())
                .title(request.getTitle())
                .topic(request.getTopic())
                .discipline(request.getDiscipline())
                .deadline(request.getDeadline())
                .budget(request.getBudget())
                .urgency(request.getUrgency() != null ? request.getUrgency().name() : null)
                .status(request.getStatus() != null ? request.getStatus().name() : null)
                .fields(fields)
                .student(student)
                .createdAt(request.getCreatedAt())
                .build();
    }
}