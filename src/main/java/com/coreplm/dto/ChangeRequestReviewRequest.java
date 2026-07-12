package com.coreplm.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangeRequestReviewRequest(
        @NotNull(message = "Decision is required")
        boolean approved,

        @Size(max = 1000)
        String comments
) {}