package com.felipe.teachgram_backend.controller;

import com.felipe.teachgram_backend.dto.user.UserFollowDTO;
import com.felipe.teachgram_backend.entity.User;
import com.felipe.teachgram_backend.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Follow", description = "Gerenciamento de seguidores e seguidos")
@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "Seguir um usuário", description = "O usuário autenticado passa a seguir o usuário " +
            "identificado pelo 'followingId'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário seguido com sucesso."),
            @ApiResponse(responseCode = "400", description = "Já está seguindo esse usuário ou requisição inválida."),
            @ApiResponse(responseCode = "404", description = "Usuário a seguir não encontrado."),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado.")
    })
    @PostMapping("/{followingId}")
    public ResponseEntity<ApiResponseMessage> follow(
            @PathVariable UUID followingId,
            @AuthenticationPrincipal User user
    ) {
        followService.followUser(user.getId(), followingId);
        return ResponseEntity.ok(new ApiResponseMessage("Usuário seguido com sucesso."));
    }

    @Operation(summary = "Deixar de seguir um usuário", description = "O usuário autenticado para de seguir o " +
            "usuário identificado pelo 'followingId'.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário deixado de seguir com sucesso."),
            @ApiResponse(responseCode = "404", description = "Usuário a deixar de seguir não encontrado."),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado.")
    })
    @DeleteMapping("/{followingId}")
    public ResponseEntity<Void> unfollow(
            @PathVariable UUID followingId,
            @AuthenticationPrincipal User user
    ) {
        followService.unfollowUser(user.getId(), followingId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar seguidores", description = "Retorna uma lista paginada dos usuários que " +
            "seguem o usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de seguidores retornada com sucesso.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserFollowDTO.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado.")
    })
    @GetMapping("/followers")
    public ResponseEntity<List<UserFollowDTO>> getFollowers(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(followService.getFollowers(user.getId()));
    }

    @Operation(summary = "Listar seguindo", description = "Retorna uma lista paginada dos usuários que o " +
            "usuário autenticado está seguindo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de seguindo retornada com sucesso.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserFollowDTO.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado.")
    })
    @GetMapping("/following")
    public ResponseEntity<List<UserFollowDTO>> getFollowing(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(followService.getFollowing(user.getId()));
    }

    /**
     * Classe auxiliar para respostas com mensagem simples.
     * Facilita o front-end a exibir feedback amigável e padronizado.
     */
    private static class ApiResponseMessage {
        private final String message;

        public ApiResponseMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}