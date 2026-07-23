package com.coreplm.controller;

import com.coreplm.dto.RevisionResponse;
import com.coreplm.dto.RevisionStatusUpdateRequest;
import com.coreplm.service.ItemRevisionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/revisions")
public class RevisionController {

    private final ItemRevisionService revisionService;

    public RevisionController(ItemRevisionService revisionService) {
        this.revisionService = revisionService;
    }

    @GetMapping("/{revisionId}")
    public ResponseEntity<RevisionResponse> getRevisionById(@PathVariable Long revisionId) {
        return ResponseEntity.ok(revisionService.getRevisionById(revisionId));
    }

    @PatchMapping("/{revisionId}/status")
    public ResponseEntity<RevisionResponse> updateStatus(
            @PathVariable Long revisionId,
            @RequestBody RevisionStatusUpdateRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                revisionService.updateStatus(
                        revisionId,
                        request,
                        authentication.getName()
                )
        );
    }
}