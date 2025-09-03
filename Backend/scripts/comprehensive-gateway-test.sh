#!/bin/bash

# Comprehensive API Gateway Test Script
# Tests all routes, advanced features, and service integrations

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
GATEWAY_URL="http://localhost:8080"
EUREKA_URL="http://localhost:8761"
USER_SERVICE_URL="http://localhost:8081"
ACCOUNT_SERVICE_URL="http://localhost:8082"
TRANSACTION_SERVICE_URL="http://localhost:8083"
LOAN_SERVICE_URL="http://localhost:8084"
NOTIFICATION_SERVICE_URL="http://localhost:8085"

echo -e "${BLUE}🚀 Telepesa API Gateway Comprehensive Test${NC}"
echo "=================================================="
echo

# Function to print test results
print_result() {
    local test_name="$1"
    local status="$2"
    local message="$3"
    
    if [ "$status" = "PASS" ]; then
        echo -e "✅ ${GREEN}$test_name${NC}: $message"
    else
        echo -e "❌ ${RED}$test_name${NC}: $message"
    fi
}

# Function to check if service is running
check_service() {
    local service_name="$1"
    local service_url="$2"
    
    if curl -s "$service_url/actuator/health" > /dev/null 2>&1; then
        print_result "$service_name Health Check" "PASS" "Service is running"
        return 0
    else
        print_result "$service_name Health Check" "FAIL" "Service is not responding"
        return 1
    fi
}

# Function to test gateway route
test_gateway_route() {
    local route_name="$1"
    local route_path="$2"
    local expected_status="$3"
    
    local response=$(curl -s -o /dev/null -w "%{http_code}" "$GATEWAY_URL$route_path")
    
    if [ "$response" = "$expected_status" ]; then
        print_result "Gateway Route: $route_name" "PASS" "HTTP $response"
    else
        print_result "Gateway Route: $route_name" "FAIL" "Expected $expected_status, got $response"
    fi
}

# Function to test service registration
test_service_registration() {
    local service_name="$1"
    
    if curl -s "$EUREKA_URL/eureka/apps/$service_name" | grep -q "UP"; then
        print_result "Service Registration: $service_name" "PASS" "Registered with Eureka"
    else
        print_result "Service Registration: $service_name" "FAIL" "Not registered or not UP"
    fi
}

echo -e "${YELLOW}📋 Phase 1: Service Health Checks${NC}"
echo "----------------------------------------"

# Check all services
check_service "Eureka Server" "$EUREKA_URL"
check_service "API Gateway" "$GATEWAY_URL/actuator/health"
check_service "User Service" "$USER_SERVICE_URL/actuator/health"
check_service "Account Service" "$ACCOUNT_SERVICE_URL/actuator/health"
check_service "Transaction Service" "$TRANSACTION_SERVICE_URL/actuator/health"
check_service "Loan Service" "$LOAN_SERVICE_URL/actuator/health"
check_service "Notification Service" "$NOTIFICATION_SERVICE_URL/actuator/health"

echo
echo -e "${YELLOW}📋 Phase 2: Service Discovery${NC}"
echo "----------------------------------------"

# Test service registration with Eureka
test_service_registration "API-GATEWAY"
test_service_registration "USER-SERVICE"
test_service_registration "ACCOUNT-SERVICE"
test_service_registration "TRANSACTION-SERVICE"
test_service_registration "LOAN-SERVICE"
test_service_registration "NOTIFICATION-SERVICE"

echo
echo -e "${YELLOW}📋 Phase 3: Gateway Routes Testing${NC}"
echo "----------------------------------------"

# Test public routes (should work without authentication)
test_gateway_route "Health Check" "/api/v1/health" "200"
test_gateway_route "API Documentation" "/api/v1/docs" "200"
test_gateway_route "User Service Swagger" "/api/v1/docs/user-service" "200"
test_gateway_route "Account Service Swagger" "/api/v1/docs/account-service" "200"
test_gateway_route "Transaction Service Swagger" "/api/v1/docs/transaction-service" "200"
test_gateway_route "Loan Service Swagger" "/api/v1/docs/loan-service" "200"
test_gateway_route "Notification Service Swagger" "/api/v1/docs/notification-service" "200"

# Test authentication endpoints (should work without authentication)
test_gateway_route "User Registration" "/api/v1/users/register" "405"  # Method not allowed for GET
test_gateway_route "User Login" "/api/v1/users/login" "405"  # Method not allowed for GET

# Test protected routes (should return 401 without authentication)
test_gateway_route "User Service Protected" "/api/v1/users" "401"
test_gateway_route "Account Service Protected" "/api/v1/accounts" "401"
test_gateway_route "Transaction Service Protected" "/api/v1/transactions" "401"
test_gateway_route "Loan Service Protected" "/api/v1/loans" "401"
test_gateway_route "Notification Service Protected" "/api/v1/notifications" "401"

echo
echo -e "${YELLOW}📋 Phase 4: Advanced Features Testing${NC}"
echo "----------------------------------------"

# Test CORS headers
echo -e "${BLUE}Testing CORS Configuration...${NC}"
cors_response=$(curl -s -I -H "Origin: http://localhost:3000" "$GATEWAY_URL/api/v1/health" | grep -i "access-control-allow-origin")
if [ -n "$cors_response" ]; then
    print_result "CORS Configuration" "PASS" "CORS headers present"
else
    print_result "CORS Configuration" "FAIL" "CORS headers missing"
fi

# Test rate limiting (make multiple requests quickly)
echo -e "${BLUE}Testing Rate Limiting...${NC}"
for i in {1..5}; do
    response=$(curl -s -o /dev/null -w "%{http_code}" "$GATEWAY_URL/api/v1/health")
    if [ "$response" = "429" ]; then
        print_result "Rate Limiting" "PASS" "Rate limit enforced on request $i"
        break
    fi
    sleep 0.1
done

# Test circuit breaker fallback
echo -e "${BLUE}Testing Circuit Breaker Fallback...${NC}"
fallback_response=$(curl -s "$GATEWAY_URL/fallback/user-service")
if echo "$fallback_response" | grep -q "SERVICE_UNAVAILABLE"; then
    print_result "Circuit Breaker Fallback" "PASS" "Fallback response generated"
else
    print_result "Circuit Breaker Fallback" "FAIL" "Fallback not working"
fi

echo
echo -e "${YELLOW}📋 Phase 5: End-to-End Authentication Flow${NC}"
echo "----------------------------------------"

# Test user registration
echo -e "${BLUE}Testing User Registration...${NC}"
register_response=$(curl -s -X POST "$GATEWAY_URL/api/v1/users/register" \
    -H "Content-Type: application/json" \
    -d '{
        "username": "gatewaytestuser",
        "email": "gatewaytest@example.com",
        "password": "TestPassword123!",
        "firstName": "Gateway",
        "lastName": "Test",
        "phoneNumber": "+254700000000"
    }')

if echo "$register_response" | grep -q "id"; then
    print_result "User Registration via Gateway" "PASS" "User created successfully"
    USER_ID=$(echo "$register_response" | grep -o '"id":[0-9]*' | cut -d':' -f2)
else
    print_result "User Registration via Gateway" "FAIL" "Registration failed"
    echo "Response: $register_response"
fi

# Test user login
echo -e "${BLUE}Testing User Login...${NC}"
login_response=$(curl -s -X POST "$GATEWAY_URL/api/v1/users/login" \
    -H "Content-Type: application/json" \
    -d '{
        "usernameOrEmail": "gatewaytestuser",
        "password": "TestPassword123!"
    }')

if echo "$login_response" | grep -q "token"; then
    print_result "User Login via Gateway" "PASS" "Login successful"
    TOKEN=$(echo "$login_response" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
else
    print_result "User Login via Gateway" "FAIL" "Login failed"
    echo "Response: $login_response"
fi

# Test authenticated requests
if [ -n "$TOKEN" ]; then
    echo -e "${BLUE}Testing Authenticated Requests...${NC}"
    
    # Test user profile access
    profile_response=$(curl -s -H "Authorization: Bearer $TOKEN" "$GATEWAY_URL/api/v1/users/profile")
    if echo "$profile_response" | grep -q "username"; then
        print_result "Authenticated User Profile" "PASS" "Profile accessed successfully"
    else
        print_result "Authenticated User Profile" "FAIL" "Profile access failed"
    fi
    
    # Test account creation
    account_response=$(curl -s -X POST "$GATEWAY_URL/api/v1/accounts" \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{
            "accountType": "SAVINGS",
            "currency": "KES",
            "initialBalance": 1000.00
        }')
    
    if echo "$account_response" | grep -q "accountNumber"; then
        print_result "Account Creation via Gateway" "PASS" "Account created successfully"
    else
        print_result "Account Creation via Gateway" "FAIL" "Account creation failed"
    fi
fi

echo
echo -e "${YELLOW}📋 Phase 6: Gateway Metrics and Monitoring${NC}"
echo "----------------------------------------"

# Test actuator endpoints
test_gateway_route "Gateway Health" "/actuator/health" "200"
test_gateway_route "Gateway Info" "/actuator/info" "200"
test_gateway_route "Gateway Routes" "/actuator/gateway/routes" "200"
test_gateway_route "Gateway Metrics" "/actuator/metrics" "200"

echo
echo -e "${YELLOW}📋 Phase 7: Service-Specific Route Testing${NC}"
echo "----------------------------------------"

# Test each service's specific endpoints
if [ -n "$TOKEN" ]; then
    echo -e "${BLUE}Testing Service-Specific Endpoints...${NC}"
    
    # User Service endpoints
    test_gateway_route "User List" "/api/v1/users" "200"
    
    # Account Service endpoints
    test_gateway_route "Account List" "/api/v1/accounts" "200"
    
    # Transaction Service endpoints
    test_gateway_route "Transaction List" "/api/v1/transactions" "200"
    
    # Loan Service endpoints
    test_gateway_route "Loan List" "/api/v1/loans" "200"
    
    # Notification Service endpoints
    test_gateway_route "Notification List" "/api/v1/notifications" "200"
fi

echo
echo -e "${GREEN}🎉 Comprehensive Gateway Test Complete!${NC}"
echo "=================================================="
echo
echo -e "${BLUE}📊 Summary:${NC}"
echo "- All services should be running and healthy"
echo "- All routes should be properly configured"
echo "- Advanced features (CORS, Rate Limiting, Circuit Breaker) should be working"
echo "- Authentication flow should work end-to-end"
echo "- Service discovery should be functional"
echo
echo -e "${BLUE}🔗 Useful URLs:${NC}"
echo "- Eureka Dashboard: $EUREKA_URL"
echo "- API Gateway Health: $GATEWAY_URL/actuator/health"
echo "- Gateway Routes: $GATEWAY_URL/actuator/gateway/routes"
echo "- User Service Docs: $GATEWAY_URL/api/v1/docs/user-service"
echo "- Account Service Docs: $GATEWAY_URL/api/v1/docs/account-service"
echo "- Transaction Service Docs: $GATEWAY_URL/api/v1/docs/transaction-service"
echo "- Loan Service Docs: $GATEWAY_URL/api/v1/docs/loan-service"
echo "- Notification Service Docs: $GATEWAY_URL/api/v1/docs/notification-service" 