package com.gfolly.backend.iam.api.dto.requests;

import com.gfolly.backend.iam.domain.Role;
import jakarta.validation.constraints.NotNull;

public record SelectProfileRequest(@NotNull Role role) {}
