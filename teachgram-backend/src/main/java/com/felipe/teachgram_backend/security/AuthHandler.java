package com.felipe.teachgram_backend.security;

import com.felipe.teachgram_backend.constants.AuthErrorMessages;
import com.felipe.teachgram_backend.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final AuthenticationManager authenticationManager;

    /**
     * Performs user authentication using Spring Security's AuthenticationManager.
     *
     * @param username user's login
     * @param password user's password
     * @return authenticated UserDetails
     * @throws ValidationException if authentication fails
     */
    public UserDetails authenticate(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            return (UserDetails) authentication.getPrincipal();
        } catch (AuthenticationException e) {
            throw new ValidationException(AuthErrorMessages.INVALID_CREDENTIALS.getMessage());
        }
    }
}
