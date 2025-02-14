# gradle, jdk 버전 17 이미지를 이미지 빌더로 사용
FROM gradle:7.6.1-jdk17-alpine AS builder

# Alpine Linux의 패키지 매니저를 사용하여 curl을 설치
RUN apk add --no-cache curl

# root 권한으로 gradle 사용자의 홈 디렉토리를 생성하고 권한을 설정
USER root
RUN mkdir -p /home/gradle && \
    chown -R gradle:gradle /home/gradle

# 작업 디렉토리를 /build로 설정하고 gradle 사용자에게 권한을 부여
WORKDIR /build
RUN chown -R gradle:gradle /build

# 보안을 위해 gradle 유저로 바꿔서 구성
USER gradle

# Gradle 빌드에 필요한 설정 파일들을 복사합니다.
# --chown 옵션으로 복사된 파일의 소유권을 gradle 사용자로 설정
COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle gradle gradle
COPY --chown=gradle:gradle gradlew gradlew.bat ./

# gradlew chmod로 실행권한 추가
RUN chmod +x gradlew
RUN ./gradlew --version

# 프로젝트의 의존성을 미리 다운로드
# --no-daemon: Gradle 데몬을 사용 X
# --stacktrace: 오류 발생 시 상세한 스택 트레이스를 출력
RUN ./gradlew dependencies --no-daemon --stacktrace

# src 폴더에 있는 소스코드들 복사
COPY --chown=gradle:gradle src src

# 애플리케이션 빌드
RUN ./gradlew build --no-daemon --stacktrace


# 런타임 스테이지 => ecr의 스캔은 런타임 스캔해줌, 소나큐브는 소스코드 스캔
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 보안을 위해 spring이라는 비특권 사용자와 그룹을 생성
RUN addgroup -S spring && adduser -S spring -G spring && \
    chown -R spring:spring /app

USER spring:spring

# 빌드 스테이지에서 생성된 JAR 파일을 현재 스테이지로 복사
COPY --from=builder --chown=spring:spring /build/build/libs/*.jar app.jar

# JVM 옵션을 설정합니다:
# UseContainerSupport: 컨테이너 환경 인식 활성화
# MaxRAMPercentage: 최대 힙 메모리를 컨테이너 메모리의 75%로 설정
# java.security.egd: 시작 시간 개선을 위한 엔트로피 소스 설정
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# 8080 포트를 노출
EXPOSE 8080

# 설정된 JVM 옵션으로 JAR 파일을 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]