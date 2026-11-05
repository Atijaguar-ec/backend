package com.abelium.inatrace.components.integration.v1;

import com.abelium.inatrace.components.exceptionhandling.SpringExceptionResponseBuilder;
import com.abelium.inatrace.components.integration.v1.services.IntegrationV1Service;
import com.abelium.inatrace.configuration.DefaultControllerExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contrato de errores HTTP de la API de integración Mieru Cacao.
 *
 * Estas pruebas atraviesan la capa MVC completa (resolución de parámetros + @ControllerAdvice),
 * que es justo donde la suite unitaria no llegaba: D-02 del informe QA 2026-11-04 pasaba
 * inadvertido porque las pruebas invocaban el servicio directamente, saltándose a Spring.
 */
@ExtendWith(MockitoExtension.class)
class IntegrationV1ErrorContractTest {

    @Mock
    private IntegrationV1Service integrationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SpringExceptionResponseBuilder responseBuilder = new SpringExceptionResponseBuilder();
        ReflectionTestUtils.setField(responseBuilder, "springJacksonConverter", new MappingJackson2HttpMessageConverter());

        DefaultControllerExceptionHandler exceptionHandler = new DefaultControllerExceptionHandler();
        ReflectionTestUtils.setField(exceptionHandler, "exceptionResponseBuilder", responseBuilder);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new IntegrationV1Controller(integrationService))
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    /**
     * CP-08 de la matriz de QA: «Si no se envía updated_since, debe arrojar 400 Bad Request».
     * Antes devolvía 500 con el nombre de la clase de excepción interna, lo que además le dice
     * al sincronizador nocturno de Mieru que reintente un error que no se arregla reintentando.
     */
    @Test
    @DisplayName("D-02 / CP-08: GET /deletions sin updated_since devuelve 400, no 500")
    void testDeletionsWithoutUpdatedSinceReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/integration/v1/deletions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errorDetails").value("MISSING_REQUIRED_PARAMETER"))
                .andExpect(jsonPath("$.errorMessage").value("Required request parameter 'updated_since' is missing"));
    }

    @Test
    @DisplayName("D-02: el cuerpo del 400 no filtra la clase de la excepción interna")
    void testBadRequestBodyDoesNotLeakExceptionClass() throws Exception {
        String body = mockMvc.perform(get("/integration/v1/deletions").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertFalse(body.contains("MissingServletRequestParameterException"),
                "el cuerpo no debe exponer la clase de excepción interna: " + body);
        org.junit.jupiter.api.Assertions.assertFalse(body.contains("Uncaught exception"),
                "el cuerpo no debe presentarse como error no capturado: " + body);
    }

    @Test
    @DisplayName("D-02: un updated_since con formato inválido también es 400, no 500")
    void testDeletionsWithMalformedUpdatedSinceReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/integration/v1/deletions")
                        .param("updated_since", "no-es-una-fecha")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
