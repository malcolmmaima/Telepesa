# Runtime stage for CI/CD (JAR already built)
FROM eclipse-temurin:17-jre

# Install security updates and required packages
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y curl && \
    groupadd -g 1001 telepesa && \
    useradd -m -u 1001 -g telepesa telepesa

# Set working directory
WORKDIR /app

# Copy the pre-built JAR file from CI/CD artifacts
COPY target/*.jar app.jar

# Change ownership to telepesa user
RUN chown -R telepesa:telepesa /app

# Switch to non-root user
USER telepesa

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8082/actuator/health || exit 1

# Expose port
EXPOSE 8082

# Set JVM options for container
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"] 