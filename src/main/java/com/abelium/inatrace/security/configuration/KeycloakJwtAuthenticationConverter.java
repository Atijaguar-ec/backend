package com.abelium.inatrace.security.configuration;

import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.security.service.CustomUserDetailsServiceImpl;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts a Keycloak JWT into a UsernamePasswordAuthenticationToken
 * whose principal is a CustomUserDetails, bridging the gap between
 * OAuth2 Resource Server JWT auth and the existing @AuthenticationPrincipal usage.
 */
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakJwtAuthenticationConverter.class);

    private final CustomUserDetailsServiceImpl userDetailsService;

    public KeycloakJwtAuthenticationConverter(CustomUserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Keycloak JWT typically has 'email' or 'preferred_username' claim
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = jwt.getClaimAsString("preferred_username");
        }

        if (email == null) {
            logger.warn("JWT token does not contain 'email' or 'preferred_username' claim. Sub: {}", jwt.getSubject());
            return new UsernamePasswordAuthenticationToken(null, jwt, java.util.Collections.emptyList());
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            CustomUserDetails customUser = (CustomUserDetails) userDetails;
            return new UsernamePasswordAuthenticationToken(
                customUser,
                jwt,
                customUser.getAuthorities()
            );
        } catch (UsernameNotFoundException e) {
            logger.warn("User with email '{}' from JWT not found in database: {}", email, e.getMessage());
            return new UsernamePasswordAuthenticationToken(null, jwt, java.util.Collections.emptyList());
        }
    }
}
