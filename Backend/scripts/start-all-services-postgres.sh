#!/bin/bash

# Telepesa All Services Startup Script (PostgreSQL Database)
# Starts all microservices with PostgreSQL database for production-like testing

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Starting Telepesa All Services with PostgreSQL Database${NC}"
echo -e "${BLUE}This will start all 5 microservices for comprehensive testing${NC}\n"

# Check if we're in the right directory
if [ ! -d "shared-libraries" ]; then
    echo -e "${RED}❌ Error: Please run this script from the Backend directory${NC}"
    echo -e "${YELLOW}💡 Run: cd Backend && ./scripts/start-all-services-postgres.sh${NC}"
    exit 1
fi

# Check if PostgreSQL container is running
echo -e "${PURPLE}🔍 Checking PostgreSQL container...${NC}"
if ! docker ps | grep -q "telepesa-postgres"; then
    echo -e "${YELLOW}⚠️  PostgreSQL container not found. Starting it...${NC}"
    docker run --name telepesa-postgres -e POSTGRES_PASSWORD=password -e POSTGRES_USER=telepesa -e POSTGRES_DB=telepesa -p 5432:5432 -d postgres:15
    echo -e "${GREEN}✅ PostgreSQL container started${NC}"
    sleep 10
else
    echo -e "${GREEN}✅ PostgreSQL container is running${NC}"
fi

# Build shared libraries first
echo -e "${PURPLE}📦 Building shared libraries...${NC}"
cd shared-libraries/common-models
mvn clean install -DskipTests
cd ../common-exceptions
mvn clean install -DskipTests
cd ../security-utils
mvn clean install -DskipTests
cd ../..

# Function to start a service
start_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    local profile=$4
    
    echo -e "${YELLOW}Starting $service_name on port $port...${NC}"
    
    cd "$service_dir"
    
    # Start the service in background
    mvn spring-boot:run -Dspring.profiles.active="$profile" > "../logs/${service_name}.log" 2>&1 &
    local pid=$!
    
    # Store the PID
    echo $pid > "../logs/${service_name}.pid"
    
    echo -e "${GREEN}✅ $service_name started with PID: $pid${NC}"
    cd ..
    
    # Wait a bit for service to start
    sleep 15
}

# Create logs directory
mkdir -p logs

# Kill any existing services
echo -e "${PURPLE}🧹 Cleaning up any existing services...${NC}"
pkill -f "spring-boot:run" || true
sleep 2

# Start all services
echo -e "${PURPLE}🚀 Starting all services...${NC}"

# 1. User Service (uses H2 for dev, but we'll keep it for now)
start_service "User Service" "user-service" "8081" "dev"

# 2. Account Service
start_service "Account Service" "account-service" "8082" "dev"

# 3. Transaction Service  
start_service "Transaction Service" "transaction-service" "8083" "dev"

# 4. Loan Service
start_service "Loan Service" "loan-service" "8084" "dev"

# 5. Notification Service
start_service "Notification Service" "notification-service" "8085" "dev"

echo -e "\n${GREEN}🎉 All services started successfully!${NC}"

# Wait a moment for all services to fully start
echo -e "${YELLOW}⏳ Waiting for services to fully initialize...${NC}"
sleep 45

# Check service health
echo -e "\n${PURPLE}🏥 Checking service health...${NC}"

check_health() {
    local service_name=$1
    local port=$2
    local url="http://localhost:$port/actuator/health"
    
    echo -e "${YELLOW}Checking $service_name...${NC}"
    
    if curl -s "$url" | grep -q "UP"; then
        echo -e "${GREEN}✅ $service_name: HEALTHY${NC}"
        return 0
    else
        echo -e "${RED}❌ $service_name: UNHEALTHY${NC}"
        return 1
    fi
}

# Check all services
check_health "User Service" "8081"
check_health "Account Service" "8082" 
check_health "Transaction Service" "8083"
check_health "Loan Service" "8084"
check_health "Notification Service" "8085"

echo -e "\n${BLUE}📊 Service Status Summary:${NC}"
echo -e "👤 User Service: ${BLUE}http://localhost:8081${NC}"
echo -e "🏦 Account Service: ${BLUE}http://localhost:8082${NC}"
echo -e "💳 Transaction Service: ${BLUE}http://localhost:8083${NC}"
echo -e "💰 Loan Service: ${BLUE}http://localhost:8084${NC}"
echo -e "📧 Notification Service: ${BLUE}http://localhost:8085${NC}"

echo -e "\n${BLUE}📚 API Documentation:${NC}"
echo -e "👤 User Service: ${BLUE}http://localhost:8081/swagger-ui.html${NC}"
echo -e "🏦 Account Service: ${BLUE}http://localhost:8082/swagger-ui.html${NC}"
echo -e "💳 Transaction Service: ${BLUE}http://localhost:8083/swagger-ui.html${NC}"
echo -e "💰 Loan Service: ${BLUE}http://localhost:8084/swagger-ui.html${NC}"
echo -e "📧 Notification Service: ${BLUE}http://localhost:8085/swagger-ui.html${NC}"

echo -e "\n${BLUE}🗄️ Database Information:${NC}"
echo -e "PostgreSQL Container: ${BLUE}telepesa-postgres${NC}"
echo -e "Host: ${BLUE}localhost:5432${NC}"
echo -e "Username: ${BLUE}telepesa${NC}"
echo -e "Password: ${BLUE}password${NC}"
echo -e "Databases: ${BLUE}telepesa_accounts_dev, telepesa_transactions_dev, telepesa_loans_dev, telepesa_notifications_dev${NC}"

echo -e "\n${YELLOW}🧪 Ready for comprehensive API testing!${NC}"
echo -e "${YELLOW}💡 Run: ./scripts/comprehensive-api-test.sh${NC}"

echo -e "\n${PURPLE}📋 Useful Commands:${NC}"
echo -e "🔍 View logs: ${BLUE}tail -f logs/*.log${NC}"
echo -e "🛑 Stop all services: ${BLUE}./scripts/stop-all-services.sh${NC}"
echo -e "🧪 Run API tests: ${BLUE}./scripts/comprehensive-api-test.sh${NC}"
echo -e "📊 Check health: ${BLUE}curl http://localhost:8081/actuator/health${NC}"
echo -e "🗄️ Connect to DB: ${BLUE}docker exec -it telepesa-postgres psql -U telepesa -d telepesa${NC}"

echo -e "\n${GREEN}🎉 All services are now running with PostgreSQL!${NC}" 