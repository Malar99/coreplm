package com.coreplm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChangeRequestCreateRequest(
        @NotNull(message = "Item ID is required")
        Long itemId,

        Long revisionId,

        @NotBlank(message = "Reason is required")
        @Size(max = 1000)
        String reason
) {}