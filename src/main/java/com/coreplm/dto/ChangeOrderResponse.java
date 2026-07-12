package com.coreplm.dto;

import java.time.LocalDateTime;

public record ChangeOrderResponse(
        Long id,
        String ecoNumber,
        Long changeRequestId,
        String ecrNumber,
        String status,
        String createdByUsername,
        String closedByUsername,
        LocalDateTime closedAt,
        LocalDateTime createdAt
) {}