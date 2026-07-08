package com.coreplm.dto;

import java.time.LocalDateTime;

public record ItemResponse(
        Long id,
        String itemNumber,
        String name,
        String description,
        String itemType,
        boolean active,
        String createdByUsername,
        LocalDateTime createdAt
) {}