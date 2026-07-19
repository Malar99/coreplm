package com.coreplm.controller;

import com.coreplm.dto.BomLineCreateRequest;
import com.coreplm.dto.BomLineResponse;
import com.coreplm.dto.BomTreeNodeResponse;
import com.coreplm.service.BomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/revisions/{revisionId}/bom")
public class BomController {

    private final BomService bomService;

    public BomController(BomService bomService) {
        this.bomService = bomService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ENGINEER','ADMIN')")
    public ResponseEntity<BomLineResponse> addBomLine(
            @PathVariable Long revisionId,
            @Valid @RequestBody BomLineCreateRequest request,
            Authentication authentication) {

        var response = bomService.addBomLine(revisionId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BomLineResponse>> getDirectChildren(@PathVariable Long revisionId) {
        return ResponseEntity.ok(bomService.getDirectChildren(revisionId));
    }

    @GetMapping("/tree")
    public ResponseEntity<BomTreeNodeResponse> getFullTree(@PathVariable Long revisionId) {
        return ResponseEntity.ok(bomService.getFullBomTree(revisionId));
    }

    @DeleteMapping("/lines/{bomLineId}")
    @PreAuthorize("hasAnyRole('ENGINEER','ADMIN')")
    public ResponseEntity<Void> removeBomLine(
            @PathVariable Long revisionId,
            @PathVariable Long bomLineId) {

        bomService.removeBomLine(bomLineId);
        return ResponseEntity.noContent().build();
    }
}