package com.felipe.teachgram_backend.dto.user;

import java.util.UUID;

public record UserFollowDTO(
        UUID id,
        String name,
        String username,
        String profileLink
) {}
