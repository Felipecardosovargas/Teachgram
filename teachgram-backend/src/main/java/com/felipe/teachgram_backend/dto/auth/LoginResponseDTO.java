package com.felipe.teachgram_backend.dto.auth;

import java.util.UUID;

public record LoginResponseDTO(String token, UUID userId, String userName, Long expiresIn) {
}