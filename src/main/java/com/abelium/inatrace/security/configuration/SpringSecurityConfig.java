package com.abelium.inatrace.security.configuration;

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
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

		return http.build();
	}

}
