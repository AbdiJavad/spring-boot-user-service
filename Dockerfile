# ==========================================
# STAGE 1: Build Stage
# ==========================================
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build

# Copy Maven wrapper and configuration files first to cache dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application (skip tests in docker build since CI/CD runs them)
RUN ./mvnw clean package -DskipTests

# Extract Spring Boot layered jars for optimized caching
WORKDIR /extract
RUN java -Djarmode=layertools -jar /build/target/*.jar extract

# ==========================================
# STAGE 2: Runtime Stage (Production-Ready)
# ==========================================
FROM eclipse-temurin:17-jre-alpine AS runner

# Create a non-root system user and group for security compliance
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy extracted layers from the builder stage with proper ownership
COPY --from=builder --chown=spring:spring /extract/dependencies/ ./
COPY --from=builder --chown=spring:spring /extract/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring /extract/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /extract/application/ ./

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Configure JVM flags optimized for container environments
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Health check endpoint (Actuator health)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q "UP" || exit 1

# Run the Spring Boot application using the layered launcher
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
