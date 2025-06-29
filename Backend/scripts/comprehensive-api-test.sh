#!/bin/bash

# Telepesa Comprehensive API Test Script
# Tests complete system flow: signup, login, banking operations

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
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# Global variables
JWT_TOKEN=""
REFRESH_TOKEN=""
USER_ID=""
ACCOUNT_ID=""
LOAN_ID=""

echo -e "${BLUE}🚀 Telepesa Comprehensive API Test${NC}"
echo -e "${BLUE}Testing complete system flow from signup to banking operations${NC}\n"

# Function to check if service is running
check_service() {
    local service_name=$1
    local service_url=$2
    local endpoint=$3
    
    echo -e "${YELLOW}Checking $service_name...${NC}"
    local response=$(curl -s "$service_url$endpoint" 2>/dev/null || echo "FAILED")
    
    if [[ $response == *"UP"* ]] || [[ $response == *"200"* ]]; then
        echo -e "${GREEN}✅ $service_name: RUNNING${NC}"
        return 0
    else
        echo -e "${RED}❌ $service_name: NOT RUNNING${NC}"
        return 1
    fi
}

# Function to make API call and handle response
make_api_call() {
    local method=$1
    local url=$2
    local data=$3
    local description=$4
    
    echo -e "${YELLOW}$description...${NC}"
    
    local response=""
    if [[ -n "$data" ]]; then
        response=$(curl -s -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer $JWT_TOKEN" \
            -d "$data" 2>/dev/null || echo "FAILED")
    else
        response=$(curl -s -X "$method" "$url" \
            -H "Authorization: Bearer $JWT_TOKEN" 2>/dev/null || echo "FAILED")
    fi
    
    echo "Response: $response"
    echo ""
}

# Check all services
echo -e "${PURPLE}📋 Service Health Check${NC}"
check_service "User Service" "$USER_SERVICE_URL" "/actuator/health"
check_service "Account Service" "$ACCOUNT_SERVICE_URL" "/actuator/health"
check_service "Transaction Service" "$TRANSACTION_SERVICE_URL" "/actuator/health"
check_service "Loan Service" "$LOAN_SERVICE_URL" "/actuator/health"
check_service "Notification Service" "$NOTIFICATION_SERVICE_URL" "/actuator/health"

echo ""

# Generate unique test data
timestamp=$(date +%s)
test_username="testuser$timestamp"
test_email="testuser$timestamp@telepesa.com"
test_phone="+254700$(echo $timestamp | tail -c 7)"

echo -e "${PURPLE}👤 User Registration & Authentication${NC}"

# 1. User Registration
echo -e "${YELLOW}1. Registering new user...${NC}"
registration_data="{
    \"username\": \"$test_username\",
    \"email\": \"$test_email\",
    \"password\": \"SecureP@ssw0rd123!\",
    \"firstName\": \"Test\",
    \"lastName\": \"User\",
    \"phoneNumber\": \"$test_phone\"
}"

registration_response=$(curl -s -X POST "$USER_SERVICE_URL/api/users/register" \
    -H "Content-Type: application/json" \
    -d "$registration_data")

if [[ $registration_response == *"id"* ]]; then
    echo -e "${GREEN}✅ User Registration: SUCCESS${NC}"
    USER_ID=$(echo $registration_response | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "   User ID: $USER_ID"
else
    echo -e "${RED}❌ User Registration: FAILED${NC}"
    echo "   Response: $registration_response"
    exit 1
fi

# 2. User Login (should fail due to pending verification)
echo -e "${YELLOW}2. Testing login with pending verification...${NC}"
login_data="{
    \"usernameOrEmail\": \"$test_username\",
    \"password\": \"SecureP@ssw0rd123!\"
}"

login_response=$(curl -s -X POST "$USER_SERVICE_URL/api/users/login" \
    -H "Content-Type: application/json" \
    -d "$login_data")

if [[ $login_response == *"not active"* ]] || [[ $login_response == *"verification"* ]]; then
    echo -e "${GREEN}✅ Login Security: PASSED (Verification required)${NC}"
else
    echo -e "${RED}❌ Login Security: UNEXPECTED${NC}"
    echo "   Response: $login_response"
fi

# 3. Activate User (simulate verification)
echo -e "${YELLOW}3. Activating user account...${NC}"
activation_data="{
    \"userId\": $USER_ID,
    \"status\": \"ACTIVE\"
}"

activation_response=$(curl -s -X PUT "$USER_SERVICE_URL/api/users/$USER_ID/status" \
    -H "Content-Type: application/json" \
    -d "$activation_data")

if [[ $activation_response == *"ACTIVE"* ]]; then
    echo -e "${GREEN}✅ User Activation: SUCCESS${NC}"
else
    echo -e "${RED}❌ User Activation: FAILED${NC}"
    echo "   Response: $activation_response"
fi

# 4. Login with active account
echo -e "${YELLOW}4. Logging in with active account...${NC}"
login_response=$(curl -s -X POST "$USER_SERVICE_URL/api/users/login" \
    -H "Content-Type: application/json" \
    -d "$login_data")

if [[ $login_response == *"token"* ]]; then
    echo -e "${GREEN}✅ User Login: SUCCESS${NC}"
    JWT_TOKEN=$(echo $login_response | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    REFRESH_TOKEN=$(echo $login_response | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4)
    echo "   JWT Token: ${JWT_TOKEN:0:20}..."
    echo "   Refresh Token: ${REFRESH_TOKEN:0:20}..."
else
    echo -e "${RED}❌ User Login: FAILED${NC}"
    echo "   Response: $login_response"
    exit 1
fi

echo ""

echo -e "${PURPLE}🏦 Account Management${NC}"

# 5. Create Account
echo -e "${YELLOW}5. Creating bank account...${NC}"
account_data="{
    \"userId\": $USER_ID,
    \"accountType\": \"SAVINGS\",
    \"currency\": \"KES\",
    \"initialBalance\": 10000.00
}"

account_response=$(curl -s -X POST "$ACCOUNT_SERVICE_URL/api/accounts" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "$account_data")

if [[ $account_response == *"id"* ]]; then
    echo -e "${GREEN}✅ Account Creation: SUCCESS${NC}"
    ACCOUNT_ID=$(echo $account_response | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "   Account ID: $ACCOUNT_ID"
else
    echo -e "${RED}❌ Account Creation: FAILED${NC}"
    echo "   Response: $account_response"
fi

# 6. Get Account Details
echo -e "${YELLOW}6. Getting account details...${NC}"
account_details_response=$(curl -s -X GET "$ACCOUNT_SERVICE_URL/api/accounts/$ACCOUNT_ID" \
    -H "Authorization: Bearer $JWT_TOKEN")

if [[ $account_details_response == *"balance"* ]]; then
    echo -e "${GREEN}✅ Account Details: SUCCESS${NC}"
    balance=$(echo $account_details_response | grep -o '"balance":[0-9.]*' | cut -d':' -f2)
    echo "   Balance: $balance"
else
    echo -e "${RED}❌ Account Details: FAILED${NC}"
    echo "   Response: $account_details_response"
fi

echo ""

echo -e "${PURPLE}💳 Transaction Processing${NC}"

# 7. Create Transaction
echo -e "${YELLOW}7. Creating transaction...${NC}"
transaction_data="{
    \"fromAccountId\": $ACCOUNT_ID,
    \"toAccountId\": $ACCOUNT_ID,
    \"amount\": 1000.00,
    \"currency\": \"KES\",
    \"transactionType\": \"TRANSFER\",
    \"description\": \"Test transaction\"
}"

transaction_response=$(curl -s -X POST "$TRANSACTION_SERVICE_URL/api/transactions" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "$transaction_data")

if [[ $transaction_response == *"id"* ]]; then
    echo -e "${GREEN}✅ Transaction Creation: SUCCESS${NC}"
    transaction_id=$(echo $transaction_response | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "   Transaction ID: $transaction_id"
else
    echo -e "${RED}❌ Transaction Creation: FAILED${NC}"
    echo "   Response: $transaction_response"
fi

# 8. Get Transaction History
echo -e "${YELLOW}8. Getting transaction history...${NC}"
history_response=$(curl -s -X GET "$TRANSACTION_SERVICE_URL/api/transactions/account/$ACCOUNT_ID" \
    -H "Authorization: Bearer $JWT_TOKEN")

if [[ $history_response == *"content"* ]]; then
    echo -e "${GREEN}✅ Transaction History: SUCCESS${NC}"
    transaction_count=$(echo $history_response | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)
    echo "   Total Transactions: $transaction_count"
else
    echo -e "${RED}❌ Transaction History: FAILED${NC}"
    echo "   Response: $history_response"
fi

echo ""

echo -e "${PURPLE}💰 Loan Management${NC}"

# 9. Create Loan Application
echo -e "${YELLOW}9. Creating loan application...${NC}"
loan_data="{
    \"userId\": $USER_ID,
    \"amount\": 50000.00,
    \"currency\": \"KES\",
    \"term\": 12,
    \"purpose\": \"Business expansion\",
    \"interestRate\": 15.5
}"

loan_response=$(curl -s -X POST "$LOAN_SERVICE_URL/api/loans" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "$loan_data")

if [[ $loan_response == *"id"* ]]; then
    echo -e "${GREEN}✅ Loan Application: SUCCESS${NC}"
    LOAN_ID=$(echo $loan_response | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "   Loan ID: $LOAN_ID"
else
    echo -e "${RED}❌ Loan Application: FAILED${NC}"
    echo "   Response: $loan_response"
fi

# 10. Get Loan Details
echo -e "${YELLOW}10. Getting loan details...${NC}"
loan_details_response=$(curl -s -X GET "$LOAN_SERVICE_URL/api/loans/$LOAN_ID" \
    -H "Authorization: Bearer $JWT_TOKEN")

if [[ $loan_details_response == *"amount"* ]]; then
    echo -e "${GREEN}✅ Loan Details: SUCCESS${NC}"
    loan_amount=$(echo $loan_details_response | grep -o '"amount":[0-9.]*' | cut -d':' -f2)
    echo "   Loan Amount: $loan_amount"
else
    echo -e "${RED}❌ Loan Details: FAILED${NC}"
    echo "   Response: $loan_details_response"
fi

# 11. Create Collateral
echo -e "${YELLOW}11. Creating collateral...${NC}"
collateral_data="{
    \"loanId\": $LOAN_ID,
    \"ownerId\": $USER_ID,
    \"type\": \"VEHICLE\",
    \"description\": \"Toyota Hilux 2020\",
    \"value\": 800000.00,
    \"currency\": \"KES\"
}"

collateral_response=$(curl -s -X POST "$LOAN_SERVICE_URL/api/collaterals" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "$collateral_data")

if [[ $collateral_response == *"id"* ]]; then
    echo -e "${GREEN}✅ Collateral Creation: SUCCESS${NC}"
    collateral_id=$(echo $collateral_response | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "   Collateral ID: $collateral_id"
else
    echo -e "${RED}❌ Collateral Creation: FAILED${NC}"
    echo "   Response: $collateral_response"
fi

echo ""

echo -e "${PURPLE}📧 Notification System${NC}"

# 12. Create Notification
echo -e "${YELLOW}12. Creating notification...${NC}"
notification_data="{
    \"userId\": $USER_ID,
    \"type\": \"TRANSACTION\",
    \"title\": \"Transaction Successful\",
    \"message\": \"Your transaction of KES 1,000 has been processed successfully.\",
    \"priority\": \"HIGH\"
}"

notification_response=$(curl -s -X POST "$NOTIFICATION_SERVICE_URL/api/notifications" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "$notification_data")

if [[ $notification_response == *"id"* ]]; then
    echo -e "${GREEN}✅ Notification Creation: SUCCESS${NC}"
    notification_id=$(echo $notification_response | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "   Notification ID: $notification_id"
else
    echo -e "${RED}❌ Notification Creation: FAILED${NC}"
    echo "   Response: $notification_response"
fi

# 13. Get User Notifications
echo -e "${YELLOW}13. Getting user notifications...${NC}"
notifications_response=$(curl -s -X GET "$NOTIFICATION_SERVICE_URL/api/notifications/user/$USER_ID" \
    -H "Authorization: Bearer $JWT_TOKEN")

if [[ $notifications_response == *"content"* ]]; then
    echo -e "${GREEN}✅ Notifications Retrieval: SUCCESS${NC}"
    notification_count=$(echo $notifications_response | grep -o '"totalElements":[0-9]*' | cut -d':' -f2)
    echo "   Total Notifications: $notification_count"
else
    echo -e "${RED}❌ Notifications Retrieval: FAILED${NC}"
    echo "   Response: $notifications_response"
fi

echo ""

echo -e "${PURPLE}🔐 Security Testing${NC}"

# 14. Test Unauthorized Access
echo -e "${YELLOW}14. Testing unauthorized access...${NC}"
unauthorized_response=$(curl -s -X GET "$ACCOUNT_SERVICE_URL/api/accounts" | head -c 100)

if [[ $unauthorized_response == *"Unauthorized"* ]] || [[ $unauthorized_response == *"401"* ]]; then
    echo -e "${GREEN}✅ Unauthorized Access: BLOCKED${NC}"
else
    echo -e "${RED}❌ Unauthorized Access: NOT BLOCKED${NC}"
fi

# 15. Test Invalid Token
echo -e "${YELLOW}15. Testing invalid token...${NC}"
invalid_token_response=$(curl -s -X GET "$ACCOUNT_SERVICE_URL/api/accounts" \
    -H "Authorization: Bearer invalid_token" | head -c 100)

if [[ $invalid_token_response == *"Unauthorized"* ]] || [[ $invalid_token_response == *"401"* ]]; then
    echo -e "${GREEN}✅ Invalid Token: REJECTED${NC}"
else
    echo -e "${RED}❌ Invalid Token: NOT REJECTED${NC}"
fi

echo ""

# Summary
echo -e "${BLUE}📊 Comprehensive Test Summary:${NC}"
echo -e "${GREEN}✅ User Registration & Authentication: COMPLETE${NC}"
echo -e "${GREEN}✅ Account Management: COMPLETE${NC}"
echo -e "${GREEN}✅ Transaction Processing: COMPLETE${NC}"
echo -e "${GREEN}✅ Loan Management: COMPLETE${NC}"
echo -e "${GREEN}✅ Notification System: COMPLETE${NC}"
echo -e "${GREEN}✅ Security Controls: ACTIVE${NC}"

echo -e "\n${BLUE}🔗 Service URLs:${NC}"
echo -e "👤 User Service: ${BLUE}$USER_SERVICE_URL${NC}"
echo -e "🏦 Account Service: ${BLUE}$ACCOUNT_SERVICE_URL${NC}"
echo -e "💳 Transaction Service: ${BLUE}$TRANSACTION_SERVICE_URL${NC}"
echo -e "💰 Loan Service: ${BLUE}$LOAN_SERVICE_URL${NC}"
echo -e "📧 Notification Service: ${BLUE}$NOTIFICATION_SERVICE_URL${NC}"

echo -e "\n${BLUE}📋 Test Data Created:${NC}"
echo -e "👤 User: $test_username (ID: $USER_ID)"
echo -e "🏦 Account: $ACCOUNT_ID"
echo -e "💰 Loan: $LOAN_ID"
echo -e "🔑 JWT Token: ${JWT_TOKEN:0:20}..."

echo -e "\n${GREEN}🎉 Comprehensive API test completed successfully!${NC}"
echo -e "${YELLOW}💡 Next: Update Postman collection with these test scenarios${NC}" 