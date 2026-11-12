package com.abelium.inatrace.components.integration.v1;

import com.abelium.inatrace.components.integration.v1.services.IntegrationV1Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Fija el alcance configurado de la API de integración Mieru Cacao, que es el único mecanismo
 * que acota la credencial M2M (D-01 del informe QA 2026-11-04).
 *
 * El perfil unocace usa "*" porque el despliegue es mono-cliente: todas sus empresas son
 * UNOCACE y sus asociaciones, que es el alcance que pide el requerimiento. Lo que estas
 * pruebas protegen es que "*" no se cuele en un perfil que aloje a más de un cliente, y que
 * el valor por defecto siga siendo fail-closed.
 */
class IntegrationAllowlistConfigTest {

    private static final String KEY = "INATrace.integration.allowedCompanyIds";

    private Properties load(String resource) throws Exception {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(in, "no se encuentra " + resource + " en el classpath");
            props.load(in);
        }
        return props;
    }

    /**
     * src/test/resources/application.properties ensombrece al principal en el classpath de
     * test, así que para comprobar lo que de verdad se despliega hay que resolver la copia
     * que vive en target/classes, no la de target/test-classes.
     */
    private Properties loadShippedMainProperties() throws Exception {
        Enumeration<URL> candidates = getClass().getClassLoader().getResources("application.properties");
        while (candidates.hasMoreElements()) {
            URL url = candidates.nextElement();
            String path = url.getPath();
            if (path.contains("/classes/") && !path.contains("/test-classes/")) {
                Properties props = new Properties();
                try (InputStream in = url.openStream()) {
                    props.load(in);
                }
                return props;
            }
        }
        return fail("no se encuentra el application.properties principal (target/classes)");
    }

    @Test
    @DisplayName("Perfil unocace: alcance a todas las empresas de la instancia (despliegue mono-cliente)")
    void testUnocaceScopeCoversTheWholeInstance() throws Exception {
        String configured = load("application-unocace.properties").getProperty(KEY);

        assertNotNull(configured, KEY + " debe estar definido en el perfil unocace");
        assertTrue(IntegrationV1Service.isAllCompanies(configured),
                "UNOCACE y todas sus asociaciones viven en esta instancia, así que el alcance es "
                        + "la instancia completa; valor configurado: '" + configured + "'");
    }

    /**
     * "*" sólo es correcto donde todas las empresas pertenecen al mismo grupo. El perfil de
     * Fortaleza del Valle es otro cliente, y su configuración no debe conceder ese alcance.
     */
    @Test
    @DisplayName("Perfil fortaleza: no concede alcance a toda la instancia")
    void testFortalezaDoesNotGrantWholeInstanceScope() throws Exception {
        String configured = load("application-fortaleza.properties").getProperty(KEY);

        assertFalse(IntegrationV1Service.isAllCompanies(configured),
                "sólo un despliegue mono-cliente puede usar '*'; valor configurado: '" + configured + "'");
    }

    @Test
    @DisplayName("Por defecto el alcance va vacío: fail-closed si un despliegue no lo configura")
    void testDefaultIsFailClosed() throws Exception {
        String configured = loadShippedMainProperties().getProperty(KEY);

        assertNotNull(configured, KEY + " debe estar documentado en application.properties");
        assertFalse(IntegrationV1Service.isAllCompanies(configured),
                "el valor por defecto no debe conceder la instancia completa");
        assertTrue(IntegrationV1Service.parseAllowedCompanyIds(configured).isEmpty(),
                "el valor por defecto no debe conceder alcance a ninguna empresa");
    }

    @Test
    @DisplayName("Una lista explícita de IDs sigue funcionando, para excluir a quien no firme el acuerdo")
    void testExplicitListStillSupported() {
        assertFalse(IntegrationV1Service.isAllCompanies("4,7,8"));
        assertEquals(Set.of(4L, 7L, 8L), IntegrationV1Service.parseAllowedCompanyIds("4,7,8"));
    }

    @Test
    @DisplayName("El parseo descarta valores no numéricos y vacíos sin romper el arranque")
    void testParsingIsResilient() {
        assertEquals(Set.of(4L, 8L), IntegrationV1Service.parseAllowedCompanyIds(" 8 , 4 ,, no-numerico , 8 "));
        assertTrue(IntegrationV1Service.parseAllowedCompanyIds(null).isEmpty());
        assertTrue(IntegrationV1Service.parseAllowedCompanyIds("   ").isEmpty());
        assertTrue(IntegrationV1Service.isAllCompanies(" * "));
        assertTrue(IntegrationV1Service.isAllCompanies("all"));
        assertFalse(IntegrationV1Service.isAllCompanies(null));
        assertFalse(IntegrationV1Service.isAllCompanies(""));
    }
}
