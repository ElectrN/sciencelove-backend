package ru.sciencelove.controller;

import ru.sciencelove.dto.request.CreateRequestRequest;
import ru.sciencelove.dto.request.UpdateRequestRequest;
import ru.sciencelove.dto.response.RequestResponse;
import ru.sciencelove.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RequestController {

    private final RequestService requestService;

    @GetMapping
    public ResponseEntity<List<RequestResponse>> getMyRequests(Authentication auth) {
        List<RequestResponse> requests = requestService.getMyRequests(auth);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestResponse> getRequestById(
            @PathVariable UUID id,
            Authentication auth) {

        RequestResponse request = requestService.getRequestById(id, auth);
        return ResponseEntity.ok(request);
    }

    @PostMapping
    public ResponseEntity<RequestResponse> createRequest(
            Authentication auth,
            @Valid @RequestBody CreateRequestRequest request) {

        RequestResponse created = requestService.createRequest(auth, request);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RequestResponse> updateRequest(
            @PathVariable UUID id,
            Authentication auth,
            @Valid @RequestBody UpdateRequestRequest request) {

        RequestResponse updated = requestService.updateRequest(id, auth, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelRequest(
            @PathVariable UUID id,
            Authentication auth) {

        requestService.cancelRequest(id, auth);
        return ResponseEntity.noContent().build();
    }
}