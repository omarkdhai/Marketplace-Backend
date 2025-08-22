# File: payment-service/Dockerfile

### Stage 1: Build the application
FROM registry.access.redhat.com/ubi8/openjdk-17:1.19

# Set the working directory inside the container
WORKDIR /work/

# Copy the Maven project files
COPY --chown=1001:0 .mvn/ ./.mvn/
COPY --chown=1001:0 mvnw ./
COPY --chown=1001:0 pom.xml ./
COPY --chown=1001:0 src ./src

# Build the application using Maven, skipping tests
RUN ./mvnw package -DskipTests

### Stage 2: Create the runtime image
# Use a lean base image for the final application.
FROM registry.access.redhat.com/ubi8/openjdk-17-runtime:1.19

# Set the working directory
WORKDIR /work/

# Copy the built application from the build stage
COPY --from=0 /work/target/quarkus-app/ ./

# Expose the application port (this is for documentation, the environment variable below is what matters)
EXPOSE 8085

# Command to run the application
CMD ["java", "-jar", "quarkus-run.jar"]