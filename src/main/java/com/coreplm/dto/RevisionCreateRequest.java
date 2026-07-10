package com.coreplm.dto;

import jakarta.validation.constraints.Size;

public record RevisionCreateRequest(
        @Size(max = 500)
        String changeDescription
) {}