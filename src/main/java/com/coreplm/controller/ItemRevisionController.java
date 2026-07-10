package com.coreplm.controller;

import com.coreplm.dto.RevisionCreateRequest;
import com.coreplm.dto.RevisionResponse;
import com.coreplm.dto.RevisionStatusUpdateRequest;
import com.coreplm.service.ItemRevisionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items/{itemId}/revisions")
public class ItemRevisionController {

    private final ItemRevisionService revisionService;

    public ItemRevisionController(ItemRevisionService revisionService) {
        this.revisionService = revisionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ENGINEER','ADMIN')")
    public ResponseEntity<RevisionResponse> createRevision(
            @PathVariable Long itemId,
            @Valid @RequestBody RevisionCreateRequest request,
            Authentication authentication) {

        RevisionResponse response = revisionService.createRevision(itemId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RevisionResponse>> getRevisionsForItem(@PathVariable Long itemId) {
        return ResponseEntity.ok(revisionService.getRevisionsForItem(itemId));
    }

    @GetMapping("/{revisionId}")
    public ResponseEntity<RevisionResponse> getRevisionById(
            @PathVariable Long itemId,
            @PathVariable Long revisionId) {
        return ResponseEntity.ok(revisionService.getRevisionById(revisionId));
    }

    @PatchMapping("/{revisionId}/status")
    @PreAuthorize("hasAnyRole('ENGINEER','ADMIN')")
    public ResponseEntity<RevisionResponse> updateStatus(
            @PathVariable Long itemId,
            @PathVariable Long revisionId,
            @Valid @RequestBody RevisionStatusUpdateRequest request,
            Authentication authentication) {

        RevisionResponse response = revisionService.updateStatus(revisionId, request, authentication.getName());
        return ResponseEntity.ok(response);
    }
}