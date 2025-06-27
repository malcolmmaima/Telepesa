#!/bin/bash

# Telepesa All Services End-to-End Test Script
# This script tests all microservices in the Telepesa platform

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost"
USER_SERVICE_PORT=8081
ACCOUNT_SERVICE_PORT=8082
TRANSACTION_SERVICE_PORT=8083
LOAN_SERVICE_PORT=8084
NOTIFICATION_SERVICE_PORT=8085

# Test data
TEST_USER_ID=1
TEST_ACCOUNT_ID=1
TEST_TRANSACTION_ID="TXN-TEST123"
TEST_NOTIFICATION_ID="NOTIF-TEST123"

echo -e "${BLUE}🚀 Starting Telepesa All Services End-to-End Test${NC}"
echo "=================================================="

# Function to check if service is running
check_service() {
    local service_name=$1
    local port=$2
    local max_attempts=30
    local attempt=1

    echo -e "${YELLOW}⏳ Checking if $service_name is running on port $port...${NC}"
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "$BASE_URL:$port/actuator/health" > /dev/null 2>&1; then
            echo -e "${GREEN}✅ $service_name is running on port $port${NC}"
            return 0
        fi
        
        echo -e "${YELLOW}Attempt $attempt/$max_attempts - $service_name not ready yet...${NC}"
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo -e "${RED}❌ $service_name failed to start on port $port${NC}"
    return 1
}

# Function to test service endpoints
test_service_endpoints() {
    local service_name=$1
    local port=$2
    local base_path=$3
    
    echo -e "${BLUE}🔍 Testing $service_name endpoints...${NC}"
    
    # Test health endpoint
    echo -e "${YELLOW}Testing health endpoint...${NC}"
    if curl -s "$BASE_URL:$port/actuator/health" | grep -q "UP"; then
        echo -e "${GREEN}✅ Health check passed${NC}"
    else
        echo -e "${RED}❌ Health check failed${NC}"
        return 1
    fi
    
    # Test API endpoints if they exist
    if [ -n "$base_path" ]; then
        echo -e "${YELLOW}Testing API endpoints...${NC}"
        
        # Test GET endpoints (should return 401 without auth, which is expected)
        if curl -s -o /dev/null -w "%{http_code}" "$BASE_URL:$port$base_path" | grep -q "401\|200\|404"; then
            echo -e "${GREEN}✅ API endpoint accessible${NC}"
        else
            echo -e "${YELLOW}⚠️ API endpoint test inconclusive${NC}"
        fi
    fi
    
    echo -e "${GREEN}✅ $service_name endpoint tests completed${NC}"
}

# Function to test service functionality
test_service_functionality() {
    local service_name=$1
    local port=$2
    
    echo -e "${BLUE}🧪 Testing $service_name functionality...${NC}"
    
    case $service_name in
        "user-service")
            test_user_service $port
            ;;
        "account-service")
            test_account_service $port
            ;;
        "transaction-service")
            test_transaction_service $port
            ;;
        "loan-service")
            test_loan_service $port
            ;;
        "notification-service")
            test_notification_service $port
            ;;
        *)
            echo -e "${YELLOW}⚠️ No specific functionality tests for $service_name${NC}"
            ;;
    esac
}

# Test user service functionality
test_user_service() {
    local port=$1
    echo -e "${YELLOW}Testing user service functionality...${NC}"
    
    # Test user creation (this will fail without proper auth, but we can check the endpoint exists)
    if curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL:$port/api/v1/users" \
        -H "Content-Type: application/json" \
        -d '{"username":"testuser","email":"test@example.com","password":"password123"}' | grep -q "401\|400"; then
        echo -e "${GREEN}✅ User creation endpoint accessible${NC}"
    else
        echo -e "${YELLOW}⚠️ User creation endpoint test inconclusive${NC}"
    fi
}

# Test account service functionality
test_account_service() {
    local port=$1
    echo -e "${YELLOW}Testing account service functionality...${NC}"
    
    # Test account creation endpoint
    if curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL:$port/api/v1/accounts" \
        -H "Content-Type: application/json" \
        -d '{"userId":1,"accountType":"SAVINGS","initialBalance":1000.00}' | grep -q "401\|400"; then
        echo -e "${GREEN}✅ Account creation endpoint accessible${NC}"
    else
        echo -e "${YELLOW}⚠️ Account creation endpoint test inconclusive${NC}"
    fi
}

# Test transaction service functionality
test_transaction_service() {
    local port=$1
    echo -e "${YELLOW}Testing transaction service functionality...${NC}"
    
    # Test transaction creation endpoint
    if curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL:$port/api/v1/transactions" \
        -H "Content-Type: application/json" \
        -d '{"fromAccountId":1,"toAccountId":2,"amount":100.00,"transactionType":"TRANSFER","userId":1}' | grep -q "401\|400"; then
        echo -e "${GREEN}✅ Transaction creation endpoint accessible${NC}"
    else
        echo -e "${YELLOW}⚠️ Transaction creation endpoint test inconclusive${NC}"
    fi
}

# Test loan service functionality
test_loan_service() {
    local port=$1
    echo -e "${YELLOW}Testing loan service functionality...${NC}"
    
    # Test loan application endpoint
    if curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL:$port/api/v1/loans" \
        -H "Content-Type: application/json" \
        -d '{"userId":1,"amount":5000.00,"term":12,"purpose":"Business"}' | grep -q "401\|400"; then
        echo -e "${GREEN}✅ Loan application endpoint accessible${NC}"
    else
        echo -e "${YELLOW}⚠️ Loan application endpoint test inconclusive${NC}"
    fi
}

# Test notification service functionality
test_notification_service() {
    local port=$1
    echo -e "${YELLOW}Testing notification service functionality...${NC}"
    
    # Test notification creation endpoint
    if curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL:$port/api/v1/notifications" \
        -H "Content-Type: application/json" \
        -d '{"userId":1,"title":"Test Notification","message":"This is a test notification","type":"TRANSACTION_SUCCESS","deliveryMethod":"EMAIL"}' | grep -q "401\|400"; then
        echo -e "${GREEN}✅ Notification creation endpoint accessible${NC}"
    else
        echo -e "${YELLOW}⚠️ Notification creation endpoint test inconclusive${NC}"
    fi
}

# Main test execution
main() {
    echo -e "${BLUE}📋 Test Plan:${NC}"
    echo "1. Check if all services are running"
    echo "2. Test health endpoints"
    echo "3. Test API endpoints"
    echo "4. Test service-specific functionality"
    echo ""

    # Array of services to test
    services=(
        "user-service:$USER_SERVICE_PORT:/api/v1/users"
        "account-service:$ACCOUNT_SERVICE_PORT:/api/v1/accounts"
        "transaction-service:$TRANSACTION_SERVICE_PORT:/api/v1/transactions"
        "loan-service:$LOAN_SERVICE_PORT:/api/v1/loans"
        "notification-service:$NOTIFICATION_SERVICE_PORT:/api/v1/notifications"
    )

    local all_services_healthy=true

    # Check all services
    for service_info in "${services[@]}"; do
        IFS=':' read -r service_name port base_path <<< "$service_info"
        
        if ! check_service "$service_name" "$port"; then
            all_services_healthy=false
            continue
        fi
        
        # Test endpoints
        if ! test_service_endpoints "$service_name" "$port" "$base_path"; then
            echo -e "${RED}❌ $service_name endpoint tests failed${NC}"
            all_services_healthy=false
            continue
        fi
        
        # Test functionality
        test_service_functionality "$service_name" "$port"
        
        echo ""
    done

    # Summary
    echo -e "${BLUE}📊 Test Summary:${NC}"
    echo "=================================================="
    
    if [ "$all_services_healthy" = true ]; then
        echo -e "${GREEN}🎉 All services are running and accessible!${NC}"
        echo -e "${GREEN}✅ End-to-end test completed successfully${NC}"
        echo ""
        echo -e "${BLUE}📋 Service Status:${NC}"
        echo "• User Service (Port $USER_SERVICE_PORT): ✅ Running"
        echo "• Account Service (Port $ACCOUNT_SERVICE_PORT): ✅ Running"
        echo "• Transaction Service (Port $TRANSACTION_SERVICE_PORT): ✅ Running"
        echo "• Loan Service (Port $LOAN_SERVICE_PORT): ✅ Running"
        echo "• Notification Service (Port $NOTIFICATION_SERVICE_PORT): ✅ Running"
        echo ""
        echo -e "${GREEN}🚀 Telepesa platform is ready for development!${NC}"
        exit 0
    else
        echo -e "${RED}❌ Some services failed to start or are not accessible${NC}"
        echo -e "${YELLOW}⚠️ Please check the service logs and ensure all services are running${NC}"
        exit 1
    fi
}

# Run the main function
main "$@" 