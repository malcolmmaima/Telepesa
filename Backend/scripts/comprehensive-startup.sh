#!/bin/bash

# Telepesa Comprehensive Startup Script
# Starts all microservices in correct order with health checks

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Configuration
EUREKA_PORT=8761
GATEWAY_PORT=8080
USER_SERVICE_PORT=8081
ACCOUNT_SERVICE_PORT=8082
TRANSACTION_SERVICE_PORT=8083
LOAN_SERVICE_PORT=8084
NOTIFICATION_SERVICE_PORT=8085

STARTUP_TIMEOUT=90
HEALTH_CHECK_INTERVAL=5

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
║                🏦 Comprehensive Microservices Startup 🏦                     ║
║                     🚀 Starting All Services 🚀                             ║
║                                                                              ║
╚══════════════════════════════════════════════════════════════════════════════╝${NC}
"

# Function to check if a service is healthy
check_service_health() {
    local service_name=$1
    local port=$2
    local max_attempts=$((STARTUP_TIMEOUT / HEALTH_CHECK_INTERVAL))
    local attempts=0
    
    echo -e "${YELLOW}[INFO] Checking health of $service_name on port $port...${NC}"
    
    while [ $attempts -lt $max_attempts ]; do
        if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            echo -e "${GREEN}[SUCCESS] ✅ $service_name is healthy!${NC}"
            return 0
        fi
        
        attempts=$((attempts + 1))
        echo -e "${YELLOW}[INFO] Waiting for $service_name... (attempt $attempts/$max_attempts)${NC}"
        sleep $HEALTH_CHECK_INTERVAL
    done
    
    echo -e "${RED}[ERROR] ❌ $service_name failed to start within $STARTUP_TIMEOUT seconds${NC}"
    return 1
}

# Function to start a service
start_service() {
    local service_name=$1
    local port=$2
    
    echo -e "${BLUE}[INFO] 🚀 Starting $service_name...${NC}"
    
    # Determine the correct relative path for each service (from Backend/scripts/)
    case "$service_name" in
        "Eureka-Server")
            service_dir="../eureka-server" ;;
        "API-Gateway")
            service_dir="../api-gateway" ;;
        "User-Service")
            service_dir="../user-service" ;;
        "Account-Service")
            service_dir="../account-service" ;;
        "Transaction-Service")
            service_dir="../transaction-service" ;;
        "Loan-Service")
            service_dir="../loan-service" ;;
        "Notification-Service")
            service_dir="../notification-service" ;;
        *)
            echo "[ERROR] Unknown service: $service_name" ; return 1 ;;
    esac

    cd "$service_dir" || { echo "[ERROR] Could not cd to $service_dir"; return 1; }
    
    # Kill any existing process on the port
    if lsof -ti:$port > /dev/null 2>&1; then
        echo -e "${YELLOW}[INFO] Killing existing process on port $port${NC}"
        kill -9 $(lsof -ti:$port) 2>/dev/null || true
        sleep 2
    fi
    
    # Start the service in background
    log_name=$(echo "$service_name" | tr '[:upper:]' '[:lower:]')
    nohup mvn spring-boot:run -Dspring.profiles.active=dev > "../logs/${log_name}.log" 2>&1 &
    local pid=$!
    echo "$pid" > "../logs/${log_name}.pid"
    cd - > /dev/null
    
    echo -e "${GREEN}[INFO] Started $service_name with PID $pid${NC}"
    
    # Wait a moment for the process to initialize
    sleep 10
    
    # Check if the service is healthy
    if check_service_health "$service_name" "$port"; then
        echo -e "${GREEN}[SUCCESS] ✅ $service_name is running successfully${NC}"
        return 0
    else
        echo -e "${RED}[ERROR] ❌ $service_name failed to start properly${NC}"
        return 1
    fi
}

# Create logs directory
mkdir -p logs

echo -e "${PURPLE}[INFO] 📋 Starting Telepesa Microservices Platform...${NC}"

# Step 1: Check Docker containers
echo -e "${BLUE}[INFO] 🐳 Checking Docker infrastructure...${NC}"
if ! docker ps | grep -q telepesa-postgres; then
    echo -e "${RED}[ERROR] ❌ PostgreSQL container not running. Please start it first.${NC}"
    exit 1
fi

if ! docker ps | grep -q telepesa-redis; then
    echo -e "${RED}[ERROR] ❌ Redis container not running. Please start it first.${NC}"
    exit 1
fi

echo -e "${GREEN}[SUCCESS] ✅ Docker infrastructure is ready${NC}"

# Step 2: Build shared libraries
echo -e "${BLUE}[INFO] Building Telepesa Shared Libraries...${NC}"
pushd ../shared-libraries > /dev/null
chmod +x build-shared-libs.sh
if ./build-shared-libs.sh; then
    echo -e "${GREEN}[SUCCESS] ✅ Shared libraries built successfully${NC}"
else
    echo -e "${RED}[ERROR] ❌ Failed to build shared libraries${NC}"
    popd > /dev/null
    exit 1
fi
popd > /dev/null

# Step 3: Start Eureka Server (Service Discovery)
if ! start_service "Eureka-Server" $EUREKA_PORT; then
    echo -e "${RED}[FATAL] ❌ Eureka Server failed to start. Cannot continue.${NC}"
    exit 1
fi

# Step 4: Start API Gateway
if ! start_service "API-Gateway" $GATEWAY_PORT; then
    echo -e "${RED}[FATAL] ❌ API Gateway failed to start. Cannot continue.${NC}"
    exit 1
fi

# Step 5: Start Core Services in parallel
echo -e "${PURPLE}[INFO] 🔄 Starting core microservices...${NC}"

# Start User Service
start_service "User-Service" $USER_SERVICE_PORT &
USER_PID=$!

# Start Account Service
start_service "Account-Service" $ACCOUNT_SERVICE_PORT &
ACCOUNT_PID=$!

# Start Transaction Service
start_service "Transaction-Service" $TRANSACTION_SERVICE_PORT &
TRANSACTION_PID=$!

# Start Loan Service
start_service "Loan-Service" $LOAN_SERVICE_PORT &
LOAN_PID=$!

# Start Notification Service
start_service "Notification-Service" $NOTIFICATION_SERVICE_PORT &
NOTIFICATION_PID=$!

# Wait for all services to complete
wait $USER_PID
USER_RESULT=$?

wait $ACCOUNT_PID
ACCOUNT_RESULT=$?

wait $TRANSACTION_PID
TRANSACTION_RESULT=$?

wait $LOAN_PID
LOAN_RESULT=$?

wait $NOTIFICATION_PID
NOTIFICATION_RESULT=$?

# Check results
FAILED_SERVICES=""
if [ $USER_RESULT -ne 0 ]; then FAILED_SERVICES="$FAILED_SERVICES User-Service"; fi
if [ $ACCOUNT_RESULT -ne 0 ]; then FAILED_SERVICES="$FAILED_SERVICES Account-Service"; fi
if [ $TRANSACTION_RESULT -ne 0 ]; then FAILED_SERVICES="$FAILED_SERVICES Transaction-Service"; fi
if [ $LOAN_RESULT -ne 0 ]; then FAILED_SERVICES="$FAILED_SERVICES Loan-Service"; fi
if [ $NOTIFICATION_RESULT -ne 0 ]; then FAILED_SERVICES="$FAILED_SERVICES Notification-Service"; fi

if [ -n "$FAILED_SERVICES" ]; then
    echo -e "${RED}[WARNING] ⚠️  Some services failed to start:$FAILED_SERVICES${NC}"
    echo -e "${YELLOW}[INFO] Check individual service logs in the logs/ directory${NC}"
else
    echo -e "${GREEN}[SUCCESS] 🎉 All microservices started successfully!${NC}"
fi

# Step 6: Final health check
echo -e "${BLUE}[INFO] 🏥 Performing final health checks...${NC}"
sleep 10

echo -e "${PURPLE}
┌─────────────────────────────────────────────────────────────────────────────┐
│                           📊 SERVICE STATUS REPORT                          │
└─────────────────────────────────────────────────────────────────────────────┘${NC}"

# Check each service
services=(
    "Eureka-Server:$EUREKA_PORT"
    "API-Gateway:$GATEWAY_PORT"
    "User-Service:$USER_SERVICE_PORT"
    "Account-Service:$ACCOUNT_SERVICE_PORT"
    "Transaction-Service:$TRANSACTION_SERVICE_PORT"
    "Loan-Service:$LOAN_SERVICE_PORT"
    "Notification-Service:$NOTIFICATION_SERVICE_PORT"
)

for service_info in "${services[@]}"; do
    IFS=':' read -r service_name port <<< "$service_info"
    if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
        status="${GREEN}✅ RUNNING${NC}"
    else
        status="${RED}❌ DOWN${NC}"
    fi
    printf "│ %-20s │ Port %-5s │ %s │\n" "$service_name" "$port" "$status"
done

echo -e "${PURPLE}└─────────────────────────────────────────────────────────────────────────────┘${NC}"

echo -e "${BLUE}
🔗 Quick Access URLs:
   • Eureka Dashboard: http://localhost:8761
   • API Gateway: http://localhost:8080
   • Gateway Health: http://localhost:8080/actuator/health
   • User Service: http://localhost:8081
   • Account Service: http://localhost:8082
   • Transaction Service: http://localhost:8083
   • Loan Service: http://localhost:8084
   • Notification Service: http://localhost:8085

📁 Logs Location: ./logs/
📋 To stop all services: pkill -f spring-boot:run

${GREEN}🎉 Telepesa Platform Startup Complete! 🎉${NC}
" 