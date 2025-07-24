package com.felipe.teachgram_backend.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserRole {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    MODERATOR("ROLE_MODERATOR");

    private final String roleName;
}