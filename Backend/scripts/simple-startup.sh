#!/bin/bash

# Simple Sequential Service Startup Script
# This script starts services one by one and verifies they're working

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Starting Telepesa Services Sequentially${NC}"

# Function to check if port is in use
check_port() {
    local port=$1
    if lsof -i :$port > /dev/null 2>&1; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    echo -e "${YELLOW}⏳ Waiting for $service_name to be ready...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s $url > /dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name is ready!${NC}"
            return 0
        fi
        echo -e "${YELLOW}   Attempt $attempt/$max_attempts...${NC}"
        sleep 3
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}❌ $service_name failed to start within timeout${NC}"
    return 1
}

# Check if PostgreSQL and Redis are running
echo -e "${BLUE}📊 Checking infrastructure services...${NC}"
if ! check_port 5432; then
    echo -e "${RED}❌ PostgreSQL is not running on port 5432${NC}"
    echo -e "${YELLOW}Starting PostgreSQL with Docker...${NC}"
    cd docker-compose && docker-compose up -d postgres
    sleep 10
    cd ..
fi

if ! check_port 6379; then
    echo -e "${RED}❌ Redis is not running on port 6379${NC}"
    echo -e "${YELLOW}Starting Redis with Docker...${NC}"
    cd docker-compose && docker-compose up -d redis
    sleep 5
    cd ..
fi

echo -e "${GREEN}✅ Infrastructure services are ready${NC}"

# Kill any existing services
echo -e "${YELLOW}🧹 Cleaning up existing services...${NC}"
pkill -f "eureka-server" 2>/dev/null || true
pkill -f "api-gateway" 2>/dev/null || true
pkill -f "user-service" 2>/dev/null || true
pkill -f "account-service" 2>/dev/null || true
pkill -f "transaction-service" 2>/dev/null || true
pkill -f "loan-service" 2>/dev/null || true
pkill -f "notification-service" 2>/dev/null || true
sleep 5

# Start Eureka Server
echo -e "${BLUE}🌐 Starting Eureka Server...${NC}"
cd eureka-server
nohup java -jar target/eureka-server-1.0.0.jar --spring.profiles.active=dev > ../logs/eureka.log 2>&1 &
cd ..

if wait_for_service "http://localhost:8761" "Eureka Server"; then
    echo -e "${GREEN}✅ Eureka Server started successfully${NC}"
else
    echo -e "${RED}❌ Eureka Server failed to start${NC}"
    exit 1
fi

# Start API Gateway
echo -e "${BLUE}🚪 Starting API Gateway...${NC}"
cd api-gateway
mvn package -DskipTests > /dev/null 2>&1
nohup java -jar target/api-gateway-1.0.0.jar --spring.profiles.active=dev > ../logs/gateway.log 2>&1 &
cd ..

if wait_for_service "http://localhost:8080/actuator/health" "API Gateway"; then
    echo -e "${GREEN}✅ API Gateway started successfully${NC}"
else
    echo -e "${RED}❌ API Gateway failed to start${NC}"
    exit 1
fi

# Start User Service
echo -e "${BLUE}👤 Starting User Service...${NC}"
cd user-service
nohup java -jar target/user-service-1.0.0.jar --spring.profiles.active=dev > ../logs/user.log 2>&1 &
cd ..

if wait_for_service "http://localhost:8081/actuator/health" "User Service"; then
    echo -e "${GREEN}✅ User Service started successfully${NC}"
else
    echo -e "${RED}❌ User Service failed to start${NC}"
    exit 1
fi

# Start Account Service
echo -e "${BLUE}🏦 Starting Account Service...${NC}"
cd account-service
mvn package -DskipTests > /dev/null 2>&1
nohup java -jar target/account-service-1.0.0.jar --spring.profiles.active=dev > ../logs/account.log 2>&1 &
cd ..

if wait_for_service "http://localhost:8082/actuator/health" "Account Service"; then
    echo -e "${GREEN}✅ Account Service started successfully${NC}"
else
    echo -e "${RED}❌ Account Service failed to start${NC}"
    exit 1
fi

# Start Transaction Service
echo -e "${BLUE}💳 Starting Transaction Service...${NC}"
cd transaction-service
mvn package -DskipTests > /dev/null 2>&1
nohup java -jar target/transaction-service-1.0.0.jar --spring.profiles.active=dev > ../logs/transaction.log 2>&1 &
cd ..

if wait_for_service "http://localhost:8083/actuator/health" "Transaction Service"; then
    echo -e "${GREEN}✅ Transaction Service started successfully${NC}"
else
    echo -e "${RED}❌ Transaction Service failed to start${NC}"
    exit 1
fi

# Start Loan Service
echo -e "${BLUE}🏠 Starting Loan Service...${NC}"
cd loan-service
nohup java -jar target/loan-service-1.0.0.jar --spring.profiles.active=dev > ../logs/loan.log 2>&1 &
cd ..

if wait_for_service "http://localhost:8084/actuator/health" "Loan Service"; then
    echo -e "${GREEN}✅ Loan Service started successfully${NC}"
else
    echo -e "${RED}❌ Loan Service failed to start${NC}"
    exit 1
fi

# Start Notification Service
echo -e "${BLUE}📧 Starting Notification Service...${NC}"
cd notification-service
mvn package -DskipTests > /dev/null 2>&1
nohup java -jar target/notification-service-1.0.0.jar --spring.profiles.active=dev > ../logs/notification.log 2>&1 &
cd ..

if wait_for_service "http://localhost:8085/actuator/health" "Notification Service"; then
    echo -e "${GREEN}✅ Notification Service started successfully${NC}"
else
    echo -e "${RED}❌ Notification Service failed to start${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}🎉 All services started successfully!${NC}"
echo ""
echo -e "${BLUE}📊 Service Status:${NC}"
echo -e "  🌐 Eureka Server:      http://localhost:8761"
echo -e "  🚪 API Gateway:        http://localhost:8080"
echo -e "  👤 User Service:       http://localhost:8081"
echo -e "  🏦 Account Service:    http://localhost:8082"
echo -e "  💳 Transaction Service: http://localhost:8083"
echo -e "  🏠 Loan Service:       http://localhost:8084"
echo -e "  📧 Notification Service: http://localhost:8085"
echo ""
echo -e "${YELLOW}💡 Logs are available in the 'logs' directory${NC}"
echo -e "${YELLOW}💡 Run './scripts/gateway-e2e-test.sh' to test all services${NC}" 