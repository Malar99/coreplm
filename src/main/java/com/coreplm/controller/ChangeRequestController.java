package com.coreplm.controller;

import com.coreplm.dto.ChangeRequestCreateRequest;
import com.coreplm.dto.ChangeRequestResponse;
import com.coreplm.dto.ChangeRequestReviewRequest;
import com.coreplm.service.ChangeOrderService;
import com.coreplm.service.ChangeRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/change-requests")
public class ChangeRequestController {

    private final ChangeRequestService changeRequestService;
    private final ChangeOrderService changeOrderService;

    public ChangeRequestController(ChangeRequestService changeRequestService,
                                    ChangeOrderService changeOrderService) {
        this.changeRequestService = changeRequestService;
        this.changeOrderService = changeOrderService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ENGINEER','ADMIN')")
    public ResponseEntity<ChangeRequestResponse> createChangeRequest(
            @Valid @RequestBody ChangeRequestCreateRequest request,
            Authentication authentication) {
        var response = changeRequestService.createChangeRequest(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ENGINEER','ADMIN')")
    public ResponseEntity<ChangeRequestResponse> submit(
            @PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(changeRequestService.submitChangeRequest(id, authentication.getName()));
    }

    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ChangeRequestResponse> review(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRequestReviewRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(changeRequestService.reviewChangeRequest(id, request, authentication.getName()));
    }

    @PostMapping("/{id}/generate-eco")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> generateEco(@PathVariable Long id, Authentication authentication) {
        var response = changeOrderService.generateFromApprovedEcr(id, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChangeRequestResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(changeRequestService.getChangeRequestById(id));
    }

    @GetMapping
    public ResponseEntity<List<ChangeRequestResponse>> getAll() {
        return ResponseEntity.ok(changeRequestService.getAllChangeRequests());
    }
}