package com.abelium.inatrace.security.configuration;

import com.abelium.inatrace.security.service.CustomUserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true
)
public class SpringSecurityConfig {

    @Autowired
    private CustomUserDetailsServiceImpl customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }



	private static final String[] SWAGGER_EXCEPTIONS = new String[] {
        "/v3/api-docs",
        "/v3/api-docs/swagger-config",
        "/swagger-ui/**"
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
					matcherRegistry.requestMatchers(SWAGGER_EXCEPTIONS).permitAll();
					matcherRegistry.anyRequest().authenticated();
				})
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtConverter)));

		return http.build();
	}
	@org.springframework.beans.factory.annotation.Value("${INATrace.fe.url:http://localhost:4200}")
	private String frontendUrl;

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
