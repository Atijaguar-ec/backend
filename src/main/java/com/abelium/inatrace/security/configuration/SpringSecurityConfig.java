package com.abelium.inatrace.security.configuration;

import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.security.service.CustomUserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true
)
public class SpringSecurityConfig {

    public static final String MIERU_READONLY_CLIENT_ID = "mieru-cacao-ro";
    private static final String MIERU_SERVICE_ACCOUNT_PREFIX = "service-account-mieru-cacao";

    @Autowired
    private CustomUserDetailsServiceImpl customUserDetailsService;

    @Value("${INATrace.env:DEV}")
    private String inatraceEnv;

    @Value("${INATrace.fe.url:http://localhost:4200}")
    private String frontendUrl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static boolean isSwaggerBlockedForEnv(String env) {
        return env != null && "PROD".equalsIgnoreCase(env.trim());
    }

    public static boolean isRestrictedMieruReadOnlyAuthentication(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        Object creds = auth.getCredentials();
        if (creds instanceof Jwt jwt) {
            String azp = jwt.getClaimAsString("azp");
            if (MIERU_READONLY_CLIENT_ID.equalsIgnoreCase(azp)) {
                return true;
            }
            String clientId = jwt.getClaimAsString("client_id");
            if (MIERU_READONLY_CLIENT_ID.equalsIgnoreCase(clientId)) {
                return true;
            }
            String clientIdCamel = jwt.getClaimAsString("clientId");
            if (MIERU_READONLY_CLIENT_ID.equalsIgnoreCase(clientIdCamel)) {
                return true;
            }
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof CustomUserDetails cud && cud.getUsername() != null) {
            return cud.getUsername().toLowerCase(Locale.ROOT).startsWith(MIERU_SERVICE_ACCOUNT_PREFIX);
        }
        if (auth.getName() != null) {
            return auth.getName().toLowerCase(Locale.ROOT).startsWith(MIERU_SERVICE_ACCOUNT_PREFIX);
        }
        return false;
    }

    public static boolean isAllowedMieruReadOnlyRequest(String method, String requestUri, String contextPath) {
        if (method == null || requestUri == null) {
            return false;
        }
        boolean safeMethod = HttpMethod.GET.matches(method)
                || HttpMethod.HEAD.matches(method)
                || HttpMethod.OPTIONS.matches(method);
        if (!safeMethod) {
            return false;
        }
        String path = requestUri;
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        return path.startsWith("/api/integration/v1/")
                || "/api/integration/v1".equals(path)
                || path.startsWith("/integration/v1/")
                || "/integration/v1".equals(path);
    }

    private static final String[] SWAGGER_EXCEPTIONS = new String[] {
        "/v3/api-docs",
        "/v3/api-docs/**",
        "/v3/api-docs/swagger-config",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/swagger-ui/index.html",
        "/api/v3/api-docs",
        "/api/v3/api-docs/**",
        "/api/v3/api-docs/swagger-config",
        "/api/swagger-ui.html",
        "/api/swagger-ui/**",
        "/api/swagger-ui/index.html"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        KeycloakJwtAuthenticationConverter jwtConverter =
                new KeycloakJwtAuthenticationConverter(customUserDetailsService);

        http
                .cors(Customizer.withDefaults())
                .sessionManagement(smc -> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(ehc -> ehc.authenticationEntryPoint(new RestAuthenticationEntryPoint()))
                .authorizeHttpRequests(matcherRegistry -> {
                    matcherRegistry.requestMatchers(
                            "/api/public/**",
                            "/api/user/login",
                            "/api/user/refresh_authentication",
                            "/api/user/register",
                            "/api/user/request_reset_password",
                            "/api/user/reset_password",
                            "/api/user/confirm_email"
                    ).permitAll();
                    if (isSwaggerBlockedForEnv(inatraceEnv)) {
                        matcherRegistry.requestMatchers(SWAGGER_EXCEPTIONS).denyAll();
                    } else {
                        matcherRegistry.requestMatchers(SWAGGER_EXCEPTIONS).permitAll();
                    }
                    matcherRegistry.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)))
                .addFilterAfter(new MieruReadOnlyClientGuardFilter(), BearerTokenAuthenticationFilter.class);

        return http.build();
    }

    static class MieruReadOnlyClientGuardFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (isRestrictedMieruReadOnlyAuthentication(auth)
                    && !isAllowedMieruReadOnlyRequest(request.getMethod(), request.getRequestURI(), request.getContextPath())) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                        "{\"status\":\"UNAUTHORIZED\",\"errorDetails\":\"READ_ONLY_INTEGRATION_SCOPE\"," +
                        "\"errorMessage\":\"M2M credential 'mieru-cacao-ro' is strictly restricted to GET /api/integration/v1/**\"}"
                );
                return;
            }
            filterChain.doFilter(request, response);
        }
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.Arrays.asList(frontendUrl));
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers", "Language"));
        configuration.setExposedHeaders(java.util.Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
