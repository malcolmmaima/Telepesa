#!/bin/bash

# Telepesa Test Users Creation Script
# This script creates test users for the Telepesa banking platform

echo "🚀 Creating test users for Telepesa platform..."
echo "================================================"

# Base URL for user service (corrected from /api/v1/users to /api/users)
BASE_URL="http://localhost:8081/api/users"

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Function to create a user
create_user() {
    local username=$1
    local email=$2
    local password=$3
    local firstName=$4
    local lastName=$5
    local phoneNumber=$6
    
    echo -e "${YELLOW}Creating user: $username ($email)${NC}"
    
    response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/register" \
        -H "Content-Type: application/json" \
        -d "{
            \"username\": \"$username\",
            \"email\": \"$email\",
            \"password\": \"$password\",
            \"firstName\": \"$firstName\",
            \"lastName\": \"$lastName\",
            \"phoneNumber\": \"$phoneNumber\"
        }")
    
    # Extract status code from last line
    http_code=$(echo "$response" | tail -n1)
    # Extract response body (all lines except last)
    response_body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -eq 201 ]; then
        echo -e "${GREEN}✅ User $username created successfully!${NC}"
        echo "Response: $response_body"
    else
        echo -e "${RED}❌ Failed to create user $username (HTTP $http_code)${NC}"
        echo "Response: $response_body"
    fi
    
    echo "----------------------------------------"
}

# Test User 1: John Doe (Regular Customer)
create_user "johndoe" "john.doe@example.com" "SecurePass123!" "John" "Doe" "+254700123456"

# Test User 2: Jane Smith (Business Customer)
create_user "janesmith" "jane.smith@business.com" "BusinessPass456!" "Jane" "Smith" "+254700123457"

# Test User 3: Michael Johnson (Premium Customer)
create_user "mikejohnson" "mike.johnson@premium.com" "PremiumPass789!" "Michael" "Johnson" "+254700123458"

# Test User 4: Sarah Wilson (Student)
create_user "sarahwilson" "sarah.wilson@student.com" "StudentPass321!" "Sarah" "Wilson" "+254700123459"

# Test User 5: David Brown (Senior Citizen)
create_user "davidbrown" "david.brown@senior.com" "SeniorPass654!" "David" "Brown" "+254700123460"

# Test User 6: Lisa Davis (Small Business Owner)
create_user "lisadavis" "lisa.davis@smallbiz.com" "SmallBizPass987!" "Lisa" "Davis" "+254700123461"

# Test User 7: Robert Wilson (Freelancer)
create_user "robertwilson" "robert.wilson@freelance.com" "FreelancePass147!" "Robert" "Wilson" "+254700123462"

# Test User 8: Maria Garcia (Teacher)
create_user "mariagarcia" "maria.garcia@teacher.com" "TeacherPass258!" "Maria" "Garcia" "+254700123463"

# Test User 9: James Taylor (Engineer)
create_user "jamestaylor" "james.taylor@engineer.com" "EngineerPass369!" "James" "Taylor" "+254700123464"

# Test User 10: Emily Anderson (Doctor)
create_user "emilyanderson" "emily.anderson@doctor.com" "DoctorPass741!" "Emily" "Anderson" "+254700123465"

echo -e "${GREEN}🎉 Test users creation completed!${NC}"
echo ""
echo "📋 Next Steps:"
echo "1. Check the database to see created users:"
echo "   docker exec -it telepesa-postgres psql -U telepesa -d user_service -c \"SELECT id, username, email, status, created_at FROM users;\""
echo ""
echo "2. Manually activate users (required for login):"
echo "   docker exec -it telepesa-postgres psql -U telepesa -d user_service -c \"UPDATE users SET status = 'ACTIVE' WHERE status = 'PENDING_VERIFICATION';\""
echo ""
echo "3. Test login with any user:"
echo "   curl -X POST http://localhost:8081/api/users/login \\"
echo "     -H \"Content-Type: application/json\" \\"
echo "     -d '{\"usernameOrEmail\":\"johndoe\",\"password\":\"SecurePass123!\"}'"
echo ""
echo "4. Run comprehensive API tests:"
echo "   cd Backend && ./scripts/comprehensive-api-test.sh" 