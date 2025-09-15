# Runtime stage for pre-built JAR
FROM eclipse-temurin:17-jre

# Install security updates and required packages
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y --no-install-recommends curl && \
    groupadd -g 1001 telepesa && \
    useradd -m -u 1001 -g 1001 telepesa && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy the pre-built JAR file
COPY target/loan-service-*.jar app.jar

# Change ownership to telepesa user
RUN chown -R telepesa:telepesa /app

# Switch to non-root user
USER telepesa

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8084/actuator/health || exit 1

# Expose port
EXPOSE 8084

# Set JVM options for container
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 