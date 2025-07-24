package com.felipe.teachgram_backend.mapper;

import com.felipe.teachgram_backend.dto.user.UserResponseDTO;
import com.felipe.teachgram_backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = java.util.stream.Collectors.class)
public interface UserMapper {
    @Mapping(
            target = "roles",
            expression = "java(user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet()))"
    )
    UserResponseDTO toDto(User user);
}