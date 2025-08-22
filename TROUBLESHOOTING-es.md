# Solución de Problemas Comunes (Troubleshooting)

> **Nota importante:** El cambio a [exchangerate.host](https://exchangerate.host) implicó modificar el código fuente para actualizar las URLs de la API. Actualmente, exchangerate.host requiere una clave API que debe configurarse en la propiedad `INAtrace.exchangerate.apiKey`. Si tienes una versión antigua, revisa que estés usando la última versión del código.

Esta sección documenta errores comunes encontrados durante el desarrollo y sus soluciones.

---

## 1. Errores de Migración con Flyway

- **Síntoma**: La aplicación no arranca y los logs muestran errores de Flyway, a menudo relacionados con sentencias SQL que intentan eliminar objetos (tablas, columnas, claves foráneas) que no existen. Por ejemplo: `Error: Can't DROP 'constraint_name'; check that column/key exists`.
- **Causa**: Los scripts de migración destructivos no son idempotentes. Un script idempotente puede ejecutarse múltiples veces sin cambiar el resultado después de la ejecución inicial.
- **Solución**: Modificar los scripts SQL para que sean idempotentes. Para MySQL, la forma más robusta es usar la sintaxis `IF EXISTS`.

  **Ejemplos:**
  ```sql
  -- Para eliminar tablas
  DROP TABLE IF EXISTS nombre_de_la_tabla;
  -- Para eliminar columnas
  ALTER TABLE nombre_de_la_tabla DROP COLUMN IF EXISTS nombre_columna;
  -- Para eliminar claves foráneas
  ALTER TABLE nombre_de_la_tabla DROP FOREIGN KEY IF EXISTS nombre_fk;
  ```

---

## 2. Puerto en uso al iniciar el backend

- **Síntoma**: Error indicando que el puerto (por ejemplo, 8083) está en uso.
- **Solución**: Identifica y detén el proceso que ocupa el puerto. En Mac/Linux:
  ```sh
  lsof -i :8083
  kill -9 <PID>
  ```

---

## 3. Error 401 Unauthorized con API de tasas de cambio

- **Síntoma**: Al consultar tasas de cambio, el backend muestra `401 Unauthorized` o `429 Too Many Requests`.
- **Causa**: Clave API inválida o límites de uso superados. Asegúrate de tener una clave API válida para exchangerate.host configurada en la propiedad `INAtrace.exchangerate.apiKey`.
- **Solución**: Asegúrate de usar exchangerate.host. Si usas otro proveedor, revisa la clave y los límites de uso.

---

## 4. Probar APIs con Swagger UI

- **Acceso**: Visita `http://localhost:8083/swagger-ui.html` para probar los endpoints del backend de forma interactiva.

---

## 5. Recomendaciones de seguridad

- No subas archivos con claves o contraseñas a git.
- Usa variables de entorno o archivos de propiedades locales para datos sensibles.

---

## 6. Problemas con DNS en MacOS

- **Síntoma**: Advertencias como `Unable to load io.netty.resolver.dns.macos.MacOSDnsServerAddressStreamProvider, fallback to system defaults. This may result in incorrect DNS resolutions on MacOS.`
- **Causa**: La aplicación utiliza Netty para conexiones HTTP (a través de Spring WebFlux) pero falta la dependencia nativa para resolver DNS en MacOS.
- **Solución**: Agregar las siguientes dependencias en el `pom.xml` para soportar tanto Mac con procesadores Intel como Apple Silicon:

  ```xml
  <!-- MacOS DNS resolver for Netty -->
  <dependency>
      <groupId>io.netty</groupId>
      <artifactId>netty-resolver-dns-native-macos</artifactId>
      <classifier>osx-x86_64</classifier>
  </dependency>
  <dependency>
      <groupId>io.netty</groupId>
      <artifactId>netty-resolver-dns-native-macos</artifactId>
      <classifier>osx-aarch_64</classifier>
  </dependency>
  ```
  
  Después ejecuta `mvn clean install` y reinicia la aplicación.

---

## 7. Problemas con autenticación en GitHub

- Usa un Personal Access Token (PAT) en vez de contraseña para autenticación HTTPS.

---
