package com.felipe.teachgram_backend.security.oauth2.user;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String provider, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(provider)) {
            return new GoogleOAuth2UserInfo(attributes);
        }

        throw new IllegalArgumentException("Login com provedor " + provider + " não é suportado.");
    }
}
