package com.coreplm.dto;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(

        Long id,

        String username,

        String email,

        String fullName,

        boolean active,

        Set<String> roles,

        LocalDateTime createdAt
) {}