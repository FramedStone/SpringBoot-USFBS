FROM maven:3.9.7-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim
WORKDIR /app

# Set timezone to Malaysia
# RUN apt-get update && apt-get install -y tzdata && \
#     ln -fs /usr/share/zoneinfo/Asia/Kuala_Lumpur /etc/localtime && \
#     dpkg-reconfigure -f noninteractive tzdata

COPY --from=build /target/springboot-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=Asia/Kuala_Lumpur", "-jar", "app.jar"]