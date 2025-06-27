# Multi-stage build for Telepesa User Service
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy parent pom.xml first for better layer caching
COPY ../pom.xml ./parent-pom.xml

# Copy shared libraries
COPY ../shared-libraries ./shared-libraries

# Copy user service source
COPY pom.xml .
COPY src ./src

# Build shared libraries first
RUN cd shared-libraries && mvn clean install -DskipTests

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install security updates and required packages
RUN apk update && apk upgrade && \
    apk add --no-cache curl && \
    addgroup -g 1001 -S telepesa && \
    adduser -S telepesa -u 1001 -G telepesa

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/target/user-service-*.jar app.jar

# Change ownership to telepesa user
RUN chown -R telepesa:telepesa /app

# Switch to non-root user
USER telepesa

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Expose port
EXPOSE 8081

# Set JVM options for container
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 