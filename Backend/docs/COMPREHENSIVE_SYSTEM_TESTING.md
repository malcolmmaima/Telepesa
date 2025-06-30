# Telepesa Comprehensive System Testing Guide

## Overview
This document provides a complete testing framework for the Telepesa banking platform, covering all microservices and their interactions through the API Gateway.

## System Architecture for Testing
```
┌─────────────────────────────────────────────────────┐
│                API Gateway (Port 8080)              │
│         Centralized routing and authentication      │
└─────────────────────────┬───────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────┐
│              Service Discovery (Port 8761)          │
└─────┬─────────┬─────────┬─────────┬─────────┬───────┘
      │         │         │         │         │
┌─────▼────┐┌───▼────┐┌───▼────┐┌───▼────┐┌───▼────┐
│  User    ││Account ││Transaction││Notification││ Loan   │
│ Service  ││Service ││ Service ││ Service ││Service │
│Port:8081││Port:8082││Port:8083││Port:8085││Port:8084│
└─────────┘└────────┘└────────┘└────────┘└────────┘
```

## Test Environment Setup

### Prerequisites
- All services running and healthy
- PostgreSQL databases initialized
- Redis cache running
- API Gateway accessible at `http://localhost:8080`

### Database Setup
```sql
-- Create test databases if not exists
CREATE DATABASE telepesa_users_dev;
CREATE DATABASE telepesa_accounts_dev;
CREATE DATABASE telepesa_transactions_dev;
CREATE DATABASE telepesa_loans_dev;
CREATE DATABASE telepesa_notifications_dev;
```

## Test Categories

### 1. User Management Tests

#### 1.1 User Registration
**Test Case**: `TC-USER-001`
- **Endpoint**: `POST /api/v1/users/register`
- **Description**: Register new user with valid data
- **Test Data**:
  ```json
  {
    "username": "testuser001",
    "email": "testuser001@telepesa.com",
    "password": "SecurePass123!",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+254700123456",
    "dateOfBirth": "1990-01-01",
    "nationalId": "12345678"
  }
  ```
- **Expected Response**: 201 Created with user details (status: PENDING_VERIFICATION)
- **Validation**: Check user exists in database with PENDING_VERIFICATION status

#### 1.2 User Registration - Duplicate Username
**Test Case**: `TC-USER-002`
- **Endpoint**: `POST /api/v1/users/register`
- **Description**: Attempt to register user with existing username
- **Expected Response**: 400 Bad Request with validation error

#### 1.3 User Registration - Invalid Email
**Test Case**: `TC-USER-003`
- **Endpoint**: `POST /api/v1/users/register`
- **Description**: Register with invalid email format
- **Expected Response**: 400 Bad Request with email validation error

#### 1.4 User Registration - Weak Password
**Test Case**: `TC-USER-004`
- **Endpoint**: `POST /api/v1/users/register`
- **Description**: Register with password that doesn't meet security requirements
- **Expected Response**: 400 Bad Request with password validation error

#### 1.5 User Login - Pending Verification
**Test Case**: `TC-USER-005`
- **Endpoint**: `POST /api/v1/users/login`
- **Description**: Attempt to login with PENDING_VERIFICATION status
- **Expected Response**: 401 Unauthorized with "Account not activated" message

#### 1.6 User Activation (Database)
**Test Case**: `TC-USER-006`
- **Database Operation**: Update user status from PENDING_VERIFICATION to ACTIVE
- **SQL**: `UPDATE users SET status = 'ACTIVE' WHERE username = 'testuser001';`
- **Validation**: Verify status change in database

#### 1.7 User Login - Successful
**Test Case**: `TC-USER-007`
- **Endpoint**: `POST /api/v1/users/login`
- **Description**: Login with activated account
- **Test Data**:
  ```json
  {
    "username": "testuser001",
    "password": "SecurePass123!"
  }
  ```
- **Expected Response**: 200 OK with JWT token
- **Validation**: Extract and store JWT token for subsequent requests

#### 1.8 User Login - Invalid Credentials
**Test Case**: `TC-USER-008`
- **Endpoint**: `POST /api/v1/users/login`
- **Description**: Login with wrong password
- **Expected Response**: 401 Unauthorized

#### 1.9 Get User Profile
**Test Case**: `TC-USER-009`
- **Endpoint**: `GET /api/v1/users/profile`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Expected Response**: 200 OK with user profile data

#### 1.10 Update User Profile
**Test Case**: `TC-USER-010`
- **Endpoint**: `PUT /api/v1/users/profile`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**:
  ```json
  {
    "firstName": "John Updated",
    "lastName": "Doe Updated",
    "phoneNumber": "+254700123457"
  }
  ```
- **Expected Response**: 200 OK with updated profile

### 2. Account Management Tests

#### 2.1 Create Savings Account
**Test Case**: `TC-ACCOUNT-001`
- **Endpoint**: `POST /api/v1/accounts`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**:
  ```json
  {
    "accountType": "SAVINGS",
    "currency": "KES",
    "initialDeposit": 1000.00
  }
  ```
- **Expected Response**: 201 Created with account details
- **Validation**: Check account exists in database with correct balance

#### 2.2 Create Checking Account
**Test Case**: `TC-ACCOUNT-002`
- **Endpoint**: `POST /api/v1/accounts`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**:
  ```json
  {
    "accountType": "CHECKING",
    "currency": "KES",
    "initialDeposit": 500.00
  }
  ```
- **Expected Response**: 201 Created with account details

#### 2.3 Create Business Account
**Test Case**: `TC-ACCOUNT-003`
- **Endpoint**: `POST /api/v1/accounts`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**:
  ```json
  {
    "accountType": "BUSINESS",
    "currency": "KES",
    "initialDeposit": 5000.00,
    "businessName": "Test Business Ltd",
    "businessRegistrationNumber": "REG123456"
  }
  ```
- **Expected Response**: 201 Created with account details

#### 2.4 Get User Accounts
**Test Case**: `TC-ACCOUNT-004`
- **Endpoint**: `GET /api/v1/accounts`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Expected Response**: 200 OK with list of user's accounts

#### 2.5 Get Account Details
**Test Case**: `TC-ACCOUNT-005`
- **Endpoint**: `GET /api/v1/accounts/{accountId}`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Expected Response**: 200 OK with account details

#### 2.6 Get Account Balance
**Test Case**: `TC-ACCOUNT-006`
- **Endpoint**: `GET /api/v1/accounts/{accountId}/balance`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Expected Response**: 200 OK with current balance

#### 2.7 Create Account - Insufficient Initial Deposit
**Test Case**: `TC-ACCOUNT-007`
- **Endpoint**: `POST /api/v1/accounts`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**:
  ```json
  {
    "accountType": "SAVINGS",
    "currency": "KES",
    "initialDeposit": 50.00
  }
  ```
- **Expected Response**: 400 Bad Request with minimum deposit error

### 3. Transaction Management Tests

#### 3.1 Deposit Money
**Test Case**: `TC-TRANSACTION-001`
- **Endpoint**: `POST /api/v1/transactions`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**:
  ```json
  {
    "fromAccountId": null,
    "toAccountId": "{ACCOUNT_ID}",
    "amount": 2000.00,
    "transactionType": "DEPOSIT",
    "description": "Cash deposit",
    "userId": "{USER_ID}"
  }
  ```
- **Expected Response**: 201 Created with transaction details
- **Validation**: Check account balance increased

#### 3.2 Withdraw Money
**Test Case**: `TC-TRANSACTION-002`
- **Endpoint**: `POST /api/v1/transactions`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**:
  ```json
  {
    "fromAccountId": "{ACCOUNT_ID}",
    "toAccountId": null,
    "amount": 500.00,
    "transactionType": "WITHDRAWAL",
    "description": "ATM withdrawal",
    "userId": "{USER_ID}"
  }
  ```
- **Expected Response**: 201 Created with transaction details
- **Validation**: Check account balance decreased

#### 3.3 Transfer Between Own Accounts
**Test Case**: `TC-TRANSACTION-003`
- **Endpoint**: `POST /api/v1/transactions`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**:
  ```json
  {
    "fromAccountId": "{SAVINGS_ACCOUNT_ID}",
    "toAccountId": "{CHECKING_ACCOUNT_ID}",
    "amount": 300.00,
    "transactionType": "TRANSFER",
    "description": "Transfer to checking account",
    "userId": "{USER_ID}"
  }
  ```
- **Expected Response**: 201 Created with transaction details
- **Validation**: Check both account balances updated correctly

#### 3.4 Transfer to Another User
**Test Case**: `TC-TRANSACTION-004`
- **Endpoint**: `POST /api/v1/transactions`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**:
  ```json
  {
    "fromAccountId": "{ACCOUNT_ID}",
    "toAccountId": "{OTHER_USER_ACCOUNT_ID}",
    "amount": 100.00,
    "transactionType": "TRANSFER",
    "description": "Payment to friend",
    "userId": "{USER_ID}"
  }
  ```
- **Expected Response**: 201 Created with transaction details

#### 3.5 Insufficient Funds Transaction
**Test Case**: `TC-TRANSACTION-005`
- **Endpoint**: `POST /api/v1/transactions`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**:
  ```json
  {
    "fromAccountId": "{ACCOUNT_ID}",
    "toAccountId": "{OTHER_ACCOUNT_ID}",
    "amount": 10000.00,
    "transactionType": "TRANSFER",
    "description": "Large transfer",
    "userId": "{USER_ID}"
  }
  ```
- **Expected Response**: 400 Bad Request with insufficient funds error

#### 3.6 Get Transaction History
**Test Case**: `TC-TRANSACTION-006`
- **Endpoint**: `GET /api/v1/transactions`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Expected Response**: 200 OK with paginated transaction list

#### 3.7 Get User Transactions
**Test Case**: `TC-TRANSACTION-007`
- **Endpoint**: `GET /api/v1/transactions/user/{userId}`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Expected Response**: 200 OK with user's transactions

#### 3.8 Get Account Transactions
**Test Case**: `TC-TRANSACTION-008`
- **Endpoint**: `GET /api/v1/transactions/account/{accountId}`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Expected Response**: 200 OK with account's transaction history

#### 3.9 Get Transaction by ID
**Test Case**: `TC-TRANSACTION-009`
- **Endpoint**: `GET /api/v1/transactions/{transactionId}`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Expected Response**: 200 OK with transaction details

#### 3.10 Update Transaction Status
**Test Case**: `TC-TRANSACTION-010`
- **Endpoint**: `PUT /api/v1/transactions/{transactionId}/status`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**: `"COMPLETED"`
- **Expected Response**: 200 OK with updated transaction

### 4. Loan Management Tests

#### 4.1 Apply for Personal Loan
**Test Case**: `TC-LOAN-001`
- **Endpoint**: `POST /api/v1/loans`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**:
  ```json
  {
    "userId": "{USER_ID}",
    "accountNumber": "{ACCOUNT_NUMBER}",
    "loanType": "PERSONAL",
    "principalAmount": 50000.00,
    "interestRate": 12.5,
    "termMonths": 24,
    "purpose": "Home improvement",
    "monthlyIncome": 80000.00
  }
  ```
- **Expected Response**: 201 Created with loan application details
- **Validation**: Check loan exists with PENDING status

#### 4.2 Apply for Business Loan
**Test Case**: `TC-LOAN-002`
- **Endpoint**: `POST /api/v1/loans`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**:
  ```json
  {
    "userId": "{USER_ID}",
    "accountNumber": "{BUSINESS_ACCOUNT_NUMBER}",
    "loanType": "BUSINESS",
    "principalAmount": 200000.00,
    "interestRate": 15.0,
    "termMonths": 36,
    "purpose": "Business expansion",
    "monthlyIncome": 150000.00
  }
  ```
- **Expected Response**: 201 Created with loan application details

#### 4.3 Get User Loans
**Test Case**: `TC-LOAN-003`
- **Endpoint**: `GET /api/v1/loans/user/{userId}`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Expected Response**: 200 OK with user's loans

#### 4.4 Get Loan Details
**Test Case**: `TC-LOAN-004`
- **Endpoint**: `GET /api/v1/loans/{loanId}`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Expected Response**: 200 OK with loan details

#### 4.5 Approve Loan (Admin Function)
**Test Case**: `TC-LOAN-005`
- **Endpoint**: `POST /api/v1/loans/{loanId}/approve`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**:
  ```json
  {
    "approvedBy": "{ADMIN_USER_ID}"
  }
  ```
- **Expected Response**: 200 OK with approved loan details
- **Validation**: Check loan status changed to APPROVED

#### 4.6 Get Active Loans
**Test Case**: `TC-LOAN-006`
- **Endpoint**: `GET /api/v1/loans/user/{userId}/active`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Expected Response**: 200 OK with active loans

#### 4.7 Apply for Loan - Insufficient Income
**Test Case**: `TC-LOAN-007`
- **Endpoint**: `POST /api/v1/loans`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Test Data**:
  ```json
  {
    "userId": "{USER_ID}",
    "accountNumber": "{ACCOUNT_NUMBER}",
    "loanType": "PERSONAL",
    "principalAmount": 1000000.00,
    "interestRate": 12.5,
    "termMonths": 24,
    "purpose": "Large purchase",
    "monthlyIncome": 50000.00
  }
  ```
- **Expected Response**: 400 Bad Request with income validation error

### 5. Notification Tests

#### 5.1 Get User Notifications
**Test Case**: `TC-NOTIFICATION-001`
- **Endpoint**: `GET /api/v1/notifications/user/{userId}`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Expected Response**: 200 OK with user's notifications

#### 5.2 Mark Notification as Read
**Test Case**: `TC-NOTIFICATION-002`
- **Endpoint**: `PUT /api/v1/notifications/{notificationId}/read`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Expected Response**: 200 OK with updated notification

#### 5.3 Delete Notification
**Test Case**: `TC-NOTIFICATION-003`
- **Endpoint**: `DELETE /api/v1/notifications/{notificationId}`
- **Headers**: `Authorization: Bearer {JWT_TOKEN}`
- **Expected Response**: 204 No Content

### 6. Security Tests

#### 6.1 Unauthorized Access
**Test Case**: `TC-SECURITY-001`
- **Endpoint**: `GET /api/v1/accounts`
- **Headers**: None or invalid token
- **Expected Response**: 401 Unauthorized

#### 6.2 Invalid JWT Token
**Test Case**: `TC-SECURITY-002`
- **Endpoint**: `GET /api/v1/accounts`
- **Headers**: `Authorization: Bearer invalid_token`
- **Expected Response**: 401 Unauthorized

#### 6.3 Expired JWT Token
**Test Case**: `TC-SECURITY-003`
- **Endpoint**: `GET /api/v1/accounts`
- **Headers**: `Authorization: Bearer {EXPIRED_TOKEN}`
- **Expected Response**: 401 Unauthorized

#### 6.4 Rate Limiting
**Test Case**: `TC-SECURITY-004`
- **Endpoint**: `POST /api/v1/users/login`
- **Description**: Make multiple rapid login attempts
- **Expected Response**: 429 Too Many Requests after threshold

### 7. Integration Tests

#### 7.1 Complete User Journey
**Test Case**: `TC-INTEGRATION-001`
- **Steps**:
  1. Register new user
  2. Activate user in database
  3. Login and get JWT token
  4. Create savings account
  5. Deposit money
  6. Transfer money to another account
  7. Apply for loan
  8. Check notifications
- **Expected Result**: All operations successful with proper data consistency

#### 7.2 Cross-Service Communication
**Test Case**: `TC-INTEGRATION-002`
- **Description**: Verify services communicate correctly through API Gateway
- **Validation**: Check Eureka service discovery, load balancing, and circuit breakers

#### 7.3 Data Consistency
**Test Case**: `TC-INTEGRATION-003`
- **Description**: Verify data consistency across services after transactions
- **Validation**: Check account balances, transaction records, and audit trails

### 8. Performance Tests

#### 8.1 Concurrent User Registration
**Test Case**: `TC-PERFORMANCE-001`
- **Description**: Register multiple users simultaneously
- **Expected Result**: All registrations successful without conflicts

#### 8.2 Concurrent Transactions
**Test Case**: `TC-PERFORMANCE-002`
- **Description**: Process multiple transactions simultaneously
- **Expected Result**: All transactions processed correctly with proper locking

#### 8.3 Database Performance
**Test Case**: `TC-PERFORMANCE-003`
- **Description**: Test with large dataset
- **Validation**: Response times within acceptable limits

### 9. Error Handling Tests

#### 9.1 Service Unavailable
**Test Case**: `TC-ERROR-001`
- **Description**: Test behavior when a service is down
- **Expected Result**: Proper error handling and fallback mechanisms

#### 9.2 Database Connection Issues
**Test Case**: `TC-ERROR-002`
- **Description**: Test behavior with database connectivity issues
- **Expected Result**: Graceful degradation and proper error messages

#### 9.3 Invalid Data Handling
**Test Case**: `TC-ERROR-003`
- **Description**: Test with malformed requests
- **Expected Result**: Proper validation and error responses

## Test Execution Strategy

### Manual Testing
1. Use Postman or curl commands
2. Follow test cases sequentially
3. Document results and any issues found

### Automated Testing
1. Create test scripts using the provided test cases
2. Run regression tests after each deployment
3. Monitor test results and performance metrics

### Test Data Management
1. Use unique test data for each test run
2. Clean up test data after testing
3. Maintain test data isolation

## Success Criteria

### Functional Requirements
- All test cases pass successfully
- No critical bugs or data inconsistencies
- Proper error handling and validation
- Security requirements met

### Performance Requirements
- Response times under 2 seconds for most operations
- System handles concurrent users without degradation
- Database queries optimized and efficient

### Security Requirements
- All endpoints properly authenticated
- JWT tokens validated correctly
- Rate limiting working as expected
- No sensitive data exposed

## Reporting

### Test Results Template
```
Test Run: {DATE_TIME}
Environment: {ENVIRONMENT}
Total Tests: {COUNT}
Passed: {COUNT}
Failed: {COUNT}
Success Rate: {PERCENTAGE}

Failed Tests:
- {TEST_CASE_ID}: {DESCRIPTION}

Performance Metrics:
- Average Response Time: {TIME}
- Peak Response Time: {TIME}
- Throughput: {REQUESTS_PER_SECOND}

Issues Found:
- {ISSUE_DESCRIPTION}
- {SEVERITY}
- {RECOMMENDATION}
```

## Maintenance

### Regular Updates
- Update test cases when new features are added
- Review and update test data as needed
- Monitor and update performance benchmarks

### Continuous Improvement
- Analyze test results for patterns
- Optimize test execution time
- Improve test coverage and quality 