package com.felipe.teachgram_backend.service;

import com.felipe.teachgram_backend.dto.user.UserRequestDTO;
import com.felipe.teachgram_backend.dto.user.UserResponseDTO;
import com.felipe.teachgram_backend.entity.Role;
import com.felipe.teachgram_backend.entity.User;
import com.felipe.teachgram_backend.exception.ResourceNotFoundException;
import com.felipe.teachgram_backend.exception.ValidationException;
import com.felipe.teachgram_backend.mapper.UserMapper;
import com.felipe.teachgram_backend.repository.RoleRepository;
import com.felipe.teachgram_backend.repository.UserRepository;
import com.felipe.teachgram_backend.constants.UserRole;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for managing {@link User} entities, including CRUD operations,
 * role assignment, and profile management.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    public final UserRepository userRepository;

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * Creates a new user with the default {@code ROLE_USER} role.
     *
     * @param userRequestDTO DTO containing the new user's data.
     * @throws ValidationException       if username, email, or phone already exist.
     * @throws ResourceNotFoundException if {@code ROLE_USER} is not found in the database.
     */
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        if (userRepository.existsByUsername(userRequestDTO.getUsername())) {
            throw new ValidationException("Username '" + userRequestDTO.getUsername() + "' already in use.");
        }
        if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new ValidationException("Email '" + userRequestDTO.getEmail() + "' already in use.");
        }
        if (userRequestDTO.getPhone() != null && userRepository.existsByPhone(userRequestDTO.getPhone())) {
            throw new ValidationException("Phone '" + userRequestDTO.getPhone() + "' already in use.");
        }

        User user = new User();
        user.setName(userRequestDTO.getName());
        user.setUsername(userRequestDTO.getUsername());
        user.setEmail(userRequestDTO.getEmail());
        user.setPhone(userRequestDTO.getPhone());
        user.setDescription(userRequestDTO.getDescription());
        user.setProfileLink(userRequestDTO.getProfileLink());
        user.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));

        Role userRole = roleRepository.findByName(UserRole.USER.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Error: 'ROLE_USER' not found. Please ensure " +
                        " roles are populated."));

        user.setRoles(new HashSet<>(List.of(userRole)));

        User savedUser = userRepository.save(user);

        return mapToUserResponseDTO(savedUser);
    }

    public void processFailedLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.incrementFailedLoginAttempts();
            userRepository.save(user);
        });
    }

    public void processSuccessfulLogin(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.resetFailedLoginAttempts();
            userRepository.save(user);
        });
    }

    /**
     * Retrieves all users with pagination.
     *
     * @param pageable Pagination and sorting information.
     * @return A {@link Page} of {@link UserResponseDTO}.
     */
    public Page<UserResponseDTO> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::toDto);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user.
     * @return {@link UserResponseDTO} of the found user.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public UserResponseDTO getUserById(UUID id) {
        User user = findUserEntityById(id); // Use helper to find entity
        return userMapper.toDto(user);
    }

    /**
     * Retrieves the profile of the currently logged-in user by their ID.
     *
     * @param loggedInUserId The ID of the authenticated user.
     * @return {@link UserResponseDTO} of the logged-in user's profile.
     * @throws ResourceNotFoundException if the user's profile is not found.
     */
    public UserResponseDTO getUserProfileByLoggedInUserId(UUID loggedInUserId) {
        // Renamed from 'userRepository.findById' to 'findUserEntityById' for consistency.
        User user = findUserEntityById(loggedInUserId);
        return mapToUserResponseDTO(user);
    }

    /**
     * Updates an existing user's data.
     *
     * @param id The ID of the user to update.
     * @param userRequestDTO DTO containing updated user data.
     * @return {@link UserResponseDTO} of the updated user.
     * @throws ResourceNotFoundException if the user is not found.
     * @throws ValidationException if username, email, or phone are already in use by another user.
     */
    @Transactional
    public UserResponseDTO updateUser(UUID id, UserRequestDTO userRequestDTO) {
        User existingUser = findUserEntityById(id); // Use helper to find entity

        // Validate uniqueness, ignoring the current user's existing values
        if (!existingUser.getUsername().equalsIgnoreCase(userRequestDTO.getUsername())
                && userRepository.existsByUsername(userRequestDTO.getUsername())) {
            throw new ValidationException("Username '" + userRequestDTO.getUsername() + "' already in use.");
        }
        if (!existingUser.getEmail().equalsIgnoreCase(userRequestDTO.getEmail())
                && userRepository.existsByEmail(userRequestDTO.getEmail())) {
            throw new ValidationException("Email '" + userRequestDTO.getEmail() + "' already in use.");
        }
        // Only validate phone if provided and different from existing
        if (userRequestDTO.getPhone() != null &&
                (existingUser.getPhone() == null || !existingUser.getPhone().equals(userRequestDTO.getPhone())) &&
                userRepository.existsByPhone(userRequestDTO.getPhone())) {
            throw new ValidationException("Phone '" + userRequestDTO.getPhone() + "' already in use.");
        }

        // Update fields from DTO
        existingUser.setName(userRequestDTO.getName());
        existingUser.setUsername(userRequestDTO.getUsername());
        existingUser.setEmail(userRequestDTO.getEmail());
        existingUser.setPhone(userRequestDTO.getPhone());
        existingUser.setDescription(userRequestDTO.getDescription());
        existingUser.setProfileLink(userRequestDTO.getProfileLink());

        // Update password only if a new one is provided and not empty
        if (userRequestDTO.getPassword() != null && !userRequestDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        }

        User updatedUser = userRepository.save(existingUser);
        return mapToUserResponseDTO(updatedUser);
    }

    /**
     * Performs a soft delete on a user by setting a 'deleted' flag (via {@code @SQLDelete}).
     *
     * @param id The ID of the user to delete.
     * @throws ResourceNotFoundException if the user is not found.
     */
    @Transactional
    public void deleteUser(UUID id) {
        User userToDelete = findUserEntityById(id); // Use helper to find entity
        userRepository.delete(userToDelete);
    }

    /**
     * Assigns a specific role to a user.
     *
     * @param userId The ID of the user to assign the role to.
     * @param roleEnum The {@link UserRole} to assign.
     * @return {@link UserResponseDTO} of the updated user.
     * @throws ResourceNotFoundException if the user or role is not found.
     */
    @Transactional
    public UserResponseDTO assignRoleToUser(UUID userId, UserRole roleEnum) {
        User user = findUserEntityById(userId); // Use helper to find entity

        Role role = roleRepository.findByName(roleEnum.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Role '" + roleEnum.getRoleName() + "' not found."));

        user.getRoles().add(role);
        User updatedUser = userRepository.save(user);

        return mapToUserResponseDTO(updatedUser);
    }

    /**
     * Removes a specific role from a user.
     *
     * @param userId The ID of the user.
     * @param roleEnum The {@link UserRole} to remove.
     * @return {@link UserResponseDTO} of the updated user.
     * @throws ResourceNotFoundException if the user or role is not found.
     */
    @Transactional
    public UserResponseDTO removeRoleFromUser(UUID userId, UserRole roleEnum) {
        User user = findUserEntityById(userId); // Use helper to find entity

        Role roleToRemove = roleRepository.findByName(roleEnum.getRoleName())
                .orElseThrow(() -> new ResourceNotFoundException("Role '" + roleEnum.getRoleName() + "' not found."));

        user.getRoles().remove(roleToRemove);
        User updatedUser = userRepository.save(user);

        return mapToUserResponseDTO(updatedUser);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    /**
     * Maps a {@link User} entity to a {@link UserResponseDTO}.
     *
     * @param user The {@link User} entity to map.
     * @return The corresponding {@link UserResponseDTO}.
     */
    private UserResponseDTO mapToUserResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getProfileLink(),
                user.getDescription(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );
    }

    /**
     * Finds a {@link User} entity by its ID. This is a common internal helper.
     *
     * @param id The ID of the user.
     * @return The {@link User} entity found.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public User findUserEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }
}