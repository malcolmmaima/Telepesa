#!/bin/bash

# Telepesa Fixed Startup Script
# Fixes all startup issues and starts services in correct order

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

echo -e "${BLUE}
╔══════════════════════════════════════════════════════════════════════════════╗
║                                                                              ║
║  ████████╗███████╗██╗     ███████╗██████╗ ███████╗███████╗ █████╗             ║
║  ╚══██╔══╝██╔════╝██║     ██╔════╝██╔══██╗██╔════╝██╔════╝██╔══██╗            ║
║     ██║   █████╗  ██║     █████╗  ██████╔╝█████╗  ███████╗███████║            ║
║     ██║   ██╔══╝  ██║     ██╔══╝  ██╔═══╝ ██╔══╝  ╚════██║██╔══██║            ║
║     ██║   ███████╗███████╗███████╗██║     ███████╗███████║██║  ██║            ║
║     ╚═╝   ╚══════╝╚══════╝╚══════╝╚═╝     ╚══════╝╚══════╝╚═╝  ╚═╝            ║
║                                                                              ║
║                     🏦 Fixed Startup Script 🏦                              ║
║                  🔧 Solving All Startup Issues 🔧                           ║
║                                                                              ║
╚══════════════════════════════════════════════════════════════════════════════╝
${NC}"

echo -e "${PURPLE}[INFO] Starting Telepesa Platform - Fixed Version${NC}"

# Check if we're in the right directory
if [ ! -d "shared-libraries" ]; then
    echo -e "${RED}❌ Error: Please run this script from the Backend directory${NC}"
    echo -e "${YELLOW}💡 Run: cd Backend && ./scripts/fixed-startup.sh${NC}"
    exit 1
fi

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
    local max_attempts=60
    local attempt=1
    
    echo -e "${YELLOW}⏳ Waiting for $service_name to be ready...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s $url > /dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name is ready!${NC}"
            return 0
        fi
        echo -e "${YELLOW}   Attempt $attempt/$max_attempts - waiting 3s...${NC}"
        sleep 3
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}❌ $service_name failed to start within timeout${NC}"
    return 1
}

# Function to start service in background
start_service() {
    local service_name=$1
    local service_dir=$2
    local port=$3
    local health_url=$4
    
    echo -e "${PURPLE}🚀 Starting $service_name...${NC}"
    
    cd "$service_dir"
    
    # Start the service in background
    nohup mvn spring-boot:run -Dspring.profiles.active=dev > ../logs/${service_name}.log 2>&1 &
    local pid=$!
    
    # Store the PID
    echo $pid > ../logs/${service_name}.pid
    
    echo -e "${GREEN}✅ $service_name started with PID: $pid${NC}"
    cd ..
    
    # Wait for service to be ready
    if ! wait_for_service "$health_url" "$service_name"; then
        echo -e "${RED}❌ $service_name failed to start properly${NC}"
        return 1
    fi
    
    return 0
}

# Kill any existing services
echo -e "${PURPLE}🧹 Cleaning up existing processes...${NC}"
pkill -f "spring-boot:run" || true
pkill -f "eureka-server" || true
pkill -f "api-gateway" || true
sleep 3

# Create logs directory
mkdir -p logs

# Step 1: Ensure PostgreSQL and Redis are running
echo -e "${BLUE}📊 Step 1: Verifying Infrastructure Services${NC}"

if ! check_port 5432; then
    echo -e "${YELLOW}⚠️  PostgreSQL not running. Starting it...${NC}"
    cd docker-compose && docker-compose up -d postgres
    sleep 15
    cd ..
else
    echo -e "${GREEN}✅ PostgreSQL is running${NC}"
fi

if ! check_port 6379; then
    echo -e "${YELLOW}⚠️  Redis not running. Starting it...${NC}"
    cd docker-compose && docker-compose up -d redis  
    sleep 10
    cd ..
else
    echo -e "${GREEN}✅ Redis is running${NC}"
fi

# Step 2: Verify database setup
echo -e "${BLUE}📊 Step 2: Verifying Database Setup${NC}"

# Check if telepesa user exists
if ! docker exec telepesa-postgres psql -U telepesa_user -d telepesa_db -c "\du" | grep -q "telepesa"; then
    echo -e "${YELLOW}⚠️  Creating telepesa user...${NC}"
    docker exec telepesa-postgres psql -U telepesa_user -d telepesa_db -c "CREATE USER telepesa WITH PASSWORD 'password';" || true
fi

# Check and create databases
for db in telepesa_users_dev telepesa_accounts_dev telepesa_transactions_dev telepesa_loans_dev telepesa_notifications_dev; do
    if ! docker exec telepesa-postgres psql -U telepesa_user -d telepesa_db -c "\l" | grep -q "$db"; then
        echo -e "${YELLOW}⚠️  Creating database $db...${NC}"
        docker exec telepesa-postgres psql -U telepesa_user -d telepesa_db -c "CREATE DATABASE $db;" || true
        docker exec telepesa-postgres psql -U telepesa_user -d $db -c "GRANT ALL PRIVILEGES ON SCHEMA public TO telepesa;" || true
    fi
done

echo -e "${GREEN}✅ Database setup verified${NC}"

# Step 3: Build shared libraries
echo -e "${BLUE}📊 Step 3: Building Shared Libraries${NC}"
cd shared-libraries
for lib in common-models common-exceptions security-utils; do
    echo -e "${YELLOW}📦 Building $lib...${NC}"
    cd $lib
    mvn clean install -DskipTests -q
    cd ..
done
cd ..
echo -e "${GREEN}✅ Shared libraries built${NC}"

# Step 4: Start services in order
echo -e "${BLUE}📊 Step 4: Starting Services in Order${NC}"

# Start Eureka Server first
if ! start_service "Eureka Server" "eureka-server" "8761" "http://localhost:8761/actuator/health"; then
    echo -e "${RED}❌ Failed to start Eureka Server${NC}"
    exit 1
fi

echo -e "${GREEN}✅ Service Discovery is ready${NC}"

# Start API Gateway second  
if ! start_service "API Gateway" "api-gateway" "8080" "http://localhost:8080/actuator/health"; then
    echo -e "${RED}❌ Failed to start API Gateway${NC}"
    exit 1
fi

echo -e "${GREEN}✅ API Gateway is ready${NC}"

# Start microservices
services=(
    "User Service:user-service:8081:http://localhost:8081/actuator/health"
    "Account Service:account-service:8082:http://localhost:8082/actuator/health"
    "Transaction Service:transaction-service:8083:http://localhost:8083/actuator/health"
    "Loan Service:loan-service:8084:http://localhost:8084/actuator/health"
    "Notification Service:notification-service:8085:http://localhost:8085/actuator/health"
)

for service in "${services[@]}"; do
    IFS=':' read -ra ADDR <<< "$service"
    service_name="${ADDR[0]}"
    service_dir="${ADDR[1]}"
    port="${ADDR[2]}"
    health_url="${ADDR[3]}"
    
    if ! start_service "$service_name" "$service_dir" "$port" "$health_url"; then
        echo -e "${RED}❌ Failed to start $service_name${NC}"
        exit 1
    fi
done

# Step 5: Verify all services through gateway
echo -e "${BLUE}📊 Step 5: Verifying Services Through Gateway${NC}"

# Wait a bit more for services to register with Eureka
echo -e "${YELLOW}⏳ Waiting for service registration...${NC}"
sleep 30

# Check gateway routes
echo -e "${YELLOW}🔍 Checking gateway routes...${NC}"
if curl -s http://localhost:8080/actuator/gateway/routes | grep -q "user-service"; then
    echo -e "${GREEN}✅ Gateway routing is working${NC}"
else
    echo -e "${YELLOW}⚠️  Services still registering...${NC}"
fi

# Final verification
echo -e "${BLUE}📊 Step 6: Final System Verification${NC}"

services_to_check=(
    "Eureka Server:8761:/actuator/health"
    "API Gateway:8080:/actuator/health"
    "User Service:8081:/actuator/health"
    "Account Service:8082:/actuator/health"  
    "Transaction Service:8083:/actuator/health"
    "Loan Service:8084:/actuator/health"
    "Notification Service:8085:/actuator/health"
)

echo -e "${PURPLE}🏥 Health Check Summary:${NC}"
all_healthy=true

for service in "${services_to_check[@]}"; do
    IFS=':' read -ra ADDR <<< "$service"
    service_name="${ADDR[0]}"
    port="${ADDR[1]}"
    endpoint="${ADDR[2]}"
    
    if curl -s "http://localhost:$port$endpoint" | grep -q "UP"; then
        echo -e "${GREEN}✅ $service_name: HEALTHY${NC}"
    else
        echo -e "${RED}❌ $service_name: UNHEALTHY${NC}"
        all_healthy=false
    fi
done

if [ "$all_healthy" = true ]; then
    echo -e "${GREEN}
🎉 SUCCESS! All Telepesa services are running properly!

📊 Service Dashboard:
   • Eureka Server: http://localhost:8761
   • API Gateway: http://localhost:8080/actuator/health
   • User Service: http://localhost:8081/swagger-ui.html
   • Account Service: http://localhost:8082/swagger-ui.html
   • Transaction Service: http://localhost:8083/swagger-ui.html
   • Loan Service: http://localhost:8084/swagger-ui.html
   • Notification Service: http://localhost:8085/swagger-ui.html

🧪 Ready for End-to-End Testing!
   Run: ./scripts/gateway-e2e-test.sh

📋 Useful Commands:
   • View logs: tail -f logs/*.log
   • Stop services: pkill -f spring-boot:run
   • Check routes: curl http://localhost:8080/actuator/gateway/routes
${NC}"
else
    echo -e "${RED}❌ Some services are not healthy. Check logs in logs/ directory.${NC}"
    exit 1
fi 