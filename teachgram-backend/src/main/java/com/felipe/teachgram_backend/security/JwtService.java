package com.felipe.teachgram_backend.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public JwtService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        long expiresInSeconds = 900;

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("teachgram-api")
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresInSeconds))
                .claim("email", userDetails.getUsername())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Jwt decoded = jwtDecoder.decode(token);
            return decoded.getSubject().equals(userDetails.getUsername());
        } catch (JwtException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        Jwt decoded = jwtDecoder.decode(token);
        return decoded.getSubject();
    }
}
