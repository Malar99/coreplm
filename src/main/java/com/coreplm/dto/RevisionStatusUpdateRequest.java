package com.coreplm.dto;

import com.coreplm.entity.RevisionStatus;
import jakarta.validation.constraints.NotNull;

public record RevisionStatusUpdateRequest(
        @NotNull(message = "Status is required")
        RevisionStatus status
) {}