package com.felipe.teachgram_backend.constants;

import lombok.Getter;

@Getter
public enum AuthErrorMessages {
    INVALID_CREDENTIALS("Invalid username or password."),
    USER_NOT_FOUND("User not found after authentication.");

    private final String message;

    AuthErrorMessages(String message) {
        this.message = message;
    }
}
