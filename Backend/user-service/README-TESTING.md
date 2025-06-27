# Telepesa User Service - Testing & CI/CD Guide

## Overview
This guide covers the comprehensive testing framework and CI/CD pipeline for the Telepesa User Service, including test coverage reporting, GitHub Actions integration, and quality assurance processes.

## 🚀 Quick Start

### Running Tests Locally
```bash
# Run all tests with coverage
mvn clean test jacoco:report

# Run tests with failure tolerance (generates coverage even if some tests fail)
mvn test jacoco:report -Dmaven.test.failure.ignore=true

# Generate coverage report with our custom script
./generate-coverage-report.sh
```

### Viewing Coverage Reports
```bash
# Open HTML coverage report
open target/site/jacoco/index.html

# Check coverage summary
cat logs/test-coverage-summary.md
```

## 📊 Test Coverage Metrics

### Current Status
- **Line Coverage:** 46.1% (1,464/3,174 lines)
- **Total Tests:** 66 tests across 4 test suites
- **Passing Tests:** 36/66 (55% success rate)
- **Target Coverage:** 80% line coverage, 75% branch coverage

### Coverage by Test Suite

#### ✅ AuditLogServiceTest (9/9 passing)
```java
// Banking compliance audit logging
@Test void logUserRegistration_ShouldLogWithCorrectFormat()
@Test void logAuthenticationAttempt_ShouldLogWithCorrectSeverity()
@Test void logSecurityViolation_ShouldLogAsError()
```

#### ✅ DeviceFingerprintServiceTest (10/11 passing)
```java
// Device fingerprinting and fraud detection
@Test void generateDeviceFingerprint_ShouldReturnConsistentFingerprint()
@Test void analyzeDevice_WithNewDevice_ShouldReturnAnalysisResult()
@Test void analyzeDevice_WithDeviceSharing_ShouldDetectSuspiciousActivity()
```

#### ⚠️ UserServiceTest (17/22 passing)
```java
// Business logic and security integration
@Test void createUser_WithValidRequest_ShouldReturnUserDto()
@Test void authenticateUser_WithValidCredentials_ShouldReturnToken()
@Test void createUserWithSecurity_ShouldLogAuditAndAnalyzeDevice()
```

#### ❌ UserControllerTest (0/24 passing - Spring context issues)
```java
// REST API endpoint testing
@Test void register_WithValidRequest_ShouldReturnCreatedUser()
@Test void login_WithValidCredentials_ShouldReturnToken()
@Test void getUsers_WithPagination_ShouldReturnPagedUsers()
```

## 🔧 Test Configuration

### JaCoCo Maven Plugin Configuration
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <configuration>
        <excludes>
            <exclude>**/dto/**</exclude>
            <exclude>**/config/**</exclude>
            <exclude>**/UserServiceApplication.class</exclude>
        </excludes>
    </configuration>
</plugin>
```

### Test Profiles
```yaml
# application-test.yml
spring:
  profiles:
    active: test
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

## 🤖 GitHub Actions CI/CD Pipeline

### Workflow Overview
```yaml
name: CI/CD Pipeline
on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]
```

### Pipeline Stages

#### 1. **Test & Coverage** 🧪
- Runs unit tests with JaCoCo coverage
- Generates HTML/XML coverage reports
- Uploads artifacts to GitHub
- Integrates with Codecov

#### 2. **Build & Package** 📦
- Compiles source code
- Creates JAR artifacts
- Validates dependencies

#### 3. **Security Scan** 🔒
- OWASP dependency vulnerability check
- CVE scanning with threshold (CVSS ≥ 7)
- Security report generation

#### 4. **Integration Tests** 🔗
- PostgreSQL service container
- Database connectivity tests
- Service integration validation

#### 5. **Quality Gate** ✅
- Coverage threshold validation (75%)
- Test failure analysis
- Artifact validation

#### 6. **Docker Build** 🐳
- Multi-stage Docker build
- Security-hardened Alpine Linux
- Non-root user execution

### Workflow Triggers
- **Push to main/develop:** Full pipeline
- **Pull Requests:** Quality gate validation
- **Manual:** On-demand execution

## 📋 Test Structure

### Test Categories

#### Unit Tests
```
src/test/java/
├── service/
│   ├── UserServiceTest.java           # Business logic tests
│   ├── AuditLogServiceTest.java       # Audit logging tests
│   └── DeviceFingerprintServiceTest.java # Security tests
└── controller/
    └── UserControllerTest.java        # REST API tests
```

#### Test Utilities
```java
// Test data builders
UserTestDataBuilder.builder()
    .withUsername("testuser")
    .withEmail("test@example.com")
    .build();

// Mock configurations
@MockBean private UserRepository userRepository;
@MockBean private DeviceFingerprintService deviceService;
```

## 🛠️ Tools & Dependencies

### Testing Framework
- **JUnit 5:** Test framework
- **Mockito:** Mocking framework
- **AssertJ:** Fluent assertions
- **Spring Boot Test:** Integration testing
- **MockMvc:** Web layer testing

### Coverage & Quality
- **JaCoCo:** Code coverage analysis
- **OWASP Dependency Check:** Security scanning
- **Surefire:** Test execution reporting
- **Maven:** Build and dependency management

### CI/CD Tools
- **GitHub Actions:** Pipeline automation
- **Docker:** Containerization
- **Codecov:** Coverage tracking
- **PostgreSQL:** Integration testing database

## 🚨 Quality Gates

### Coverage Requirements
- **Line Coverage:** Minimum 80%
- **Branch Coverage:** Minimum 75%
- **Method Coverage:** Minimum 85%

### Test Requirements
- **All Tests Pass:** No failing tests in main branch
- **Test Categories:** Unit, integration, security tests
- **Performance:** Tests complete within 5 minutes

### Security Requirements
- **No High CVEs:** CVSS score < 7.0
- **Dependency Scanning:** All dependencies scanned
- **Security Headers:** Banking-grade security validation

## 🔍 Coverage Analysis

### Well-Covered Components (>70%)
- **Service Layer:** Business logic well tested
- **Security Services:** Audit logging, device fingerprinting
- **Exception Handling:** Custom exceptions validated
- **Validation Logic:** Input validation comprehensive

### Needs Improvement (<50%)
- **Controller Layer:** Spring Security context issues
- **Configuration Classes:** Application config testing
- **Security Filters:** JWT, rate limiting filters
- **Integration Scenarios:** Cross-service interactions

## 🛡️ Security Testing

### Implemented Security Tests
```java
// Rate limiting validation
@Test void rateLimiting_ExceedsThreshold_ShouldReturn429()

// Device fingerprinting
@Test void deviceFingerprinting_DetectsSuspiciousActivity()

// Audit logging
@Test void auditLogging_CapturesSecurityEvents()

// Input validation
@Test void inputValidation_PreventsSQLInjection()
```

### Security Test Coverage
- **Authentication:** JWT token validation
- **Authorization:** Role-based access control
- **Rate Limiting:** IP-based throttling
- **Input Validation:** XSS/SQL injection prevention
- **Audit Logging:** Security event tracking

## 📊 Monitoring & Reporting

### Automated Reports
- **Coverage HTML Report:** `target/site/jacoco/index.html`
- **Test Results:** `target/surefire-reports/`
- **Security Scan:** OWASP dependency check report
- **Build Logs:** Detailed execution logs

### Metrics Tracking
- Coverage trends over time
- Test execution performance
- Security vulnerability counts
- Build success rates

## 🎯 Continuous Improvement

### Current Sprint Goals
1. Fix UserControllerTest Spring Security context
2. Achieve 80% line coverage target
3. Implement integration test scenarios
4. Add performance benchmarking

### Upcoming Features
- Mutation testing for robustness validation
- Contract testing between services
- Chaos engineering for resilience
- Performance regression testing

## 📚 Best Practices

### Test Writing Guidelines
```java
// Use descriptive test names
@Test void createUser_WithDuplicateEmail_ShouldThrowDuplicateUserException()

// Follow AAA pattern (Arrange, Act, Assert)
@Test void authenticateUser_WithValidCredentials_ShouldReturnToken() {
    // Arrange
    LoginRequest request = createValidLoginRequest();
    
    // Act
    LoginResponse response = userService.authenticate(request);
    
    // Assert
    assertThat(response.getToken()).isNotNull();
}

// Use test data builders
User testUser = UserTestDataBuilder.defaultUser()
    .withEmail("custom@test.com")
    .build();
```

### CI/CD Best Practices
- **Fast Feedback:** Tests complete within 5 minutes
- **Parallel Execution:** Multiple test suites run simultaneously
- **Artifact Management:** Coverage reports stored for 30 days
- **Security First:** Vulnerability scanning on every build

## 🆘 Troubleshooting

### Common Issues

#### Test Failures
```bash
# Run specific test class
mvn test -Dtest=UserServiceTest

# Debug with verbose output
mvn test -X -Dtest=UserServiceTest

# Skip tests during build
mvn package -DskipTests
```

#### Coverage Issues
```bash
# Generate coverage without test execution
mvn jacoco:report

# Check coverage with threshold enforcement
mvn jacoco:check

# View detailed coverage by package
open target/site/jacoco/index.html
```

#### CI/CD Pipeline Issues
- Check GitHub Actions logs
- Validate Docker build locally
- Test database connectivity
- Review security scan results

### Support Resources
- **Documentation:** This README and inline code comments
- **Test Examples:** Existing test suites for reference
- **CI/CD Logs:** GitHub Actions execution details
- **Coverage Reports:** JaCoCo HTML reports

---

*This guide is maintained by the Telepesa Quality Engineering Team* 