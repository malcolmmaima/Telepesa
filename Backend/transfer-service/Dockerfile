# Use Maven image for building
FROM maven:3.9-eclipse-temurin-17 AS builder

# Set working directory
WORKDIR /app

# Copy parent pom and shared libraries
COPY pom.xml ./
COPY shared-libraries ./shared-libraries

# Install parent pom first
RUN mvn -N install

# Build and install shared libraries
RUN mvn -f shared-libraries/common-exceptions/pom.xml clean install -DskipTests
RUN mvn -f shared-libraries/common-models/pom.xml clean install -DskipTests
RUN mvn -f shared-libraries/security-utils/pom.xml clean install -DskipTests

# Copy transfer-service pom and download dependencies
COPY transfer-service/pom.xml ./transfer-service/
RUN mvn -f transfer-service/pom.xml dependency:resolve -B

# Copy transfer-service source code and build
COPY transfer-service/src ./transfer-service/src
RUN mvn -f transfer-service/pom.xml clean package -DskipTests -Dmaven.test.skip=true

# Runtime image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/transfer-service/target/transfer-service-1.0.0.jar ./transfer-service.jar

# Expose the port the application runs on
EXPOSE 8086

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Install curl for health check
RUN apt-get update && apt-get install -y --fix-missing curl && rm -rf /var/lib/apt/lists/* && apt-get clean

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8086/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar transfer-service.jar"]
