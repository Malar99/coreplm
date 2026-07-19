package com.coreplm.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BomLineResponse(
        Long id,
        Long parentRevisionId,
        Long childRevisionId,
        String childItemNumber,
        String childRevisionLabel,
        BigDecimal quantity,
        String addedByUsername,
        LocalDateTime createdAt
) {}