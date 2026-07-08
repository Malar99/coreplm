package com.coreplm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ItemCreateRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 150)
        String name,

        @Size(max = 500)
        String description,

        @NotBlank(message = "Item type is required")
        String itemType
) {}