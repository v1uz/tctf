FROM eclipse-temurin:21.0.1_12-jdk

# Cache gradle distribution
COPY gradlew /app/
COPY gradle /app/gradle
WORKDIR /app
RUN ./gradlew --no-daemon help

# Cache gradle dependencies
COPY build.gradle.kts settings.gradle.kts /app/
RUN ./gradlew --no-daemon downloadDependencies

# Build actual project
COPY src /app/src
RUN ./gradlew --no-daemon bootJar

# Build a small container
FROM eclipse-temurin:21.0.1_12-jdk
WORKDIR /app
COPY --from=0 /app/build/libs/overhaul-1.0-SNAPSHOT.jar /overhaul.jar
CMD java -jar /overhaul.jar
EXPOSE 8080