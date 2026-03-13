FROM gradle:jdk17 AS cache

WORKDIR /app

COPY gradle ./gradle
COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts ./

RUN gradle dependencies --no-daemon --stacktrace

FROM gradle:jdk17 AS builder

WORKDIR /app

COPY --from=cache /app /app
COPY src ./src

RUN gradle bootJar -x test --no-daemon --stacktrace

FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar ./app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
