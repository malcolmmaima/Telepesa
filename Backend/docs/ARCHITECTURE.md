# Telepesa Backend Architecture

## 🏛️ System Architecture Overview

This document provides comprehensive technical architecture details for the Telepesa Banking Platform backend services.

## 🗺️ Microservices Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Telepesa Backend Architecture            │
├─────────────────────────────────────────────────────────────┤
│  Load Balancer → API Gateway → Service Discovery           │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │ User Service│ │Account Svc  │ │Transaction  │           │
│  │ Port: 8081  │ │ Port: 8082  │ │ Port: 8083  │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
│  ┌─────────────┐ ┌─────────────┐                           │
│  │ Loan Service│ │Notification │                           │
│  │ Port: 8084  │ │ Port: 8085  │                           │
│  └─────────────┘ └─────────────┘                           │
├─────────────────────────────────────────────────────────────┤
│                    Shared Libraries                         │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │Common Models│ │ Exceptions  │ │Security Utils│           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│                Data Layer & Infrastructure                  │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │ PostgreSQL  │ │    Redis    │ │   Docker    │           │
│  │  Database   │ │    Cache    │ │Containerization│        │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

### Service Communication Patterns

```
┌────────────────┐    HTTP/REST    ┌────────────────┐
│   Frontend     │ ←──────────────→ │  API Gateway   │
│ (Web/Mobile)   │                  │                │
└────────────────┘                  └────────────────┘
                                           │
                                           ▼
┌─────────────────────────────────────────────────────────────┐
│                   Internal Service Mesh                     │
│                                                             │
│  ┌──────────────┐   ┌──────────────┐   ┌──────────────┐    │
│  │ User Service │   │Account Service│   │Transaction   │    │
│  │              │◄──┤              │◄──┤Service       │    │
│  │              │   │              │   │              │    │
│  └──────────────┘   └──────────────┘   └──────────────┘    │
│         ▲                   ▲                   ▲           │
│         │                   │                   │           │
│  ┌──────▼──────┐     ┌──────▼──────┐     ┌──────▼──────┐    │
│  │Notification │     │ Loan Service│     │   Events    │    │
│  │Service      │     │             │     │   Queue     │    │
│  └─────────────┘     └─────────────┘     └─────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

## 🛠️ Technology Stack

### Core Framework & Runtime
- **Spring Boot 3.2.0** - Main application framework with auto-configuration
- **Java 17** - LTS programming language with modern features
- **Maven 3.8+** - Build automation and dependency management
- **Spring Framework 6.1** - Dependency injection and application context

### Data & Persistence Layer
- **Spring Data JPA 3.2** - Repository abstraction and ORM integration
- **Hibernate 6.2** - ORM implementation with advanced caching
- **PostgreSQL 15** - Production ACID-compliant relational database
- **H2 Database** - In-memory database for testing and development
- **Flyway 9.x** - Database migration and versioning
- **HikariCP** - High-performance JDBC connection pooling

### Security & Authentication
- **Spring Security 6.2** - Comprehensive security framework
- **JWT (JSON Web Tokens)** - Stateless authentication mechanism
- **BCrypt** - Secure password hashing algorithm
- **OWASP Guidelines** - Security best practices implementation
- **Rate Limiting** - Request throttling and abuse prevention

### Testing & Quality Assurance
- **JUnit 5** - Modern unit testing framework
- **Mockito 5.x** - Mocking framework for isolated testing
- **TestContainers** - Integration testing with real databases
- **JaCoCo** - Code coverage analysis and reporting
- **AssertJ** - Fluent assertion library
- **Postman/Newman** - API testing and automation

### Monitoring & Documentation
- **Spring Actuator** - Production-ready monitoring endpoints
- **Micrometer** - Application metrics collection
- **OpenAPI 3.0** - API specification and documentation
- **Swagger UI** - Interactive API documentation interface
- **SLF4J + Logback** - Structured logging with JSON output

### DevOps & Infrastructure
- **Docker** - Application containerization
- **Docker Compose** - Multi-container orchestration
- **GitHub Actions** - CI/CD pipeline automation
- **Maven Wrapper** - Build tool version standardization

## 🔐 Security Architecture

### Multi-Layer Security Model

```
┌─────────────────────────────────────────────────────────────┐
│                     Security Layers                         │
├─────────────────────────────────────────────────────────────┤
│ Network Layer: HTTPS/TLS, CORS, Rate Limiting              │
├─────────────────────────────────────────────────────────────┤
│ Application Layer: Input Validation, Business Rules        │
├─────────────────────────────────────────────────────────────┤
│ Authentication Layer: JWT Tokens, Password Policies        │
├─────────────────────────────────────────────────────────────┤
│ Authorization Layer: RBAC, Method-Level Security           │
├─────────────────────────────────────────────────────────────┤
│ Data Layer: Encryption at Rest, Audit Logging              │
├─────────────────────────────────────────────────────────────┤
│ Infrastructure Layer: Container Security, Secrets Mgmt     │
└─────────────────────────────────────────────────────────────┘
```

### Authentication Flow

```
1. User Registration
   ├── Email/Phone Validation
   ├── Password Complexity Check
   ├── Account Creation (PENDING_VERIFICATION)
   └── Email Verification Token Generation

2. Account Activation
   ├── Email Verification Token Validation
   ├── Account Status Update (ACTIVE)
   └── Welcome Notification

3. Login Process
   ├── Credentials Validation
   ├── Account Status Check
   ├── Failed Attempt Tracking
   ├── Device Fingerprinting
   ├── JWT Token Generation
   └── Audit Log Entry

4. API Request Authorization
   ├── JWT Token Extraction
   ├── Token Signature Validation
   ├── Token Expiration Check
   ├── User Context Loading
   └── Permission Verification
```

### Security Features Implementation

#### Input Validation
- **Bean Validation (JSR-303)** - Declarative validation annotations
- **Custom Validators** - Business-specific validation rules
- **SQL Injection Prevention** - Parameterized queries only
- **XSS Protection** - Input sanitization and output encoding

#### Authentication & Authorization
- **Stateless Authentication** - JWT tokens with configurable expiration
- **Role-Based Access Control** - Method-level security annotations
- **Multi-Factor Authentication** - SMS/Email verification support
- **Session Management** - Secure token lifecycle management

#### Data Protection
- **Encryption at Rest** - AES-256 for sensitive data fields
- **Encryption in Transit** - TLS 1.3 for all communications
- **PII Handling** - Data masking and anonymization
- **Audit Trails** - Complete activity logging

## 📊 Data Architecture

### Database Design Principles

#### Entity Relationship Model
```
Users (1) ────────── (*) Accounts
  │                      │
  │                      │
  │ (1)              (*) │
  │                      │
AuditLogs           Transactions
  │                      │
  │                      │
  │ (*)              (*) │
  │                      │
NotificationLogs ────────┘
```

#### Core Entities

##### User Entity
```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    private String username;        // Unique identifier
    private String email;          // Email address (unique)
    private String phoneNumber;    // Phone number
    private String firstName;      // First name
    private String lastName;       // Last name
    private String password;       // BCrypt hashed password
    private UserStatus status;     // PENDING_VERIFICATION, ACTIVE, SUSPENDED
    private boolean emailVerified; // Email verification status
    private boolean phoneVerified; // Phone verification status
    private LocalDateTime lastLoginAt;
    private int failedLoginAttempts;
    // ... additional fields
}
```

##### Base Entity Pattern
```java
@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Version
    private Long version; // Optimistic locking
}
```

### Database Configuration

#### Production Database (PostgreSQL)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/telepesa
    username: ${DB_USERNAME:telepesa}
    password: ${DB_PASSWORD:password}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

#### Testing Database (H2)
```yaml
spring:
  profiles: test
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: create-drop
```

## 🚀 Service Implementation Patterns

### Service Layer Architecture

#### Service Interface Pattern
```java
public interface UserService {
    UserDto createUser(CreateUserRequest request);
    UserDto getUser(Long id);
    Page<UserDto> getUsers(Pageable pageable);
    UserDto updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
    LoginResponse authenticate(LoginRequest request);
}
```

#### Implementation with Transaction Management
```java
@Service
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuditLogService auditLogService;
    
    @Override
    public UserDto createUser(CreateUserRequest request) {
        // Business logic implementation
        // Input validation
        // Entity creation and persistence
        // Audit logging
        return userMapper.toDto(savedUser);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getUsers(Pageable pageable) {
        // Read-only operations
        return userRepository.findAll(pageable)
            .map(userMapper::toDto);
    }
}
```

### Repository Layer Patterns

#### Standard Repository Interface
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.createdAt >= :since")
    Page<User> findActiveUsersSince(@Param("status") UserStatus status, 
                                   @Param("since") LocalDateTime since, 
                                   Pageable pageable);
    
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.lastLoginAt < :threshold")
    int deactivateInactiveUsers(@Param("status") UserStatus status, 
                               @Param("threshold") LocalDateTime threshold);
}
```

### Controller Layer Patterns

#### RESTful Controller Implementation
```java
@RestController
@RequestMapping("/api/v1/users")
@Validated
@Tag(name = "User Management")
public class UserController {
    
    private final UserService userService;
    
    @PostMapping
    @Operation(summary = "Create new user")
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody CreateUserRequest request,
            HttpServletRequest httpRequest) {
        
        UserDto user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isOwner(#id, authentication.name)")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        UserDto user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }
}
```

## 🧪 Testing Architecture

### Test Pyramid Implementation

```
                /\
               /  \       E2E Tests (Postman Collection)
              /    \      - Complete user journeys
             /______\     - API integration testing
            /        \    
           /          \   Integration Tests (TestContainers)
          /            \  - Database integration
         /              \ - Service communication  
        /________________\- Security integration
       
       Unit Tests (JUnit + Mockito)
       - Business logic validation
       - Component isolation
       - Mock dependencies
       - Fast feedback loop (80%+ coverage)
```

### Testing Strategy by Layer

#### Unit Testing
- **Service Layer**: Business logic validation with mocked dependencies
- **Repository Layer**: Data access testing with @DataJpaTest
- **Controller Layer**: HTTP endpoint testing with @WebMvcTest
- **Security Layer**: Authentication and authorization testing

#### Integration Testing
- **Database Integration**: TestContainers with real PostgreSQL
- **Service Integration**: Cross-service communication testing
- **Security Integration**: End-to-end authentication flows

#### End-to-End Testing
- **API Testing**: Postman collection with automated assertions
- **User Journey Testing**: Complete workflows from registration to usage
- **Performance Testing**: Response time and load testing

## 🔄 Development Workflow

### Service Development Lifecycle

```
1. Planning & Design
   ├── API Contract Definition (OpenAPI)
   ├── Database Schema Design
   ├── Security Requirements Analysis
   └── Testing Strategy Planning

2. Implementation
   ├── Entity & Repository Creation
   ├── Service Layer Implementation
   ├── Controller Layer Development
   ├── Security Configuration
   └── Error Handling Implementation

3. Testing
   ├── Unit Test Development (TDD approach)
   ├── Integration Test Creation
   ├── API Test Suite Updates (Postman)
   └── Security Testing Validation

4. Documentation
   ├── API Documentation Updates
   ├── Architecture Documentation
   ├── Deployment Guide Updates
   └── Troubleshooting Documentation

5. Deployment
   ├── Docker Image Creation
   ├── Environment Configuration
   ├── Database Migration Execution
   └── Production Deployment
```

### Quality Gates

#### Automated Quality Checks
- **Code Coverage**: Minimum 80% line coverage
- **Test Success**: 100% passing tests required
- **Security Scan**: OWASP dependency check
- **Code Quality**: SonarQube analysis
- **Performance**: API response time < 2000ms

#### Manual Review Process
- **Code Review**: Peer review for all changes
- **Security Review**: Security team validation
- **Architecture Review**: Technical architecture compliance

## 🚀 Deployment Architecture

### Containerization Strategy

#### Multi-Stage Docker Build
```dockerfile
# Build stage
FROM openjdk:17-jdk-slim as build
WORKDIR /workspace/app
COPY . .
RUN ./mvnw install -DskipTests

# Production stage
FROM openjdk:17-jre-slim
VOLUME /tmp
COPY --from=build /workspace/app/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

#### Docker Compose Orchestration
```yaml
services:
  user-service:
    build: ./user-service
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=database
    depends_on:
      - database
  
  database:
    image: postgres:15
    environment:
      POSTGRES_DB: telepesa
      POSTGRES_USER: telepesa
      POSTGRES_PASSWORD: password
```

### Production Deployment Patterns

#### Infrastructure Requirements
- **Load Balancer**: NGINX or AWS ALB
- **Service Discovery**: Spring Cloud Gateway
- **Database**: PostgreSQL with replication
- **Caching**: Redis cluster
- **Monitoring**: Prometheus + Grafana
- **Logging**: ELK Stack (Elasticsearch, Logstash, Kibana)

#### Environment Configuration
```yaml
# Production profile
spring:
  profiles:
    active: prod
  datasource:
    url: ${DATABASE_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: ${JWT_EXPIRATION:86400000}
```

## 📈 Monitoring & Observability

### Application Monitoring

#### Health Check Endpoints
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // Database connectivity check
            long userCount = userRepository.count();
            return Health.up()
                .withDetail("userCount", userCount)
                .withDetail("database", "PostgreSQL")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

#### Custom Metrics
```java
@Component
public class UserMetrics {
    
    private final Counter userRegistrations;
    private final Timer loginTime;
    
    public UserMetrics(MeterRegistry meterRegistry) {
        this.userRegistrations = Counter.builder("user.registrations.total")
            .description("Total user registrations")
            .register(meterRegistry);
            
        this.loginTime = Timer.builder("user.login.duration")
            .description("User login duration")
            .register(meterRegistry);
    }
}
```

### Logging Strategy

#### Structured Logging
```java
@Slf4j
@Service
public class UserService {
    
    public UserDto createUser(CreateUserRequest request) {
        MDC.put("operation", "createUser");
        MDC.put("username", request.getUsername());
        
        log.info("Creating new user: {}", request.getUsername());
        
        try {
            // Implementation
            log.info("User created successfully: {}", savedUser.getId());
            return userMapper.toDto(savedUser);
        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
```

## 🎯 Future Architecture Considerations

### Scalability Patterns
- **Database Sharding** - Horizontal partitioning for large datasets
- **Read Replicas** - Separate read/write database instances
- **Microservice Decomposition** - Further service boundaries
- **Event-Driven Architecture** - Asynchronous service communication

### Performance Optimization
- **Caching Strategy** - Redis for frequently accessed data
- **Query Optimization** - Database indexing and query tuning
- **Connection Pooling** - Optimized database connections
- **Response Compression** - GZIP compression for API responses

### Security Enhancements
- **OAuth2 Integration** - Third-party authentication providers
- **API Rate Limiting** - Per-user and per-endpoint limits
- **Fraud Detection** - Machine learning-based anomaly detection
- **Data Encryption** - Field-level encryption for sensitive data

---

*This architecture documentation evolves with the system and reflects current implementation decisions and future planning.* 