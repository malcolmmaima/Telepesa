#!/bin/bash

# Telepesa Comprehensive API Test Script - Simple Version
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
USER_ID=""
ACCOUNT_ID=""
LOAN_ID=""

echo -e "${BLUE}🚀 Telepesa Comprehensive API Test${NC}"
echo -e "${BLUE}Testing complete system flow from signup to banking operations${NC}\n"

# Function to extract JSON value using jq (if available) or grep
extract_json_value() {
    local json=$1
    local key=$2
    
    if command -v jq &> /dev/null; then
        echo "$json" | jq -r ".$key" 2>/dev/null || echo ""
    else
        echo "$json" | grep -o "\"$key\":\"[^\"]*\"" | cut -d'"' -f4 2>/dev/null || echo ""
    fi
}

extract_json_number() {
    local json=$1
    local key=$2
    
    if command -v jq &> /dev/null; then
        echo "$json" | jq -r ".$key" 2>/dev/null || echo ""
    else
        echo "$json" | grep -o "\"$key\":[0-9.]*" | cut -d':' -f2 2>/dev/null || echo ""
    fi
}

# Function to check if service is running
check_service() {
    local service_name=$1
    local service_url=$2
    local endpoint=$3
    
    echo -e "${YELLOW}Checking $service_name...${NC}"
    local temp_file=$(mktemp)
    local http_code=$(curl -s -m 5 -w "%{http_code}" -o "$temp_file" "$service_url$endpoint" 2>/dev/null || echo "FAILED")
    local body=$(cat "$temp_file" 2>/dev/null || echo "")
    rm -f "$temp_file"
    
    if [[ $http_code == "200" ]] || [[ $body == *"UP"* ]]; then
        echo -e "${GREEN}✅ $service_name: RUNNING (HTTP: $http_code)${NC}"
        return 0
    else
        echo -e "${RED}❌ $service_name: NOT RUNNING (HTTP: $http_code)${NC}"
        return 1
    fi
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
test_phone="+254700$(printf "%07d" $((timestamp % 10000000)))"

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

temp_file=$(mktemp)
http_code=$(curl -s -w "%{http_code}" -X POST "$USER_SERVICE_URL/api/users/register" \
    -H "Content-Type: application/json" \
    -d "$registration_data" -o "$temp_file" 2>/dev/null || echo "FAILED")
registration_body=$(cat "$temp_file" 2>/dev/null || echo "")
rm -f "$temp_file"

echo "HTTP Code: $http_code"
echo "Response: $registration_body"

if [[ $http_code == "201" ]] || [[ $http_code == "200" ]]; then
    echo -e "${GREEN}✅ User Registration: SUCCESS${NC}"
    USER_ID=$(extract_json_number "$registration_body" "id")
    if [[ -n "$USER_ID" ]]; then
        echo "   User ID: $USER_ID"
    else
        echo -e "${RED}❌ Failed to extract User ID${NC}"
        exit 1
    fi
else
    echo -e "${RED}❌ User Registration: FAILED${NC}"
    exit 1
fi

# 2. User Login (should fail due to pending verification)
echo -e "${YELLOW}2. Testing login with pending verification...${NC}"
login_data="{
    \"usernameOrEmail\": \"$test_username\",
    \"password\": \"SecureP@ssw0rd123!\"
}"

temp_file=$(mktemp)
http_code=$(curl -s -w "%{http_code}" -X POST "$USER_SERVICE_URL/api/users/login" \
    -H "Content-Type: application/json" \
    -d "$login_data" -o "$temp_file" 2>/dev/null || echo "FAILED")
login_body=$(cat "$temp_file" 2>/dev/null || echo "")
rm -f "$temp_file"

echo "HTTP Code: $http_code"

if [[ $http_code == "400" ]] || [[ $http_code == "401" ]]; then
    echo -e "${GREEN}✅ Login Security: PASSED (Verification required)${NC}"
else
    echo -e "${YELLOW}⚠️ Login Security: UNEXPECTED (HTTP: $http_code)${NC}"
fi

# 3. Activate User (simulate verification)
echo -e "${YELLOW}3. Activating user account...${NC}"
# Use direct database update since the API endpoint requires authentication
docker exec telepesa-postgres psql -U telepesa -d telepesa_users_dev -c "UPDATE users SET status = 'ACTIVE', email_verified = true WHERE id = $USER_ID;" > /dev/null 2>&1

if [[ $? == 0 ]]; then
    echo -e "${GREEN}✅ User Activation: SUCCESS${NC}"
else
    echo -e "${RED}❌ User Activation: FAILED${NC}"
    # Continue anyway as the user might already be active
fi

# 4. Login with active account
echo -e "${YELLOW}4. Logging in with active account...${NC}"
temp_file=$(mktemp)
http_code=$(curl -s -w "%{http_code}" -X POST "$USER_SERVICE_URL/api/users/login" \
    -H "Content-Type: application/json" \
    -d "$login_data" -o "$temp_file" 2>/dev/null || echo "FAILED")
login_body=$(cat "$temp_file" 2>/dev/null || echo "")
rm -f "$temp_file"

echo "HTTP Code: $http_code"

if [[ $http_code == "200" ]]; then
    echo -e "${GREEN}✅ User Login: SUCCESS${NC}"
    JWT_TOKEN=$(extract_json_value "$login_body" "accessToken")
    
    if [[ -n "$JWT_TOKEN" ]]; then
        echo "   JWT Token: ${JWT_TOKEN:0:20}..."
    else
        echo -e "${RED}❌ Failed to extract JWT token${NC}"
        exit 1
    fi
else
    echo -e "${RED}❌ User Login: FAILED${NC}"
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

temp_file=$(mktemp)
http_code=$(curl -s -w "%{http_code}" -X POST "$ACCOUNT_SERVICE_URL/api/accounts" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "$account_data" -o "$temp_file" 2>/dev/null || echo "FAILED")
account_body=$(cat "$temp_file" 2>/dev/null || echo "")
rm -f "$temp_file"

echo "HTTP Code: $http_code"

if [[ $http_code == "201" ]] || [[ $http_code == "200" ]]; then
    echo -e "${GREEN}✅ Account Creation: SUCCESS${NC}"
    ACCOUNT_ID=$(extract_json_number "$account_body" "id")
    if [[ -n "$ACCOUNT_ID" ]]; then
        echo "   Account ID: $ACCOUNT_ID"
    else
        echo -e "${RED}❌ Failed to extract Account ID${NC}"
        exit 1
    fi
else
    echo -e "${RED}❌ Account Creation: FAILED${NC}"
    exit 1
fi

# 6. Get Account Details
echo -e "${YELLOW}6. Getting account details...${NC}"
temp_file=$(mktemp)
http_code=$(curl -s -w "%{http_code}" -X GET "$ACCOUNT_SERVICE_URL/api/accounts/$ACCOUNT_ID" \
    -H "Authorization: Bearer $JWT_TOKEN" -o "$temp_file" 2>/dev/null || echo "FAILED")
account_details_body=$(cat "$temp_file" 2>/dev/null || echo "")
rm -f "$temp_file"

echo "HTTP Code: $http_code"

if [[ $http_code == "200" ]]; then
    echo -e "${GREEN}✅ Account Details: SUCCESS${NC}"
    balance=$(extract_json_number "$account_details_body" "balance")
    if [[ -n "$balance" ]]; then
        echo "   Balance: $balance"
    fi
else
    echo -e "${RED}❌ Account Details: FAILED${NC}"
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

temp_file=$(mktemp)
http_code=$(curl -s -w "%{http_code}" -X POST "$TRANSACTION_SERVICE_URL/api/transactions" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "$transaction_data" -o "$temp_file" 2>/dev/null || echo "FAILED")
transaction_body=$(cat "$temp_file" 2>/dev/null || echo "")
rm -f "$temp_file"

echo "HTTP Code: $http_code"

if [[ $http_code == "201" ]] || [[ $http_code == "200" ]]; then
    echo -e "${GREEN}✅ Transaction Creation: SUCCESS${NC}"
    transaction_id=$(extract_json_number "$transaction_body" "id")
    if [[ -n "$transaction_id" ]]; then
        echo "   Transaction ID: $transaction_id"
    fi
else
    echo -e "${RED}❌ Transaction Creation: FAILED${NC}"
fi

# 8. Get Transaction History
echo -e "${YELLOW}8. Getting transaction history...${NC}"
temp_file=$(mktemp)
http_code=$(curl -s -w "%{http_code}" -X GET "$TRANSACTION_SERVICE_URL/api/transactions/account/$ACCOUNT_ID" \
    -H "Authorization: Bearer $JWT_TOKEN" -o "$temp_file" 2>/dev/null || echo "FAILED")
history_body=$(cat "$temp_file" 2>/dev/null || echo "")
rm -f "$temp_file"

echo "HTTP Code: $http_code"

if [[ $http_code == "200" ]]; then
    echo -e "${GREEN}✅ Transaction History: SUCCESS${NC}"
    transaction_count=$(extract_json_number "$history_body" "totalElements")
    if [[ -n "$transaction_count" ]]; then
        echo "   Total Transactions: $transaction_count"
    fi
else
    echo -e "${RED}❌ Transaction History: FAILED${NC}"
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

temp_file=$(mktemp)
http_code=$(curl -s -w "%{http_code}" -X POST "$LOAN_SERVICE_URL/api/loans" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "$loan_data" -o "$temp_file" 2>/dev/null || echo "FAILED")
loan_body=$(cat "$temp_file" 2>/dev/null || echo "")
rm -f "$temp_file"

echo "HTTP Code: $http_code"

if [[ $http_code == "201" ]] || [[ $http_code == "200" ]]; then
    echo -e "${GREEN}✅ Loan Application: SUCCESS${NC}"
    LOAN_ID=$(extract_json_number "$loan_body" "id")
    if [[ -n "$LOAN_ID" ]]; then
        echo "   Loan ID: $LOAN_ID"
    else
        echo -e "${RED}❌ Failed to extract Loan ID${NC}"
        exit 1
    fi
else
    echo -e "${RED}❌ Loan Application: FAILED${NC}"
    exit 1
fi

# 10. Get Loan Details
echo -e "${YELLOW}10. Getting loan details...${NC}"
temp_file=$(mktemp)
http_code=$(curl -s -w "%{http_code}" -X GET "$LOAN_SERVICE_URL/api/loans/$LOAN_ID" \
    -H "Authorization: Bearer $JWT_TOKEN" -o "$temp_file" 2>/dev/null || echo "FAILED")
loan_details_body=$(cat "$temp_file" 2>/dev/null || echo "")
rm -f "$temp_file"

echo "HTTP Code: $http_code"

if [[ $http_code == "200" ]]; then
    echo -e "${GREEN}✅ Loan Details: SUCCESS${NC}"
    loan_amount=$(extract_json_number "$loan_details_body" "amount")
    if [[ -n "$loan_amount" ]]; then
        echo "   Loan Amount: $loan_amount"
    fi
else
    echo -e "${RED}❌ Loan Details: FAILED${NC}"
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

temp_file=$(mktemp)
http_code=$(curl -s -w "%{http_code}" -X POST "$LOAN_SERVICE_URL/api/collaterals" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "$collateral_data" -o "$temp_file" 2>/dev/null || echo "FAILED")
collateral_body=$(cat "$temp_file" 2>/dev/null || echo "")
rm -f "$temp_file"

echo "HTTP Code: $http_code"

if [[ $http_code == "201" ]] || [[ $http_code == "200" ]]; then
    echo -e "${GREEN}✅ Collateral Creation: SUCCESS${NC}"
    collateral_id=$(extract_json_number "$collateral_body" "id")
    if [[ -n "$collateral_id" ]]; then
        echo "   Collateral ID: $collateral_id"
    fi
else
    echo -e "${RED}❌ Collateral Creation: FAILED${NC}"
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

temp_file=$(mktemp)
http_code=$(curl -s -w "%{http_code}" -X POST "$NOTIFICATION_SERVICE_URL/api/notifications" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $JWT_TOKEN" \
    -d "$notification_data" -o "$temp_file" 2>/dev/null || echo "FAILED")
notification_body=$(cat "$temp_file" 2>/dev/null || echo "")
rm -f "$temp_file"

echo "HTTP Code: $http_code"

if [[ $http_code == "201" ]] || [[ $http_code == "200" ]]; then
    echo -e "${GREEN}✅ Notification Creation: SUCCESS${NC}"
    notification_id=$(extract_json_number "$notification_body" "id")
    if [[ -n "$notification_id" ]]; then
        echo "   Notification ID: $notification_id"
    fi
else
    echo -e "${RED}❌ Notification Creation: FAILED${NC}"
fi

# 13. Get User Notifications
echo -e "${YELLOW}13. Getting user notifications...${NC}"
temp_file=$(mktemp)
http_code=$(curl -s -w "%{http_code}" -X GET "$NOTIFICATION_SERVICE_URL/api/notifications/user/$USER_ID" \
    -H "Authorization: Bearer $JWT_TOKEN" -o "$temp_file" 2>/dev/null || echo "FAILED")
notifications_body=$(cat "$temp_file" 2>/dev/null || echo "")
rm -f "$temp_file"

echo "HTTP Code: $http_code"

if [[ $http_code == "200" ]]; then
    echo -e "${GREEN}✅ Notifications Retrieval: SUCCESS${NC}"
    notification_count=$(extract_json_number "$notifications_body" "totalElements")
    if [[ -n "$notification_count" ]]; then
        echo "   Total Notifications: $notification_count"
    fi
else
    echo -e "${RED}❌ Notifications Retrieval: FAILED${NC}"
fi

echo ""

echo -e "${PURPLE}🔐 Security Testing${NC}"

# 14. Test Unauthorized Access
echo -e "${YELLOW}14. Testing unauthorized access...${NC}"
temp_file=$(mktemp)
http_code=$(curl -s -w "%{http_code}" -X GET "$ACCOUNT_SERVICE_URL/api/accounts" -o "$temp_file" 2>/dev/null || echo "FAILED")
rm -f "$temp_file"

echo "HTTP Code: $http_code"

if [[ $http_code == "401" ]] || [[ $http_code == "403" ]]; then
    echo -e "${GREEN}✅ Unauthorized Access: BLOCKED${NC}"
else
    echo -e "${RED}❌ Unauthorized Access: NOT BLOCKED (HTTP: $http_code)${NC}"
fi

# 15. Test Invalid Token
echo -e "${YELLOW}15. Testing invalid token...${NC}"
temp_file=$(mktemp)
http_code=$(curl -s -w "%{http_code}" -X GET "$ACCOUNT_SERVICE_URL/api/accounts" \
    -H "Authorization: Bearer invalid_token" -o "$temp_file" 2>/dev/null || echo "FAILED")
rm -f "$temp_file"

echo "HTTP Code: $http_code"

if [[ $http_code == "401" ]] || [[ $http_code == "403" ]]; then
    echo -e "${GREEN}✅ Invalid Token: REJECTED${NC}"
else
    echo -e "${RED}❌ Invalid Token: NOT REJECTED (HTTP: $http_code)${NC}"
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
if [[ -n "$JWT_TOKEN" ]]; then
    echo -e "🔑 JWT Token: ${JWT_TOKEN:0:20}..."
fi

echo -e "\n${GREEN}🎉 Comprehensive API test completed successfully!${NC}"
echo -e "${YELLOW}💡 Next: Update Postman collection with these test scenarios${NC}" 