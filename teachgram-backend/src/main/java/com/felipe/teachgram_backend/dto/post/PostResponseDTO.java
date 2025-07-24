package com.felipe.teachgram_backend.dto.post;

import com.felipe.teachgram_backend.dto.user.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String photoLink;
    private String videoLink;
    private Boolean privatePost;
    private Integer likesCount;
    private LocalDate createdAt;
    private LocalDate updatedAt;
    private UserResponseDTO userResponseDTO;
}