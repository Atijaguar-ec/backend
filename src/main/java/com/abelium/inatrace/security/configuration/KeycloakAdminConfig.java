package com.abelium.inatrace.security.configuration;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakAdminConfig {

    @Value("${inatrace.keycloak.admin.server-url}")
    private String serverUrl;

    @Value("${inatrace.keycloak.admin.realm}")
    private String realm;

    @Value("${inatrace.keycloak.admin.client-id}")
    private String clientId;

    @Value("${inatrace.keycloak.admin.client-secret}")
    private String clientSecret;

    @Bean
    public Keycloak keycloakAdminClient() {
        KeycloakBuilder builder = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId);

        if (clientSecret != null && !clientSecret.isEmpty()) {
            builder.clientSecret(clientSecret);
        }

        return builder.build();
    }
}
