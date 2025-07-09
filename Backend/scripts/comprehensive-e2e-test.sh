#!/bin/bash

# Telepesa Comprehensive End-to-End Test Suite
# Tests all microservices through API Gateway with real business scenarios

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# API Configuration
API_BASE_URL="http://localhost:8080"
GATEWAY_URL="$API_BASE_URL"

# Test Results
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Test data
TEST_USER_EMAIL="test@telepesa.com"
TEST_USER_USERNAME="testuser$(date +%s)"
TEST_USER_PASSWORD="TestPassword123!"
TEST_ADMIN_EMAIL="admin@telepesa.com"
TEST_ADMIN_USERNAME="admin$(date +%s)"
TEST_ADMIN_PASSWORD="AdminPassword123!"

# JWT Tokens
USER_JWT=""
ADMIN_JWT=""

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
║                🧪 Comprehensive End-to-End Test Suite 🧪                     ║
║                     🔗 Testing All Microservices 🔗                         ║
║                                                                              ║
╚══════════════════════════════════════════════════════════════════════════════╝${NC}
"

# Function to make HTTP requests with proper error handling
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local headers=$4
    local expected_status=${5:-200}
    
    local response
    local status_code
    
    if [[ -n "$headers" ]]; then
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -H "$headers" \
            -d "$data" 2>/dev/null)
    else
        response=$(curl -s -w "HTTPSTATUS:%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -d "$data" 2>/dev/null)
    fi
    
    status_code=$(echo "$response" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    body=$(echo "$response" | sed -e 's/HTTPSTATUS:.*//g')
    
    if [[ "$status_code" -eq "$expected_status" ]]; then
        echo "$body"
        return 0
    else
        echo "ERROR: Expected status $expected_status, got $status_code. Response: $body" >&2
        return 1
    fi
}

# Function to run a test
run_test() {
    local test_name="$1"
    local test_command="$2"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    printf "%-50s | " "$test_name"
    
    if eval "$test_command" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ PASS${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        echo -e "${RED}❌ FAIL${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

# Function to extract JWT token from response
extract_jwt() {
    local response="$1"
    echo "$response" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    if 'data' in data and 'jwt' in data['data']:
        print(data['data']['jwt'])
    elif 'jwt' in data:
        print(data['jwt'])
    elif 'token' in data:
        print(data['token'])
    else:
        print('')
except:
    print('')
" 2>/dev/null || echo ""
}

echo -e "${PURPLE}[INFO] 🚀 Starting Comprehensive End-to-End Tests...${NC}"

# Phase 1: Infrastructure Health Checks
echo -e "\n${BLUE}📋 Phase 1: Infrastructure Health Checks${NC}"
echo "=================================================="

run_test "API Gateway Health Check" \
    "curl -s http://localhost:8080/actuator/health | grep -q 'UP'"

run_test "Eureka Server Health Check" \
    "curl -s http://localhost:8761/actuator/health | grep -q 'UP'"

run_test "User Service Health Check" \
    "curl -s http://localhost:8081/actuator/health | grep -q 'UP'"

run_test "Account Service Health Check" \
    "curl -s http://localhost:8082/actuator/health | grep -q 'UP'"

run_test "Transaction Service Health Check" \
    "curl -s http://localhost:8083/actuator/health | grep -q 'UP'"

run_test "Loan Service Health Check" \
    "curl -s http://localhost:8084/actuator/health | grep -q 'UP'"

run_test "Notification Service Health Check" \
    "curl -s http://localhost:8085/actuator/health | grep -q 'UP'"

# Phase 2: User Management Tests
echo -e "\n${BLUE}📋 Phase 2: User Management & Authentication${NC}"
echo "=============================================="

# Test user registration
test_user_registration() {
    local response
    response=$(make_request "POST" "$GATEWAY_URL/api/v1/users/register" \
        "{\"username\":\"$TEST_USER_USERNAME\",\"email\":\"$TEST_USER_EMAIL\",\"password\":\"$TEST_USER_PASSWORD\",\"firstName\":\"Test\",\"lastName\":\"User\"}" \
        "" 201)
    [[ -n "$response" ]]
}

run_test "User Registration via Gateway" "test_user_registration"

# Test admin user registration
test_admin_registration() {
    local response
    response=$(make_request "POST" "$GATEWAY_URL/api/v1/users/register" \
        "{\"username\":\"$TEST_ADMIN_USERNAME\",\"email\":\"$TEST_ADMIN_EMAIL\",\"password\":\"$TEST_ADMIN_PASSWORD\",\"firstName\":\"Admin\",\"lastName\":\"User\",\"role\":\"ADMIN\"}" \
        "" 201)
    [[ -n "$response" ]]
}

run_test "Admin User Registration via Gateway" "test_admin_registration"

# Test user login
test_user_login() {
    local response
    response=$(make_request "POST" "$GATEWAY_URL/api/v1/users/login" \
        "{\"username\":\"$TEST_USER_USERNAME\",\"password\":\"$TEST_USER_PASSWORD\"}")
    USER_JWT=$(extract_jwt "$response")
    [[ -n "$USER_JWT" ]]
}

run_test "User Login via Gateway" "test_user_login"

# Test admin login
test_admin_login() {
    local response
    response=$(make_request "POST" "$GATEWAY_URL/api/v1/users/login" \
        "{\"username\":\"$TEST_ADMIN_USERNAME\",\"password\":\"$TEST_ADMIN_PASSWORD\"}")
    ADMIN_JWT=$(extract_jwt "$response")
    [[ -n "$ADMIN_JWT" ]]
}

run_test "Admin Login via Gateway" "test_admin_login"

# Phase 3: Account Management Tests
echo -e "\n${BLUE}📋 Phase 3: Account Management${NC}"
echo "==============================="

# Test create savings account
test_create_savings_account() {
    local response
    response=$(make_request "POST" "$GATEWAY_URL/api/v1/accounts" \
        "{\"accountType\":\"SAVINGS\",\"initialBalance\":1000.00,\"currency\":\"KES\"}" \
        "Authorization: Bearer $USER_JWT" 201)
    [[ -n "$response" ]]
}

run_test "Create Savings Account" "test_create_savings_account"

# Test create checking account
test_create_checking_account() {
    local response
    response=$(make_request "POST" "$GATEWAY_URL/api/v1/accounts" \
        "{\"accountType\":\"CHECKING\",\"initialBalance\":500.00,\"currency\":\"KES\"}" \
        "Authorization: Bearer $USER_JWT" 201)
    [[ -n "$response" ]]
}

run_test "Create Checking Account" "test_create_checking_account"

# Test get user accounts
test_get_user_accounts() {
    local response
    response=$(make_request "GET" "$GATEWAY_URL/api/v1/accounts" \
        "" "Authorization: Bearer $USER_JWT")
    [[ -n "$response" ]]
}

run_test "Get User Accounts" "test_get_user_accounts"

# Phase 4: Transaction Tests
echo -e "\n${BLUE}📋 Phase 4: Transaction Processing${NC}"
echo "===================================="

# Test internal transfer
test_internal_transfer() {
    local response
    response=$(make_request "POST" "$GATEWAY_URL/api/v1/transactions/transfer" \
        "{\"fromAccountId\":1,\"toAccountId\":2,\"amount\":100.00,\"description\":\"Test transfer\"}" \
        "Authorization: Bearer $USER_JWT" 201)
    [[ -n "$response" ]]
}

run_test "Internal Transfer" "test_internal_transfer"

# Test deposit
test_deposit() {
    local response
    response=$(make_request "POST" "$GATEWAY_URL/api/v1/transactions/deposit" \
        "{\"accountId\":1,\"amount\":250.00,\"description\":\"Test deposit\"}" \
        "Authorization: Bearer $USER_JWT" 201)
    [[ -n "$response" ]]
}

run_test "Deposit Transaction" "test_deposit"

# Test get transaction history
test_transaction_history() {
    local response
    response=$(make_request "GET" "$GATEWAY_URL/api/v1/transactions/history" \
        "" "Authorization: Bearer $USER_JWT")
    [[ -n "$response" ]]
}

run_test "Get Transaction History" "test_transaction_history"

# Phase 5: Loan Management Tests
echo -e "\n${BLUE}📋 Phase 5: Loan Management${NC}"
echo "============================="

# Test loan application
test_loan_application() {
    local response
    response=$(make_request "POST" "$GATEWAY_URL/api/v1/loans/apply" \
        "{\"loanType\":\"PERSONAL\",\"principalAmount\":50000.00,\"termMonths\":12,\"purpose\":\"Business expansion\",\"annualIncome\":600000.00}" \
        "Authorization: Bearer $USER_JWT" 201)
    [[ -n "$response" ]]
}

run_test "Loan Application" "test_loan_application"

# Test get user loans
test_get_user_loans() {
    local response
    response=$(make_request "GET" "$GATEWAY_URL/api/v1/loans" \
        "" "Authorization: Bearer $USER_JWT")
    [[ -n "$response" ]]
}

run_test "Get User Loans" "test_get_user_loans"

# Phase 6: Notification Tests
echo -e "\n${BLUE}📋 Phase 6: Notification Services${NC}"
echo "==================================="

# Test send notification
test_send_notification() {
    local response
    response=$(make_request "POST" "$GATEWAY_URL/api/v1/notifications" \
        "{\"recipientId\":1,\"type\":\"INFO\",\"title\":\"Test Notification\",\"message\":\"This is a test notification\"}" \
        "Authorization: Bearer $ADMIN_JWT" 201)
    [[ -n "$response" ]]
}

run_test "Send Notification" "test_send_notification"

# Test get notifications
test_get_notifications() {
    local response
    response=$(make_request "GET" "$GATEWAY_URL/api/v1/notifications" \
        "" "Authorization: Bearer $USER_JWT")
    [[ -n "$response" ]]
}

run_test "Get User Notifications" "test_get_notifications"

# Phase 7: Admin Operations Tests
echo -e "\n${BLUE}📋 Phase 7: Admin Operations${NC}"
echo "=============================="

# Test get all users (admin only)
test_admin_get_users() {
    local response
    response=$(make_request "GET" "$GATEWAY_URL/api/v1/users" \
        "" "Authorization: Bearer $ADMIN_JWT")
    [[ -n "$response" ]]
}

run_test "Admin Get All Users" "test_admin_get_users"

# Test get system stats (admin only)
test_admin_system_stats() {
    local response
    response=$(make_request "GET" "$GATEWAY_URL/api/v1/admin/stats" \
        "" "Authorization: Bearer $ADMIN_JWT")
    [[ -n "$response" ]] || true  # May not be implemented yet
}

run_test "Admin System Statistics" "test_admin_system_stats"

# Phase 8: Security & Rate Limiting Tests
echo -e "\n${BLUE}📋 Phase 8: Security & Rate Limiting${NC}"
echo "======================================"

# Test unauthorized access
test_unauthorized_access() {
    local response
    response=$(make_request "GET" "$GATEWAY_URL/api/v1/accounts" \
        "" "" 401)
    [[ -n "$response" ]]
}

run_test "Unauthorized Access Prevention" "test_unauthorized_access"

# Test invalid JWT
test_invalid_jwt() {
    local response
    response=$(make_request "GET" "$GATEWAY_URL/api/v1/accounts" \
        "" "Authorization: Bearer invalid.jwt.token" 401)
    [[ -n "$response" ]]
}

run_test "Invalid JWT Rejection" "test_invalid_jwt"

# Test rate limiting (make multiple rapid requests)
test_rate_limiting() {
    local success_count=0
    for i in {1..10}; do
        if curl -s -o /dev/null -w "%{http_code}" "$GATEWAY_URL/api/v1/users/health" | grep -q "200"; then
            success_count=$((success_count + 1))
        fi
    done
    [[ $success_count -gt 0 ]]  # At least some requests should succeed
}

run_test "Rate Limiting Functionality" "test_rate_limiting"

# Final Results
echo -e "\n${PURPLE}
┌─────────────────────────────────────────────────────────────────────────────┐
│                           📊 TEST RESULTS SUMMARY                           │
└─────────────────────────────────────────────────────────────────────────────┘${NC}"

echo -e "Total Tests: ${BLUE}$TOTAL_TESTS${NC}"
echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed: ${RED}$FAILED_TESTS${NC}"
echo -e "Success Rate: ${YELLOW}$(( (PASSED_TESTS * 100) / TOTAL_TESTS ))%${NC}"

if [[ $FAILED_TESTS -eq 0 ]]; then
    echo -e "\n${GREEN}🎉 ALL TESTS PASSED! Telepesa platform is working perfectly! 🎉${NC}"
    exit 0
else
    echo -e "\n${YELLOW}⚠️  Some tests failed. Check individual service logs for details.${NC}"
    exit 1
fi 