#!/bin/bash

set -e

# Stop all local Java processes (Spring Boot apps)
echo "Stopping all local Java processes..."
if pgrep java > /dev/null; then
  pkill -f 'java'
  echo "All local Java processes stopped."
else
  echo "No local Java processes running."
fi

# Build and install shared libraries
echo "Building and installing shared libraries..."
for LIB in Backend/shared-libraries/*; do
  if [ -d "$LIB" ] && [ -f "$LIB/pom.xml" ]; then
    echo "Building $LIB..."
    (cd "$LIB" && mvn clean install)
  fi
done

# Return to Backend directory before building microservices
cd Backend

# Build all microservice JARs
SERVICES=(user-service account-service transaction-service loan-service notification-service api-gateway eureka-server)
echo "Building JARs for all microservices..."
for SERVICE in "${SERVICES[@]}"; do
  echo "Building $SERVICE..."
  (cd $SERVICE && mvn clean package -DskipTests)
done

cd ..

echo "All JARs built."

# Start the full stack in Docker Compose
echo "Starting full stack with Docker Compose..."
cd Backend/docker-compose
docker-compose up --build 