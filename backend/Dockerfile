# --- Stage 1: Build the JAR ---
FROM maven:3.9.6-eclipse-temurin-21 as builder
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Run the JAR ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/springboot-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
