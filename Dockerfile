# ==============================================================================
# Multi-Stage Dockerfile for Smart Reminder Backend (Render Deployment)
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build the Application
# ------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copy Maven POM and download dependencies to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build the JAR package (skipping tests for fast build)
COPY src ./src
RUN mvn clean package -DskipTests

# ------------------------------------------------------------------------------
# Stage 2: Minimal Production Runtime
# ------------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-privileged user and group for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the generated JAR artifact from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set ownership to non-root user
RUN chown -R appuser:appgroup /app
USER appuser

# Render injects the PORT environment variable (default: 8080)
ENV PORT=8080
EXPOSE 8080

# Production-tuned JVM configuration for containerized environments:
# -XX:+UseContainerSupport: respects container CPU and memory cgroups
# -XX:MaxRAMPercentage=75.0: caps heap at 75% of container RAM (ideal for Render 512MB limit)
# -Djava.security.egd: speeds up cryptographic initialization
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
