package com.coreplm.dto;

import java.time.LocalDateTime;

public record ChangeRequestResponse(
        Long id,
        String ecrNumber,
        Long itemId,
        String itemNumber,
        Long revisionId,
        String revisionLabel,
        String reason,
        String status,
        String submittedByUsername,
        String reviewedByUsername,
        LocalDateTime reviewedAt,
        String reviewComments,
        LocalDateTime createdAt
) {}