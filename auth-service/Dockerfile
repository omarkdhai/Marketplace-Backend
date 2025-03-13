FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy from local directory
COPY auth-service-1.0.0-SNAPSHOT.jar ./auth-service.jar

EXPOSE 8081

CMD ["java", "-jar", "auth-service.jar"]