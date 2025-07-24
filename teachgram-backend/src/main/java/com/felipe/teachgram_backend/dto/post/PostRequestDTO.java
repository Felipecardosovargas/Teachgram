package com.felipe.teachgram_backend.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRequestDTO {

    @NotBlank(message = "O título é obrigatório.")
    @Size(max = 50, message = "O título deve ter no máximo 50 caracteres.")
    private String title;

    @Size(max = 200, message = "A descrição deve ter no máximo 200 caracteres.")
    private String description;

    private String photoLink;

    private String videoLink;

    @NotNull(message = "A privacidade do post é obrigatória.")
    private Boolean privatePost;
}