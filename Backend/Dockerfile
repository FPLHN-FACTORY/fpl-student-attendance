# Stage 1: Build jar
FROM gradle:8.2-jdk17 AS builder
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle bootJar --no-daemon

# Stage 2: Run jar
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY  /app/build/libs/student-attendance-0.0.1-SNAPSHOT.jar app.jar
COPY  .env .env
EXPOSE 8765
ENTRYPOINT ["java", "-jar", "app.jar"]
