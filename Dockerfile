# Multi-stage build for Ktor server in Kotlin Multiplatform repo
FROM eclipse-temurin:21-jdk AS build
WORKDIR /home/gradle/project

# Copy Gradle wrapper and configuration files first for caching
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle

# Copy subprojects required for the server and web build
COPY core core
COPY app/shared app/shared
COPY app/webApp app/webApp
COPY server server

# Ensure Gradle wrapper is executable and build the server distribution
RUN chmod +x ./gradlew && ./gradlew :server:installDist --no-daemon

# Minimal runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy built distribution from build stage
COPY --from=build /home/gradle/project/server/build/install/server /app

# Render passes PORT dynamically, defaults to 8080 locally
ENV PORT=8080
EXPOSE 8080

CMD ["./bin/server"]
