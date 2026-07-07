package com.coreplm.dto;

import java.util.Set;

public record RoleAssignmentRequest(Set<String> roleNames) {}