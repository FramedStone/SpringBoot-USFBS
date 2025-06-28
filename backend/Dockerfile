FROM maven:3.9.7-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /target/springboot-0.0.1-SNAPSHOT.jar app.jar
COPY application.yaml ./application.yaml
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=classpath:/application.properties,classpath:/application.yaml,./application.yaml"]