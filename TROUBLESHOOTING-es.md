# Solución de Problemas Comunes (Troubleshooting)

> **Nota importante:** El cambio a [exchangerate.host](https://exchangerate.host) implicó modificar el código fuente para actualizar las URLs de la API. Actualmente, exchangerate.host requiere una clave API que debe configurarse en la propiedad `INAtrace.exchangerate.apiKey`. Si tienes una versión antigua, revisa que estés usando la última versión del código.

Esta sección documenta errores comunes encontrados durante el desarrollo y sus soluciones.

### Cómo usar este documento

- Localiza el problema por sección según el síntoma.
- Sigue el flujo: Síntoma → Causa → Diagnóstico → Solución → Validación.
- Ejecuta los comandos en el entorno correcto (tu máquina, dentro del contenedor o en el servidor remoto).
- Si persiste, recopila evidencias y consulta la sección "Recolección de logs y soporte".

### Convenciones y notas

- Rutas y archivos se muestran como `deploy/backend/dev/docker-compose.yml`.
- Los bloques de comandos indican el intérprete: `sh`, `sql`, `xml`.
- Variables como `DB_ROOT_PASSWORD` o `INATRACE_ENV` deben sustituirse por valores reales.
- En MySQL, preferir scripts idempotentes con `IF EXISTS`/`IF NOT EXISTS` en migraciones.

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

### Diagnóstico

- Revisa el log de arranque y busca `FlywayException`, `Failed to execute SQL` o mensajes de migración.
- Verifica el estado de migraciones en la base de datos:
  ```sql
  SELECT installed_rank, version, description, success
  FROM flyway_schema_history
  ORDER BY installed_rank DESC;
  ```
- En pruebas locales, confirma que el perfil de test usa auto‑generación de esquema por Hibernate y Flyway deshabilitado (si aplica):
  - `spring.jpa.properties.hibernate.hbm2ddl.auto=update`
  - `spring.flyway.enabled=false`

### Buenas prácticas

- Escribe scripts idempotentes (`IF EXISTS`/`IF NOT EXISTS`).
- Evita `DROP`/`ALTER` sin comprobaciones previas.
- Mantén las migraciones alineadas con las entidades JPA y los perfiles de ejecución (test/prod).

### Validación

- Reinicia la aplicación y confirma que no hay errores de Flyway.
- Comprueba que las últimas filas de `flyway_schema_history` tengan `success = 1`.

---

## 2. Puerto en uso al iniciar el backend

- **Síntoma**: Error indicando que el puerto (por ejemplo, 8083) está en uso.
- **Solución**: Identifica y detén el proceso que ocupa el puerto. En Mac/Linux:
  ```sh
  lsof -i :8083
  kill -9 <PID>
  ```

### Diagnóstico

- Comprueba qué servicio está levantando el puerto (Docker, una app previa, etc.).
- Si usas Docker Compose, revisa el mapeo de puertos en `docker-compose.yml` (`"8080:8080"`, etc.).
- En Mac, si el puerto es de una app que reinicia, considera cambiar temporalmente el puerto de la app.

### Validación

- Reintenta iniciar el servicio y asegura que el puerto ahora está libre.
- Ejecuta `curl http://localhost:<PUERTO>/actuator/health` y valida un `status: "UP"`.

---

## 3. Error 401 Unauthorized con API de tasas de cambio

- **Síntoma**: Al consultar tasas de cambio, el backend muestra `401 Unauthorized` o `429 Too Many Requests`.
- **Causa**: Clave API inválida o límites de uso superados. Asegúrate de tener una clave API válida para exchangerate.host configurada en la propiedad `INAtrace.exchangerate.apiKey`.
- **Solución**: Asegúrate de usar exchangerate.host. Si usas otro proveedor, revisa la clave y los límites de uso.

### Diagnóstico

- Verifica el valor de `INAtrace.exchangerate.apiKey` en tu configuración (perfil actual).
- Comprueba en logs del backend las URL llamadas y el código HTTP devuelto.
- Si hay límites de uso, revisa el plan de tu API key o aplica caché local.

### Validación

- Ejecuta una llamada controlada (p. ej., un endpoint interno que consume la API) y valida que responde 200.
- Supervisa que no aparezcan `401/429` posteriores bajo carga normal.

---

## 4. Probar APIs con Swagger UI

- **Acceso**: Por defecto, `http://localhost:8080/swagger-ui.html`. Si tu entorno local expone otro puerto (p. ej., 8083), ajusta la URL en consecuencia.

---

## 5. Recomendaciones de seguridad

- No versiones secretos (claves/contraseñas) en git.
- Usa secretos del sistema (GitHub Secrets, variables de entorno, gestores de secretos).
- Minimiza permisos de claves/API (principio de mínimo privilegio).
- Rota claves periódicamente y revoca accesos no usados.
- Evita exponer puertos innecesarios y valida orígenes en proxies (p. ej., Traefik).

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

## 8. Error en CI/CD con appleboy/scp-action: "tar: empty archive"

- **Síntoma**: al ejecutar un paso `uses: appleboy/scp-action@v0.1.7`, el log muestra `tar: empty archive` y termina con `exit status 1`.
- **Causa**: el valor de `source:` no coincide con ningún archivo/ruta en el workspace del job, por lo que no hay nada que empaquetar y subir.

### Diagnóstico

- Imprime y verifica la ruta antes del SCP:
  ```sh
  ls -lah deploy/backend/dev || true
  test -f deploy/backend/dev/docker-compose.yml || { echo "Missing deploy/backend/dev/docker-compose.yml"; exit 1; }
  ```
- Si subes un artifact (p. ej., `be-image.tar`), valida su existencia:
  ```sh
  ls -lah .
  test -f be-image.tar || { echo "be-image.tar not found"; exit 1; }
  ```
- Confirma el repositorio/carpeta de trabajo del job:
  - Monorepo o workflows reutilizados pueden requerir un segundo checkout con `actions/checkout@v4` indicando `repository:` y `path:`.

### Solución

- Asegura que el archivo exista en esa rama y ruta exacta, o ajusta `source:` al path real.
- Si descargas artifacts, usa el mismo `name:` que se usó al subirlos y revisa la ruta de descarga (`path:`).
- En escenarios multi‑repo:
  - Añade un checkout explícito del repo que contiene los archivos a subir:
    ```yaml
    - uses: actions/checkout@v4
      with:
        repository: <org>/<backend-repo>
        path: backend
    ```
    y actualiza `source:` a `backend/deploy/backend/dev/docker-compose.yml`.
- Activa `debug: true` en `appleboy/scp-action` para obtener más información.

### Validación

- Reejecuta el workflow y verifica que las comprobaciones previas pasan y que el archivo se sube correctamente.
