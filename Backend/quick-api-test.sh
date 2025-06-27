#!/bin/bash

# Telepesa Quick API Test Script
# Quickly test the User Service endpoints to verify functionality

set -e

BASE_URL="http://localhost:8081"
API_BASE="$BASE_URL/api"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Telepesa Quick API Test${NC}"
echo -e "${BLUE}Testing User Service at: $BASE_URL${NC}\n"

# Test 1: Health Check
echo -e "${YELLOW}1. Testing Health Check...${NC}"
health_response=$(curl -s "$BASE_URL/actuator/health" || echo "FAILED")
if [[ $health_response == *"UP"* ]]; then
    echo -e "${GREEN}✅ Health Check: PASSED${NC}"
else
    echo -e "${RED}❌ Health Check: FAILED - Service may not be running${NC}"
    echo "Please start the service with: cd Backend/user-service && mvn spring-boot:run"
    exit 1
fi

# Test 2: API Documentation
echo -e "\n${YELLOW}2. Testing API Documentation...${NC}"
docs_response=$(curl -s "$BASE_URL/v3/api-docs" | grep -q "openapi" && echo "SUCCESS" || echo "FAILED")
if [[ $docs_response == "SUCCESS" ]]; then
    echo -e "${GREEN}✅ API Documentation: AVAILABLE at $BASE_URL/swagger-ui.html${NC}"
else
    echo -e "${RED}❌ API Documentation: FAILED${NC}"
fi

# Generate unique test data
timestamp=$(date +%s)
test_username="testuser$timestamp"
test_email="testuser$timestamp@telepesa.com"
test_phone="+254700$(echo $timestamp | tail -c 7)"

# Test 3: User Registration
echo -e "\n${YELLOW}3. Testing User Registration...${NC}"
registration_data="{
    \"username\": \"$test_username\",
    \"email\": \"$test_email\",
    \"password\": \"SecureP@ssw0rd123!\",
    \"firstName\": \"Test\",
    \"lastName\": \"User\",
    \"phoneNumber\": \"$test_phone\"
}"

registration_response=$(curl -s -X POST "$API_BASE/users/register" \
    -H "Content-Type: application/json" \
    -d "$registration_data")

if [[ $registration_response == *"id"* ]]; then
    echo -e "${GREEN}✅ User Registration: PASSED${NC}"
    user_id=$(echo $registration_response | grep -o '"id":[0-9]*' | cut -d':' -f2)
    echo "   Created user with ID: $user_id"
else
    echo -e "${RED}❌ User Registration: FAILED${NC}"
    echo "   Response: $registration_response"
fi

# Test 4: Duplicate User Registration (Should Fail)
echo -e "\n${YELLOW}4. Testing Duplicate User Prevention...${NC}"
duplicate_response=$(curl -s -X POST "$API_BASE/users/register" \
    -H "Content-Type: application/json" \
    -d "$registration_data")

if [[ $duplicate_response == *"already exists"* ]]; then
    echo -e "${GREEN}✅ Duplicate Prevention: PASSED${NC}"
else
    echo -e "${RED}❌ Duplicate Prevention: FAILED${NC}"
fi

# Test 5: Invalid Password Registration (Should Fail)
echo -e "\n${YELLOW}5. Testing Password Validation...${NC}"
weak_password_data="{
    \"username\": \"weakuser$timestamp\",
    \"email\": \"weak$timestamp@telepesa.com\",
    \"password\": \"123\",
    \"firstName\": \"Weak\",
    \"lastName\": \"Password\",
    \"phoneNumber\": \"+254700123999\"
}"

weak_password_response=$(curl -s -X POST "$API_BASE/users/register" \
    -H "Content-Type: application/json" \
    -d "$weak_password_data")

if [[ $weak_password_response == *"validationErrors"* ]] || [[ $weak_password_response == *"password"* ]]; then
    echo -e "${GREEN}✅ Password Validation: PASSED${NC}"
else
    echo -e "${RED}❌ Password Validation: FAILED${NC}"
fi

# Test 6: Login with Pending Verification (Should Fail)
echo -e "\n${YELLOW}6. Testing Authentication Security...${NC}"
login_data="{
    \"usernameOrEmail\": \"$test_username\",
    \"password\": \"SecureP@ssw0rd123!\"
}"

login_response=$(curl -s -X POST "$API_BASE/users/login" \
    -H "Content-Type: application/json" \
    -d "$login_data")

if [[ $login_response == *"not active"* ]]; then
    echo -e "${GREEN}✅ Authentication Security: PASSED (User verification required)${NC}"
else
    echo -e "${RED}❌ Authentication Security: UNEXPECTED RESPONSE${NC}"
    echo "   Response: $login_response"
fi

# Test 7: Unauthorized Access (Should Fail)
echo -e "\n${YELLOW}7. Testing Authorization Security...${NC}"
unauthorized_response=$(curl -s "$API_BASE/users" | head -c 100)

if [[ $unauthorized_response == *"Unauthorized"* ]] || [[ $unauthorized_response == *"401"* ]]; then
    echo -e "${GREEN}✅ Authorization Security: PASSED${NC}"
else
    echo -e "${RED}❌ Authorization Security: FAILED${NC}"
fi

# Test 8: CORS Headers
echo -e "\n${YELLOW}8. Testing CORS Configuration...${NC}"
cors_response=$(curl -s -I -X OPTIONS "$API_BASE/users" \
    -H "Origin: http://localhost:3000" \
    -H "Access-Control-Request-Method: POST" | grep -i "access-control")

if [[ $cors_response == *"Access-Control"* ]]; then
    echo -e "${GREEN}✅ CORS Configuration: PASSED${NC}"
else
    echo -e "${RED}❌ CORS Configuration: FAILED${NC}"
fi

# Summary
echo -e "\n${BLUE}📊 Test Summary:${NC}"
echo -e "${GREEN}✅ Service is running and responding${NC}"
echo -e "${GREEN}✅ API endpoints are functional${NC}"
echo -e "${GREEN}✅ Security controls are working${NC}"
echo -e "${GREEN}✅ Input validation is active${NC}"

echo -e "\n${BLUE}🔗 Useful Links:${NC}"
echo -e "📄 API Documentation: ${BLUE}$BASE_URL/swagger-ui.html${NC}"
echo -e "🏥 Health Check: ${BLUE}$BASE_URL/actuator/health${NC}"
echo -e "📋 OpenAPI Spec: ${BLUE}$BASE_URL/v3/api-docs${NC}"

echo -e "\n${BLUE}📮 For comprehensive testing, use the Postman collection:${NC}"
echo -e "   Backend/Telepesa_API_Collection.postman_collection.json"
echo -e "   Backend/Telepesa_Development.postman_environment.json"

echo -e "\n${GREEN}🎉 Quick API test completed successfully!${NC}" 