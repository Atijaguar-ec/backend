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
 * Fija la lista blanca de empresas de la API de integración Mieru Cacao, que es el único
 * mecanismo que acota el alcance de la credencial M2M (D-01 del informe QA 2026-11-04).
 *
 * Un error tipográfico aquí no rompe el arranque: los valores no numéricos se descartan con
 * un log, de modo que «1l» en lugar de «11» reduciría el alcance en silencio. Esta prueba
 * convierte ese fallo silencioso en un fallo de compilación del build.
 *
 * Si cambia el acuerdo de cesión de datos con Mieru (RF §5), hay que actualizar esta prueba
 * a propósito — y eso es deliberado: la lista no debe poder cambiar sin que nadie lo note.
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
    @DisplayName("Perfil unocace: la lista acordada con Mieru es exactamente 1,4,7,8,11,15")
    void testUnocaceAllowlistMatchesTheAgreement() throws Exception {
        String configured = load("application-unocace.properties").getProperty(KEY);

        assertNotNull(configured, KEY + " debe estar definido en el perfil unocace");
        assertEquals(Set.of(1L, 4L, 7L, 8L, 11L, 15L), IntegrationV1Service.parseAllowedCompanyIds(configured),
                "lista confirmada por el dueño de producto el 2026-11-04 y ampliada con el id 1 el 2026-11-05; "
                        + "actualizar sólo con el acuerdo firmado");
    }

    /**
     * CP-02 de la matriz de QA: «Devuelve UNOCACE con RUC 0992258814001». Mieru conoce a las
     * empresas sólo por nombre y resuelve los IDs con GET /companies (RF §4.3), así que sacar a
     * UNOCACE del alcance le impediría resolver una de las dos empresas que el RF §4.1 nombra.
     */
    @Test
    @DisplayName("Perfil unocace: la lista incluye UNOCACE (id 1), que el caso CP-02 necesita")
    void testUnocaceAllowlistIncludesUnocaceItself() throws Exception {
        String configured = load("application-unocace.properties").getProperty(KEY);

        assertTrue(IntegrationV1Service.parseAllowedCompanyIds(configured).contains(1L),
                "sin el id 1, GET /companies no devuelve UNOCACE y CP-02 no puede pasar");
    }

    @Test
    @DisplayName("Perfil unocace: la lista no incluye la empresa 5, usada en el caso CP-09")
    void testUnocaceAllowlistExcludesCp09Company() throws Exception {
        String configured = load("application-unocace.properties").getProperty(KEY);

        assertFalse(IntegrationV1Service.parseAllowedCompanyIds(configured).contains(5L),
                "CP-09 exige que GET /farmers?company_id=5 responda 403");
    }

    @Test
    @DisplayName("Por defecto la lista va vacía: fail-closed si un despliegue no la configura")
    void testDefaultIsFailClosed() throws Exception {
        String configured = loadShippedMainProperties().getProperty(KEY);

        assertNotNull(configured, KEY + " debe estar documentado en application.properties");
        assertTrue(IntegrationV1Service.parseAllowedCompanyIds(configured).isEmpty(),
                "el valor por defecto no debe conceder alcance a ninguna empresa");
    }

    @Test
    @DisplayName("El parseo descarta valores no numéricos y vacíos sin romper el arranque")
    void testParsingIsResilient() {
        assertEquals(Set.of(4L, 8L), IntegrationV1Service.parseAllowedCompanyIds(" 8 , 4 ,, no-numerico , 8 "));
        assertTrue(IntegrationV1Service.parseAllowedCompanyIds(null).isEmpty());
        assertTrue(IntegrationV1Service.parseAllowedCompanyIds("   ").isEmpty());
    }
}
