# Multi-stage Docker build for Eureka Server
# Build stage
FROM amazoncorretto:17-alpine-jdk as build

# Install Maven
RUN apk add --no-cache maven

# Set working directory
WORKDIR /workspace/app

# Copy pom file and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN mvn clean package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Runtime stage
FROM amazoncorretto:17-alpine

# Install security updates and required packages
RUN apk update && apk upgrade && \
    apk add --no-cache curl && \
    addgroup -g 1001 -S telepesa && \
    adduser -S telepesa -u 1001 -G telepesa

# Set working directory
WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /workspace/app/target/eureka-server-*.jar app.jar

# Change ownership to telepesa user
RUN chown -R telepesa:telepesa /app

# Switch to non-root user
USER telepesa

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8761/actuator/health || exit 1

# Expose port
EXPOSE 8761

# Set JVM options for container
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 