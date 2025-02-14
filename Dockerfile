# gradle, jdk 버전 17 이미지를 이미지 빌더로 사용
FROM gradle:7.6.1-jdk17-alpine AS builder

# 빌드할 때 필요한 의존성 추가
RUN apk add --no-cache curl

# root 유저로 gradle 유저의 권한을 설정
USER root
RUN mkdir -p /home/gradle && \
    chown -R gradle:gradle /home/gradle

WORKDIR /build
RUN chown -R gradle:gradle /build

# gradle 유저로 바꿔서 구성
USER gradle

# gradle 파일들 복사
COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle gradlew gradlew.bat ./

# gradlew chmod로 실행권한 추가
RUN chmod +x gradlew
RUN ./gradlew --version

# gradlew에 있는 의존성 설치
RUN ./gradlew dependencies --no-daemon --stacktrace

# src 폴더에 있는 소스코드들 복사
COPY --chown=gradle:gradle src src

# 애플리케이션 빌드
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