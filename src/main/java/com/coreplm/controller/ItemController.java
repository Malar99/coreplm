package com.coreplm.controller;

import com.coreplm.dto.ItemCreateRequest;
import com.coreplm.dto.ItemResponse;
import com.coreplm.dto.ItemUpdateRequest;
import com.coreplm.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ENGINEER','ADMIN')")
    public ResponseEntity<ItemResponse> createItem(
            @Valid @RequestBody ItemCreateRequest request,
            Authentication authentication) {

        ItemResponse response = itemService.createItem(request, authentication.getName());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getItemById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @GetMapping
    public ResponseEntity<List<ItemResponse>> getAllItems(
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(itemService.getAllItems(type));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENGINEER','ADMIN')")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody ItemUpdateRequest request) {
        return ResponseEntity.ok(itemService.updateItem(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENGINEER','ADMIN')")
    public ResponseEntity<Void> deactivateItem(@PathVariable Long id) {
        itemService.deactivateItem(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/by-number/{itemNumber}")
    public ResponseEntity<ItemResponse> getItemByItemNumber(@PathVariable String itemNumber) {
        return ResponseEntity.ok(itemService.getItemByItemNumber(itemNumber));
    }
}