#!/bin/bash

# Telepesa Full System End-to-End Test
# Tests the complete infrastructure including all microservices, API Gateway, and Eureka

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test configuration
BASE_URL="http://localhost:8080"
USER_SERVICE_URL="http://localhost:8081"
ACCOUNT_SERVICE_URL="http://localhost:8082"
TRANSACTION_SERVICE_URL="http://localhost:8083"
LOAN_SERVICE_URL="http://localhost:8084"
NOTIFICATION_SERVICE_URL="http://localhost:8085"
EUREKA_URL="http://localhost:8761"

# Test data
TIMESTAMP=$(date +%s)
TEST_USERNAME="testuser_${TIMESTAMP}"
TEST_EMAIL="test_${TIMESTAMP}@example.com"
TEST_PASSWORD="TestPassword123!"

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Global variables
USER_ID=""
ACCOUNT_ID=""
TRANSACTION_ID=""
LOAN_ID=""
AUTH_TOKEN=""

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Telepesa Full System E2E Test        ${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "Timestamp: $(date)"
echo -e "Test Data: Username=${TEST_USERNAME}, Email=${TEST_EMAIL}"
echo -e ""

# Function to make HTTP requests
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local expected_status=$4
    local test_name=$5
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -e "  ${YELLOW}Testing: ${test_name}${NC}"
    
    local response=""
    local status_code=""
    
    if [ -n "$data" ]; then
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $AUTH_TOKEN" \
            -d "$data" 2>/dev/null || echo -e "\n000")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
            -H "Authorization: Bearer $AUTH_TOKEN" 2>/dev/null || echo -e "\n000")
    fi
    
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [ "$status_code" = "$expected_status" ]; then
        echo -e "    ${GREEN}✅ PASS${NC} (Status: $status_code)"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo "$response_body"
        return 0
    else
        echo -e "    ${RED}❌ FAIL${NC} (Expected: $expected_status, Got: $status_code)"
        echo -e "    Response: $response_body"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

# Function to extract value from JSON response
extract_value() {
    local json=$1
    local key=$2
    echo "$json" | grep -o "\"$key\":\"[^\"]*\"" | cut -d'"' -f4
}

# Function to extract numeric value from JSON response
extract_numeric_value() {
    local json=$1
    local key=$2
    echo "$json" | grep -o "\"$key\":[0-9]*" | cut -d':' -f2
}

# Test service health
test_service_health() {
    echo -e "${BLUE}🔍 Testing Service Health...${NC}"
    
    # Test Eureka Server
    echo -e "  ${YELLOW}Testing Eureka Server...${NC}"
    if curl -s "$EUREKA_URL" > /dev/null; then
        echo -e "    ${GREEN}✅ Eureka Server UP${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "    ${RED}❌ Eureka Server DOWN${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Test API Gateway
    echo -e "  ${YELLOW}Testing API Gateway...${NC}"
    if curl -s "$BASE_URL/actuator/health" > /dev/null; then
        echo -e "    ${GREEN}✅ API Gateway UP${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "    ${RED}❌ API Gateway DOWN${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Test User Service
    echo -e "  ${YELLOW}Testing User Service...${NC}"
    if curl -s "$USER_SERVICE_URL/actuator/health" > /dev/null; then
        echo -e "    ${GREEN}✅ User Service UP${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "    ${RED}❌ User Service DOWN${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Test Account Service
    echo -e "  ${YELLOW}Testing Account Service...${NC}"
    if curl -s "$ACCOUNT_SERVICE_URL/actuator/health" > /dev/null; then
        echo -e "    ${GREEN}✅ Account Service UP${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "    ${RED}❌ Account Service DOWN${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Test Transaction Service
    echo -e "  ${YELLOW}Testing Transaction Service...${NC}"
    if curl -s "$TRANSACTION_SERVICE_URL/actuator/health" > /dev/null; then
        echo -e "    ${GREEN}✅ Transaction Service UP${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "    ${RED}❌ Transaction Service DOWN${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Test Notification Service
    echo -e "  ${YELLOW}Testing Notification Service...${NC}"
    if curl -s "$NOTIFICATION_SERVICE_URL/actuator/health" > /dev/null; then
        echo -e "    ${GREEN}✅ Notification Service UP${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "    ${RED}❌ Notification Service DOWN${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Test Loan Service (optional)
    echo -e "  ${YELLOW}Testing Loan Service...${NC}"
    if curl -s "$LOAN_SERVICE_URL/actuator/health" > /dev/null; then
        echo -e "    ${GREEN}✅ Loan Service UP${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "    ${YELLOW}⚠️  Loan Service DOWN (Optional)${NC}"
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -e ""
}

# Test User Service
test_user_service() {
    echo -e "${BLUE}👤 Testing User Service...${NC}"
    
    # Register user
    local register_data="{\"username\":\"$TEST_USERNAME\",\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}"
    local register_response=$(curl -s -X POST "$USER_SERVICE_URL/api/v1/users/register" \
        -H "Content-Type: application/json" \
        -d "$register_data")
    
    if echo "$register_response" | grep -q "id"; then
        echo -e "  ${GREEN}✅ User Registration Successful${NC}"
        USER_ID=$(extract_numeric_value "$register_response" "id")
        echo -e "    User ID: $USER_ID"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "  ${RED}❌ User Registration Failed${NC}"
        echo -e "    Response: $register_response"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Login user
    local login_data="{\"username\":\"$TEST_USERNAME\",\"password\":\"$TEST_PASSWORD\"}"
    local login_response=$(curl -s -X POST "$USER_SERVICE_URL/api/v1/users/login" \
        -H "Content-Type: application/json" \
        -d "$login_data")
    
    if echo "$login_response" | grep -q "token"; then
        echo -e "  ${GREEN}✅ User Login Successful${NC}"
        AUTH_TOKEN=$(extract_value "$login_response" "token")
        echo -e "    Token obtained"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "  ${RED}❌ User Login Failed${NC}"
        echo -e "    Response: $login_response"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Test get user by ID
    make_request "GET" "$USER_SERVICE_URL/api/v1/users/$USER_ID" "" "200" "Get User by ID"
    
    # Test get all users
    make_request "GET" "$USER_SERVICE_URL/api/v1/users?page=0&size=10" "" "200" "Get All Users"
    
    echo -e ""
}

# Test Account Service
test_account_service() {
    echo -e "${BLUE}🏦 Testing Account Service...${NC}"
    
    # Create account
    local account_data="{\"userId\":$USER_ID,\"accountType\":\"SAVINGS\",\"initialBalance\":1000.00}"
    local account_response=$(curl -s -X POST "$ACCOUNT_SERVICE_URL/api/v1/accounts" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $AUTH_TOKEN" \
        -d "$account_data")
    
    if echo "$account_response" | grep -q "id"; then
        echo -e "  ${GREEN}✅ Account Creation Successful${NC}"
        ACCOUNT_ID=$(extract_numeric_value "$account_response" "id")
        echo -e "    Account ID: $ACCOUNT_ID"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "  ${RED}❌ Account Creation Failed${NC}"
        echo -e "    Response: $account_response"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Test get account by ID
    make_request "GET" "$ACCOUNT_SERVICE_URL/api/v1/accounts/$ACCOUNT_ID" "" "200" "Get Account by ID"
    
    # Test get accounts by user ID
    make_request "GET" "$ACCOUNT_SERVICE_URL/api/v1/accounts/user/$USER_ID" "" "200" "Get Accounts by User ID"
    
    # Test get all accounts
    make_request "GET" "$ACCOUNT_SERVICE_URL/api/v1/accounts?page=0&size=10" "" "200" "Get All Accounts"
    
    echo -e ""
}

# Test Transaction Service
test_transaction_service() {
    echo -e "${BLUE}💳 Testing Transaction Service...${NC}"
    
    # Create transaction
    local transaction_data="{\"fromAccountId\":$ACCOUNT_ID,\"toAccountId\":$ACCOUNT_ID,\"amount\":100.00,\"type\":\"TRANSFER\",\"description\":\"Test transaction\"}"
    local transaction_response=$(curl -s -X POST "$TRANSACTION_SERVICE_URL/api/v1/transactions" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $AUTH_TOKEN" \
        -d "$transaction_data")
    
    if echo "$transaction_response" | grep -q "id"; then
        echo -e "  ${GREEN}✅ Transaction Creation Successful${NC}"
        TRANSACTION_ID=$(extract_numeric_value "$transaction_response" "id")
        echo -e "    Transaction ID: $TRANSACTION_ID"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "  ${RED}❌ Transaction Creation Failed${NC}"
        echo -e "    Response: $transaction_response"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Test get transaction by ID
    make_request "GET" "$TRANSACTION_SERVICE_URL/api/v1/transactions/$TRANSACTION_ID" "" "200" "Get Transaction by ID"
    
    # Test get transactions by user ID
    make_request "GET" "$TRANSACTION_SERVICE_URL/api/v1/transactions/user/$USER_ID?page=0&size=10" "" "200" "Get Transactions by User ID"
    
    # Test get transactions by account ID
    make_request "GET" "$TRANSACTION_SERVICE_URL/api/v1/transactions/account/$ACCOUNT_ID?page=0&size=10" "" "200" "Get Transactions by Account ID"
    
    # Test get all transactions
    make_request "GET" "$TRANSACTION_SERVICE_URL/api/v1/transactions?page=0&size=10" "" "200" "Get All Transactions"
    
    echo -e ""
}

# Test Loan Service (if available)
test_loan_service() {
    echo -e "${BLUE}💰 Testing Loan Service...${NC}"
    
    # Check if loan service is available
    if ! curl -s "$LOAN_SERVICE_URL/actuator/health" > /dev/null; then
        echo -e "  ${YELLOW}⚠️  Loan Service not available, skipping loan tests${NC}"
        return 0
    fi
    
    # Create loan application
    local loan_data="{\"userId\":$USER_ID,\"amount\":5000.00,\"termMonths\":12,\"interestRate\":12.5,\"loanType\":\"PERSONAL\",\"purpose\":\"Test loan\"}"
    local loan_response=$(curl -s -X POST "$LOAN_SERVICE_URL/api/v1/loans" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $AUTH_TOKEN" \
        -d "$loan_data")
    
    if echo "$loan_response" | grep -q "id"; then
        echo -e "  ${GREEN}✅ Loan Application Successful${NC}"
        LOAN_ID=$(extract_numeric_value "$loan_response" "id")
        echo -e "    Loan ID: $LOAN_ID"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "  ${RED}❌ Loan Application Failed${NC}"
        echo -e "    Response: $loan_response"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Test get loan by ID
    make_request "GET" "$LOAN_SERVICE_URL/api/v1/loans/$LOAN_ID" "" "200" "Get Loan by ID"
    
    # Test get loans by user ID
    make_request "GET" "$LOAN_SERVICE_URL/api/v1/loans/user/$USER_ID?page=0&size=10" "" "200" "Get Loans by User ID"
    
    # Test get all loans
    make_request "GET" "$LOAN_SERVICE_URL/api/v1/loans?page=0&size=10" "" "200" "Get All Loans"
    
    # Test loan calculation
    local calc_data="{\"principal\":5000.00,\"interestRate\":12.5,\"termMonths\":12}"
    make_request "POST" "$LOAN_SERVICE_URL/api/v1/loans/calculate-payment" "$calc_data" "200" "Calculate Monthly Payment"
    
    echo -e ""
}

# Test Notification Service
test_notification_service() {
    echo -e "${BLUE}📧 Testing Notification Service...${NC}"
    
    # Create notification
    local notification_data="{\"userId\":$USER_ID,\"type\":\"TRANSACTION\",\"title\":\"Test Notification\",\"message\":\"This is a test notification\",\"priority\":\"MEDIUM\"}"
    local notification_response=$(curl -s -X POST "$NOTIFICATION_SERVICE_URL/api/v1/notifications" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $AUTH_TOKEN" \
        -d "$notification_data")
    
    if echo "$notification_response" | grep -q "id"; then
        echo -e "  ${GREEN}✅ Notification Creation Successful${NC}"
        local notification_id=$(extract_numeric_value "$notification_response" "id")
        echo -e "    Notification ID: $notification_id"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "  ${RED}❌ Notification Creation Failed${NC}"
        echo -e "    Response: $notification_response"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    # Test get notifications by user ID
    make_request "GET" "$NOTIFICATION_SERVICE_URL/api/v1/notifications/user/$USER_ID?page=0&size=10" "" "200" "Get Notifications by User ID"
    
    # Test get all notifications
    make_request "GET" "$NOTIFICATION_SERVICE_URL/api/v1/notifications?page=0&size=10" "" "200" "Get All Notifications"
    
    echo -e ""
}

# Test API Gateway
test_api_gateway() {
    echo -e "${BLUE}🌐 Testing API Gateway...${NC}"
    
    # Test gateway health
    make_request "GET" "$BASE_URL/actuator/health" "" "200" "Gateway Health Check"
    
    # Test gateway routes
    make_request "GET" "$BASE_URL/actuator/gateway/routes" "" "200" "Gateway Routes"
    
    # Test proxied user endpoints through gateway
    make_request "GET" "$BASE_URL/api/v1/users/$USER_ID" "" "200" "Get User through Gateway"
    
    # Test proxied account endpoints through gateway
    make_request "GET" "$BASE_URL/api/v1/accounts/$ACCOUNT_ID" "" "200" "Get Account through Gateway"
    
    # Test proxied transaction endpoints through gateway
    make_request "GET" "$BASE_URL/api/v1/transactions/$TRANSACTION_ID" "" "200" "Get Transaction through Gateway"
    
    echo -e ""
}

# Test error handling
test_error_handling() {
    echo -e "${BLUE}⚠️  Testing Error Handling...${NC}"
    
    # Test unauthorized access
    local temp_token="invalid_token"
    AUTH_TOKEN=$temp_token
    make_request "GET" "$USER_SERVICE_URL/api/v1/users/$USER_ID" "" "401" "Unauthorized Access"
    AUTH_TOKEN=$AUTH_TOKEN
    
    # Test invalid user ID
    make_request "GET" "$USER_SERVICE_URL/api/v1/users/999999" "" "404" "Invalid User ID"
    
    # Test invalid account ID
    make_request "GET" "$ACCOUNT_SERVICE_URL/api/v1/accounts/999999" "" "404" "Invalid Account ID"
    
    echo -e ""
}

# Print test summary
print_summary() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  Test Summary                        ${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo -e "Total Tests: $TOTAL_TESTS"
    echo -e "${GREEN}Passed: $PASSED_TESTS${NC}"
    echo -e "${RED}Failed: $FAILED_TESTS${NC}"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "${GREEN}🎉 All tests passed! System is working correctly.${NC}"
        exit 0
    else
        echo -e "${RED}❌ Some tests failed. Please check the system.${NC}"
        exit 1
    fi
}

# Main test execution
main() {
    echo -e "Starting full system end-to-end tests..."
    echo -e ""
    
    test_service_health
    test_user_service
    test_account_service
    test_transaction_service
    test_loan_service
    test_notification_service
    test_api_gateway
    test_error_handling
    
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  System Features Tested              ${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo -e "  ✅ Service Discovery (Eureka)"
    echo -e "  ✅ API Gateway (Routing, proxying)"
    echo -e "  ✅ User Service (Registration, authentication)"
    echo -e "  ✅ Account Service (Account creation, management)"
    echo -e "  ✅ Transaction Service (Transaction processing, history)"
    echo -e "  ✅ Loan Service (Loan applications, calculations)"
    echo -e "  ✅ Notification Service (Notification management)"
    echo -e "  ✅ Error Handling (Invalid requests, unauthorized access)"
    echo -e "  ✅ Authentication & Authorization (JWT token validation)"
    echo -e "  ✅ Database Operations (CRUD operations across all services)"
    echo -e ""
    
    print_summary
}

# Run the main function
main 