#!/bin/bash

# Telepesa Gateway End-to-End Testing Script
# Tests the entire banking system through API Gateway

set -e

# Configuration
GATEWAY_URL="http://localhost:8080"
DB_HOST="localhost"
DB_PORT="5432"
DB_USER="telepesa"
DB_PASSWORD="password"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Test data
TEST_USERNAME="testuser_$(date +%s)"
TEST_EMAIL="${TEST_USERNAME}@telepesa.com"
TEST_PASSWORD="SecurePass123!"
JWT_TOKEN=""
USER_ID=""
SAVINGS_ACCOUNT_ID=""
CHECKING_ACCOUNT_ID=""

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Test function
run_test() {
    local test_name="$1"
    local test_command="$2"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    log_info "Running test: $test_name"
    
    if eval "$test_command"; then
        log_success "Test passed: $test_name"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        log_error "Test failed: $test_name"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

# Check if services are running
check_services() {
    log_info "Checking if all services are running..."
    
    local services=(
        "Eureka Server:8761"
        "API Gateway:8080"
        "User Service:8081"
        "Account Service:8082"
        "Transaction Service:8083"
        "Loan Service:8084"
        "Notification Service:8085"
    )
    
    for service in "${services[@]}"; do
        local name=$(echo "$service" | cut -d':' -f1)
        local port=$(echo "$service" | cut -d':' -f2)
        
        if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            log_success "$name is running on port $port"
        else
            log_error "$name is not running on port $port"
            return 1
        fi
    done
    
    log_success "All services are running!"
}

# Test 1: User Registration
test_user_registration() {
    local test_data="{
        \"username\": \"$TEST_USERNAME\",
        \"email\": \"$TEST_EMAIL\",
        \"password\": \"$TEST_PASSWORD\",
        \"firstName\": \"John\",
        \"lastName\": \"Doe\",
        \"phoneNumber\": \"+254700123456\",
        \"dateOfBirth\": \"1990-01-01\",
        \"nationalId\": \"12345678\"
    }"
    
    local response=$(curl -s -w '%{http_code}' -X POST "$GATEWAY_URL/api/v1/users/register" \
        -H 'Content-Type: application/json' \
        -d "$test_data")
    
    local status_code=$(echo "$response" | tail -c 4)
    local response_body=$(echo "$response" | head -c -4)
    
    if [ "$status_code" = "201" ]; then
        log_success "User registration successful"
        return 0
    else
        log_error "User registration failed: $response_body"
        return 1
    fi
}

# Test 2: User Activation (Database)
test_user_activation() {
    log_info "Activating user in database..."
    
    local sql="UPDATE users SET status = 'ACTIVE' WHERE username = '$TEST_USERNAME';"
    
    if PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "telepesa_users_dev" -c "$sql" > /dev/null 2>&1; then
        log_success "User activated successfully"
        return 0
    else
        log_error "Failed to activate user"
        return 1
    fi
}

# Test 3: User Login
test_user_login() {
    local test_data="{
        \"username\": \"$TEST_USERNAME\",
        \"password\": \"$TEST_PASSWORD\"
    }"
    
    local response=$(curl -s -w '%{http_code}' -X POST "$GATEWAY_URL/api/v1/users/login" \
        -H 'Content-Type: application/json' \
        -d "$test_data")
    
    local status_code=$(echo "$response" | tail -c 4)
    local response_body=$(echo "$response" | head -c -4)
    
    if [ "$status_code" = "200" ]; then
        JWT_TOKEN=$(echo "$response_body" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        USER_ID=$(echo "$response_body" | grep -o '"userId":[0-9]*' | cut -d':' -f2)
        log_success "User login successful. JWT Token: ${JWT_TOKEN:0:20}..."
        return 0
    else
        log_error "User login failed: $response_body"
        return 1
    fi
}

# Test 4: Create Savings Account
test_create_savings_account() {
    local test_data="{
        \"accountType\": \"SAVINGS\",
        \"currency\": \"KES\",
        \"initialDeposit\": 1000.00
    }"
    
    local response=$(curl -s -w '%{http_code}' -X POST "$GATEWAY_URL/api/v1/accounts" \
        -H 'Content-Type: application/json' \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$test_data")
    
    local status_code=$(echo "$response" | tail -c 4)
    local response_body=$(echo "$response" | head -c -4)
    
    if [ "$status_code" = "201" ]; then
        SAVINGS_ACCOUNT_ID=$(echo "$response_body" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        log_success "Savings account created successfully. Account ID: $SAVINGS_ACCOUNT_ID"
        return 0
    else
        log_error "Failed to create savings account: $response_body"
        return 1
    fi
}

# Test 5: Create Checking Account
test_create_checking_account() {
    local test_data="{
        \"accountType\": \"CHECKING\",
        \"currency\": \"KES\",
        \"initialDeposit\": 500.00
    }"
    
    local response=$(curl -s -w '%{http_code}' -X POST "$GATEWAY_URL/api/v1/accounts" \
        -H 'Content-Type: application/json' \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$test_data")
    
    local status_code=$(echo "$response" | tail -c 4)
    local response_body=$(echo "$response" | head -c -4)
    
    if [ "$status_code" = "201" ]; then
        CHECKING_ACCOUNT_ID=$(echo "$response_body" | grep -o '"id":[0-9]*' | cut -d':' -f2)
        log_success "Checking account created successfully. Account ID: $CHECKING_ACCOUNT_ID"
        return 0
    else
        log_error "Failed to create checking account: $response_body"
        return 1
    fi
}

# Test 6: Get User Accounts
test_get_user_accounts() {
    local response=$(curl -s -w '%{http_code}' -X GET "$GATEWAY_URL/api/v1/accounts" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    local status_code=$(echo "$response" | tail -c 4)
    local response_body=$(echo "$response" | head -c -4)
    
    if [ "$status_code" = "200" ]; then
        log_success "Retrieved user accounts successfully"
        return 0
    else
        log_error "Failed to get user accounts: $response_body"
        return 1
    fi
}

# Test 7: Deposit Money
test_deposit_money() {
    local test_data="{
        \"fromAccountId\": null,
        \"toAccountId\": $SAVINGS_ACCOUNT_ID,
        \"amount\": 2000.00,
        \"transactionType\": \"DEPOSIT\",
        \"description\": \"Cash deposit\",
        \"userId\": $USER_ID
    }"
    
    local response=$(curl -s -w '%{http_code}' -X POST "$GATEWAY_URL/api/v1/transactions" \
        -H 'Content-Type: application/json' \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$test_data")
    
    local status_code=$(echo "$response" | tail -c 4)
    local response_body=$(echo "$response" | head -c -4)
    
    if [ "$status_code" = "201" ]; then
        log_success "Deposit transaction successful"
        return 0
    else
        log_error "Failed to deposit money: $response_body"
        return 1
    fi
}

# Test 8: Transfer Between Accounts
test_transfer_between_accounts() {
    local test_data="{
        \"fromAccountId\": $SAVINGS_ACCOUNT_ID,
        \"toAccountId\": $CHECKING_ACCOUNT_ID,
        \"amount\": 300.00,
        \"transactionType\": \"TRANSFER\",
        \"description\": \"Transfer to checking account\",
        \"userId\": $USER_ID
    }"
    
    local response=$(curl -s -w '%{http_code}' -X POST "$GATEWAY_URL/api/v1/transactions" \
        -H 'Content-Type: application/json' \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$test_data")
    
    local status_code=$(echo "$response" | tail -c 4)
    local response_body=$(echo "$response" | head -c -4)
    
    if [ "$status_code" = "201" ]; then
        log_success "Transfer transaction successful"
        return 0
    else
        log_error "Failed to transfer money: $response_body"
        return 1
    fi
}

# Test 9: Get Transaction History
test_get_transaction_history() {
    local response=$(curl -s -w '%{http_code}' -X GET "$GATEWAY_URL/api/v1/transactions" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    local status_code=$(echo "$response" | tail -c 4)
    local response_body=$(echo "$response" | head -c -4)
    
    if [ "$status_code" = "200" ]; then
        log_success "Retrieved transaction history successfully"
        return 0
    else
        log_error "Failed to get transaction history: $response_body"
        return 1
    fi
}

# Test 10: Apply for Loan
test_apply_for_loan() {
    local test_data="{
        \"userId\": $USER_ID,
        \"accountNumber\": \"ACC001\",
        \"loanType\": \"PERSONAL\",
        \"principalAmount\": 50000.00,
        \"interestRate\": 12.5,
        \"termMonths\": 24,
        \"purpose\": \"Home improvement\",
        \"monthlyIncome\": 80000.00
    }"
    
    local response=$(curl -s -w '%{http_code}' -X POST "$GATEWAY_URL/api/v1/loans" \
        -H 'Content-Type: application/json' \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$test_data")
    
    local status_code=$(echo "$response" | tail -c 4)
    local response_body=$(echo "$response" | head -c -4)
    
    if [ "$status_code" = "201" ]; then
        log_success "Loan application submitted successfully"
        return 0
    else
        log_error "Failed to apply for loan: $response_body"
        return 1
    fi
}

# Test 11: Get User Loans
test_get_user_loans() {
    local response=$(curl -s -w '%{http_code}' -X GET "$GATEWAY_URL/api/v1/loans/user/$USER_ID" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    local status_code=$(echo "$response" | tail -c 4)
    local response_body=$(echo "$response" | head -c -4)
    
    if [ "$status_code" = "200" ]; then
        log_success "Retrieved user loans successfully"
        return 0
    else
        log_error "Failed to get user loans: $response_body"
        return 1
    fi
}

# Test 12: Get User Notifications
test_get_user_notifications() {
    local response=$(curl -s -w '%{http_code}' -X GET "$GATEWAY_URL/api/v1/notifications/user/$USER_ID" \
        -H "Authorization: Bearer $JWT_TOKEN")
    
    local status_code=$(echo "$response" | tail -c 4)
    local response_body=$(echo "$response" | head -c -4)
    
    if [ "$status_code" = "200" ]; then
        log_success "Retrieved user notifications successfully"
        return 0
    else
        log_error "Failed to get user notifications: $response_body"
        return 1
    fi
}

# Test 13: Security Test - Unauthorized Access
test_unauthorized_access() {
    local response=$(curl -s -w '%{http_code}' -X GET "$GATEWAY_URL/api/v1/accounts")
    
    local status_code=$(echo "$response" | tail -c 4)
    
    if [ "$status_code" = "401" ]; then
        log_success "Unauthorized access properly blocked"
        return 0
    else
        log_error "Security vulnerability: unauthorized access allowed"
        return 1
    fi
}

# Test 14: Security Test - Invalid JWT
test_invalid_jwt() {
    local response=$(curl -s -w '%{http_code}' -X GET "$GATEWAY_URL/api/v1/accounts" \
        -H "Authorization: Bearer invalid_token")
    
    local status_code=$(echo "$response" | tail -c 4)
    
    if [ "$status_code" = "401" ]; then
        log_success "Invalid JWT properly rejected"
        return 0
    else
        log_error "Security vulnerability: invalid JWT accepted"
        return 1
    fi
}

# Main test execution
main() {
    log_info "Starting Telepesa Gateway End-to-End Testing"
    log_info "Test User: $TEST_USERNAME"
    log_info "Gateway URL: $GATEWAY_URL"
    
    # Check if services are running
    run_test "Service Health Check" "check_services"
    
    # User Management Tests
    run_test "User Registration" "test_user_registration"
    run_test "User Activation" "test_user_activation"
    run_test "User Login" "test_user_login"
    
    # Account Management Tests
    run_test "Create Savings Account" "test_create_savings_account"
    run_test "Create Checking Account" "test_create_checking_account"
    run_test "Get User Accounts" "test_get_user_accounts"
    
    # Transaction Management Tests
    run_test "Deposit Money" "test_deposit_money"
    run_test "Transfer Between Accounts" "test_transfer_between_accounts"
    run_test "Get Transaction History" "test_get_transaction_history"
    
    # Loan Management Tests
    run_test "Apply for Loan" "test_apply_for_loan"
    run_test "Get User Loans" "test_get_user_loans"
    
    # Notification Tests
    run_test "Get User Notifications" "test_get_user_notifications"
    
    # Security Tests
    run_test "Unauthorized Access Test" "test_unauthorized_access"
    run_test "Invalid JWT Test" "test_invalid_jwt"
    
    # Print test results
    echo ""
    log_info "=== TEST RESULTS ==="
    log_info "Total Tests: $TOTAL_TESTS"
    log_success "Passed: $PASSED_TESTS"
    if [ $FAILED_TESTS -gt 0 ]; then
        log_error "Failed: $FAILED_TESTS"
    else
        log_success "Failed: $FAILED_TESTS"
    fi
    
    local success_rate=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    log_info "Success Rate: $success_rate%"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        log_success "🎉 All tests passed! System is working correctly."
        exit 0
    else
        log_error "❌ Some tests failed. Please check the system."
        exit 1
    fi
}

# Cleanup function
cleanup() {
    log_info "Cleaning up test data..."
    
    # Delete test user from database
    if [ -n "$TEST_USERNAME" ]; then
        PGPASSWORD="$DB_PASSWORD" psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "telepesa_users_dev" \
            -c "DELETE FROM users WHERE username = '$TEST_USERNAME';" > /dev/null 2>&1 || true
    fi
    
    log_info "Cleanup completed"
}

# Trap to ensure cleanup runs on exit
trap cleanup EXIT

# Run main function
main "$@"
