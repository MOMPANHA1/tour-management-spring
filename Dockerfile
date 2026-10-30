# ---- Build stage ----
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Cache Gradle dependencies separately from source so `docker build` doesn't
# re-download the world every time only application code changes.
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --version

COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# ---- Runtime stage ----
FROM eclipse-temurin:25-jre
WORKDIR /app

RUN addgroup --system spring && adduser --system --ingroup spring spring
COPY --from=build /app/build/libs/*.jar app.jar
USER spring

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
