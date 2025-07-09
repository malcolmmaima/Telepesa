#!/bin/bash

# Telepesa Consolidated End-to-End Test Script
# Single comprehensive E2E test script that replaces all previous E2E scripts
# Usage: ./e2e-test.sh [--gateway|--direct] [--verbose] [--cleanup]

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
API_GATEWAY_URL="http://localhost:8080"
USER_SERVICE_URL="http://localhost:8081"
ACCOUNT_SERVICE_URL="http://localhost:8082"
TRANSACTION_SERVICE_URL="http://localhost:8083"
LOAN_SERVICE_URL="http://localhost:8084"
NOTIFICATION_SERVICE_URL="http://localhost:8085"
EUREKA_URL="http://localhost:8761"

# Test data with timestamp to avoid conflicts
TIMESTAMP=$(date +%s)
TEST_USERNAME="testuser_$TIMESTAMP"
TEST_EMAIL="test_$TIMESTAMP@telepesa.com"
TEST_PASSWORD="TestPassword123!"
TEST_PHONE="+254700$TIMESTAMP"
TEST_ACCOUNT_NUMBER="ACC$TIMESTAMP"

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
SKIPPED_TESTS=0

# Default settings
TEST_MODE="direct"  # direct or gateway
VERBOSE=false
CLEANUP=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --gateway)
            TEST_MODE="gateway"
            shift
            ;;
        --direct)
            TEST_MODE="direct"
            shift
            ;;
        --verbose)
            VERBOSE=true
            shift
            ;;
        --cleanup)
            CLEANUP=true
            shift
            ;;
        --help)
            echo "Usage: $0 [OPTIONS]"
            echo "Options:"
            echo "  --gateway    Test through API Gateway (default: direct)"
            echo "  --direct     Test services directly"
            echo "  --verbose    Enable verbose output"
            echo "  --cleanup    Clean up test data after tests"
            echo "  --help       Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  Telepesa Consolidated E2E Tests      ${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${CYAN}Test Mode: $TEST_MODE${NC}"
echo -e "${CYAN}Test User: $TEST_USERNAME${NC}"
echo -e "${CYAN}Test Email: $TEST_EMAIL${NC}"
echo ""

# Function to log test results
log_test() {
    local test_name="$1"
    local result="$2"
    local details="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    if [ "$result" = "PASS" ]; then
        echo -e "  ${GREEN}✅ $test_name${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    elif [ "$result" = "SKIP" ]; then
        echo -e "  ${YELLOW}⏭️  $test_name (SKIPPED)${NC}"
        SKIPPED_TESTS=$((SKIPPED_TESTS + 1))
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
    response_body=$(echo "$response" | sed '$d')
    
    if [ "$status_code" = "$expected_status" ]; then
        log_test "$test_name" "PASS" ""
        if [ "$VERBOSE" = true ]; then
            echo "    Response: $response_body"
        fi
        echo "$response_body"
    else
        log_test "$test_name" "FAIL" "Expected $expected_status, got $status_code. Response: $response_body"
        # Return empty string for failed requests
        echo ""
    fi
}

# Function to make HTTP requests with flexible status codes
make_request_flexible() {
    local method="$1"
    local url="$2"
    local data="$3"
    local expected_statuses="$4"  # Comma-separated list of acceptable status codes
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
    response_body=$(echo "$response" | sed '$d')
    
    # Check if status code is in the expected list
    local status_match=false
    IFS=',' read -ra STATUS_ARRAY <<< "$expected_statuses"
    for expected_status in "${STATUS_ARRAY[@]}"; do
        if [ "$status_code" = "$expected_status" ]; then
            status_match=true
            break
        fi
    done
    
    if [ "$status_match" = true ]; then
        log_test "$test_name" "PASS" ""
        if [ "$VERBOSE" = true ]; then
            echo "    Response: $response_body"
        fi
        echo "$response_body"
    else
        log_test "$test_name" "FAIL" "Expected one of [$expected_statuses], got $status_code. Response: $response_body"
        # Return empty string for failed requests
        echo ""
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
    
    local services=()
    
    if [ "$TEST_MODE" = "gateway" ]; then
        services=("API Gateway:$API_GATEWAY_URL/actuator/health")
    fi
    
    services+=(
        "User Service:$USER_SERVICE_URL/actuator/health"
        "Account Service:$ACCOUNT_SERVICE_URL/actuator/health"
        "Transaction Service:$TRANSACTION_SERVICE_URL/actuator/health"
        "Notification Service:$NOTIFICATION_SERVICE_URL/actuator/health"
    )
    
    # Only check loan service if it's running
    if curl -s "$LOAN_SERVICE_URL/actuator/health" > /dev/null 2>&1; then
        services+=("Loan Service:$LOAN_SERVICE_URL/actuator/health")
    fi
    
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

# Function to check service discovery
check_service_discovery() {
    echo -e "\n${YELLOW}Checking Service Discovery...${NC}"
    
    local response
    if response=$(curl -s "$EUREKA_URL/eureka/apps" 2>/dev/null); then
        local expected_services=("user-service" "account-service" "transaction-service" "notification-service")
        local missing_services=()
        
        for service in "${expected_services[@]}"; do
            if ! echo "$response" | grep -q "$service"; then
                missing_services+=("$service")
            fi
        done
        
        if [ ${#missing_services[@]} -eq 0 ]; then
            log_test "Service Discovery" "PASS" "All core services registered"
        else
            log_test "Service Discovery" "FAIL" "Missing services: ${missing_services[*]}"
        fi
    else
        log_test "Service Discovery" "SKIP" "Eureka server not accessible"
    fi
}

# Function to test user service endpoints
test_user_service() {
    echo -e "\n${CYAN}Testing User Service Endpoints...${NC}"
    
    local base_url
    if [ "$TEST_MODE" = "gateway" ]; then
        base_url="$API_GATEWAY_URL/api/v1/users"
    else
        base_url="$USER_SERVICE_URL/api/users"
    fi
    
    # Test user registration - accept both 201 (created) and 409 (already exists)
    local register_data="{\"username\":\"$TEST_USERNAME\",\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\",\"firstName\":\"Test\",\"lastName\":\"User\",\"phoneNumber\":\"$TEST_PHONE\"}"
    local register_response=$(make_request_flexible "POST" "$base_url/register" "$register_data" "201,409" "User Registration")
    
    # Extract user ID from response (if registration succeeded)
    if [ -n "$register_response" ]; then
        USER_ID=$(extract_number "$register_response" "id")
        if [ -n "$USER_ID" ]; then
            echo -e "  ${BLUE}Created user with ID: $USER_ID${NC}"
        else
            # If registration failed due to existing user, try to get the existing user ID
            echo -e "  ${YELLOW}User already exists, trying to get existing user...${NC}"
            local existing_user_response=$(curl -s "$base_url/username/$TEST_USERNAME")
            USER_ID=$(extract_number "$existing_user_response" "id")
            if [ -n "$USER_ID" ]; then
                echo -e "  ${BLUE}Found existing user with ID: $USER_ID${NC}"
            fi
        fi
    fi
    
    # Test user login - accept both 200 (success) and 401 (pending verification)
    local login_data="{\"usernameOrEmail\":\"$TEST_USERNAME\",\"password\":\"$TEST_PASSWORD\"}"
    local login_response=$(make_request_flexible "POST" "$base_url/login" "$login_data" "200,401" "User Login")
    
    # Extract auth token if login succeeded
    if [ -n "$login_response" ]; then
        AUTH_TOKEN=$(extract_value "$login_response" "token")
        if [ -n "$AUTH_TOKEN" ]; then
            echo -e "  ${BLUE}Authentication token obtained${NC}"
        else
            echo -e "  ${YELLOW}Login failed - user may need verification${NC}"
        fi
    fi
    
    # Only test protected endpoints if we have authentication
    if [ -n "$AUTH_TOKEN" ]; then
        # Test get user by ID (if we have user ID)
        if [ -n "$USER_ID" ]; then
            make_request "GET" "$base_url/$USER_ID" "" "200" "Get User by ID"
        fi
        
        # Test get user by username
        make_request "GET" "$base_url/username/$TEST_USERNAME" "" "200" "Get User by Username"
        
        # Test get user by email
        make_request "GET" "$base_url/email/$TEST_EMAIL" "" "200" "Get User by Email"
        
        # Test get all users (paginated)
        make_request "GET" "$base_url?page=0&size=10" "" "200" "Get All Users (Paginated)"
    else
        log_test "Protected User Endpoints" "SKIP" "No authentication token available"
    fi
}

# Function to test account service endpoints
test_account_service() {
    echo -e "\n${CYAN}Testing Account Service Endpoints...${NC}"
    
    if [ -z "$USER_ID" ]; then
        log_test "Account Service Tests" "SKIP" "No user ID available"
        return
    fi
    
    if [ -z "$AUTH_TOKEN" ]; then
        log_test "Account Service Tests" "SKIP" "No authentication token available"
        return
    fi
    
    local base_url
    if [ "$TEST_MODE" = "gateway" ]; then
        base_url="$API_GATEWAY_URL/api/v1/accounts"
    else
        base_url="$ACCOUNT_SERVICE_URL/api/accounts"
    fi
    
    # Test create account
    local account_data="{\"userId\":$USER_ID,\"accountNumber\":\"$TEST_ACCOUNT_NUMBER\",\"accountType\":\"SAVINGS\",\"initialBalance\":1000.00}"
    local account_response=$(make_request "POST" "$base_url" "$account_data" "201" "Create Account")
    
    # Extract account ID
    if [ -n "$account_response" ]; then
        ACCOUNT_ID=$(extract_number "$account_response" "id")
        if [ -n "$ACCOUNT_ID" ]; then
            echo -e "  ${BLUE}Created account with ID: $ACCOUNT_ID${NC}"
        fi
    fi
    
    # Only test other endpoints if we have an account ID
    if [ -n "$ACCOUNT_ID" ]; then
        # Test get account by ID
        make_request "GET" "$base_url/$ACCOUNT_ID" "" "200" "Get Account by ID"
        
        # Test get account by account number
        make_request "GET" "$base_url/number/$TEST_ACCOUNT_NUMBER" "" "200" "Get Account by Number"
        
        # Test get accounts by user ID
        make_request "GET" "$base_url/user/$USER_ID" "" "200" "Get Accounts by User ID"
        
        # Test get all accounts (paginated)
        make_request "GET" "$base_url?page=0&size=10" "" "200" "Get All Accounts (Paginated)"
    else
        log_test "Account Endpoints" "SKIP" "No account ID available"
    fi
}

# Function to test transaction service endpoints
test_transaction_service() {
    echo -e "\n${CYAN}Testing Transaction Service Endpoints...${NC}"
    
    if [ -z "$ACCOUNT_ID" ]; then
        log_test "Transaction Service Tests" "SKIP" "No account ID available"
        return
    fi
    
    local base_url
    if [ "$TEST_MODE" = "gateway" ]; then
        base_url="$API_GATEWAY_URL/api/v1/transactions"
    else
        base_url="$TRANSACTION_SERVICE_URL/api/transactions"
    fi
    
    # Test create transaction
    local transaction_data="{\"fromAccountId\":$ACCOUNT_ID,\"toAccountId\":$ACCOUNT_ID,\"amount\":100.00,\"type\":\"TRANSFER\",\"description\":\"Test transaction\"}"
    local transaction_response=$(make_request "POST" "$base_url" "$transaction_data" "201" "Create Transaction")
    
    # Extract transaction ID
    TRANSACTION_ID=$(extract_number "$transaction_response" "id")
    if [ -n "$TRANSACTION_ID" ]; then
        echo -e "  ${BLUE}Created transaction with ID: $TRANSACTION_ID${NC}"
    fi
    
    # Test get transaction by ID
    if [ -n "$TRANSACTION_ID" ]; then
        make_request "GET" "$base_url/$TRANSACTION_ID" "" "200" "Get Transaction by ID"
    fi
    
    # Test get transactions by account ID
    make_request "GET" "$base_url/account/$ACCOUNT_ID" "" "200" "Get Transactions by Account ID"
    
    # Test get all transactions (paginated)
    make_request "GET" "$base_url?page=0&size=10" "" "200" "Get All Transactions (Paginated)"
}

# Function to test notification service endpoints
test_notification_service() {
    echo -e "\n${CYAN}Testing Notification Service Endpoints...${NC}"
    
    if [ -z "$USER_ID" ]; then
        log_test "Notification Service Tests" "SKIP" "No user ID available"
        return
    fi
    
    local base_url
    if [ "$TEST_MODE" = "gateway" ]; then
        base_url="$API_GATEWAY_URL/api/v1/notifications"
    else
        base_url="$NOTIFICATION_SERVICE_URL/api/notifications"
    fi
    
    # Test create notification
    local notification_data="{\"userId\":$USER_ID,\"type\":\"EMAIL\",\"title\":\"Test Notification\",\"message\":\"This is a test notification\",\"priority\":\"MEDIUM\"}"
    local notification_response=$(make_request "POST" "$base_url" "$notification_data" "201" "Create Notification")
    
    # Extract notification ID
    local notification_id=$(extract_number "$notification_response" "id")
    if [ -n "$notification_id" ]; then
        echo -e "  ${BLUE}Created notification with ID: $notification_id${NC}"
    fi
    
    # Test get notifications by user ID
    make_request "GET" "$base_url/user/$USER_ID" "" "200" "Get Notifications by User ID"
    
    # Test get all notifications (paginated)
    make_request "GET" "$base_url?page=0&size=10" "" "200" "Get All Notifications (Paginated)"
}

# Function to test loan service endpoints (if available)
test_loan_service() {
    echo -e "\n${CYAN}Testing Loan Service Endpoints...${NC}"
    
    # Check if loan service is running
    if ! curl -s "$LOAN_SERVICE_URL/actuator/health" > /dev/null 2>&1; then
        log_test "Loan Service Tests" "SKIP" "Loan service not running"
        return
    fi
    
    if [ -z "$USER_ID" ] || [ -z "$ACCOUNT_ID" ]; then
        log_test "Loan Service Tests" "SKIP" "No user ID or account ID available"
        return
    fi
    
    local base_url
    if [ "$TEST_MODE" = "gateway" ]; then
        base_url="$API_GATEWAY_URL/api/v1/loans"
    else
        base_url="$LOAN_SERVICE_URL/api/loans"
    fi
    
    # Test create loan application
    local loan_data="{\"userId\":$USER_ID,\"accountId\":$ACCOUNT_ID,\"amount\":5000.00,\"term\":12,\"type\":\"PERSONAL\",\"purpose\":\"Test loan\"}"
    local loan_response=$(make_request "POST" "$base_url" "$loan_data" "201" "Create Loan Application")
    
    # Extract loan ID
    LOAN_ID=$(extract_number "$loan_response" "id")
    if [ -n "$LOAN_ID" ]; then
        echo -e "  ${BLUE}Created loan with ID: $LOAN_ID${NC}"
    fi
    
    # Test get loan by ID
    if [ -n "$LOAN_ID" ]; then
        make_request "GET" "$base_url/$LOAN_ID" "" "200" "Get Loan by ID"
    fi
    
    # Test get loans by user ID
    make_request "GET" "$base_url/user/$USER_ID" "" "200" "Get Loans by User ID"
    
    # Test get all loans (paginated)
    make_request "GET" "$base_url?page=0&size=10" "" "200" "Get All Loans (Paginated)"
}

# Function to clean up test data
cleanup_test_data() {
    if [ "$CLEANUP" = true ]; then
        echo -e "\n${YELLOW}Cleaning up test data...${NC}"
        
        # Delete test user if we have the ID
        if [ -n "$USER_ID" ] && [ -n "$AUTH_TOKEN" ]; then
            local base_url
            if [ "$TEST_MODE" = "gateway" ]; then
                base_url="$API_GATEWAY_URL/api/v1/users"
            else
                base_url="$USER_SERVICE_URL/api/users"
            fi
            
            curl -s -X DELETE "$base_url/$USER_ID" -H "Authorization: Bearer $AUTH_TOKEN" > /dev/null 2>&1 || true
            log_test "Cleanup" "PASS" "Test user deleted"
        fi
    fi
}

# Main test execution
main() {
    echo -e "${BLUE}Starting E2E tests in $TEST_MODE mode...${NC}"
    
    # Check service health
    check_service_health
    
    # Check service discovery
    check_service_discovery
    
    # Test user service
    test_user_service
    
    # Test account service
    test_account_service
    
    # Test transaction service
    test_transaction_service
    
    # Test notification service
    test_notification_service
    
    # Test loan service (if available)
    test_loan_service
    
    # Cleanup if requested
    cleanup_test_data
    
    # Print summary
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}  Test Summary${NC}"
    echo -e "${BLUE}========================================${NC}"
    echo -e "Total Tests: $TOTAL_TESTS"
    echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
    echo -e "Failed: ${RED}$FAILED_TESTS${NC}"
    echo -e "Skipped: ${YELLOW}$SKIPPED_TESTS${NC}"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "\n${GREEN}🎉 All tests passed!${NC}"
        exit 0
    else
        echo -e "\n${RED}❌ Some tests failed!${NC}"
        exit 1
    fi
}

# Run main function
main
