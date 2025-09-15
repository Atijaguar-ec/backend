# ═══════════════════════════════════════════════════════════════
# 🚀 INATRACE BACKEND - OPTIMIZED MULTI-STAGE DOCKERFILE
# ═══════════════════════════════════════════════════════════════

# Build stage
FROM maven:3.8.5-openjdk-17-slim as build-stage

WORKDIR /src

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
ARG VERSION=unknown
ARG BUILD_DATE=unknown
ARG VCS_REF=unknown

RUN mvn clean package -B -DskipTests \
    -Dversion.number=${VERSION} \
    -Dbuild.date=${BUILD_DATE} \
    -Dvcs.ref=${VCS_REF}

# Runtime stage
FROM eclipse-temurin:17-jre-alpine as runtime-stage

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -g 1001 -S inatrace && \
    adduser -S inatrace -u 1001 -G inatrace

# Create application directories
RUN mkdir -p /app/files /app/import /app/documents /app/logs && \
    chown -R inatrace:inatrace /app

# Copy application jar
ARG JAR_FILE=target/*.jar
COPY --from=build-stage /src/${JAR_FILE} /app/app.jar
RUN chown inatrace:inatrace /app/app.jar

# Switch to non-root user
USER inatrace

WORKDIR /app

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication"

# Application entry point
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
