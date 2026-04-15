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
 *
 * Security policy:
 *  - If the JWT has no user claim (email / preferred_username) → reject (401)
 *  - If the user is not found in the INATrace database → reject (401)
 *  - Only fully resolved, database-registered users are authenticated.
 */
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakJwtAuthenticationConverter.class);

    private final CustomUserDetailsServiceImpl userDetailsService;

    public KeycloakJwtAuthenticationConverter(CustomUserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Keycloak JWT typically contains 'email' or 'preferred_username'
        String email = jwt.getClaimAsString("email");
        if (email == null) {
            email = jwt.getClaimAsString("preferred_username");
        }

        if (email == null) {
            logger.error("JWT token is missing both 'email' and 'preferred_username' claims. Sub: {}. Rejecting.", jwt.getSubject());
            // Throw instead of returning a null-principal token — Spring Security
            // will treat this as unauthenticated and return 401.
            throw new UsernameNotFoundException("JWT token has no identifiable user claim");
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            CustomUserDetails customUser = (CustomUserDetails) userDetails;
            logger.debug("Authenticated JWT for user: {}", email);
            return new UsernamePasswordAuthenticationToken(
                customUser,
                jwt,
                customUser.getAuthorities()
            );
        } catch (UsernameNotFoundException e) {
            logger.error("User '{}' from valid Keycloak JWT not found in INATrace database. " +
                "User must be registered in the application first. Rejecting.", email);
            // Reject clearly — do NOT silently grant an anonymous-like token.
            throw new UsernameNotFoundException(
                "Keycloak user '" + email + "' has a valid token but is not registered in INATrace."
            );
        }
    }
}
