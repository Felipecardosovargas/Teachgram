package com.felipe.teachgram_backend.service;

import com.felipe.teachgram_backend.constants.UserRole;
import com.felipe.teachgram_backend.dto.user.UserRequestDTO;
import com.felipe.teachgram_backend.dto.user.UserResponseDTO;
import com.felipe.teachgram_backend.entity.Role;
import com.felipe.teachgram_backend.entity.User;
import com.felipe.teachgram_backend.exception.ResourceNotFoundException;
import com.felipe.teachgram_backend.exception.ValidationException;
import com.felipe.teachgram_backend.mapper.UserMapper;
import com.felipe.teachgram_backend.repository.RoleRepository;
import com.felipe.teachgram_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    RoleRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private UserRequestDTO buildUserRequestDTO() {
        return UserRequestDTO.builder()
                .name("John Doe")
                .username("johndoe")
                .email("john@example.com")
                .phone("1234567890")
                .description("desc")
                .profileLink("http://profile")
                .password("secret")
                .build();
    }

    private User buildUser() {
        User user = new User();
        user.setId(userId);
        user.setName("John Doe");
        user.setUsername("johndoe");
        user.setEmail("john@example.com");
        user.setPhone("1234567890");
        user.setDescription("desc");
        user.setProfileLink("http://profile");
        user.setPassword("hashed");
        user.setRoles(new HashSet<>());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private Role buildRole(UserRole role) {
        Role r = new Role();
        r.setName(role.getRoleName());
        return r;
    }

    @Test
    @DisplayName("Should create user successfully with hashed password and default USER role")
    void createUser_success() {
        // Arrange
        UserRequestDTO dto = new UserRequestDTO(
                "felipeuser",
                "123456",
                "felipe@example.com",
                "Felipe Vargas",
                "+5551999999999",
                null,
                null
        );

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(dto.getPhone())).thenReturn(false);
        when(roleRepository.findByName(UserRole.USER.getRoleName()))
                .thenReturn(Optional.of(new Role(1L, UserRole.USER.getRoleName())));
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.createUser(dto);

        // Assert
        verify(userRepository).save(argThat(user ->
                user.getUsername().equals(dto.getUsername())
                        && user.getEmail().equals(dto.getEmail())
                        && user.getPhone().equals(dto.getPhone())
                        && user.getName().equals(dto.getName())
                        && user.getPassword().equals("hashed-password")
                        && user.getRoles().stream().anyMatch(role -> role.getName().equals(UserRole.USER.getRoleName()))
        ));
        verify(roleRepository).findByName(UserRole.USER.getRoleName());
        verify(passwordEncoder).encode(dto.getPassword());
    }

    @Test
    @DisplayName("Should throw ValidationException when username already exists")
    void createUser_whenUsernameAlreadyExists_thenThrowsValidationException() {
        // Arrange
        UserRequestDTO dto = new UserRequestDTO(
                "existingUser",
                "password123",
                "email@example.com",
                "Existing User",
                "+5551999999999",
                null,
                null
        );
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Username");

        verify(userRepository).existsByUsername(dto.getUsername());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when email already exists")
    void createUser_whenEmailAlreadyExists_thenThrowsValidationException() {
        // Arrange
        UserRequestDTO dto = new UserRequestDTO(
                "newuser",
                "password123",
                "existing@email.com",
                "Existing Email User",
                "+5551999999999",
                null,
                null
        );

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email");

        verify(userRepository).existsByUsername(dto.getUsername());
        verify(userRepository).existsByEmail(dto.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when phone already exists")
    void createUser_whenPhoneAlreadyExists_thenThrowsValidationException() {
        // Arrange
        UserRequestDTO dto = new UserRequestDTO(
                "felipevargas",
                "123456",
                "felipe@example.com",
                "Felipe Vargas",
                "+5551999999999",
                null,
                null
        );

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(dto.getPhone())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Phone");

        verify(userRepository).existsByUsername(dto.getUsername());
        verify(userRepository).existsByEmail(dto.getEmail());
        verify(userRepository).existsByPhone(dto.getPhone());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when USER role is not found")
    void createUser_whenUserRoleNotFound_thenThrowsResourceNotFoundException() {
        // Arrange
        UserRequestDTO dto = new UserRequestDTO(
                "felipeuser",
                "123456",
                "felipe@example.com",
                "Felipe Vargas",
                "+5551999999999",
                null,
                null
        );

        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(dto.getPhone())).thenReturn(false);
        when(roleRepository.findByName(UserRole.USER.getRoleName())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(UserRole.USER.getRoleName());

        verify(userRepository).existsByUsername(dto.getUsername());
        verify(userRepository).existsByEmail(dto.getEmail());
        verify(userRepository).existsByPhone(dto.getPhone());
        verify(roleRepository).findByName(UserRole.USER.getRoleName());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should increment failed login attempts and save user on failed login")
    void processFailedLogin_whenValidUser_thenIncrementsFailedAttemptsAndSaves() {
        // Arrange
        User user = new User();
        user.setUsername("felipeuser");
        user.setFailedLoginAttempts(0);

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.processFailedLogin(user.getUsername());

        // Assert
        assertThat(user.getFailedLoginAttempts())
                .as("Failed login attempts should be incremented by 1")
                .isEqualTo(1);

        verify(userRepository).findByUsername(user.getUsername());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should do nothing and not throw when username not found during failed login process")
    void processFailedLogin_userNotFound_noException() {
        // Arrange
        String username = "notfound";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert (no exception expected)
        assertThatCode(() -> userService.processFailedLogin(username))
                .doesNotThrowAnyException();

        // Verify no save was called
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reset failed login attempts to zero and save user on successful login")
    void processSuccessfulLogin_resetsAndSaves() {
        // Arrange
        User user = buildUser();
        user.incrementFailedLoginAttempts(); // Simulate failed attempts
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        userService.processSuccessfulLogin(user.getUsername());

        // Assert
        verify(userRepository).save(user);
        assertThat(user.getFailedLoginAttempts()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should return a paged list of UserResponseDTO matching users from repository")
    void getAllUsers_returnsPagedDTO() {
        // Arrange
        User user = buildUser();
        Page<User> userPage = new PageImpl<>(List.of(user));
        Pageable pageable = PageRequest.of(0, 10);

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toDto(user)).thenReturn(
                UserResponseDTO.builder()
                        .id(user.getId())
                        .id(user.getId())
                        .userName(user.getUsername())
                        .name(user.getName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .profileLink(user.getProfileLink())
                        .build()
        );

        // Act
        Page<UserResponseDTO> result = userService.getAllUsers(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);

        UserResponseDTO dto = result.getContent().get(0);
        assertThat(dto.getUserName()).isEqualTo(user.getUsername());
        assertThat(dto.getName()).isEqualTo(user.getName());
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
        assertThat(dto.getPhone()).isEqualTo(user.getPhone());
        assertThat(dto.getProfileLink()).isEqualTo(user.getProfileLink());

        verify(userRepository).findAll(pageable);
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("Should return UserResponseDTO when user is found by ID")
    void getUserById_found() {
        // Arrange
        User user = buildUser();
        UUID userId = user.getId();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(UserResponseDTO.builder()
                .id(user.getId())
                .userName(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profileLink(user.getProfileLink())
                .build()
        );

        // Act
        UserResponseDTO dto = userService.getUserById(userId);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getUserName()).isEqualTo(user.getUsername());
        verify(userRepository).findById(userId);
        verify(userMapper).toDto(user);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user by ID is not found")
    void getUserById_notFound_throws() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should return user profile DTO when logged-in user exists")
    void getUserProfileByLoggedInUserId_found() {
        User user = buildUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponseDTO dto = userService.getUserProfileByLoggedInUserId(userId);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(userId);
        assertThat(dto.getUserName()).isEqualTo(user.getUsername());
        assertThat(dto.getName()).isEqualTo(user.getName());

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user profile not found")
    void getUserProfileByLoggedInUserId_notFound_throws() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserProfileByLoggedInUserId(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("Should update user successfully including password hashing")
    void updateUser_success_updatePassword() {
        // Arrange
        User existing = buildUser();
        UserRequestDTO dto = buildUserRequestDTO();
        dto.setPassword("newpass");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(dto.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashedNew");
        when(userRepository.save(existing)).thenReturn(existing);

        // Act
        UserResponseDTO updated = userService.updateUser(userId, dto);

        // Assert
        assertThat(updated.getUserName()).isEqualTo(dto.getUsername());
        assertThat(existing.getPassword()).isEqualTo("hashedNew");
        verify(passwordEncoder).encode("newpass");
        verify(userRepository).save(existing);
    }

    @Test
    @DisplayName("Should update user without changing password when password is null")
    void updateUser_success_noPasswordChange() {
        // Arrange
        User existing = buildUser();
        UserRequestDTO dto = buildUserRequestDTO();
        dto.setPassword(null); // senha não será alterada

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(dto.getPhone())).thenReturn(false);
        when(userRepository.save(existing)).thenReturn(existing);

        // Act
        UserResponseDTO updated = userService.updateUser(userId, dto);

        // Assert
        verify(userRepository).save(existing);
        verify(passwordEncoder, never()).encode(any());
        assertThat(existing.getPassword()).isEqualTo("hashed"); // mantém senha antiga

        // Verifica que outros campos foram atualizados (exemplo)
        assertThat(existing.getUsername()).isEqualTo(dto.getUsername());
        assertThat(existing.getEmail()).isEqualTo(dto.getEmail());
        assertThat(existing.getPhone()).isEqualTo(dto.getPhone());
        assertThat(existing.getName()).isEqualTo(dto.getName());
    }

    @Test
    @DisplayName("Should throw ValidationException when updating user with existing username")
    void updateUser_usernameExists_throws() {
        // Arrange
        User existing = buildUser();
        UserRequestDTO dto = buildUserRequestDTO();

        existing.setUsername("existingUser");
        dto.setUsername("newUser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(userId, dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Username");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when updating user with existing email")
    void updateUser_emailExists_throws() {
        // Arrange
        User existing = buildUser();
        UserRequestDTO dto = buildUserRequestDTO();
        dto.setEmail("newemail@example.com"); // email diferente do existente

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(userId, dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when updating user with an existing phone number")
    void updateUser_phoneExists_throws() {
        // Arrange
        User existing = buildUser();
        existing.setPhone("existing-phone-number");

        UserRequestDTO dto = buildUserRequestDTO();
        dto.setPhone("different-phone-number");

        when(userRepository.findById(userId)).thenReturn(Optional.of(existing));
        when(userRepository.existsByUsername(dto.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(dto.getPhone())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(userId, dto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Phone '" + dto.getPhone() + "' already in use.");

        // Verifica que não houve tentativa de salvar
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete user successfully when user exists")
    void deleteUser_success() {
        // Arrange
        User user = buildUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting a user that does not exist")
    void deleteUser_notFound_throws() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should assign ADMIN role to user successfully and return updated UserResponseDTO")
    void assignRoleToUser_success() {
        // Arrange
        User user = buildUser();
        Role role = buildRole(UserRole.ADMIN);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(UserRole.ADMIN.getRoleName())).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenReturn(user);

        // Act
        UserResponseDTO dto = userService.assignRoleToUser(userId, UserRole.ADMIN);

        // Assert
        assertThat(dto.getRoles()).contains(UserRole.ADMIN.getRoleName());
        assertThat(user.getRoles()).extracting(Role::getName).contains(UserRole.ADMIN.getRoleName());

        verify(userRepository).findById(userId);
        verify(roleRepository).findByName(UserRole.ADMIN.getRoleName());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("assignRoleToUser should throw ResourceNotFoundException when user is not found")
    void assignRoleToUser_userNotFound_throws() {
        // Arrange
        UUID nonExistentUserId = UUID.randomUUID();
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                userService.assignRoleToUser(nonExistentUserId, UserRole.ADMIN)
        );

        // Verifica mensagem da exceção para garantir feedback correto
        assertThat(exception.getMessage())
                .isNotNull()
                .contains("User not found")
                .contains(nonExistentUserId.toString());

        // Verifica que não houve tentativa de salvar usuário nem buscar role
        verify(userRepository, times(1)).findById(nonExistentUserId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(roleRepository);
    }

    @Test
    @DisplayName("assignRoleToUser should throw ResourceNotFoundException when role is not found")
    void assignRoleToUser_roleNotFound_throws() {
        // Arrange
        User user = buildUser();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(UserRole.ADMIN.getRoleName())).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                userService.assignRoleToUser(userId, UserRole.ADMIN)
        );

        // Verifica mensagem da exceção para garantir feedback claro e informativo
        assertThat(exception.getMessage())
                .isNotNull()
                .contains("ROLE_ADMIN");

        // Verifica interações para garantir fluxo correto
        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findByName(UserRole.ADMIN.getRoleName());
        verify(userRepository, never()).save(any());

        verifyNoMoreInteractions(userRepository, roleRepository);
    }

    @Test
    @DisplayName("removeRoleFromUser should successfully remove role and persist changes")
    void removeRoleFromUser_success() {
        // Arrange
        User user = buildUser();
        Role role = buildRole(UserRole.USER);
        user.getRoles().add(role); // Usuário já tem a role a ser removida

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(UserRole.USER.getRoleName())).thenReturn(Optional.of(role));
        when(userRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponseDTO dto = userService.removeRoleFromUser(userId, UserRole.USER);

        // Assert
        assertThat(dto.getRoles())
                .doesNotContain(UserRole.USER.getRoleName())
                .withFailMessage("Role %s should be removed from user roles", UserRole.USER.getRoleName());

        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findByName(UserRole.USER.getRoleName());
        verify(userRepository, times(1)).save(user);
        verifyNoMoreInteractions(userRepository, roleRepository);
    }

    @Test
    @DisplayName("removeRoleFromUser should throw ResourceNotFoundException when user not found")
    void removeRoleFromUser_userNotFound_throws() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.removeRoleFromUser(userId, UserRole.USER))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found")
                .hasNoCause();

        // Verify that no other repository methods were called
        verify(userRepository, times(1)).findById(userId);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(roleRepository);
    }

    @Test
    @DisplayName("removeRoleFromUser should throw ResourceNotFoundException when role not found")
    void removeRoleFromUser_roleNotFound_throws() {
        // Arrange
        User user = buildUser();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(UserRole.USER.getRoleName())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.removeRoleFromUser(userId, UserRole.USER))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Role")
                .hasNoCause();

        // Verify interactions
        verify(userRepository, times(1)).findById(userId);
        verify(roleRepository, times(1)).findByName(UserRole.USER.getRoleName());
        verifyNoMoreInteractions(userRepository, roleRepository);
    }

    @Test
    @DisplayName("findByEmail should return user when email exists")
    void findByEmail_found() {
        // Arrange
        User user = buildUser();
        String email = user.getEmail();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertThat(result)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getEmail()).isEqualTo(email);
                    assertThat(u.getId()).isEqualTo(user.getId());
                    assertThat(u.getUsername()).isEqualTo(user.getUsername());
                });

        // Verify repository interaction
        verify(userRepository, times(1)).findByEmail(email);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("findByEmail should return empty when email does not exist")
    void findByEmail_notFound() {
        // Arrange
        String email = "no@mail.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByEmail(email);

        // Assert
        assertThat(result).isEmpty();

        // Verify repository interaction
        verify(userRepository, times(1)).findByEmail(email);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("findByPhone should return user when phone exists")
    void findByPhone_found() {
        // Arrange
        User user = buildUser();
        String phone = user.getPhone();
        when(userRepository.findByPhone(phone)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = userService.findByPhone(phone);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getPhone()).isEqualTo(phone);

        // Verify interactions
        verify(userRepository, times(1)).findByPhone(phone);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("findByPhone should return empty when phone does not exist")
    void findByPhone_notFound() {
        // Arrange
        String phone = "12345";
        when(userRepository.findByPhone(phone)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.findByPhone(phone);

        // Assert
        assertThat(result).isEmpty();

        // Verify interactions
        verify(userRepository, times(1)).findByPhone(phone);
        verifyNoMoreInteractions(userRepository);
    }
}
