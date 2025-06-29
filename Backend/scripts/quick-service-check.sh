#!/bin/bash

# Quick Service Health Check Script
# Verifies all Telepesa services are running before comprehensive testing

set -e

# Service URLs
USER_SERVICE_URL="http://localhost:8081"
ACCOUNT_SERVICE_URL="http://localhost:8082"
TRANSACTION_SERVICE_URL="http://localhost:8083"
LOAN_SERVICE_URL="http://localhost:8084"
NOTIFICATION_SERVICE_URL="http://localhost:8085"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}🔍 Telepesa Service Health Check${NC}"
echo -e "${BLUE}Verifying all services are running...${NC}\n"

# Function to check service health
check_service() {
    local service_name=$1
    local service_url=$2
    local endpoint=$3
    
    echo -e "${YELLOW}Checking $service_name...${NC}"
    
    # Try to connect with timeout and capture both response and HTTP code
    local temp_file=$(mktemp)
    local http_code=$(curl -s -m 5 -w "%{http_code}" -o "$temp_file" "$service_url$endpoint" 2>/dev/null || echo "FAILED")
    local body=$(cat "$temp_file" 2>/dev/null || echo "")
    rm -f "$temp_file"
    
    if [[ $http_code == "200" ]] || [[ $body == *"UP"* ]] || [[ $body == *"status"* ]]; then
        echo -e "${GREEN}✅ $service_name: RUNNING (HTTP: $http_code)${NC}"
        return 0
    else
        echo -e "${RED}❌ $service_name: NOT RUNNING (HTTP: $http_code)${NC}"
        if [[ -n "$body" ]]; then
            echo "   Response: $body"
        fi
        return 1
    fi
}

# Check all services
services_status=0

check_service "User Service" "$USER_SERVICE_URL" "/actuator/health" || services_status=1
check_service "Account Service" "$ACCOUNT_SERVICE_URL" "/actuator/health" || services_status=1
check_service "Transaction Service" "$TRANSACTION_SERVICE_URL" "/actuator/health" || services_status=1
check_service "Loan Service" "$LOAN_SERVICE_URL" "/actuator/health" || services_status=1
check_service "Notification Service" "$NOTIFICATION_SERVICE_URL" "/actuator/health" || services_status=1

echo ""

if [[ $services_status -eq 0 ]]; then
    echo -e "${GREEN}🎉 All services are running! Ready for comprehensive testing.${NC}"
    echo -e "${YELLOW}💡 Run: ./scripts/comprehensive-api-test.sh${NC}"
    exit 0
else
    echo -e "${RED}❌ Some services are not running. Please start all services first.${NC}"
    echo -e "${YELLOW}💡 Start services with:${NC}"
    echo -e "   cd Backend/user-service && mvn spring-boot:run -Dspring.profiles.active=dev &"
    echo -e "   cd Backend/account-service && mvn spring-boot:run -Dspring.profiles.active=dev &"
    echo -e "   cd Backend/transaction-service && mvn spring-boot:run -Dspring.profiles.active=dev &"
    echo -e "   cd Backend/loan-service && mvn spring-boot:run -Dspring.profiles.active=dev &"
    echo -e "   cd Backend/notification-service && mvn spring-boot:run -Dspring.profiles.active=dev &"
    exit 1
fi 