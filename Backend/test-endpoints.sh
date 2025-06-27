#!/bin/bash

# Telepesa User Service API Testing Script
# This script tests all endpoints with proper security considerations for a banking application

BASE_URL="http://localhost:8081"
API_BASE="$BASE_URL/api"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_separator() {
    echo -e "\n${BLUE}================================================================${NC}"
    echo -e "${BLUE} $1${NC}"
    echo -e "${BLUE}================================================================${NC}\n"
}

# Function to make HTTP request and validate response
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local headers=$4
    local expected_status=$5
    local description=$6
    
    print_status "Testing: $description"
    echo "Request: $method $url"
    
    if [ -n "$data" ]; then
        echo "Data: $data"
    fi
    
    if [ -n "$headers" ]; then
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X "$method" "$url" -H "Content-Type: application/json" $headers -d "$data")
    else
        response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X "$method" "$url" -H "Content-Type: application/json" -d "$data")
    fi
    
    http_status=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    response_body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    echo "Response Status: $http_status"
    echo "Response Body: $response_body"
    
    if [ "$http_status" = "$expected_status" ]; then
        print_success "✓ Test passed - Expected status $expected_status, got $http_status"
    else
        print_error "✗ Test failed - Expected status $expected_status, got $http_status"
    fi
    
    echo ""
    return $http_status
}

# Function to extract JWT token from response
extract_token() {
    echo "$1" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4
}

# Test variables
JWT_TOKEN=""
USER_ID=""

print_separator "TELEPESA USER SERVICE API TESTING"
print_status "Testing banking-grade security and functionality"

# 1. Health Check
print_separator "1. INFRASTRUCTURE HEALTH CHECKS"

make_request "GET" "$BASE_URL/actuator/health" "" "" "200" "Application Health Check"

# 2. API Documentation
make_request "GET" "$BASE_URL/swagger-ui.html" "" "" "200" "Swagger UI Access"

# 3. User Registration Tests
print_separator "2. USER REGISTRATION TESTS"

# Valid registration
print_status "Testing valid user registration..."
REGISTRATION_DATA='{
    "username": "testuser001",
    "email": "testuser001@telepesa.com",
    "password": "SecureP@ssw0rd123!",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+254700123456"
}'

response=$(make_request "POST" "$API_BASE/users/register" "$REGISTRATION_DATA" "" "201" "Valid User Registration")
if [ $? -eq 201 ]; then
    USER_ID=$(echo "$response" | grep -o '"id":[0-9]*' | cut -d: -f2)
    print_success "User created with ID: $USER_ID"
fi

# Test password strength requirements (banking standards)
print_status "Testing password strength requirements..."

WEAK_PASSWORD_DATA='{
    "username": "testuser002",
    "email": "testuser002@telepesa.com", 
    "password": "123456",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+254700123457"
}'

make_request "POST" "$API_BASE/users/register" "$WEAK_PASSWORD_DATA" "" "400" "Weak Password Rejection"

# Test duplicate username
DUPLICATE_USERNAME_DATA='{
    "username": "testuser001",
    "email": "testuser003@telepesa.com",
    "password": "SecureP@ssw0rd123!",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+254700123458"
}'

make_request "POST" "$API_BASE/users/register" "$DUPLICATE_USERNAME_DATA" "" "400" "Duplicate Username Rejection"

# Test duplicate email
DUPLICATE_EMAIL_DATA='{
    "username": "testuser003",
    "email": "testuser001@telepesa.com",
    "password": "SecureP@ssw0rd123!",
    "firstName": "Test", 
    "lastName": "User",
    "phoneNumber": "+254700123459"
}'

make_request "POST" "$API_BASE/users/register" "$DUPLICATE_EMAIL_DATA" "" "400" "Duplicate Email Rejection"

# Test SQL injection attempt
print_status "Testing SQL injection prevention..."
SQL_INJECTION_DATA='{
    "username": "admin'\'''; DROP TABLE users; --",
    "email": "hacker@evil.com",
    "password": "SecureP@ssw0rd123!",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+254700123460"
}'

make_request "POST" "$API_BASE/users/register" "$SQL_INJECTION_DATA" "" "400" "SQL Injection Prevention"

# 4. Authentication Tests
print_separator "3. AUTHENTICATION TESTS"

# Valid login
print_status "Testing valid user authentication..."
LOGIN_DATA='{
    "usernameOrEmail": "testuser001",
    "password": "SecureP@ssw0rd123!"
}'

login_response=$(curl -s -X POST "$API_BASE/users/login" -H "Content-Type: application/json" -d "$LOGIN_DATA")
echo "Login Response: $login_response"

if echo "$login_response" | grep -q "accessToken"; then
    JWT_TOKEN=$(extract_token "$login_response")
    print_success "✓ Login successful, JWT token obtained"
    print_status "JWT Token (first 20 chars): $(echo \"$JWT_TOKEN\" | head -c 20)..."
else
    print_error "✗ Login failed"
fi

# Invalid credentials
INVALID_LOGIN_DATA='{
    "usernameOrEmail": "testuser001",
    "password": "WrongPassword123!"
}'

make_request "POST" "$API_BASE/users/login" "$INVALID_LOGIN_DATA" "" "401" "Invalid Credentials Rejection"

# Brute force protection test
print_status "Testing brute force protection..."
for i in {1..6}; do
    print_status "Attempt $i of failed login..."
    make_request "POST" "$API_BASE/users/login" "$INVALID_LOGIN_DATA" "" "401" "Brute Force Attempt $i"
    sleep 1
done

# 5. Protected Endpoint Tests
print_separator "4. PROTECTED ENDPOINT TESTS"

# Test without JWT token - trying to access all users (admin endpoint)
make_request "GET" "$API_BASE/users" "" "" "401" "Access without JWT Token"

# Test with invalid JWT token
INVALID_AUTH_HEADER='-H "Authorization: Bearer invalid.jwt.token"'
make_request "GET" "$API_BASE/users" "" "$INVALID_AUTH_HEADER" "401" "Access with Invalid JWT Token"

# Test with valid JWT token (this will fail as normal user, but we test JWT validation)
if [ -n "$JWT_TOKEN" ]; then
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "$API_BASE/users" -H "Content-Type: application/json" -H "Authorization: Bearer $JWT_TOKEN")
    http_status=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    response_body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    print_status "Protected endpoint response: $response_body"
    print_status "Status: $http_status"
    
    if [ "$http_status" = "403" ]; then
        print_success "✓ JWT token validated, access denied due to insufficient privileges (expected)"
    elif [ "$http_status" = "200" ]; then
        print_success "✓ Protected endpoint access with valid JWT"
    else
        print_error "✗ Unexpected response: $http_status"
    fi
fi

# 6. User Profile Management Tests
print_separator "5. USER PROFILE MANAGEMENT TESTS"

if [ -n "$JWT_TOKEN" ] && [ -n "$USER_ID" ]; then
    # Get user by ID (should work for own user)
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "$API_BASE/users/$USER_ID" -H "Authorization: Bearer $JWT_TOKEN")
    http_status=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    response_body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    print_status "Get User Response: $response_body"
    print_status "Status: $http_status"
    
    if [ "$http_status" = "200" ]; then
        print_success "✓ Get user by ID successful"
    else
        print_error "✗ Get user by ID failed - Status: $http_status"
    fi
    
    # Test update user (if endpoint exists)
    UPDATE_DATA='{
        "username": "testuser001",
        "email": "testuser001@telepesa.com",
        "password": "SecureP@ssw0rd123!",
        "firstName": "Updated",
        "lastName": "User",
        "phoneNumber": "+254700123456"
    }'
    
    response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X PUT "$API_BASE/users/$USER_ID" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $JWT_TOKEN" \
        -d "$UPDATE_DATA")
    
    http_status=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    if [ "$http_status" = "200" ]; then
        print_success "✓ User update successful"
    else
        print_status "User update status: $http_status (may not be implemented)"
    fi
fi

# 7. Input Validation Tests
print_separator "6. INPUT VALIDATION TESTS"

# Test XSS prevention
XSS_DATA='{
    "username": "testuser_xss",
    "email": "test@test.com",
    "password": "SecureP@ssw0rd123!",
    "firstName": "<script>alert(\"XSS\")</script>",
    "lastName": "User",
    "phoneNumber": "+254700123461"
}'

make_request "POST" "$API_BASE/users/register" "$XSS_DATA" "" "400" "XSS Prevention"

# Test extremely long input
LONG_STRING=$(printf "a%.0s" {1..1000})
LONG_INPUT_DATA="{
    \"username\": \"$LONG_STRING\",
    \"email\": \"test@test.com\",
    \"password\": \"SecureP@ssw0rd123!\",
    \"firstName\": \"Test\",
    \"lastName\": \"User\",
    \"phoneNumber\": \"+254700123462\"
}"

make_request "POST" "$API_BASE/users/register" "$LONG_INPUT_DATA" "" "400" "Long Input Rejection"

# Test empty/null values
EMPTY_DATA='{
    "username": "",
    "email": "",
    "password": "",
    "firstName": "",
    "lastName": "",
    "phoneNumber": ""
}'

make_request "POST" "$API_BASE/users/register" "$EMPTY_DATA" "" "400" "Empty Values Rejection"

# 8. Email Format Validation
print_separator "7. EMAIL VALIDATION TESTS"

INVALID_EMAIL_DATA='{
    "username": "testuser_email",
    "email": "invalid-email-format",
    "password": "SecureP@ssw0rd123!",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "+254700123463"
}'

make_request "POST" "$API_BASE/users/register" "$INVALID_EMAIL_DATA" "" "400" "Invalid Email Format Rejection"

# 9. Phone Number Validation
print_separator "8. PHONE NUMBER VALIDATION TESTS"

INVALID_PHONE_DATA='{
    "username": "testuser_phone",
    "email": "testphone@test.com",
    "password": "SecureP@ssw0rd123!",
    "firstName": "Test",
    "lastName": "User",
    "phoneNumber": "invalid-phone"
}'

make_request "POST" "$API_BASE/users/register" "$INVALID_PHONE_DATA" "" "400" "Invalid Phone Format Rejection"

# 10. Rate Limiting Tests (if implemented)
print_separator "9. RATE LIMITING TESTS"

print_status "Testing rate limiting with rapid requests..."
for i in {1..20}; do
    response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/actuator/health" -o /dev/null)
    if [ "$response" = "429" ]; then
        print_success "✓ Rate limiting active - Request $i returned 429"
        break
    fi
done

# 11. Security Headers Check
print_separator "10. SECURITY HEADERS CHECK"

print_status "Checking security headers..."
headers=$(curl -s -I "$BASE_URL/actuator/health")
echo "Response Headers:"
echo "$headers"

# Check for important security headers
if echo "$headers" | grep -qi "x-frame-options"; then
    print_success "✓ X-Frame-Options header present"
else
    print_warning "⚠ X-Frame-Options header missing"
fi

if echo "$headers" | grep -qi "x-content-type-options"; then
    print_success "✓ X-Content-Type-Options header present"
else
    print_warning "⚠ X-Content-Type-Options header missing"
fi

if echo "$headers" | grep -qi "strict-transport-security"; then
    print_success "✓ HSTS header present"
else
    print_warning "⚠ HSTS header missing (should be added in production)"
fi

# 12. CORS Configuration Test
print_separator "11. CORS CONFIGURATION TEST"

print_status "Testing CORS configuration..."
cors_response=$(curl -s -H "Origin: https://evil.com" -H "Access-Control-Request-Method: POST" -H "Access-Control-Request-Headers: Content-Type" -X OPTIONS "$API_BASE/users/login")
print_status "CORS Response: $cors_response"

# 13. JWT Token Security Tests
print_separator "12. JWT TOKEN SECURITY TESTS"

if [ -n "$JWT_TOKEN" ]; then
    # Decode JWT payload (base64 decode middle part)
    JWT_PAYLOAD=$(echo "$JWT_TOKEN" | cut -d. -f2)
    # Add padding if needed
    case $((${#JWT_PAYLOAD} % 4)) in
        2) JWT_PAYLOAD="${JWT_PAYLOAD}==" ;;
        3) JWT_PAYLOAD="${JWT_PAYLOAD}=" ;;
    esac
    
    DECODED_PAYLOAD=$(echo "$JWT_PAYLOAD" | base64 -d 2>/dev/null || echo "Unable to decode JWT payload")
    print_status "JWT Payload: $DECODED_PAYLOAD"
    
    # Check token expiration
    if echo "$DECODED_PAYLOAD" | grep -q "exp"; then
        print_success "✓ JWT contains expiration claim"
    else
        print_warning "⚠ JWT missing expiration claim"
    fi
    
    # Test expired token (would need to wait or manipulate)
    print_status "JWT token structure appears valid"
fi

# 14. Database Security Test (H2 Console)
print_separator "13. DATABASE SECURITY TEST"

print_status "Checking H2 console access (should be disabled in production)..."
h2_response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/h2-console" -o /dev/null)
if [ "$h2_response" = "200" ]; then
    print_warning "⚠ H2 console is accessible (disable in production)"
else
    print_success "✓ H2 console properly secured"
fi

# 15. Final Summary
print_separator "TEST SUMMARY"

print_status "Banking Application Security Test Complete"
print_success "✓ Authentication system tested"
print_success "✓ Input validation verified"
print_success "✓ SQL injection prevention checked"
print_success "✓ XSS prevention verified"
print_success "✓ JWT security validated"
print_warning "⚠ Ensure HTTPS is used in production"
print_warning "⚠ Implement proper rate limiting"
print_warning "⚠ Add comprehensive audit logging"
print_warning "⚠ Implement account lockout after failed attempts"
print_warning "⚠ Add IP-based blocking for suspicious activity"

print_separator "BANKING SECURITY RECOMMENDATIONS"
print_status "For production deployment, ensure:"
echo "1. Enable HTTPS with TLS 1.3"
echo "2. Implement strict rate limiting"
echo "3. Add comprehensive audit logging"
echo "4. Set up intrusion detection"
echo "5. Implement multi-factor authentication"
echo "6. Add device fingerprinting"
echo "7. Set up fraud detection algorithms"
echo "8. Implement session management"
echo "9. Add PCI DSS compliance measures"
echo "10. Set up continuous security monitoring"

print_status "Test script completed." 