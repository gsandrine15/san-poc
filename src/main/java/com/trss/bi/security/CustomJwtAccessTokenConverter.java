package com.trss.bi.security;

import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.Map;

/*
 * Converts between JWT Access Tokens and Authentication
 * The default implementation wasn't extracting claims properly
 */
public class CustomJwtAccessTokenConverter extends JwtAccessTokenConverter {

    // Override extractAuthentication to make sure claims get added to the authentication
    @Override
    public OAuth2Authentication extractAuthentication(Map<String, ?> claims) {
        OAuth2Authentication authentication = super.extractAuthentication(claims);
        authentication.setDetails(claims);
        return authentication;
    }
}
