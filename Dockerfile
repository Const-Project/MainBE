# =================================================================
# 1단계: 빌드(Build) 스테이지
# - 소스코드를 컴파일하고 실행 가능한 .jar 파일을 만드는 역할만 수행합니다.
# - JDK가 포함된 무거운 이미지를 사용합니다.
# =================================================================
FROM amazoncorretto:17-alpine-jdk AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 관련 파일들을 먼저 복사하여 의존성 레이어 캐싱 활용
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Gradle 의존성을 다운로드합니다. 소스코드가 변경되어도 이 레이어는 캐시로 재사용됩니다.
RUN ./gradlew dependencies

# 소스코드 전체를 복사합니다.
COPY src ./src

# Gradle을 사용하여 애플리케이션을 빌드합니다. (테스트는 CI 단계에서 이미 수행했으므로 생략)
RUN ./gradlew build -x test


# =================================================================
# 2단계: 실행(Runtime) 스테이지
# - 빌드된 .jar 파일을 실행하는 역할만 수행합니다.
# - JRE만 포함된 매우 가벼운 이미지를 사용하여 최종 이미지 크기를 줄입니다.
# =================================================================
FROM amazoncorretto:17-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 보안을 위해 non-root 사용자를 생성하고 전환합니다.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# 1단계(builder)에서 생성된 .jar 파일을 복사해옵니다.
# build/libs/ 안에 어떤 이름의 jar가 생겨도 app.jar 라는 이름으로 복사됩니다.
COPY --from=builder /app/build/libs/*.jar ./app.jar

# 애플리케이션이 8080 포트를 사용함을 명시
EXPOSE 8080

# 컨테이너가 시작될 때 이 명령어를 실행하여 애플리케이션을 구동
ENTRYPOINT ["java", "-jar", "app.jar"]