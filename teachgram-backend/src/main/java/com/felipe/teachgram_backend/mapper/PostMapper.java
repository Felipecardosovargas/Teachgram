package com.felipe.teachgram_backend.mapper;

import com.felipe.teachgram_backend.dto.post.PostResponseDTO;
import org.mapstruct.Mapper;
import com.felipe.teachgram_backend.entity.Post;

@Mapper(componentModel = "spring")
public interface PostMapper {
    PostResponseDTO toDto(Post post);
}
