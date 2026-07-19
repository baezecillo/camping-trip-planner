# --- Stage 1: build ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy the whole backend context. Simpler and safer than cherry-picking
# individual build files, since Gradle project layouts (Groovy vs Kotlin
# DSL, single vs multi-module) can vary in exact filenames.
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test --no-daemon

# --- Stage 2: run ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Assumes a single bootable jar is produced under build/libs/.
# If the build produces more than one jar (e.g. a plain + a -plain.jar),
# narrow this glob to the specific artifact name.
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]