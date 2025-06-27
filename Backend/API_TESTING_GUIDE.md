# Telepesa API Testing Guide

## 📋 Overview

This guide covers comprehensive API testing for the Telepesa Banking Platform using Postman collections and automated test scripts.

## 🚀 Quick Start

### Prerequisites
- **Postman** (Desktop or Web) - For comprehensive testing
- **curl** - For quick testing with provided script
- **User Service** running on `http://localhost:8081`
- **Java 17+** and **Maven** for running services

### Quick Testing (No Postman Required)
For rapid API validation, use our provided test script:

```bash
cd Backend
chmod +x quick-api-test.sh
./quick-api-test.sh
```

This script tests:
- ✅ Service health and availability
- ✅ User registration functionality
- ✅ Input validation (passwords, duplicates)
- ✅ Authentication security
- ✅ Authorization controls
- ✅ CORS configuration

### Import Collection & Environment

1. **Import Collection**:
   ```
   File → Import → Backend/Telepesa_API_Collection.postman_collection.json
   ```

2. **Import Environment**:
   ```
   File → Import → Backend/Telepesa_Development.postman_environment.json
   ```

3. **Select Environment**:
   - Click the environment dropdown (top right)
   - Select "Telepesa Development"

## 📊 Test Collection Structure

### 1. Health & Infrastructure
- **Health Check** - Verifies service availability
- **API Documentation** - Validates OpenAPI spec

### 2. User Registration
- **Valid User Registration** - Creates new user with proper validation
- **Weak Password** - Tests password complexity requirements
- **Duplicate Username** - Tests unique constraint enforcement
- **Invalid Email** - Tests email format validation

### 3. User Authentication
- **Pending Verification Login** - Tests user status validation
- **Invalid Credentials** - Tests authentication failure
- **Non-existent User** - Tests user not found scenarios

### 4. Protected Endpoints
- **Authorization Testing** - Tests JWT protection on all endpoints
- **Unauthorized Access** - Verifies security controls

### 5. Security & Error Handling
- **Malformed JSON** - Tests request parsing
- **Invalid Content Types** - Tests content type validation
- **Method Not Allowed** - Tests HTTP method restrictions
- **CORS Headers** - Tests cross-origin configurations

### 6. Rate Limiting
- **Multiple Requests** - Tests rate limiting implementation

## 🔧 Environment Variables

The development environment includes these pre-configured variables:

| Variable | Value | Description |
|----------|-------|-------------|
| `base_url` | `http://localhost:8081` | Primary service endpoint |
| `user_service_url` | `http://localhost:8081` | User management service |
| `account_service_url` | `http://localhost:8082` | Account management service |
| `transaction_service_url` | `http://localhost:8083` | Transaction processing service |
| `loan_service_url` | `http://localhost:8084` | Loan management service |
| `notification_service_url` | `http://localhost:8085` | Notification service |
| `jwt_token` | *(dynamic)* | Authentication token |
| `api_version` | `v1` | API version |

## 🧪 Running Tests

### Option 1: Run Entire Collection
1. Click on "Telepesa Banking Platform API" collection
2. Click "Run collection" button
3. Select "Telepesa Development" environment
4. Click "Run Telepesa Banking Platform API"

### Option 2: Run Individual Test Groups
1. Expand the collection folders
2. Right-click on a folder (e.g., "2. User Registration")
3. Select "Run folder"

### Option 3: Run Single Tests
1. Click on individual test requests
2. Click "Send" button
3. Review results in the response section

## 📈 Test Results & Reporting

### Understanding Test Results

#### ✅ Passing Tests
```javascript
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
}); // ✅ PASS
```

#### ❌ Failing Tests
```javascript
pm.test("User created successfully", function () {
    pm.expect(jsonData.id).to.exist;
}); // ❌ FAIL - Response missing 'id' field
```

#### ⚠️ Conditional Tests
```javascript
// Tests that may pass or fail based on system state
pm.test("Rate limiting may trigger", function () {
    pm.expect([401, 429]).to.include(pm.response.code);
}); // ⚠️ CONDITIONAL
```

### Collection Test Statistics

Current test coverage:

| Category | Tests | Expected Results |
|----------|-------|------------------|
| **Health Checks** | 4 tests | All should pass |
| **User Registration** | 8 tests | 6 pass, 2 expected failures |
| **Authentication** | 6 tests | All expected failures (design) |
| **Authorization** | 8 tests | All expected failures (security) |
| **Security** | 8 tests | All should pass |
| **Rate Limiting** | 2 tests | May pass/fail (conditional) |
| **Total** | **36 tests** | **96% expected pass rate** |

## 🔐 Security Testing Features

### Authentication Testing
- JWT token validation
- Session management
- Account lockout mechanisms
- Failed login attempt tracking

### Input Validation Testing
- SQL injection prevention
- XSS attack prevention
- Parameter tampering protection
- Request size limits

### CORS & Security Headers
- Cross-origin request validation
- Security header verification
- Content type enforcement

### Rate Limiting
- Login attempt limiting
- API rate limiting
- DDoS protection testing

## 🎯 Expected Test Behaviors

### Successful Scenarios ✅
- Health checks return 200 OK
- Valid user registration returns 201 Created
- Proper validation error messages for invalid input
- Security headers present in responses
- CORS configuration working

### Expected Failures 🚫
- Login attempts with PENDING_VERIFICATION status (by design)
- Unauthorized access to protected endpoints (security working)
- Invalid credentials rejection (authentication working)
- Duplicate user prevention (data integrity working)

### Conditional Results ⚠️
- Rate limiting may or may not trigger depending on request frequency
- Performance tests may vary based on system load

## 🔄 Continuous Integration

### Automated Testing
The Postman collection integrates with CI/CD pipelines:

```bash
# Install Newman (Postman CLI)
npm install -g newman

# Run collection from command line
newman run Backend/Telepesa_API_Collection.postman_collection.json \
  -e Backend/Telepesa_Development.postman_environment.json \
  --reporters cli,html \
  --reporter-html-export test-results.html
```

### GitHub Actions Integration
The collection can be integrated into GitHub Actions workflows:

```yaml
- name: Run API Tests
  run: |
    newman run Backend/Telepesa_API_Collection.postman_collection.json \
      -e Backend/Telepesa_Development.postman_environment.json \
      --bail newman \
      --reporters cli,junit \
      --reporter-junit-export test-results.xml
```

## 🛠️ Updating the Collection

### Adding New Endpoints
When new endpoints are added:

1. **Create New Request**:
   - Right-click on appropriate folder
   - Select "Add Request"
   - Configure method, URL, headers, body

2. **Add Tests**:
   ```javascript
   pm.test("Status code is 200", function () {
       pm.response.to.have.status(200);
   });
   
   pm.test("Response has required fields", function () {
       const jsonData = pm.response.json();
       pm.expect(jsonData).to.have.property("id");
       pm.expect(jsonData).to.have.property("createdAt");
   });
   ```

3. **Update Environment Variables**:
   - Add new service URLs
   - Add new test data variables
   - Update API version if needed

### Test Best Practices

#### ✅ Good Test Practices
```javascript
// Specific and descriptive test names
pm.test("User registration with valid data returns 201 status", function () {
    pm.response.to.have.status(201);
});

// Validate response structure
pm.test("Response contains user ID and timestamps", function () {
    const jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property("id");
    pm.expect(jsonData).to.have.property("createdAt");
    pm.expect(jsonData).to.have.property("updatedAt");
});

// Store dynamic values for later tests
if (pm.response.code === 201) {
    const user = pm.response.json();
    pm.globals.set("created_user_id", user.id);
}
```

#### ❌ Avoid These Patterns
```javascript
// Too generic
pm.test("It works", function () {
    pm.response.to.have.status(200);
});

// No error handling
const jsonData = pm.response.json(); // May fail if response isn't JSON

// Hard-coded values
pm.expect(jsonData.id).to.eql(123); // Will break with different test data
```

## 📞 Support & Troubleshooting

### Common Issues

#### Service Not Running
```
Error: connect ECONNREFUSED 127.0.0.1:8081
```
**Solution**: Start the user service:
```bash
cd Backend/user-service
mvn spring-boot:run
```

#### Environment Not Selected
```
Error: base_url is not defined
```
**Solution**: Select "Telepesa Development" environment in Postman

#### Tests Failing Unexpectedly
1. Check service logs for errors
2. Verify database connectivity
3. Ensure proper test data cleanup
4. Check for rate limiting

### Getting Help
- Check service health: `GET {{base_url}}/actuator/health`
- Review application logs in terminal
- Validate OpenAPI spec: `GET {{base_url}}/v3/api-docs`
- Contact development team for persistent issues

---

## 📊 Current Implementation Status

### ✅ Implemented Services
- **User Service** (Port 8081) - Complete with authentication, registration, and profile management

### 🚧 Planned Services
- **Account Service** (Port 8082) - Account management and banking operations
- **Transaction Service** (Port 8083) - Payment processing and transfers
- **Loan Service** (Port 8084) - Loan origination and management
- **Notification Service** (Port 8085) - Email, SMS, and push notifications

### 📋 Next Steps
1. **Account Service Implementation** - Basic account CRUD operations
2. **Transaction Service Implementation** - Payment processing workflows
3. **Service Integration Testing** - Cross-service communication
4. **Load Testing** - Performance validation
5. **Security Penetration Testing** - Advanced security validation

---

*This guide is automatically updated as new services and endpoints are implemented.* 