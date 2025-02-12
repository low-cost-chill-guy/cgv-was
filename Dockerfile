# Build stage
FROM gradle:7.6.1-jdk17-alpine AS builder

# Add build dependencies
RUN apk add --no-cache curl

# Set gradle user permissions
USER root
RUN mkdir -p /home/gradle && \
    chown -R gradle:gradle /home/gradle

WORKDIR /build
RUN chown -R gradle:gradle /build

# Switch to gradle user
USER gradle

# Copy gradle files for dependency resolution
COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle gradlew gradlew.bat ./

# Verify gradle wrapper
RUN chmod +x gradlew
RUN ./gradlew --version

# Download dependencies
RUN ./gradlew dependencies --no-daemon --stacktrace

# Copy source code
COPY --chown=gradle:gradle src src

# Build application
RUN ./gradlew build --no-daemon --stacktrace

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Add non-root user
RUN addgroup -S spring && adduser -S spring -G spring && \
    chown -R spring:spring /app

USER spring:spring

# Copy JAR file
COPY --from=builder --chown=spring:spring /build/build/libs/*.jar app.jar

# Set JVM options
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]