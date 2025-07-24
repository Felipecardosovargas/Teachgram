package com.felipe.teachgram_backend.controller;

import com.felipe.teachgram_backend.dto.post.PostRequestDTO;
import com.felipe.teachgram_backend.dto.post.PostResponseDTO;
import com.felipe.teachgram_backend.entity.User;
import com.felipe.teachgram_backend.service.PostService;
import com.felipe.teachgram_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Gerenciamento de posts da aplicação Teachgram")
public class PostController {

    private final PostService postService;
    private final UserService userService;

    private UUID getCurrentLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuário não autenticado no contexto de segurança.");
        }
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userService.userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("Usuário logado não encontrado no banco de dados."))
                    .getId();
        }
        throw new IllegalStateException("Tipo de principal desconhecido.");
    }

    @Operation(
            summary = "Criar novo post",
            description = "Cria um novo post associado ao usuário atualmente autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post criado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou mal formatados", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(
            @Parameter(description = "Dados do post a ser criado", required = true)
            @Valid @RequestBody PostRequestDTO postRequestDTO) {

        UUID userId = getCurrentLoggedInUserId();
        PostResponseDTO response = postService.createPost(postRequestDTO, userId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Listar posts públicos",
            description = "Retorna uma lista paginada de todos os posts públicos disponíveis no sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de posts retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class, subTypes = { PostResponseDTO.class })))
    })
    @GetMapping
    public ResponseEntity<Page<PostResponseDTO>> getAllPublicPosts(
            @Parameter(description = "Configurações de paginação e ordenação (ex: page=0&size=10&sort=title,asc)")
            Pageable pageable) {

        Page<PostResponseDTO> posts = postService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @Operation(
            summary = "Obter post por ID",
            description = "Retorna os detalhes de um post específico pelo seu ID. " +
                    "Se o post for privado, o acesso é permitido apenas ao autor."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado ao post privado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDTO> getPostById(
            @Parameter(description = "ID do post a ser recuperado", required = true)
            @PathVariable Long id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal User currentUser) {

        PostResponseDTO post = postService.getPostById(id, currentUser);
        return ResponseEntity.ok(post);
    }

    @Operation(
            summary = "Listar posts de um usuário",
            description = "Retorna uma lista paginada de posts públicos criados por um usuário específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts do usuário retornados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class, subTypes = { PostResponseDTO.class }))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponseDTO>> getPostsByUserId(
            @Parameter(description = "ID do usuário autor dos posts", required = true)
            @PathVariable UUID userId,

            @Parameter(description = "Configurações de paginação e ordenação (ex: page=0&size=10&sort=createdAt,desc)")
            Pageable pageable) {

        Page<PostResponseDTO> posts = postService.getPostsByUserId(userId, pageable);
        return ResponseEntity.ok(posts);
    }

    @Operation(
            summary = "Atualizar post",
            description = "Atualiza os dados de um post existente. Somente o autor do post pode realizar a atualização."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado (usuário não é o autor do post)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @Parameter(description = "ID do post a ser atualizado", required = true)
            @PathVariable Long id,

            @Parameter(description = "Novos dados do post", required = true)
            @Valid @RequestBody PostRequestDTO postRequestDTO) {

        UUID userId = getCurrentLoggedInUserId();
        PostResponseDTO updatedPost = postService.updatePost(id, postRequestDTO, userId);
        return ResponseEntity.ok(updatedPost);
    }

    @Operation(
            summary = "Deletar post",
            description = "Realiza a exclusão de um post. Somente o autor do post pode deletá-lo."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post deletado com sucesso (sem conteúdo)"),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado (usuário não é o autor do post)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @Parameter(description = "ID do post a ser deletado", required = true)
            @PathVariable Long id) {

        UUID userId = getCurrentLoggedInUserId();
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Curtir um post",
            description = "Incrementa o número de curtidas de um post específico. A ação é considerada uma " +
                    "atualização parcial de estado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post curtido com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado", content = @Content)
    })
    @PatchMapping("/{id}/like")
    public ResponseEntity<PostResponseDTO> likePost(
            @Parameter(description = "ID do post a ser curtido", required = true)
            @PathVariable Long id) {

        PostResponseDTO updatedPost = postService.likePost(id);
        return ResponseEntity.ok(updatedPost);
    }

    @Operation(
            summary = "Alternar privacidade do post",
            description = "Alterna o estado de privacidade de um post (público ↔ privado). Apenas o autor do post " +
                    "pode realizar essa ação."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Privacidade do post atualizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PostResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado (usuário não é o autor do post)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Post não encontrado",
                    content = @Content)
    })
    @PatchMapping("/{id}/toggle-privacy")
    public ResponseEntity<PostResponseDTO> togglePostPrivacy(
            @Parameter(description = "ID do post cuja privacidade será alternada", required = true)
            @PathVariable Long id) {

        UUID userId = getCurrentLoggedInUserId();
        PostResponseDTO updatedPost = postService.togglePostPrivacy(id, userId);
        return ResponseEntity.ok(updatedPost);
    }

    @Operation(summary = "Get public posts by user ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Public posts fetched successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/public")
    public ResponseEntity<Page<PostResponseDTO>> getPublicPostsByUser(
            @PathVariable UUID userId,
            Pageable pageable) {
        Page<PostResponseDTO> posts = postService.getPublicPostsByUserId(userId, pageable);
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Get all posts by user ID (public and private)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All posts fetched successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<PostResponseDTO>> getAllPostsByUser(@PathVariable UUID userId) {
        List<PostResponseDTO> posts = postService.getAllPostsByUser(userId);
        return ResponseEntity.ok(posts);
    }
}