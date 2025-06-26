# filepath: backend/Dockerfile
FROM eclipse-temurin:21-jre
ARG JAR_FILE=target/springboot-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]