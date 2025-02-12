# Build stage
FROM gradle:7.6.1-jdk17-alpine AS builder
WORKDIR /build

# Copy gradle files first for better caching
COPY settings.gradle build.gradle ./
COPY gradle gradle
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat

# Download dependencies first (cached if gradle files don't change)
RUN gradle dependencies --no-daemon

# Copy source code
COPY src src

# Build application
RUN gradle build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring && \
    chown -R spring:spring /app
USER spring:spring

# Copy JAR file
COPY --from=builder --chown=spring:spring /build/build/libs/*.jar app.jar

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Expose port
EXPOSE 8080

# Start application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]