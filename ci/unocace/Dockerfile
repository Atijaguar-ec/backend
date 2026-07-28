# Dockerfile de emergencia para UNOCACE: empaqueta un app.jar ya compilado
# (build multi-stage completo vive en backend/Dockerfile, usado por CI para
# publicar a GHCR). Este archivo replica exactamente lo que hoy corre en
# /opt/inatrace-test/Dockerfile en 95.217.182.220 — permite reconstruir la
# imagen localmente en el servidor a partir de un app.jar subido por scp,
# sin depender del registro.
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
