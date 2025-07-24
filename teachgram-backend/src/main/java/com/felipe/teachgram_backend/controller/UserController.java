package com.felipe.teachgram_backend.controller;

import com.felipe.teachgram_backend.constants.UserRole;
import com.felipe.teachgram_backend.dto.user.UserRequestDTO;
import com.felipe.teachgram_backend.dto.user.UserResponseDTO;
import com.felipe.teachgram_backend.entity.User;
import com.felipe.teachgram_backend.mapper.UserMapper;
import com.felipe.teachgram_backend.security.CustomUserDetails;
import com.felipe.teachgram_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Gerenciamento de usuários da aplicação Teachgram")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    private UUID getLoggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Usuário não autenticado no contexto de segurança.");
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getId();
        }

        throw new IllegalStateException("Principal inválido.");
    }

    @Operation(
            summary = "Obter todos os usuários",
            description = "Retorna uma lista paginada de todos os usuários registrados no sistema. Acesso " +
                    "restrito a administradores."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class, subTypes = {UserResponseDTO.class}))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado (requer papel ADMIN)", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @Parameter(description = "Configurações de paginação e ordenação (ex: page=0&size=10&sort=name,asc)")
            Pageable pageable) {
        Page<UserResponseDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Obter usuário por ID",
            description = "Retorna os detalhes de um usuário específico pelo seu ID. Acesso restrito."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID do usuário a ser buscado") @PathVariable UUID id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Obter perfil do usuário logado",
            description = "Retorna os detalhes do perfil do usuário atualmente autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile() {
        UUID userId = getLoggedInUserId();
        UserResponseDTO user = userService.getUserProfileByLoggedInUserId(userId);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Atualizar perfil do usuário",
            description = "Atualiza os dados do perfil do usuário logado. Um usuário só pode atualizar " +
                    "seu próprio perfil."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil atualizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos ou em uso",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado (tentativa de atualizar outro usuário)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "ID do usuário a ser atualizado") @PathVariable UUID id,
            @Valid @RequestBody UserRequestDTO userRequestDTO) {
        UUID loggedInUserId = getLoggedInUserId();
        if (!id.equals(loggedInUserId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        UserResponseDTO updatedUser = userService.updateUser(id, userRequestDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(
            summary = "Deletar usuário (exclusão lógica)",
            description = "Realiza a exclusão lógica do perfil do usuário logado. Um usuário só pode deletar " +
                    "seu próprio perfil."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário deletado com sucesso (sem conteúdo)"),
            @ApiResponse(responseCode = "403", description = "Acesso negado (tentativa de deletar outro usuário)",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID do usuário a ser deletado") @PathVariable UUID id) {
        UUID loggedInUserId = getLoggedInUserId();
        if (!id.equals(loggedInUserId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Atribuir função ao usuário",
            description = "Este endpoint atribui uma função específica a um usuário existente pelo ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Função atribuída com sucesso",
                            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
                    @ApiResponse(responseCode = "403", description = "Acesso negado - sem permissão"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> assignRole(
            @Parameter(description = "ID do usuário", required = true) @PathVariable UUID id,
            @Parameter(description = "Função a ser atribuída", required = true) @RequestParam UserRole role) {
        return ResponseEntity.ok(userService.assignRoleToUser(id, role));
    }

    @Operation(
            summary = "Remover função do usuário",
            description = "Este endpoint remove uma função específica de um usuário existente pelo ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Função removida com sucesso",
                            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
                    @ApiResponse(responseCode = "403", description = "Acesso negado - sem permissão"),
                    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
            }
    )
    @DeleteMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> removeRole(
            @Parameter(description = "ID do usuário", required = true) @PathVariable UUID id,
            @Parameter(description = "Função a ser removida", required = true) @RequestParam UserRole role) {
        return ResponseEntity.ok(userService.removeRoleFromUser(id, role));
    }

    @Operation(
            summary = "Processar login bem-sucedido",
            description = "Reseta o contador de tentativas de login falhadas para o usuário fornecido. Geralmente " +
                    "chamado após a autenticação bem-sucedida."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tentativas de login resetadas com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Não autorizado")
    })
    @PostMapping("/login-success/{username}")
    @ResponseStatus(HttpStatus.OK)
    public void processSuccessfulLogin(@PathVariable String username) {
        userService.processSuccessfulLogin(username);
    }

    @Operation(
            summary = "Buscar usuário por e-mail",
            description = "Retorna os dados de um usuário cadastrado com o e-mail informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/by-email")
    public ResponseEntity<UserResponseDTO> getByEmail(
            @Parameter(description = "E-mail do usuário a ser buscado", required = true)
            @RequestParam String email) {

        return userService.findByEmail(email)
                .map(user -> ResponseEntity.ok(userMapper.toDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Buscar usuário por telefone",
            description = "Retorna os dados de um usuário cadastrado com o número de telefone informado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/by-phone")
    public ResponseEntity<UserResponseDTO> getByPhone(
            @Parameter(description = "Número de telefone do usuário a ser buscado", required = true)
            @RequestParam String phone) {

        return userService.findByPhone(phone)
                .map(user -> ResponseEntity.ok(userMapper.toDto(user)))
                .orElse(ResponseEntity.notFound().build());
    }
}