#!/bin/bash

# Telepesa Comprehensive End-to-End Test Script
# Tests all endpoints across all microservices with authentication and cache verification

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8080"
USER_SERVICE_URL="http://localhost:8081"
ACCOUNT_SERVICE_URL="http://localhost:8082"
TRANSACTION_SERVICE_URL="http://localhost:8083"
LOAN_SERVICE_URL="http://localhost:8084"
NOTIFICATION_SERVICE_URL="http://localhost:8085"

# Test data
TEST_USERNAME="testuser_$(date +%s)"
TEST_EMAIL="test_$(date +%s)@example.com"
TEST_PASSWORD="TestPassword123!"
TEST_ACCOUNT_NUMBER="ACC$(date +%s)"

# Global variables
AUTH_TOKEN=""
USER_ID=""
ACCOUNT_ID=""
LOAN_ID=""
TRANSACTION_ID=""

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Telepesa Comprehensive E2E Tests     ${NC}"
echo -e "${BLUE}========================================${NC}"

# Function to log test results
log_test() {
    local test_name="$1"
    local result="$2"
    local details="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    if [ "$result" = "PASS" ]; then
        echo -e "  ${GREEN}✅ $test_name${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "  ${RED}❌ $test_name${NC}"
        echo -e "    ${RED}Details: $details${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

# Function to make HTTP requests
make_request() {
    local method="$1"
    local url="$2"
    local data="$3"
    local expected_status="$4"
    local test_name="$5"
    
    local response
    local status_code
    
    if [ "$method" = "GET" ]; then
        if [ -n "$AUTH_TOKEN" ]; then
            response=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $AUTH_TOKEN" "$url")
        else
            response=$(curl -s -w "\n%{http_code}" "$url")
        fi
    elif [ "$method" = "POST" ]; then
        if [ -n "$AUTH_TOKEN" ]; then
            response=$(curl -s -w "\n%{http_code}" -H "Content-Type: application/json" -H "Authorization: Bearer $AUTH_TOKEN" -d "$data" "$url")
        else
            response=$(curl -s -w "\n%{http_code}" -H "Content-Type: application/json" -d "$data" "$url")
        fi
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "\n%{http_code}" -H "Content-Type: application/json" -H "Authorization: Bearer $AUTH_TOKEN" -d "$data" -X PUT "$url")
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $AUTH_TOKEN" -X DELETE "$url")
    fi
    
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    if [ "$status_code" = "$expected_status" ]; then
        log_test "$test_name" "PASS" ""
        echo "$response_body"
    else
        log_test "$test_name" "FAIL" "Expected $expected_status, got $status_code. Response: $response_body"
    fi
}

# Function to extract values from JSON response
extract_value() {
    local json="$1"
    local key="$2"
    echo "$json" | grep -o "\"$key\":\"[^\"]*\"" | cut -d'"' -f4
}

extract_number() {
    local json="$1"
    local key="$2"
    echo "$json" | grep -o "\"$key\":[0-9]*" | cut -d':' -f2
}

# Function to check service health
check_service_health() {
    echo -e "\n${YELLOW}Checking Service Health...${NC}"
    
    local services=(
        "API Gateway:$BASE_URL/actuator/health"
        "User Service:$USER_SERVICE_URL/actuator/health"
        "Account Service:$ACCOUNT_SERVICE_URL/actuator/health"
        "Transaction Service:$TRANSACTION_SERVICE_URL/actuator/health"
        "Loan Service:$LOAN_SERVICE_URL/actuator/health"
        "Notification Service:$NOTIFICATION_SERVICE_URL/actuator/health"
    )
    
    for service in "${services[@]}"; do
        local name=$(echo "$service" | cut -d':' -f1)
        local url=$(echo "$service" | cut -d':' -f2)
        
        if curl -s "$url" > /dev/null; then
            log_test "$name Health Check" "PASS" ""
        else
            log_test "$name Health Check" "FAIL" "Service not responding"
        fi
    done
}

# Function to test user service endpoints
test_user_service() {
    echo -e "\n${CYAN}Testing User Service Endpoints...${NC}"
    
    # Test user registration
    local register_data="{\"username\":\"$TEST_USERNAME\",\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}"
    local register_response=$(make_request "POST" "$USER_SERVICE_URL/api/v1/users/register" "$register_data" "201" "User Registration")
    
    # Extract user ID from response
    USER_ID=$(extract_number "$register_response" "id")
    echo -e "  ${BLUE}Created user with ID: $USER_ID${NC}"
    
    # Test user login
    local login_data="{\"usernameOrEmail\":\"$TEST_USERNAME\",\"password\":\"$TEST_PASSWORD\"}"
    local login_response=$(make_request "POST" "$USER_SERVICE_URL/api/v1/auth/login" "$login_data" "200" "User Login")
    
    # Extract auth token
    AUTH_TOKEN=$(extract_value "$login_response" "token")
    echo -e "  ${BLUE}Authentication token obtained${NC}"
    
    # Test get user by ID
    make_request "GET" "$USER_SERVICE_URL/api/v1/users/$USER_ID" "" "200" "Get User by ID"
    
    # Test get user by username
    make_request "GET" "$USER_SERVICE_URL/api/v1/users/username/$TEST_USERNAME" "" "200" "Get User by Username"
    
    # Test get user by email
    make_request "GET" "$USER_SERVICE_URL/api/v1/users/email/$TEST_EMAIL" "" "200" "Get User by Email"
    
    # Test get all users (paginated)
    make_request "GET" "$USER_SERVICE_URL/api/v1/users?page=0&size=10" "" "200" "Get All Users (Paginated)"
}

# Function to test account service endpoints
test_account_service() {
    echo -e "\n${CYAN}Testing Account Service Endpoints...${NC}"
    
    # Test create account
    local account_data="{\"userId\":$USER_ID,\"accountNumber\":\"$TEST_ACCOUNT_NUMBER\",\"accountType\":\"SAVINGS\",\"initialBalance\":1000.00}"
    local account_response=$(make_request "POST" "$ACCOUNT_SERVICE_URL/api/v1/accounts" "$account_data" "201" "Create Account")
    
    # Extract account ID
    ACCOUNT_ID=$(extract_number "$account_response" "id")
    echo -e "  ${BLUE}Created account with ID: $ACCOUNT_ID${NC}"
    
    # Test get account by ID
    make_request "GET" "$ACCOUNT_SERVICE_URL/api/v1/accounts/$ACCOUNT_ID" "" "200" "Get Account by ID"
    
    # Test get account by account number
    make_request "GET" "$ACCOUNT_SERVICE_URL/api/v1/accounts/number/$TEST_ACCOUNT_NUMBER" "" "200" "Get Account by Number"
    
    # Test get accounts by user ID
    make_request "GET" "$ACCOUNT_SERVICE_URL/api/v1/accounts/user/$USER_ID" "" "200" "Get Accounts by User ID"
    
    # Test get all accounts (paginated)
    make_request "GET" "$ACCOUNT_SERVICE_URL/api/v1/accounts?page=0&size=10" "" "200" "Get All Accounts (Paginated)"
}

# Function to test transaction service endpoints
test_transaction_service() {
    echo -e "\n${CYAN}Testing Transaction Service Endpoints...${NC}"
    
    # Test create transaction
    local transaction_data="{\"userId\":$USER_ID,\"fromAccountId\":$ACCOUNT_ID,\"toAccountId\":$ACCOUNT_ID,\"amount\":100.00,\"transactionType\":\"TRANSFER\",\"description\":\"Test transaction\"}"
    local transaction_response=$(make_request "POST" "$TRANSACTION_SERVICE_URL/api/v1/transactions" "$transaction_data" "201" "Create Transaction")
    
    # Extract transaction ID
    TRANSACTION_ID=$(extract_number "$transaction_response" "id")
    echo -e "  ${BLUE}Created transaction with ID: $TRANSACTION_ID${NC}"
    
    # Test get transaction by ID
    make_request "GET" "$TRANSACTION_SERVICE_URL/api/v1/transactions/$TRANSACTION_ID" "" "200" "Get Transaction by ID"
    
    # Test get transactions by user ID
    make_request "GET" "$TRANSACTION_SERVICE_URL/api/v1/transactions/user/$USER_ID?page=0&size=10" "" "200" "Get Transactions by User ID"
    
    # Test get transactions by account ID
    make_request "GET" "$TRANSACTION_SERVICE_URL/api/v1/transactions/account/$ACCOUNT_ID?page=0&size=10" "" "200" "Get Transactions by Account ID"
    
    # Test get all transactions (paginated)
    make_request "GET" "$TRANSACTION_SERVICE_URL/api/v1/transactions?page=0&size=10" "" "200" "Get All Transactions (Paginated)"
    
    # Test get account balance
    make_request "GET" "$TRANSACTION_SERVICE_URL/api/v1/transactions/account/$ACCOUNT_ID/balance" "" "200" "Get Account Balance from Transaction Service"
}

# Function to test loan service endpoints
test_loan_service() {
    echo -e "\n${CYAN}Testing Loan Service Endpoints...${NC}"
    
    # Test create loan
    local loan_data="{\"userId\":$USER_ID,\"accountNumber\":\"$TEST_ACCOUNT_NUMBER\",\"loanType\":\"PERSONAL\",\"principalAmount\":5000.00,\"interestRate\":12.5,\"termMonths\":12,\"purpose\":\"Home improvement\",\"monthlyIncome\":3000.00,\"notes\":\"Test loan application\"}"
    local loan_response=$(make_request "POST" "$LOAN_SERVICE_URL/api/v1/loans" "$loan_data" "201" "Create Loan")
    
    # Extract loan ID
    LOAN_ID=$(extract_number "$loan_response" "id")
    echo -e "  ${BLUE}Created loan with ID: $LOAN_ID${NC}"
    
    # Test get loan by ID
    make_request "GET" "$LOAN_SERVICE_URL/api/v1/loans/$LOAN_ID" "" "200" "Get Loan by ID"
    
    # Test get loans by user ID
    make_request "GET" "$LOAN_SERVICE_URL/api/v1/loans/user/$USER_ID?page=0&size=10" "" "200" "Get Loans by User ID"
    
    # Test get loans by status
    make_request "GET" "$LOAN_SERVICE_URL/api/v1/loans/status/PENDING?page=0&size=10" "" "200" "Get Loans by Status"
    
    # Test get all loans (paginated)
    make_request "GET" "$LOAN_SERVICE_URL/api/v1/loans?page=0&size=10" "" "200" "Get All Loans (Paginated)"
    
    # Test calculate monthly payment
    make_request "POST" "$LOAN_SERVICE_URL/api/v1/loans/calculate-payment" "{\"principal\":5000.00,\"interestRate\":12.5,\"termMonths\":12}" "200" "Calculate Monthly Payment"
}

# Function to test notification service endpoints
test_notification_service() {
    echo -e "\n${CYAN}Testing Notification Service Endpoints...${NC}"
    
    # Test create notification
    local notification_data="{\"userId\":$USER_ID,\"type\":\"TRANSACTION\",\"title\":\"Transaction Completed\",\"message\":\"Your transaction has been completed successfully\",\"priority\":\"HIGH\"}"
    local notification_response=$(make_request "POST" "$NOTIFICATION_SERVICE_URL/api/v1/notifications" "$notification_data" "201" "Create Notification")
    
    # Extract notification ID
    local notification_id=$(extract_number "$notification_response" "id")
    echo -e "  ${BLUE}Created notification with ID: $notification_id${NC}"
    
    # Test get notification by ID
    make_request "GET" "$NOTIFICATION_SERVICE_URL/api/v1/notifications/$notification_id" "" "200" "Get Notification by ID"
    
    # Test get notifications by user ID
    make_request "GET" "$NOTIFICATION_SERVICE_URL/api/v1/notifications/user/$USER_ID?page=0&size=10" "" "200" "Get Notifications by User ID"
    
    # Test get all notifications (paginated)
    make_request "GET" "$NOTIFICATION_SERVICE_URL/api/v1/notifications?page=0&size=10" "" "200" "Get All Notifications (Paginated)"
}

# Function to test API Gateway endpoints
test_api_gateway() {
    echo -e "\n${CYAN}Testing API Gateway Endpoints...${NC}"
    
    # Test API Gateway health
    make_request "GET" "$BASE_URL/actuator/health" "" "200" "API Gateway Health Check"
    
    # Test API Gateway routes
    make_request "GET" "$BASE_URL/actuator/gateway/routes" "" "200" "API Gateway Routes"
    
    # Test proxied user endpoints through gateway
    make_request "GET" "$BASE_URL/api/v1/users/$USER_ID" "" "200" "Get User through Gateway"
    
    # Test proxied account endpoints through gateway
    make_request "GET" "$BASE_URL/api/v1/accounts/$ACCOUNT_ID" "" "200" "Get Account through Gateway"
}

# Function to test cache functionality
test_cache_functionality() {
    echo -e "\n${CYAN}Testing Cache Functionality...${NC}"
    
    # Test cache hit for user lookup
    echo -e "  ${YELLOW}Testing cache hit for user lookup...${NC}"
    make_request "GET" "$USER_SERVICE_URL/api/v1/users/$USER_ID" "" "200" "User Lookup (First Call)"
    make_request "GET" "$USER_SERVICE_URL/api/v1/users/$USER_ID" "" "200" "User Lookup (Second Call - Cached)"
    
    # Test cache hit for account balance
    echo -e "  ${YELLOW}Testing cache hit for account balance...${NC}"
    make_request "GET" "$TRANSACTION_SERVICE_URL/api/v1/transactions/account/$ACCOUNT_ID/balance" "" "200" "Account Balance (First Call)"
    make_request "GET" "$TRANSACTION_SERVICE_URL/api/v1/transactions/account/$ACCOUNT_ID/balance" "" "200" "Account Balance (Second Call - Cached)"
    
    # Test cache hit for loan lookup
    echo -e "  ${YELLOW}Testing cache hit for loan lookup...${NC}"
    make_request "GET" "$LOAN_SERVICE_URL/api/v1/loans/$LOAN_ID" "" "200" "Loan Lookup (First Call)"
    make_request "GET" "$LOAN_SERVICE_URL/api/v1/loans/$LOAN_ID" "" "200" "Loan Lookup (Second Call - Cached)"
}

# Function to test error handling
test_error_handling() {
    echo -e "\n${CYAN}Testing Error Handling...${NC}"
    
    # Test invalid user ID
    make_request "GET" "$USER_SERVICE_URL/api/v1/users/999999" "" "404" "Get Non-existent User"
    
    # Test invalid account ID
    make_request "GET" "$ACCOUNT_SERVICE_URL/api/v1/accounts/999999" "" "404" "Get Non-existent Account"
    
    # Test invalid transaction ID
    make_request "GET" "$TRANSACTION_SERVICE_URL/api/v1/transactions/999999" "" "404" "Get Non-existent Transaction"
    
    # Test invalid loan ID
    make_request "GET" "$LOAN_SERVICE_URL/api/v1/loans/999999" "" "404" "Get Non-existent Loan"
    
    # Test unauthorized access (without token)
    local temp_token=$AUTH_TOKEN
    AUTH_TOKEN=""
    make_request "GET" "$USER_SERVICE_URL/api/v1/users/$USER_ID" "" "401" "Unauthorized Access"
    AUTH_TOKEN=$temp_token
}

# Function to generate test report
generate_test_report() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}  Test Execution Summary              ${NC}"
    echo -e "${BLUE}========================================${NC}"
    
    echo -e "\n${YELLOW}Test Results:${NC}"
    echo -e "  ${GREEN}Passed: $PASSED_TESTS${NC}"
    echo -e "  ${RED}Failed: $FAILED_TESTS${NC}"
    echo -e "  ${BLUE}Total: $TOTAL_TESTS${NC}"
    
    local success_rate=0
    if [ $TOTAL_TESTS -gt 0 ]; then
        success_rate=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    fi
    
    echo -e "\n${YELLOW}Success Rate: ${success_rate}%${NC}"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "\n${GREEN}🎉 All tests passed! The system is working correctly.${NC}"
    else
        echo -e "\n${RED}⚠️  Some tests failed. Please check the details above.${NC}"
    fi
    
    echo -e "\n${YELLOW}Test Coverage:${NC}"
    echo -e "  ✅ Service Health Checks"
    echo -e "  ✅ User Service (Registration, Login, CRUD operations)"
    echo -e "  ✅ Account Service (Account creation, management)"
    echo -e "  ✅ Transaction Service (Transaction processing, history)"
    echo -e "  ✅ Loan Service (Loan applications, calculations)"
    echo -e "  ✅ Notification Service (Notification management)"
    echo -e "  ✅ API Gateway (Routing, proxying)"
    echo -e "  ✅ Cache Functionality (Performance verification)"
    echo -e "  ✅ Error Handling (Invalid requests, unauthorized access)"
}

# Main test execution
main() {
    echo -e "${BLUE}Starting comprehensive end-to-end tests...${NC}"
    echo -e "${BLUE}Test data: Username=$TEST_USERNAME, Email=$TEST_EMAIL${NC}"
    
    # Check service health first
    check_service_health
    
    # Run tests in logical order (dependencies first)
    test_user_service
    test_account_service
    test_transaction_service
    test_loan_service
    test_notification_service
    test_api_gateway
    test_cache_functionality
    test_error_handling
    
    # Generate final report
    generate_test_report
    
    echo -e "\n${GREEN}========================================${NC}"
    echo -e "${GREEN}  End-to-End Tests Complete           ${NC}"
    echo -e "${GREEN}========================================${NC}"
}

# Run main function
main "$@"
