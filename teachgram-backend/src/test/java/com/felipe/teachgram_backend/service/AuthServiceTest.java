package com.felipe.teachgram_backend.service;

import com.felipe.teachgram_backend.constants.AuthErrorMessages;
import com.felipe.teachgram_backend.dto.auth.LoginRequestDTO;
import com.felipe.teachgram_backend.dto.auth.LoginResponseDTO;
import com.felipe.teachgram_backend.dto.user.UserRequestDTO;
import com.felipe.teachgram_backend.entity.Role;
import com.felipe.teachgram_backend.entity.User;
import com.felipe.teachgram_backend.exception.ValidationException;
import com.felipe.teachgram_backend.repository.UserRepository;
import com.felipe.teachgram_backend.security.AuthHandler;
import com.felipe.teachgram_backend.security.JwtService;
import com.felipe.teachgram_backend.security.oauth2.user.OAuth2UserInfoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthHandler authHandler;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private OAuth2UserInfoFactory oAuth2UserInfoFactory;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        injectField(authService, "jwtExpirationMinutes", 60L);
    }

    private void injectField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void signup_callsCreateUserAndReturnsLoginResponse() {
        // Arrange
        UserRequestDTO userRequestDTO = new UserRequestDTO(
                "username",
                "password",
                "email",
                "name",
                null,
                null,
                null
        );
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(userRequestDTO.getUsername(), userRequestDTO.getPassword());
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO("token", UUID.randomUUID(), "username", 3600L);

        // Cria spy do authService para poder mockar métodos internos
        AuthService spyAuthService = Mockito.spy(authService);

        // Mocka método createUser do userService (que é mock mesmo)
        doNothing().when(userService).createUser(userRequestDTO);

        // Mocka o método login dentro do spy
        doReturn(loginResponseDTO).when(spyAuthService).login(loginRequestDTO);

        // Act
        LoginResponseDTO response = spyAuthService.signup(userRequestDTO);

        // Assert
        assertThat(response).isEqualTo(loginResponseDTO);
        verify(userService).createUser(userRequestDTO);
        verify(spyAuthService).login(loginRequestDTO);
    }

    @Test
    void login_success_returnsLoginResponse() {
        // Arrange
        String username = "user";
        String password = "pass";
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("User Name");
        user.setUsername(username);
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(username, password);

        UserDetails userDetails = mock(UserDetails.class);
        when(authHandler.authenticate(username, password)).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(userDetails)).thenReturn("jwtToken");

        // Act
        LoginResponseDTO response = authService.login(loginRequestDTO);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwtToken");
        assertThat(response.userId()).isEqualTo(user.getId());
        assertThat(response.userName()).isEqualTo(user.getName());
        assertThat(response.expiresIn()).isEqualTo(60L * 60); // jwtExpirationMinutes * 60
        verify(authHandler).authenticate(username, password);
        verify(userRepository).findByUsername(username);
        verify(jwtService).generateToken(userDetails);
    }

    @Test
    void login_userNotFound_throwsValidationException() {
        // Arrange
        String username = "user";
        String password = "pass";
        LoginRequestDTO loginRequestDTO = new LoginRequestDTO(username, password);
        UserDetails userDetails = mock(UserDetails.class);

        when(authHandler.authenticate(username, password)).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginRequestDTO))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining(AuthErrorMessages.USER_NOT_FOUND.getMessage());

        verify(authHandler).authenticate(username, password);
        verify(userRepository).findByUsername(username);
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void signupWithOAuth2_existingUser_returnsLoginResponse() {
        // Arrange
        String idToken = "fake-token";
        Map<String, Object> attributes = Map.of(
                "email", "email@example.com",
                "name", "OAuth User",
                "picture", "profileLink"
        );

        // Mock verifyGoogleIdToken to return attributes
        AuthService spyService = Mockito.spy(authService);
        doReturn(attributes).when(spyService).verifyGoogleIdToken(idToken);

        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setEmail("email@example.com");
        existingUser.setName("OAuth User");
        existingUser.setUsername("email");
        existingUser.setPassword("encodedPassword");
        existingUser.setProfileLink("profileLink");
        existingUser.setRoles(Set.of(new Role("ROLE_USER")));

        when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(any())).thenReturn("jwtToken");

        // Act
        LoginResponseDTO response = spyService.signupWithOAuth2(idToken);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwtToken");
        assertThat(response.userId()).isEqualTo(existingUser.getId());
        assertThat(response.userName()).isEqualTo(existingUser.getUsername());
        assertThat(response.expiresIn()).isEqualTo(60L * 60);
        verify(userRepository).findByEmail("email@example.com");
        verify(jwtService).generateToken(any());
    }

    @Test
    void signupWithOAuth2_newUser_createsUserAndReturnsLoginResponse() {
        // Arrange
        String idToken = "fake-token";
        Map<String, Object> attributes = Map.of(
                "email", "newuser@example.com",
                "name", "New User",
                "picture", "newProfileLink"
        );

        AuthService spyService = Mockito.spy(authService);
        doReturn(attributes).when(spyService).verifyGoogleIdToken(idToken);

        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedRandomPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtService.generateToken(any())).thenReturn("jwtToken");

        // Act
        LoginResponseDTO response = spyService.signupWithOAuth2(idToken);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwtToken");
        assertThat(response.userId()).isNotNull();
        assertThat(response.userName()).isEqualTo("newuser");
        assertThat(response.expiresIn()).isEqualTo(60L * 60);

        verify(userRepository).findByEmail("newuser@example.com");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any());
        verify(passwordEncoder).encode(anyString());
    }

    protected RestTemplate createRestTemplate() {
        return new RestTemplate();
    }

    @Test
    void verifyGoogleIdToken_validToken_returnsAttributes() {
        // Arrange
        String token = "valid-token";
        Map<String, Object> responseMap = Map.of("email", "user@example.com");

        RestTemplate restTemplate = mock(RestTemplate.class);
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseMap, HttpStatus.OK);

        AuthService spyService = Mockito.spy(authService);
        doReturn(restTemplate).when(spyService).createRestTemplate(); // precisa existir esse método

        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        // Act
        Map<String, Object> result = spyService.verifyGoogleIdToken(token);

        // Assert
        assertThat(result).isEqualTo(responseMap);
    }

    @Test
    void verifyGoogleIdToken_invalidToken_throws() {
        // Arrange
        String token = "invalid-token";
        RestTemplate restTemplate = mock(RestTemplate.class);
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

        AuthService spyService = Mockito.spy(authService);
        doReturn(restTemplate).when(spyService).createRestTemplate();

        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(responseEntity);

        // Act & Assert
        assertThatThrownBy(() -> spyService.verifyGoogleIdToken(token))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token inválido");
    }
}
