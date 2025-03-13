# Use official OpenJDK image as base
FROM openjdk:17-jdk-slim AS build

# Set the working directory inside the container
WORKDIR /app

# Copy Maven wrapper and the source code
COPY . .

# Download dependencies and build the Quarkus application
RUN ./mvnw clean package -DskipTests

# Use a smaller JDK image for the final image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the generated jar from the build stage
COPY --from=build /app/target/notif-service-1.0.0-SNAPSHOT.jar /app/notif-service-1.0.0-SNAPSHOT.jar

# Command to run the application
ENTRYPOINT ["java", "-jar", "/app/notif-service-1.0.0-SNAPSHOT.jar"]