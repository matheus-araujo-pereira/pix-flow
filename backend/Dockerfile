# ==============================================================================
# PixFlow: Event-Driven Payment & Settlement Engine
# Multi-Stage Dockerfile (Optimized JVM 21 Alpine Container)
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build and Package
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven Wrapper and POM first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build application jar
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# Extract Spring Boot layered jar for optimal Docker caching
WORKDIR /build/target
RUN java -Djarmode=layertools -jar pix-flow-backend-1.0.0-SNAPSHOT.jar extract

# ------------------------------------------------------------------------------
# Stage 2: Lightweight Production Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Install curl/wget for health checks
RUN apk add --no-cache curl tzdata \
    && cp /usr/share/zoneinfo/UTC /etc/localtime \
    && echo "UTC" > /etc/timezone

# Create a non-privileged user and group for hardened security
RUN addgroup -g 1001 -S spring && adduser -u 1001 -S spring -G spring

# Copy Spring Boot layers from the build stage
COPY --from=builder --chown=spring:spring /build/target/dependencies/ ./
COPY --from=builder --chown=spring:spring /build/target/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /build/target/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /build/target/application/ ./

USER spring:spring

EXPOSE 8080

# Production-grade JVM ergonomics flags
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"
ENV SERVER_PORT=8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
