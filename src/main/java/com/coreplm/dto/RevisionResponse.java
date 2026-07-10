package com.coreplm.dto;

import java.time.LocalDateTime;

public record RevisionResponse(
        Long id,
        Long itemId,
        String itemNumber,
        String revisionLabel,
        String changeDescription,
        String status,
        String createdByUsername,
        String releasedByUsername,
        LocalDateTime releasedAt,
        LocalDateTime createdAt
) {}