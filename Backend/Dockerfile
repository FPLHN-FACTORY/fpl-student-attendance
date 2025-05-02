# Stage 1: Build Stage
FROM gradle:7.5.1-jdk17 as builder

WORKDIR /app
# Copy wrapper nếu có, hoặc chỉ copy gradle/ nếu bạn dùng wrapper
COPY gradlew gradlew
COPY gradle/ gradle/
RUN chmod +x gradlew

ENV GRADLE_USER_HOME=/app/.gradle

COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
COPY src src

RUN ls -al

# Build với Gradle wrapper (hoặc dùng gradle CLI)
# Nếu dùng wrapper:
RUN ./gradlew build --no-daemon -x test && ls build/libs
# Nếu không có wrapper, dùng:
# RUN gradle build --no-daemon -g /app/.gradle

# Stage 2: Runtime Stage
FROM eclipse-temurin:17

WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=builder /app/build/libs/student-attendance-0.0.1-SNAPSHOT.jar app.jar

# Command to run the Spring Boot application
CMD ["java", "-jar", "app.jar"]

