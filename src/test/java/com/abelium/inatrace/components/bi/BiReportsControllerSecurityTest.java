package com.abelium.inatrace.components.bi;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.components.exceptionhandling.SpringExceptionResponseBuilder;
import com.abelium.inatrace.configuration.DefaultControllerExceptionHandler;
import com.abelium.inatrace.security.annotations.RequireCompanyAccess;
import com.abelium.inatrace.security.configuration.SpringSecurityConfig;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BiReportsControllerSecurityTest {

	@Mock
	private BiReportsService biReportsService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		SpringExceptionResponseBuilder responseBuilder = new SpringExceptionResponseBuilder();
		ReflectionTestUtils.setField(responseBuilder, "springJacksonConverter", new MappingJackson2HttpMessageConverter());

		DefaultControllerExceptionHandler exceptionHandler = new DefaultControllerExceptionHandler();
		ReflectionTestUtils.setField(exceptionHandler, "exceptionResponseBuilder", responseBuilder);

		mockMvc = MockMvcBuilders
				.standaloneSetup(new BiReportsController(biReportsService))
				.setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
				.setControllerAdvice(exceptionHandler)
				.build();
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void everyEndpointRequiresAuthenticationRoleAndCompanyAccess() throws Exception {
		Method getDashboards = BiReportsController.class.getMethod("getDashboards", Long.class,
				com.abelium.inatrace.security.service.CustomUserDetails.class);
		Method createGuestToken = BiReportsController.class.getMethod("createGuestToken",
				com.abelium.inatrace.components.bi.api.ApiBiGuestTokenRequest.class,
				com.abelium.inatrace.security.service.CustomUserDetails.class);
		Method registerCustom = BiReportsController.class.getMethod("registerCustomDashboard",
				com.abelium.inatrace.components.bi.api.ApiCustomBiDashboardRequest.class,
				com.abelium.inatrace.security.service.CustomUserDetails.class);
		Method removeCustom = BiReportsController.class.getMethod("removeCustomDashboard",
				String.class, Long.class,
				com.abelium.inatrace.security.service.CustomUserDetails.class);

		for (Method m : new Method[] { getDashboards, createGuestToken, registerCustom, removeCustom }) {
			assertNotNull(m.getAnnotation(RequireCompanyAccess.class), m.getName() + " must require company access");
			assertNotNull(m.getAnnotation(PreAuthorize.class), m.getName() + " must declare @PreAuthorize");
		}

		assertTrue(registerCustom.getAnnotation(PreAuthorize.class).value().contains("SYSTEM_ADMIN"));
		assertTrue(registerCustom.getAnnotation(PreAuthorize.class).value().contains("REGIONAL_ADMIN"));
		assertFalse(registerCustom.getAnnotation(PreAuthorize.class).value().contains("'USER'"));

		assertTrue(removeCustom.getAnnotation(PreAuthorize.class).value().contains("SYSTEM_ADMIN"));
		assertTrue(removeCustom.getAnnotation(PreAuthorize.class).value().contains("REGIONAL_ADMIN"));
		assertFalse(removeCustom.getAnnotation(PreAuthorize.class).value().contains("'USER'"));
	}

	@Test
	void springSecurityConfigNeverExposesBiRoutesAsPermitAll() throws Exception {
		String configSource = Files.readString(Path.of(
				"src/main/java/com/abelium/inatrace/security/configuration/SpringSecurityConfig.java"
		));
		assertFalse(configSource.contains("/api/bi"), "BI routes must never appear in SpringSecurityConfig permitAll");
		assertTrue(configSource.contains(".anyRequest().authenticated()"));
		assertNotNull(SpringSecurityConfig.class);
	}

	@Test
	void guestTokenEndpointPropagates422ValidationErrorCleanly() throws Exception {
		CustomUserDetails user = new CustomUserDetails(10L, "op@fv.ec", "Operador", "FV", UserRole.USER);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
		);

		when(biReportsService.issueGuestToken(any(), any()))
				.thenThrow(new BiReportException(
						HttpStatus.UNPROCESSABLE_ENTITY,
						ApiStatus.VALIDATION_ERROR,
						"El tablero no autoriza el dominio del portal."
				));

		mockMvc.perform(post("/bi/guest-token")
						.accept(MediaType.APPLICATION_JSON)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"companyId\":5,\"slug\":\"fortaleza-cacao-arqueo-bascula-staging\"}"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status").value("VALIDATION_ERROR"))
				.andExpect(jsonPath("$.errorMessage").value("El tablero no autoriza el dominio del portal."));
	}
}
