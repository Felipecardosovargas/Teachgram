package com.felipe.teachgram_backend.service;

import com.felipe.teachgram_backend.dto.auth.LoginRequestDTO;
import com.felipe.teachgram_backend.dto.auth.LoginResponseDTO;
import com.felipe.teachgram_backend.dto.user.UserRequestDTO;
import com.felipe.teachgram_backend.entity.User;
import com.felipe.teachgram_backend.exception.ValidationException;
import com.felipe.teachgram_backend.repository.UserRepository;
import com.felipe.teachgram_backend.security.AuthHandler;
import com.felipe.teachgram_backend.security.JwtService;
import com.felipe.teachgram_backend.security.oauth2.user.OAuth2UserInfo;
import com.felipe.teachgram_backend.security.oauth2.user.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AuthService handles user registration and authentication,
 * including automatic login with JWT generation.
 * This service adheres to the Single Responsibility Principle (SRP)
 * by delegating user management to UserService and authentication to AuthHandler.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthHandler authHandler;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.security.jwt.expiration-minutes}")
    private long jwtExpirationMinutes;

    /**
     * Registers a new user and logs them in by generating a JWT.
     *
     * @param userRequestDTO request payload with user details
     * @return LoginResponseDTO with token, user ID and name
     */
    public LoginResponseDTO signup(UserRequestDTO userRequestDTO) {
        userService.createUser(userRequestDTO);
        return login(new LoginRequestDTO(userRequestDTO.getEmail(), userRequestDTO.getPassword()));
    }

    protected RestTemplate createRestTemplate() {
        return new RestTemplate();
    }

    public LoginResponseDTO signupWithOAuth2(String idToken) {
        Map<String, Object> attributes = verifyGoogleIdToken(idToken);
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo("google", attributes);

        Optional<User> existingUser = userRepository.findByEmail(userInfo.getEmail());
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            user = new User();
            user.setEmail(userInfo.getEmail());
            user.setName(userInfo.getName());
            user.setUsername(userInfo.getEmail().split("@")[0]);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setProfileLink(userInfo.getImageUrl());
            userRepository.save(user);
        }

        List<SimpleGrantedAuthority> authorities = user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );

        String token = jwtService.generateToken(userDetails);
        long expiresInMillis = jwtExpirationMinutes * 60;
        return new LoginResponseDTO(token, user.getId(), user.getUsername(), expiresInMillis);
    }

    public Map<String, Object> verifyGoogleIdToken(String idToken) {
        RestTemplate restTemplate = createRestTemplate();
        String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Token inválido");
        }

        return response.getBody();
    }

    /**
     * Authenticates a user using credentials and returns a JWT with basic user info.
     *
     * @param loginRequestDTO credentials (username + password)
     * @return LoginResponseDTO with JWT and user data
     * @throws ValidationException if authentication fails
     */
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        var userDetails = authHandler.authenticate(
                loginRequestDTO.email(),
                loginRequestDTO.password()
        );

        User user = (User) userDetails;

        String jwtToken = jwtService.generateToken(userDetails);
        return buildLoginResponse(user, jwtToken);
    }


    /**
     * Helper method to build LoginResponseDTO with user data and JWT.
     */
    private LoginResponseDTO buildLoginResponse(User user, String token) {
        long expiresInMillis = jwtExpirationMinutes * 60;
        return new LoginResponseDTO(token, user.getId(), user.getName(), expiresInMillis);
    }
}
