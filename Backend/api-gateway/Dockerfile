# Multi-stage Docker build for API Gateway
# Build stage
FROM openjdk:17-jdk-slim as builder

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy parent POM and shared libraries first
COPY pom.xml .
COPY shared-libraries ./shared-libraries

# Install parent POM to local repository
RUN mvn -N clean install -DskipTests

# Install shared libraries to local repository
RUN cd shared-libraries/common-models && mvn clean install -DskipTests
RUN cd shared-libraries/security-utils && mvn clean install -DskipTests
RUN cd shared-libraries/common-exceptions && mvn clean install -DskipTests

# Copy API gateway files
COPY api-gateway/pom.xml ./api-gateway/
COPY api-gateway/src ./api-gateway/src

# Build the API gateway
WORKDIR /app/api-gateway
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

# Install security updates and required packages
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y --no-install-recommends curl && \
    groupadd -g 1001 telepesa && \
    useradd -m -u 1001 -g 1001 telepesa && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the JAR from build stage
COPY --from=builder /app/api-gateway/target/api-gateway-1.0.0.jar app.jar

# Change ownership to telepesa user
RUN chown -R telepesa:telepesa /app

# Switch to non-root user
USER telepesa

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Set JVM options for container
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 