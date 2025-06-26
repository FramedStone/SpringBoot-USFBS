# Use official base image for Java 21 (JRE only)
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Add application JAR (Maven/Gradle)
ARG JAR_FILE=target/springboot-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Expose port (optional, helps local testing)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
