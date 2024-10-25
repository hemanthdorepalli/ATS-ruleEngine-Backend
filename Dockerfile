# Start with the official OpenJDK 17 image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml ./
COPY src ./src

# Install Maven dependencies and package the application
RUN ./mvnw dependency:go-offline
RUN ./mvnw package -DskipTests

# Copy the built jar file into the container
COPY target/ATS-ruleEngine-Backend-0.0.1-SNAPSHOT.jar app.jar

# Expose the server port
EXPOSE 8080

# Set the entry point
ENTRYPOINT ["java","-jar","/app/app.jar"]
