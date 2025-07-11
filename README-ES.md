# Guía de Inicio Rápido - Backend INATrace

---

## Notas sobre tasas de cambio

A partir de julio 2025, el backend utiliza el proveedor [exchangerate.host](https://exchangerate.host) para obtener tasas de cambio de monedas. **No es necesario configurar ninguna clave API** para esta funcionalidad.

- La configuración anterior con `exchangeratesapi.io` y la propiedad `INAtrace.exchangerate.apiKey` ya no es necesaria.
- El sistema es compatible y transparente para el usuario; las tasas se actualizan automáticamente (ver detalles en el código).
- **Importante:** El cambio de proveedor implicó modificar el código fuente para actualizar las URLs y eliminar la necesidad de clave API. Estos cambios ya están implementados en este repositorio.

---

## Integración opcional con Beyco

INATrace permite integrarse con la plataforma [Beyco](https://beyco.nl) para que los usuarios puedan crear ofertas de Beyco automáticamente a partir de pedidos de stock en INATrace. **Esta integración es completamente opcional.**

- Si deseas usar Beyco, deberás solicitar a Beyco los siguientes datos y configurarlos en tu `application.properties`:
  - `beyco.oauth2.clientId`
  - `beyco.oauth2.clientSecret`
  - `beyco.oauth2.url`
- Si **no necesitas** la integración con Beyco, simplemente deja estos campos vacíos:
  ```properties
  beyco.oauth2.clientId =
  beyco.oauth2.clientSecret =
  beyco.oauth2.url =
  ```
- El resto de funcionalidades de INATrace no se ven afectadas si no configuras Beyco.

---

## Ayuda y Solución de Problemas

Para ver la lista de errores comunes y sus soluciones, consulta el archivo [TROUBLESHOOTING-ES.md](./TROUBLESHOOTING-ES.md).


    -- Para eliminar columnas (Requiere MySQL 8.0.33+ o MariaDB 10.5+)
    ALTER TABLE nombre_de_la_tabla DROP COLUMN IF EXISTS nombre_de_la_columna;

    -- Para eliminar claves foráneas (Foreign Keys)
    ALTER TABLE nombre_de_la_tabla DROP FOREIGN KEY IF EXISTS nombre_del_constraint;
    ```

-   **Nota Adicional**: Si la tabla de historial de Flyway (`flyway_schema_history`) se vuelve inconsistente, puede ser necesario resetear la base de datos. Esto implica eliminar la base de datos, volver a crearla y permitir que Flyway ejecute todas las migraciones desde cero.

### 2. Error al Arrancar: Puerto en Uso

-   **Síntoma**: La aplicación falla al iniciar con un error similar a `Port 8083 was already in use`.
-   **Causa**: Otro proceso ya está ocupando el puerto que la aplicación necesita. A menudo es una instancia anterior de la misma aplicación que no se cerró correctamente.
-   **Solución**: Encontrar y detener el proceso que está usando el puerto.

    **Comandos para macOS/Linux:**
    ```bash
    # 1. Encontrar el ID del proceso (PID) que usa el puerto 8083
    lsof -ti :8083

    # 2. Detener el proceso usando el PID obtenido (reemplaza <PID>)
    kill -9 <PID>
    ```

### 3. Error `401 Unauthorized` con API Externa

-   **Síntoma**: La aplicación arranca pero falla al intentar comunicarse con una API externa, mostrando un error `401 Unauthorized` en los logs. En nuestro caso, con `api.exchangeratesapi.io`.
-   **Causa**: Falta una clave de API (API Key) válida en la configuración de la aplicación.
-   **Solución**:
    1.  Obtener una clave de API válida del proveedor del servicio (ej. [exchangeratesapi.io](https://exchangeratesapi.io/)).
    2.  Añadir la clave al archivo de configuración local `src/main/resources/application.properties`. Este archivo está correctamente ignorado por Git para no exponer secretos.
    3.  Añadir la línea correspondiente. Para el servicio de cambio de divisas, la propiedad es:
        ```properties
        INAtrace.exchangerate.apiKey=TU_API_KEY_AQUI
        ```

### 4. Cómo Probar las APIs del Backend

-   **Pregunta**: ¿Cómo puedo ver y probar los endpoints de la API que ofrece el backend?
-   **Solución**: El proyecto utiliza `springdoc-openapi`, que genera automáticamente una interfaz de usuario interactiva con Swagger UI.
-   **Acceso**: Una vez que la aplicación esté en funcionamiento, abre tu navegador y ve a la siguiente URL:
    
    [**http://localhost:8083/swagger-ui.html**](http://localhost:8083/swagger-ui.html)

    Desde allí podrás explorar todos los endpoints, ver sus parámetros y ejecutar peticiones de prueba.

Este documento describe, en español, los pasos necesarios para poner en marcha el backend del sistema INATrace.

## Requisitos previos

- **Java 17** o superior
- **Maven** (gestor de dependencias y construcción para Java)
- Acceso a una base de datos compatible (por ejemplo, MySQL)

### Instalación de Java y Maven en Ubuntu, Rocky Linux y macOS

#### Ubuntu
Instala Java y Maven usando apt:

```sh
sudo apt update
sudo apt install openjdk-17-jdk maven -y
```

Verifica las instalaciones:
```sh
java -version
mvn -v
```

#### Rocky Linux
Instala Java y Maven usando dnf:

```sh
sudo dnf install java-17-openjdk-devel maven -y
```

Verifica las instalaciones:
```sh
java -version
mvn -v
```

#### macOS
Instala Java y Maven usando Homebrew:

```sh
brew install openjdk@17
brew install maven
```

Agrega Java al PATH (si es necesario):
```sh
echo 'export PATH="/usr/local/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

Verifica las instalaciones:
```sh
java -version
mvn -v
```

### Validación de herramientas instaladas

Se validó la instalación de las siguientes herramientas ejecutando los comandos:

```sh
java -version
mvn -v
```

- **Java 17** está correctamente instalado.
- **Maven 3.9.10** está correctamente instalado.

#### Problemas encontrados y soluciones
- Si al ejecutar `mvn -v` aparece el error `command not found: mvn`, es necesario instalar Maven. En macOS, se resolvió ejecutando:
  ```sh
  brew install maven
  ```
- Si Java no está instalado, instalarlo según el sistema operativo siguiendo las instrucciones anteriores.

Ambas herramientas están listas para utilizarse en el proceso de arranque del backend.

## Instalación de MySQL

Puedes instalar MySQL de dos formas: **instalación nativa** (directamente en tu sistema operativo) o usando **Docker**. Elige la que mejor se adapte a tus necesidades y entorno.

### 1. Instalación nativa

#### Ubuntu
```sh
sudo apt update
sudo apt install mysql-server -y
```

#### Rocky Linux
```sh
sudo dnf install mysql-server -y
sudo systemctl enable mysqld
sudo systemctl start mysqld
```

#### macOS (usando Homebrew)
```sh
brew install mysql
brew services start mysql
```

Verifica la instalación:
```sh
mysql --version
```

Accede a la consola de MySQL:
```sh
mysql -u root -p
```

### 2. Instalación con Docker (recomendado para desarrollo rápido)

```sh
docker run --name inatrace-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=inatrace -e MYSQL_USER=inatrace -e MYSQL_PASSWORD=inatrace -p 3306:3306 -d mysql:8.0.33
```

- Si el puerto 3306 está ocupado, puedes cambiarlo (por ejemplo, `-p 3307:3306`) y ajustar el puerto en tu archivo de configuración.
- Para ver los logs del contenedor y diagnosticar problemas:
  ```sh
  docker logs inatrace-mysql
  ```
- Para detener y eliminar el contenedor:
  ```sh
  docker stop inatrace-mysql
  docker rm inatrace-mysql
  ```

---

## Cómo ejecutar el backend (flujo recomendado)

### 1. Clona el repositorio
```sh
git clone https://github.com/Atijaguar-ec/backend.git
cd backend
```

### 2. Importa el proyecto como Maven en tu IDE
Puedes usar IntelliJ IDEA, Eclipse, VS Code u otro IDE compatible con Maven. Selecciona la opción de importar proyecto Maven y elige la carpeta del backend.

### 3. Prepara el entorno

#### a) Crea la base de datos

**Opción recomendada (Docker):**
```sh
docker run --name inatrace-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=inatrace -e MYSQL_USER=inatrace -e MYSQL_PASSWORD=inatrace -p 3306:3306 -d mysql:8.0.33
```

**Alternativa nativa:**
Instala MySQL según tu sistema operativo (ver sección anterior) y crea la base de datos y usuario:
```sh
mysql -u root -p -e "CREATE DATABASE inatrace; CREATE USER 'inatrace'@'%' IDENTIFIED BY 'inatrace'; GRANT ALL PRIVILEGES ON inatrace.* TO 'inatrace'@'%'; FLUSH PRIVILEGES;"
```

#### b) Configura el correo electrónico (obligatorio)

El backend requiere una cuenta SMTP válida para poder enviar correos electrónicos (por ejemplo, para notificaciones, recuperación de contraseña, etc.). **Si no configuras estos parámetros, la aplicación no podrá arrancar.**

Agrega en tu archivo `src/main/resources/application.properties` los siguientes parámetros, usando los datos de tu proveedor SMTP:

```properties
spring.mail.host=smtp.tu-proveedor.com
spring.mail.port=587
spring.mail.username=tu_usuario
spring.mail.password=tu_contraseña
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Ejemplo para pruebas locales:**
- Puedes usar servicios gratuitos como [Mailtrap](https://mailtrap.io/) o [Ethereal](https://ethereal.email/) para pruebas de desarrollo. Ellos te darán los datos SMTP de prueba.

**Errores comunes:**
- Si no configuras estos parámetros, verás un error como:
  > `No qualifying bean of type 'org.springframework.mail.javamail.JavaMailSenderImpl' available`
- Si los datos SMTP son incorrectos, el backend no podrá enviar correos y mostrará errores de autenticación.

**Nota:**
- Es obligatorio tener la dependencia `spring-boot-starter-mail` en tu `pom.xml`.
- Si ves el mensaje: `Standard Commons Logging discovery in action with spring-jcl: please remove commons-logging.jar from classpath in order to avoid potential conflicts`, es solo una advertencia, pero puedes eliminar `commons-logging.jar` del classpath para evitar posibles conflictos.

#### c) Configuración de Spring
Copia y edita el archivo de propiedades:
```sh
cp src/main/resources/application.properties.template src/main/resources/application.properties
# Edita src/main/resources/application.properties según tu entorno
```
Parámetros mínimos:
```properties
INATrace.database.name=inatrace
INATrace.database.hostname=localhost
INATrace.database.port=3306
spring.datasource.username=inatrace
spring.datasource.password=inatrace
```

**Errores comunes:**
- Si el puerto 3306 está en uso, cambia el puerto en el comando Docker y en `application.properties`.
- Si hay errores de conexión a la base de datos, revisa usuario, contraseña y existencia de la base de datos.
- Si el archivo `application.properties` no existe, asegúrate de haberlo copiado correctamente.
- **Error 'Public Key Retrieval is not allowed':** Ocurre cuando intentas conectarte a MySQL con usuarios que usan autenticación por contraseña y la opción de recuperación de clave pública no está habilitada.

**Solución:**
Asegúrate de que la URL de conexión en tu `application.properties` contenga el parámetro:
```properties
spring.datasource.url=jdbc:mysql://${INATrace.database.hostname}:${INATrace.database.port}/${INATrace.database.name}?autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
```
El parámetro `allowPublicKeyRetrieval=true` permite la recuperación de la clave pública y soluciona este error.

- **Error en migraciones Flyway: "Can't DROP 'ingredients'; check that column/key exists"**

  Este error ocurre cuando se intenta eliminar una columna que ya no existe en la base de datos durante una migración automática con Flyway.

  **Soluciones recomendadas:**
  - **Desarrollo:** Puedes borrar la base de datos y dejar que Flyway la recree desde cero:
    ```sh
    mysql -u root -p -e "DROP DATABASE inatrace; CREATE DATABASE inatrace;"
    ```
    Luego reinicia el backend para que Flyway aplique todas las migraciones desde el inicio.
  - **Producción:** Revisa el estado de la base y las migraciones. Si la columna ya fue eliminada manualmente o por otra migración, ajusta o elimina el script conflictivo (`V2023_06_14_10_04__Drop__column__ingredients__in__Product.sql`) o usa condicionales en SQL:
    ```sql
    ALTER TABLE Product DROP COLUMN IF EXISTS ingredients;
    ```
  - Siempre realiza respaldos antes de modificar la base de datos en producción.

#### Nota sobre migraciones y columnas eliminadas

- Si una migración intenta eliminar una columna que no existe (por ejemplo, `ALTER TABLE Product DROP COLUMN ingredients;`), es porque:
  - La columna existía en una versión anterior del modelo, pero ya no está en el modelo Java ni en las migraciones actuales.
  - En instalaciones nuevas, la columna nunca se crea, por lo que la instrucción de borrado falla si no se usa `IF EXISTS`.
- **Recomendación:** Siempre usa `DROP COLUMN IF EXISTS nombre_columna;` en migraciones destructivas para evitar errores en bases limpias o después de un reseteo.
- No es necesario crear columnas que ya no forman parte del modelo ni de las migraciones previas.
- Deja los scripts de borrado para asegurar que bases antiguas se actualicen correctamente; en bases nuevas, simplemente no harán nada.

### 4. Ejecuta el backend

**Desde tu IDE:**
Busca la clase `INATraceBackendApplication.java` y ejecútala (botón "Run" o similar).

**Desde terminal:**
```sh
mvn spring-boot:run
```

El backend se ejecutará por defecto en el puerto 8080. Puedes modificar esto en el archivo `application.properties`:
```properties
server.port=8080
```

**Errores comunes y soluciones:**
- **Puerto en uso:** Cambia el puerto o libera el ocupado.
- **Dependencias Maven:** Si hay errores, ejecuta `mvn clean install`.
- **Permisos:** Ejecuta comandos con usuario adecuado.

---

*Esta guía sigue el flujo recomendado en el README original y documenta alternativas y soluciones prácticas para cada paso.*

## Ejecución del backend

Con Maven instalado y el archivo de configuración listo, puedes iniciar el backend ejecutando:

```sh
mvn spring-boot:run
```

El backend se ejecutará por defecto en el puerto 8080. Puedes modificar esto en el archivo `application.properties` cambiando la línea:
```properties
server.port=8080
```

**Errores comunes al iniciar el backend:**
- **Puerto en uso:** Si aparece un error indicando que el puerto 8080 ya está en uso, puedes cambiar el puerto en `application.properties` o liberar el puerto ocupado.
- **Errores de conexión a la base de datos:** Verifica los parámetros y que el servicio de MySQL esté activo.
- **Dependencias faltantes:** Si Maven muestra errores de dependencias, ejecuta:
  ```sh
  mvn clean install
  ```
- **Permisos insuficientes:** Si ves errores de permisos, ejecuta el comando con un usuario que tenga los permisos necesarios.

**Soluciones prácticas:**
- Cambia el puerto si es necesario:
  ```properties
  server.port=8081
  ```
- Asegúrate de que la base de datos y el usuario existen y tienen permisos.
- Si el backend no levanta, revisa los logs en consola para identificar el error específico y consulta las secciones anteriores para posibles soluciones.

---

*Esta guía está en desarrollo y se irá actualizando conforme se documente cada paso del proceso.*
