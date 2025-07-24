package com.felipe.teachgram_backend.controller;

import com.felipe.teachgram_backend.dto.auth.LoginRequestDTO;
import com.felipe.teachgram_backend.dto.auth.LoginResponseDTO;
import com.felipe.teachgram_backend.dto.user.UserRequestDTO;
import com.felipe.teachgram_backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Gerenciamento de autenticação (login) e registro de usuários")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Registrar novo usuário",
            description = "Cria uma nova conta de usuário e retorna um token JWT para login automático."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou usuário já existente",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno no servidor",
                    content = @Content)
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserRequestDTO userRequestDTO) {
        try {
            LoginResponseDTO response = authService.signup(userRequestDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erro interno: " + e.getMessage()));
        }
    }

    @Operation(
            summary = "Login tradicional com username e senha",
            description = "Autentica um usuário com credenciais e retorna um token JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content)
    })
    @PostMapping("/signin")
    public ResponseEntity<LoginResponseDTO> signin(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        return ResponseEntity.ok(authService.login(loginRequestDTO));
    }

    @Operation(
            summary = "Login/Cadastro via OAuth2 com Google",
            description = "Autentica ou registra automaticamente um usuário a partir de um idToken do Google OAuth2."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login ou registro via Google bem-sucedido",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Token inválido ou ausente",
                    content = @Content)
    })
    @PostMapping("/oauth2/signup")
    public ResponseEntity<LoginResponseDTO> signupWithOAuth2(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(authService.signupWithOAuth2(idToken));
    }
}
