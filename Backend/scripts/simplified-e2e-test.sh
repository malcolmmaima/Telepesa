#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print test results
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✅ PASS: $2${NC}"
    else
        echo -e "${RED}❌ FAIL: $2${NC}"
    fi
}

echo "====================================="
echo "   TELEPESA SIMPLIFIED E2E TEST"
echo "====================================="

echo -e "\n${BLUE}1. USER SERVICE TESTS${NC}"
echo "=================================="

echo -e "${YELLOW}🧪 Testing user service...${NC}"

# Test user registration
mvn -f user-service/pom.xml -Dtest=UserServiceTest#createUser_WithValidRequest_ShouldReturnUserDto test -q > /dev/null 2>&1
print_result $? "User registration"

# Test authentication
mvn -f user-service/pom.xml -Dtest=UserServiceTest#authenticateUser_WithValidCredentials_ShouldReturnLoginResponse test -q > /dev/null 2>&1
print_result $? "User authentication"

# Test validation
mvn -f user-service/pom.xml -Dtest=UserControllerTest#registerUser_WithInvalidData_ShouldReturnBadRequest test -q > /dev/null 2>&1
print_result $? "Input validation"

echo -e "\n${BLUE}2. ACCOUNT SERVICE TESTS${NC}"
echo "=================================="

if [ -d "account-service" ]; then
    echo -e "${YELLOW}🧪 Testing account service...${NC}"
    
    # Test account creation
    mvn -f account-service/pom.xml -Dtest=AccountServiceTest#createAccount_WithValidRequest_ShouldReturnAccountDto test -q > /dev/null 2>&1
    print_result $? "Account creation"
    
    # Test account balance
    mvn -f account-service/pom.xml -Dtest=AccountServiceTest#getAccountBalance_WithValidAccountId_ShouldReturnBalance test -q > /dev/null 2>&1
    print_result $? "Account balance retrieval"
else
    echo -e "${YELLOW}⏭️  SKIP: Account service not implemented${NC}"
fi

echo -e "\n${BLUE}3. TRANSACTION SERVICE TESTS${NC}"
echo "=================================="

if [ -d "transaction-service" ]; then
    echo -e "${YELLOW}🧪 Testing transaction service...${NC}"
    
    # Test transaction creation
    mvn -f transaction-service/pom.xml -Dtest=TransactionServiceImplTest#createTransaction_WithValidRequest_ShouldReturnTransactionDto test -q > /dev/null 2>&1
    print_result $? "Transaction creation"
    
    # Test transaction history
    mvn -f transaction-service/pom.xml -Dtest=TransactionServiceImplTest#getTransactionsByUserId_ShouldReturnPagedTransactions test -q > /dev/null 2>&1
    print_result $? "Transaction history"
else
    echo -e "${YELLOW}⏭️  SKIP: Transaction service not implemented${NC}"
fi

echo -e "\n${BLUE}4. INTEGRATION TESTS${NC}"
echo "=================================="

echo -e "${YELLOW}🧪 Running full test suites...${NC}"

# Run user service tests
mvn -f user-service/pom.xml test -q > /dev/null 2>&1
user_result=$?
print_result $user_result "User service test suite"

# Run account service tests if it exists
if [ -d "account-service" ]; then
    mvn -f account-service/pom.xml test -q > /dev/null 2>&1
    account_result=$?
    print_result $account_result "Account service test suite"
else
    account_result=0
fi

# Run transaction service tests if it exists
if [ -d "transaction-service" ]; then
    mvn -f transaction-service/pom.xml test -q > /dev/null 2>&1
    transaction_result=$?
    print_result $transaction_result "Transaction service test suite"
else
    transaction_result=0
fi

# Overall result
echo -e "\n${BLUE}=====================================${NC}"
echo -e "${BLUE}      TEST RESULTS SUMMARY${NC}"
echo -e "${BLUE}=====================================${NC}"

overall_result=$((user_result + account_result + transaction_result))

if [ $overall_result -eq 0 ]; then
    echo -e "${GREEN}🎉 ALL TESTS PASSED!${NC}"
    echo -e "${GREEN}✅ User Service: OPERATIONAL${NC}"
    if [ -d "account-service" ]; then
        echo -e "${GREEN}✅ Account Service: OPERATIONAL${NC}"
    fi
    if [ -d "transaction-service" ]; then
        echo -e "${GREEN}✅ Transaction Service: OPERATIONAL${NC}"
    fi
    echo ""
    echo -e "${GREEN}🏦 TELEPESA BANKING PLATFORM IS READY!${NC}"
else
    echo -e "${RED}❌ SOME TESTS FAILED${NC}"
    echo -e "${RED}Please review the test output and fix any issues${NC}"
    exit 1
fi

echo ""
echo -e "${BLUE}Test completed at: $(date)${NC}" 