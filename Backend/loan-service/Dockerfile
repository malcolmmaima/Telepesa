# Multi-stage build for loan service with shared libraries
FROM openjdk:17-jdk-slim as builder

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy parent POM first
COPY pom.xml .

# Copy shared libraries
COPY shared-libraries ./shared-libraries

# Install parent POM to local repository
RUN mvn -N clean install -DskipTests

# Install shared libraries to local repository
RUN cd shared-libraries/common-models && mvn clean install -DskipTests
RUN cd shared-libraries/security-utils && mvn clean install -DskipTests
RUN cd shared-libraries/common-exceptions && mvn clean install -DskipTests

# Copy loan service files
COPY loan-service/pom.xml ./loan-service/
COPY loan-service/src ./loan-service/src

# Build the loan service
WORKDIR /app/loan-service
RUN mvn clean compile -DskipTests && mvn package -DskipTests -Dmaven.test.skip=true

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/loan-service/target/loan-service-1.0.0.jar app.jar

# Set JVM options
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8084

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 