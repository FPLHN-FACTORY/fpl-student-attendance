FROM gradle:8.12.1-jdk17 AS builder
WORKDIR /app

COPY gradlew gradlew
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# Khai báo ENV để wrapper dùng đúng thư mục mặc định của Gradle
ENV GRADLE_USER_HOME=/home/gradle/.gradle
RUN mkdir -p $GRADLE_USER_HOME

# Bước preload dependency (plugin + wrapper)
RUN --mount=type=cache,id=gradle-cache,target=/home/gradle/.gradle,sharing=shared \
    ./gradlew dependencies --no-daemon || true

COPY src/ src/

# Bước build jar
RUN --mount=type=cache,id=gradle-cache,target=/home/gradle/.gradle,sharing=shared \
    ./gradlew bootJar --no-daemon -x test


# -------------------------
# Stage 2: Runtime Stage
# -------------------------
FROM eclipse-temurin:17
WORKDIR /app

# Copy any built JAR into app.jar
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose default Spring Boot port
EXPOSE 8765

# Run the application
CMD ["java", "-jar", "app.jar"]
