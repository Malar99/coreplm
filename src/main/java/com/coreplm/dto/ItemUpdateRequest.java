package com.coreplm.dto;

import jakarta.validation.constraints.Size;

public record ItemUpdateRequest(
        @Size(max = 150)
        String name,

        @Size(max = 500)
        String description
) {}