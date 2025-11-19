# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Gradle wrapper와 설정 파일 먼저 복사 (캐싱 활용)
COPY gradlew .
RUN chmod +x gradlew && \
    ./gradlew dependencies --no-daemon
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드 (레이어 캐싱)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 빌드 실행 (테스트 제외)
RUN ./gradlew bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 타임존 설정 (Asia/Seoul)
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Seoul /etc/localtime && \
    echo "Asia/Seoul" > /etc/timezone && \
    apk del tzdata

# 빌드 단계에서 생성된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 8080 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]