package com.abelium.inatrace.components.bi;

import com.abelium.inatrace.api.ApiStatus;
import com.abelium.inatrace.security.service.CustomUserDetails;
import com.abelium.inatrace.types.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SupersetClientResilienceTest {

	private BiSupersetProperties properties;
	private CustomUserDetails user;

	@BeforeEach
	void setUp() {
		properties = new BiSupersetProperties();
		properties.setInternalUrl("http://inatrace-bi-superset-fortaleza:8088/bi");
		properties.setPublicDomain("https://testinatrace.espam.edu.ec/bi");
		properties.setAllowedPortalOrigin("https://testinatrace.espam.edu.ec");
		properties.setOrganizationSlug("fortaleza");
		properties.setEnvironment("staging");
		properties.setServiceUsername("bi_embed_service");
		properties.setServicePassword("secret-pass");
		user = new CustomUserDetails(10L, "op@fv.ec", "Operador", "FV", UserRole.USER);
	}

	@Test
	void rejectsInvalidAndTraversalSlugsWithBadRequest() {
		SupersetClient client = new SupersetClient(WebClient.builder(), properties, new ObjectMapper());

		for (String badSlug : new String[] { "", "../etc/passwd", "foo/bar", "bad?query=1", "UPPER_BAD!" }) {
			BiReportException ex = assertThrows(BiReportException.class,
					() -> client.getDashboardEmbedded(badSlug),
					"Should reject invalid slug: " + badSlug);
			assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
			assertEquals(ApiStatus.VALIDATION_ERROR, ex.getApiStatus());

			BiReportException metaEx = assertThrows(BiReportException.class,
					() -> client.getDashboardMetadata(badSlug));
			assertEquals(HttpStatus.BAD_REQUEST, metaEx.getHttpStatus());
		}
	}

	@Test
	void mapsUpstreamTimeoutRuntimeExceptionToServiceUnavailable503() {
		ExchangeFunction timeoutExchange = request -> Mono.error(
				new IllegalStateException("Timeout on blocking read", new TimeoutException("Read timed out"))
		);

		SupersetClient client = new SupersetClient(
				WebClient.builder().exchangeFunction(timeoutExchange),
				properties,
				new ObjectMapper()
		);

		BiReportException embEx = assertThrows(BiReportException.class,
				() -> client.getDashboardEmbedded("fv-custom-report"));
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, embEx.getHttpStatus());
		assertEquals(ApiStatus.UPSTREAM_HTTP_ERROR, embEx.getApiStatus());

		BiReportException guestEx = assertThrows(BiReportException.class,
				() -> client.createGuestToken("uuid-1", user));
		assertEquals(HttpStatus.SERVICE_UNAVAILABLE, guestEx.getHttpStatus());
		assertEquals(ApiStatus.UPSTREAM_HTTP_ERROR, guestEx.getApiStatus());
	}
}
