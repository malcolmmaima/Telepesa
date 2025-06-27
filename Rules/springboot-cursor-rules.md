# Telepesa: Spring Boot Backend Development Rules

These rules are based on best practices for building robust, scalable Spring Boot applications using modern Java development patterns.

## Project Structure & Architecture

### Maven/Gradle Structure
- Use Maven or Gradle for dependency management (prefer Maven for enterprise projects)
- Follow standard Maven directory structure:
  ```
  src/
    main/
      java/
        com/company/project/
          config/
          controller/
          service/
          repository/
          model/
          dto/
          exception/
          security/
          util/
      resources/
        application.yml
        application-{profile}.yml
        static/
        templates/
    test/
      java/
      resources/
  ```

### Package Naming
- Use reverse domain naming: `com.{company}.{project}.{module}`
- Example: `com.maelcolium.telepesa.user`, `com.maelcolium.telepesa.payment`
- Organize by feature/domain, not by layer

### Layered Architecture
- Follow clean architecture principles with clear separation:
  - **Controller Layer** - REST endpoints and request handling
  - **Service Layer** - Business logic and orchestration
  - **Repository Layer** - Data access and persistence
  - **Model/Entity Layer** - Domain models and JPA entities
  - **DTO Layer** - Data transfer objects for API contracts

### Modular Design
- Organize packages by business domain/feature:
  ```
  com.company.project/
    user/
      controller/
      service/
      repository/
      model/
      dto/
    payment/
      controller/
      service/
      repository/
      model/
      dto/
    notification/
      controller/
      service/
      repository/
      model/
      dto/
  ```

## Spring Boot Conventions

### Application Configuration
- Use `application.yml` over `application.properties`
- Organize configuration by profiles (dev, test, prod)
- Use `@ConfigurationProperties` for complex configurations
- Example:
  ```yaml
  spring:
    profiles:
      active: dev
    datasource:
      url: jdbc:postgresql://localhost:5432/telepesa
      username: ${DB_USERNAME:admin}
      password: ${DB_PASSWORD:password}
    jpa:
      hibernate:
        ddl-auto: validate
      properties:
        hibernate:
          dialect: org.hibernate.dialect.PostgreSQLDialect
          format_sql: true
    security:
      jwt:
        secret: ${JWT_SECRET:your-secret-key}
        expiration: 86400000
  ```

### Profile Management
- Use Spring profiles for environment-specific configurations
- Standard profiles: `dev`, `test`, `prod`
- Profile-specific configuration files: `application-{profile}.yml`
- Use `@Profile` annotation for environment-specific beans

### Dependency Injection
- Use constructor injection (preferred over field injection)
- Use `@Autowired` only when necessary
- Prefer interface-based dependency injection
- Example:
  ```java
  @Service
  public class UserService {
      
      private final UserRepository userRepository;
      private final EmailService emailService;
      
      public UserService(UserRepository userRepository, EmailService emailService) {
          this.userRepository = userRepository;
          this.emailService = emailService;
      }
  }
  ```

## REST API Design

### Controller Conventions
- Use `@RestController` for REST endpoints
- Apply `@RequestMapping` at class level for base path
- Use specific HTTP method annotations (`@GetMapping`, `@PostMapping`, etc.)
- Follow RESTful URL conventions
- Example:
  ```java
  @RestController
  @RequestMapping("/api/v1/users")
  @Validated
  public class UserController {
      
      private final UserService userService;
      
      public UserController(UserService userService) {
          this.userService = userService;
      }
      
      @GetMapping
      public ResponseEntity<Page<UserDto>> getUsers(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "20") int size) {
          
          Page<UserDto> users = userService.getUsers(PageRequest.of(page, size));
          return ResponseEntity.ok(users);
      }
      
      @GetMapping("/{id}")
      public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
          UserDto user = userService.getUser(id);
          return ResponseEntity.ok(user);
      }
      
      @PostMapping
      public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
          UserDto user = userService.createUser(request);
          return ResponseEntity.status(HttpStatus.CREATED).body(user);
      }
  }
  ```

### URL Conventions
- Use nouns, not verbs in URLs
- Use plural nouns for collections: `/api/v1/users`
- Use specific resources: `/api/v1/users/{id}`
- Use sub-resources: `/api/v1/users/{id}/orders`
- Use query parameters for filtering, sorting, pagination

### Response Standards
- Use standard HTTP status codes
- Return consistent response formats
- Use ResponseEntity for explicit status control
- Implement pagination for collections
- Example response format:
  ```java
  public class ApiResponse<T> {
      private boolean success;
      private String message;
      private T data;
      private LocalDateTime timestamp;
      
      // constructors, getters, setters
  }
  ```

### Request/Response DTOs
- Always use DTOs for API contracts
- Never expose entities directly in controllers
- Use validation annotations on DTOs
- Example:
  ```java
  public class CreateUserRequest {
      
      @NotBlank(message = "Username is required")
      @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
      private String username;
      
      @NotBlank(message = "Email is required")
      @Email(message = "Email must be valid")
      private String email;
      
      @NotBlank(message = "Password is required")
      @Size(min = 8, message = "Password must be at least 8 characters")
      private String password;
      
      // getters, setters
  }
  ```

### API Versioning
- Use URL versioning: `/api/v1/`, `/api/v2/`
- Maintain backward compatibility when possible
- Document API changes and migration paths

## Service Layer

### Service Conventions
- Use `@Service` annotation
- Implement business logic in service layer
- Use interfaces for service contracts
- Keep services focused on single responsibilities
- Example:
  ```java
  public interface UserService {
      UserDto createUser(CreateUserRequest request);
      UserDto getUser(Long id);
      Page<UserDto> getUsers(Pageable pageable);
      UserDto updateUser(Long id, UpdateUserRequest request);
      void deleteUser(Long id);
  }
  
  @Service
  @Transactional
  public class UserServiceImpl implements UserService {
      
      private final UserRepository userRepository;
      private final PasswordEncoder passwordEncoder;
      private final UserMapper userMapper;
      
      public UserServiceImpl(
          UserRepository userRepository, 
          PasswordEncoder passwordEncoder,
          UserMapper userMapper) {
          this.userRepository = userRepository;
          this.passwordEncoder = passwordEncoder;
          this.userMapper = userMapper;
      }
      
      @Override
      public UserDto createUser(CreateUserRequest request) {
          if (userRepository.existsByUsername(request.getUsername())) {
              throw new DuplicateUserException("Username already exists");
          }
          
          User user = User.builder()
              .username(request.getUsername())
              .email(request.getEmail())
              .password(passwordEncoder.encode(request.getPassword()))
              .createdAt(LocalDateTime.now())
              .build();
              
          User savedUser = userRepository.save(user);
          return userMapper.toDto(savedUser);
      }
  }
  ```

### Transaction Management
- Use `@Transactional` appropriately
- Apply at service layer, not repository layer
- Use `readOnly = true` for read operations
- Handle transaction boundaries carefully
- Example:
  ```java
  @Transactional
  public UserDto transferFunds(Long fromUserId, Long toUserId, BigDecimal amount) {
      User fromUser = userRepository.findById(fromUserId)
          .orElseThrow(() -> new UserNotFoundException("User not found"));
      User toUser = userRepository.findById(toUserId)
          .orElseThrow(() -> new UserNotFoundException("User not found"));
      
      if (fromUser.getBalance().compareTo(amount) < 0) {
          throw new InsufficientFundsException("Insufficient balance");
      }
      
      fromUser.deductBalance(amount);
      toUser.addBalance(amount);
      
      userRepository.save(fromUser);
      userRepository.save(toUser);
      
      return userMapper.toDto(fromUser);
  }
  
  @Transactional(readOnly = true)
  public Page<UserDto> getUsers(Pageable pageable) {
      Page<User> users = userRepository.findAll(pageable);
      return users.map(userMapper::toDto);
  }
  ```

### Business Logic Patterns
- Keep business logic in service layer
- Use domain models for complex business rules
- Implement validation at multiple layers
- Use events for loose coupling between services

## Data Access Layer

### JPA Entity Design
- Use JPA annotations properly
- Follow entity naming conventions
- Use appropriate relationships and fetch types
- Example:
  ```java
  @Entity
  @Table(name = "users")
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public class User {
      
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;
      
      @Column(unique = true, nullable = false, length = 50)
      private String username;
      
      @Column(unique = true, nullable = false)
      private String email;
      
      @Column(nullable = false)
      private String password;
      
      @Enumerated(EnumType.STRING)
      @Column(nullable = false)
      private UserStatus status;
      
      @Column(name = "created_at", nullable = false)
      private LocalDateTime createdAt;
      
      @Column(name = "updated_at")
      private LocalDateTime updatedAt;
      
      @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
      private List<Order> orders = new ArrayList<>();
      
      @PrePersist
      protected void onCreate() {
          createdAt = LocalDateTime.now();
      }
      
      @PreUpdate
      protected void onUpdate() {
          updatedAt = LocalDateTime.now();
      }
  }
  ```

### Repository Pattern
- Extend JpaRepository for basic CRUD operations
- Use custom queries with `@Query` annotation
- Implement custom repository interfaces for complex queries
- Use method naming conventions for query derivation
- Example:
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

### Database Configuration
- Use connection pooling (HikariCP is default)
- Configure proper database dialect
- Use Flyway or Liquibase for database migrations
- Example Flyway migration:
  ```sql
  -- V1__Create_users_table.sql
  CREATE TABLE users (
      id BIGSERIAL PRIMARY KEY,
      username VARCHAR(50) UNIQUE NOT NULL,
      email VARCHAR(255) UNIQUE NOT NULL,
      password VARCHAR(255) NOT NULL,
      status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP
  );
  
  CREATE INDEX idx_users_email ON users(email);
  CREATE INDEX idx_users_username ON users(username);
  CREATE INDEX idx_users_status ON users(status);
  ```

### Query Optimization
- Use appropriate fetch strategies
- Implement pagination for large datasets
- Use projections for read-only operations
- Monitor and optimize N+1 query problems
- Example:
  ```java
  // Use projections for read-only data
  public interface UserSummary {
      Long getId();
      String getUsername();
      String getEmail();
      UserStatus getStatus();
  }
  
  @Query("SELECT u.id as id, u.username as username, u.email as email, u.status as status FROM User u")
  Page<UserSummary> findUserSummaries(Pageable pageable);
  
  // Use JOIN FETCH to avoid N+1 queries
  @Query("SELECT u FROM User u JOIN FETCH u.orders WHERE u.id = :id")
  Optional<User> findUserWithOrders(@Param("id") Long id);
  ```

## Security

### Spring Security Configuration
- Configure security properly for REST APIs
- Use JWT tokens for stateless authentication
- Implement role-based access control
- Example:
  ```java
  @Configuration
  @EnableWebSecurity
  @EnableMethodSecurity(prePostEnabled = true)
  public class SecurityConfig {
      
      private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
      private final JwtRequestFilter jwtRequestFilter;
      
      public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                           JwtRequestFilter jwtRequestFilter) {
          this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
          this.jwtRequestFilter = jwtRequestFilter;
      }
      
      @Bean
      public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
          http.csrf(csrf -> csrf.disable())
              .authorizeHttpRequests(authz -> authz
                  .requestMatchers("/api/v1/auth/**").permitAll()
                  .requestMatchers("/api/v1/public/**").permitAll()
                  .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole("ADMIN")
                  .requestMatchers(HttpMethod.POST, "/api/v1/users").hasRole("ADMIN")
                  .anyRequest().authenticated()
              )
              .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
              .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
          
          http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
          
          return http.build();
      }
      
      @Bean
      public PasswordEncoder passwordEncoder() {
          return new BCryptPasswordEncoder();
      }
  }
  ```

### JWT Implementation
- Use secure JWT tokens with proper expiration
- Implement token refresh mechanism
- Store sensitive data securely
- Example:
  ```java
  @Component
  public class JwtTokenUtil {
      
      private final String secret;
      private final int jwtExpiration;
      
      public JwtTokenUtil(@Value("${app.jwt.secret}") String secret,
                         @Value("${app.jwt.expiration}") int jwtExpiration) {
          this.secret = secret;
          this.jwtExpiration = jwtExpiration;
      }
      
      public String generateToken(UserDetails userDetails) {
          Map<String, Object> claims = new HashMap<>();
          return createToken(claims, userDetails.getUsername());
      }
      
      private String createToken(Map<String, Object> claims, String subject) {
          return Jwts.builder()
              .setClaims(claims)
              .setSubject(subject)
              .setIssuedAt(new Date(System.currentTimeMillis()))
              .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000L))
              .signWith(SignatureAlgorithm.HS512, secret)
              .compact();
      }
      
      public Boolean validateToken(String token, UserDetails userDetails) {
          final String username = getUsernameFromToken(token);
          return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
      }
  }
  ```

### Input Validation & Sanitization
- Use Bean Validation annotations
- Implement custom validators when needed
- Sanitize input data to prevent XSS attacks
- Example:
  ```java
  @Component
  public class EmailValidator implements ConstraintValidator<ValidEmail, String> {
      
      private static final String EMAIL_PATTERN = 
          "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
      
      private Pattern pattern;
      
      @Override
      public void initialize(ValidEmail constraintAnnotation) {
          pattern = Pattern.compile(EMAIL_PATTERN);
      }
      
      @Override
      public boolean isValid(String email, ConstraintValidatorContext context) {
          return email != null && pattern.matcher(email).matches();
      }
  }
  
  @Target({ElementType.TYPE, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  @Constraint(validatedBy = EmailValidator.class)
  @Documented
  public @interface ValidEmail {
      String message() default "Invalid email";
      Class<?>[] groups() default {};
      Class<? extends Payload>[] payload() default {};
  }
  ```

## Error Handling

### Global Exception Handling
- Use `@ControllerAdvice` for global exception handling
- Create custom exception types for business logic
- Return consistent error responses
- Example:
  ```java
  @ControllerAdvice
  @Order(Ordered.HIGHEST_PRECEDENCE)
  public class GlobalExceptionHandler {
      
      @ExceptionHandler(UserNotFoundException.class)
      public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
          ErrorResponse error = ErrorResponse.builder()
              .status(HttpStatus.NOT_FOUND.value())
              .error("User Not Found")
              .message(ex.getMessage())
              .timestamp(LocalDateTime.now())
              .build();
          return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
      }
      
      @ExceptionHandler(MethodArgumentNotValidException.class)
      public ResponseEntity<ErrorResponse> handleValidationExceptions(
          MethodArgumentNotValidException ex) {
          
          Map<String, String> errors = new HashMap<>();
          ex.getBindingResult().getAllErrors().forEach((error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
          });
          
          ErrorResponse error = ErrorResponse.builder()
              .status(HttpStatus.BAD_REQUEST.value())
              .error("Validation Failed")
              .message("Invalid input data")
              .validationErrors(errors)
              .timestamp(LocalDateTime.now())
              .build();
              
          return ResponseEntity.badRequest().body(error);
      }
      
      @ExceptionHandler(Exception.class)
      public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
          ErrorResponse error = ErrorResponse.builder()
              .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
              .error("Internal Server Error")
              .message("An unexpected error occurred")
              .timestamp(LocalDateTime.now())
              .build();
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
      }
  }
  ```

### Custom Exceptions
- Create domain-specific exception hierarchies
- Include meaningful error messages
- Use proper exception inheritance
- Example:
  ```java
  public abstract class BusinessException extends RuntimeException {
      protected BusinessException(String message) {
          super(message);
      }
      
      protected BusinessException(String message, Throwable cause) {
          super(message, cause);
      }
  }
  
  public class UserNotFoundException extends BusinessException {
      public UserNotFoundException(String message) {
          super(message);
      }
      
      public UserNotFoundException(Long userId) {
          super("User not found with id: " + userId);
      }
  }
  
  public class DuplicateUserException extends BusinessException {
      public DuplicateUserException(String message) {
          super(message);
      }
  }
  
  public class InsufficientFundsException extends BusinessException {
      public InsufficientFundsException(String message) {
          super(message);
      }
  }
  ```

### Error Response Format
- Use consistent error response structure
- Include helpful debugging information
- Example:
  ```java
  @Data
  @Builder
  public class ErrorResponse {
      private int status;
      private String error;
      private String message;
      private String path;
      private LocalDateTime timestamp;
      private Map<String, String> validationErrors;
  }
  ```

## Testing

### Test Structure
- Organize tests in same package structure as main code
- Use proper test naming conventions
- Separate unit tests, integration tests, and end-to-end tests
- Example structure:
  ```
  src/test/java/
    com/company/project/
      controller/
        UserControllerTest.java
        UserControllerIntegrationTest.java
      service/
        UserServiceTest.java
        UserServiceIntegrationTest.java
      repository/
        UserRepositoryTest.java
  ```

### Unit Testing
- Use JUnit 5 for unit testing
- Use Mockito for mocking dependencies
- Test business logic thoroughly
- Example:
  ```java
  @ExtendWith(MockitoExtension.class)
  class UserServiceTest {
      
      @Mock
      private UserRepository userRepository;
      
      @Mock
      private PasswordEncoder passwordEncoder;
      
      @Mock
      private UserMapper userMapper;
      
      @InjectMocks
      private UserServiceImpl userService;
      
      @Test
      void createUser_WithValidRequest_ShouldReturnUserDto() {
          // Given
          CreateUserRequest request = CreateUserRequest.builder()
              .username("testuser")
              .email("test@example.com")
              .password("password123")
              .build();
              
          User savedUser = User.builder()
              .id(1L)
              .username("testuser")
              .email("test@example.com")
              .password("encoded-password")
              .build();
              
          UserDto expectedDto = UserDto.builder()
              .id(1L)
              .username("testuser")
              .email("test@example.com")
              .build();
          
          when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
          when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");
          when(userRepository.save(any(User.class))).thenReturn(savedUser);
          when(userMapper.toDto(savedUser)).thenReturn(expectedDto);
          
          // When
          UserDto result = userService.createUser(request);
          
          // Then
          assertThat(result).isEqualTo(expectedDto);
          verify(userRepository).existsByUsername(request.getUsername());
          verify(userRepository).save(any(User.class));
      }
      
      @Test
      void createUser_WithDuplicateUsername_ShouldThrowException() {
          // Given
          CreateUserRequest request = CreateUserRequest.builder()
              .username("existinguser")
              .email("test@example.com")
              .password("password123")
              .build();
              
          when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);
          
          // When & Then
          assertThrows(DuplicateUserException.class, () -> userService.createUser(request));
          verify(userRepository, never()).save(any(User.class));
      }
  }
  ```

### Integration Testing
- Use `@SpringBootTest` for integration tests
- Use test containers for database testing
- Test complete request/response cycles
- Example:
  ```java
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  @TestPropertySource(properties = {
      "spring.datasource.url=jdbc:h2:mem:testdb",
      "spring.jpa.hibernate.ddl-auto=create-drop"
  })
  class UserControllerIntegrationTest {
      
      @Autowired
      private TestRestTemplate restTemplate;
      
      @Autowired
      private UserRepository userRepository;
      
      @BeforeEach
      void setUp() {
          userRepository.deleteAll();
      }
      
      @Test
      void createUser_WithValidRequest_ShouldReturnCreatedUser() {
          // Given
          CreateUserRequest request = CreateUserRequest.builder()
              .username("testuser")
              .email("test@example.com")
              .password("password123")
              .build();
          
          // When
          ResponseEntity<UserDto> response = restTemplate.postForEntity(
              "/api/v1/users", 
              request, 
              UserDto.class
          );
          
          // Then
          assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
          assertThat(response.getBody().getUsername()).isEqualTo("testuser");
          assertThat(response.getBody().getEmail()).isEqualTo("test@example.com");
          
          Optional<User> savedUser = userRepository.findByUsername("testuser");
          assertThat(savedUser).isPresent();
      }
  }
  ```

### Repository Testing
- Use `@DataJpaTest` for repository testing
- Test custom queries and repository methods
- Use TestEntityManager for setup
- Example:
  ```java
  @DataJpaTest
  class UserRepositoryTest {
      
      @Autowired
      private TestEntityManager entityManager;
      
      @Autowired
      private UserRepository userRepository;
      
      @Test
      void findByUsername_WithExistingUser_ShouldReturnUser() {
          // Given
          User user = User.builder()
              .username("testuser")
              .email("test@example.com")
              .password("password")
              .status(UserStatus.ACTIVE)
              .createdAt(LocalDateTime.now())
              .build();
          entityManager.persistAndFlush(user);
          
          // When
          Optional<User> found = userRepository.findByUsername("testuser");
          
          // Then
          assertThat(found).isPresent();
          assertThat(found.get().getEmail()).isEqualTo("test@example.com");
      }
  }
  ```

### Test Data Builders
- Use builder pattern for test data creation
- Create reusable test data factories
- Example:
  ```java
  public class UserTestDataBuilder {
      private String username = "defaultuser";
      private String email = "default@example.com";
      private String password = "defaultpassword";
      private UserStatus status = UserStatus.ACTIVE;
      
      public UserTestDataBuilder withUsername(String username) {
          this.username = username;
          return this;
      }
      
      public UserTestDataBuilder withEmail(String email) {
          this.email = email;
          return this;
      }
      
      public UserTestDataBuilder withPassword(String password) {
          this.password = password;
          return this;
      }
      
      public UserTestDataBuilder withStatus(UserStatus status) {
          this.status = status;
          return this;
      }
      
      public User build() {
          return User.builder()
              .username(username)
              .email(email)
              .password(password)
              .status(status)
              .createdAt(LocalDateTime.now())
              .build();
      }
      
      public CreateUserRequest buildRequest() {
          return CreateUserRequest.builder()
              .username(username)
              .email(email)
              .password(password)
              .build();
      }
  }
  ```

## Configuration Management

### External Configuration
- Use environment variables for sensitive data
- Use Spring Boot configuration properties
- Organize configuration by profile
- Example:
  ```java
  @ConfigurationProperties(prefix = "app")
  @Data
  public class AppProperties {
      
      private Security security = new Security();
      private Database database = new Database();
      private Email email = new Email();
      
      @Data
      public static class Security {
          private String jwtSecret;
          private int jwtExpiration = 86400;
          private boolean enableCors = false;
      }
      
      @Data
      public static class Database {
          private int maxPoolSize = 20;
          private int minIdle = 5;
          private long connectionTimeout = 30000;
      }
      
      @Data
      public static class Email {
          private String host;
          private int port = 587;
          private String username;
          private String password;
          private boolean enableAuth = true;
      }
  }
  ```

### Feature Flags
- Use configuration properties for feature toggles
- Implement feature flag service
- Example:
  ```java
  @Component
  public class FeatureToggleService {
      
      private final AppProperties appProperties;
      
      public FeatureToggleService(AppProperties appProperties) {
          this.appProperties = appProperties;
      }
      
      public boolean isFeatureEnabled(String featureName) {
          return appProperties.getFeatures()
              .getOrDefault(featureName, false);
      }
  }
  ```

## Logging & Monitoring

### Logging Configuration
- Use SLF4J with Logback
- Configure appropriate log levels by environment
- Structure logs for easy parsing
- Example logback configuration:
  ```xml
  <configuration>
      <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
          <encoder class="net.logstash.logback.encoder.LogstashEncoder">
              <includeContext>true</includeContext>
              <includeMdc>true</includeMdc>
          </encoder>
      </appender>
      
      <logger name="com.company.project" level="DEBUG"/>
      <logger name="org.springframework.security" level="INFO"/>
      <logger name="org.hibernate.SQL" level="DEBUG"/>
      
      <root level="INFO">
          <appender-ref ref="STDOUT"/>
      </root>
  </configuration>
  ```

### Structured Logging
- Use structured logging with meaningful context
- Include correlation IDs for request tracing
- Example:
  ```java
  @Component
  public class LoggingService {
      
      private static final Logger logger = LoggerFactory.getLogger(LoggingService.class);
      
      public void logUserAction(String action, Long userId, Map<String, Object> context) {
          MDC.put("userId", String.valueOf(userId));
          MDC.put("action", action);
          
          context.forEach((key, value) -> MDC.put(key, String.valueOf(value)));
          
          logger.info("User action performed: {}", action);
          
          MDC.clear();
      }
  }
  ```

### Health Checks & Metrics
- Implement Spring Boot Actuator endpoints
- Create custom health indicators
- Monitor application metrics
- Example:
  ```java
  @Component
  public class DatabaseHealthIndicator implements HealthIndicator {
      
      private final UserRepository userRepository;
      
      public DatabaseHealthIndicator(UserRepository userRepository) {
          this.userRepository = userRepository;
      }
      
      @Override
      public Health health() {
          try {
              long userCount = userRepository.count();
              return Health.up()
                  .withDetail("userCount", userCount)
                  .build();
          } catch (Exception e) {
              return Health.down()
                  .withDetail("error", e.getMessage())
                  .build();
          }
      }
  }
  ```

## Performance & Optimization

### Caching
- Use Spring Cache abstraction
- Implement appropriate caching strategies
- Use Redis for distributed caching
- Example:
  ```java
  @Service
  @CacheConfig(cacheNames = "users")
  public class UserService {
      
      @Cacheable(key = "#id")
      public UserDto getUser(Long id) {
          return userRepository.findById(id)
              .map(userMapper::toDto)
              .orElseThrow(() -> new UserNotFoundException(id));
      }
      
      @CacheEvict(key = "#result.id")
      public UserDto updateUser(Long id, UpdateUserRequest request) {
          // Update logic
      }
      
      @CacheEvict(allEntries = true)
      public void clearUserCache() {
          // Clear all user cache entries
      }
  }
  ```

### Database Optimization
- Use database indexing appropriately
- Implement query optimization
- Use connection pooling
- Monitor slow queries

### Async Processing
- Use `@Async` for non-blocking operations
- Implement task executors properly
- Example:
  ```java
  @Service
  public class EmailService {
      
      private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
      
      @Async("taskExecutor")
      public CompletableFuture<Void> sendWelcomeEmail(String email, String username) {
          try {
              // Email sending logic
              logger.info("Welcome email sent to: {}", email);
              return CompletableFuture.completedFuture(null);
          } catch (Exception e) {
              logger.error("Failed to send welcome email to: {}", email, e);
              return CompletableFuture.failedFuture(e);
          }
      }
  }
  
  @Configuration
  @EnableAsync
  public class AsyncConfig {
      
      @Bean(name = "taskExecutor")
      public Executor taskExecutor() {
          ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
          executor.setCorePoolSize(4);
          executor.setMaxPoolSize(8);
          executor.setQueueCapacity(100);
          executor.setThreadNamePrefix("async-");
          executor.initialize();
          return executor;
      }
  }
  ```

## Documentation

### API Documentation
- Use OpenAPI/Swagger for API documentation
- Include comprehensive examples
- Document all endpoints, parameters, and responses
- Example:
  ```java
  @RestController
  @RequestMapping("/api/v1/users")
  @Tag(name = "User Management", description = "APIs for managing users")
  public class UserController {
      
      @Operation(
          summary = "Get user by ID",
          description = "Retrieve a user's details by their unique identifier",
          responses = {
              @ApiResponse(responseCode = "200", description = "User found"),
              @ApiResponse(responseCode = "404", description = "User not found")
          }
      )
      @GetMapping("/{id}")
      public ResponseEntity<UserDto> getUser(
          @Parameter(description = "User ID", example = "1")
          @PathVariable Long id) {
          
          UserDto user = userService.getUser(id);
          return ResponseEntity.ok(user);
      }
  }
  ```

### Code Documentation
- Use JavaDoc for public APIs
- Document complex business logic
- Include examples in documentation
- Example:
  ```java
  /**
   * Service for managing user operations including registration, authentication,
   * and profile management.
   * 
   * @author Development Team
   * @version 1.0
   * @since 1.0
   */
  @Service
  public class UserService {
      
      /**
       * Creates a new user account with the provided information.
       * 
       * @param request the user creation request containing username, email, and password
       * @return the created user's details as a DTO
       * @throws DuplicateUserException if username or email already exists
       * @throws ValidationException if request data is invalid
       * 
       * @example
       * <pre>
       * CreateUserRequest request = CreateUserRequest.builder()
       *     .username("johndoe")
       *     .email("john@example.com")
       *     .password("securePassword123")
       *     .build();
       * UserDto user = userService.createUser(request);
       * </pre>
       */
      public UserDto createUser(CreateUserRequest request) {
          // Implementation
      }
  }
  ```

## Build & Deployment

### Maven Configuration
- Use proper Maven structure and plugins
- Configure profiles for different environments
- Example `pom.xml` structure:
  ```xml
  <project>
      <modelVersion>4.0.0</modelVersion>
      
      <parent>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-starter-parent</artifactId>
          <version>3.1.0</version>
          <relativePath/>
      </parent>
      
      <groupId>com.company</groupId>
      <artifactId>project-name</artifactId>
      <version>1.0.0</version>
      <packaging>jar</packaging>
      
      <properties>
          <java.version>17</java.version>
          <maven.compiler.source>17</maven.compiler.source>
          <maven.compiler.target>17</maven.compiler.target>
      </properties>
      
      <dependencies>
          <!-- Spring Boot Starters -->
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-web</artifactId>
          </dependency>
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-data-jpa</artifactId>
          </dependency>
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-security</artifactId>
          </dependency>
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-validation</artifactId>
          </dependency>
      </dependencies>
      
      <build>
          <plugins>
              <plugin>
                  <groupId>org.springframework.boot</groupId>
                  <artifactId>spring-boot-maven-plugin</artifactId>
              </plugin>
          </plugins>
      </build>
      
      <profiles>
          <profile>
              <id>dev</id>
              <activation>
                  <activeByDefault>true</activeByDefault>
              </activation>
              <properties>
                  <spring.profiles.active>dev</spring.profiles.active>
              </properties>
          </profile>
          <profile>
              <id>prod</id>
              <properties>
                  <spring.profiles.active>prod</spring.profiles.active>
              </properties>
          </profile>
      </profiles>
  </project>
  ```

### Containerization
- Use Docker for containerization
- Create multi-stage builds for optimization
- Example Dockerfile:
  ```dockerfile
  # Build stage
  FROM openjdk:17-jdk-slim as build
  WORKDIR /workspace/app
  
  COPY mvnw .
  COPY .mvn .mvn
  COPY pom.xml .
  COPY src src
  
  RUN ./mvnw install -DskipTests
  RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)
  
  # Production stage
  FROM openjdk:17-jre-slim
  VOLUME /tmp
  ARG DEPENDENCY=/workspace/app/target/dependency
  COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
  COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
  COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
  
  ENTRYPOINT ["java","-cp","app:app/lib/*","com.company.project.Application"]
  ```

## Security Best Practices

### Input Validation
- Validate all input data
- Use whitelist validation over blacklist
- Sanitize data to prevent injection attacks
- Use parameterized queries

### Authentication & Authorization
- Implement proper session management
- Use secure password policies
- Implement rate limiting for authentication endpoints
- Use HTTPS in production

### Data Protection
- Encrypt sensitive data at rest
- Use secure communication protocols
- Implement proper access controls
- Regular security audits

## Real-time Features

### WebSocket Support
- Use Spring WebSocket for real-time communication
- Implement STOMP messaging protocol
- Handle connection management and cleanup
- Example:
  ```java
  @Configuration
  @EnableWebSocketMessageBroker
  public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
      
      @Override
      public void configureMessageBroker(MessageBrokerRegistry config) {
          config.enableSimpleBroker("/topic");
          config.setApplicationDestinationPrefixes("/app");
      }
      
      @Override
      public void registerStompEndpoints(StompEndpointRegistry registry) {
          registry.addEndpoint("/ws").withSockJS();
      }
  }
  ```

### Event-Driven Architecture
- Use Spring Events for loose coupling
- Implement asynchronous event processing
- Handle event failures gracefully
- Example:
  ```java
  @EventListener
  @Async
  public void handleUserCreated(UserCreatedEvent event) {
      // Send welcome email, update analytics, etc.
  }
  ```

## Message Queues & Integration

### Spring Integration
- Use Spring Integration for enterprise integration patterns
- Implement message channels and endpoints
- Handle message routing and transformation

### Redis Integration
- Use Redis for caching and session storage
- Implement Redis pub/sub for messaging
- Configure Redis clustering for high availability

### Apache Kafka Integration
- Use Spring Kafka for event streaming
- Implement proper producer and consumer configurations
- Handle message serialization and error recovery

## Microservices Patterns

### Service Discovery
- Use Spring Cloud for service discovery
- Implement circuit breaker patterns
- Configure load balancing strategies

### API Gateway
- Implement API gateway with Spring Cloud Gateway
- Configure rate limiting and authentication
- Handle cross-cutting concerns centrally

### Distributed Tracing
- Use Spring Cloud Sleuth for distributed tracing
- Integrate with Zipkin or Jaeger
- Track requests across service boundaries

## Advanced Security

### OAuth2 & OpenID Connect
- Implement OAuth2 resource server
- Configure multiple authentication providers
- Handle token validation and refresh
- Example:
  ```java
  @Configuration
  @EnableResourceServer
  public class ResourceServerConfig extends ResourceServerConfigurerAdapter {
      
      @Override
      public void configure(HttpSecurity http) throws Exception {
          http.oauth2ResourceServer(oauth2 -> oauth2.jwt());
      }
  }
  ```

### Rate Limiting
- Implement rate limiting for API endpoints
- Use Redis or in-memory storage for counters
- Configure different limits per user/IP
- Example:
  ```java
  @Component
  public class RateLimitingFilter implements Filter {
      
      private final RedisTemplate<String, String> redisTemplate;
      
      @Override
      public void doFilter(ServletRequest request, ServletResponse response, 
                          FilterChain chain) throws IOException, ServletException {
          // Rate limiting logic
      }
  }
  ```

### CORS Configuration
- Configure CORS policies properly
- Use environment-specific configurations
- Implement preflight request handling
- Example:
  ```java
  @Configuration
  public class CorsConfig {
      
      @Bean
      public CorsConfigurationSource corsConfigurationSource() {
          CorsConfiguration configuration = new CorsConfiguration();
          configuration.setAllowedOriginPatterns(Arrays.asList("*"));
          configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
          configuration.setAllowedHeaders(Arrays.asList("*"));
          configuration.setAllowCredentials(true);
          
          UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
          source.registerCorsConfiguration("/**", configuration);
          return source;
      }
  }
  ```

## Email & Notifications

### Email Integration
- Use Spring Mail for email functionality
- Implement template-based emails
- Handle email delivery failures
- Configure SMTP settings properly

### Push Notifications
- Integrate with Firebase Cloud Messaging
- Implement notification templates
- Handle device token management
- Track notification delivery and engagement

## Payment Integration

### Payment Gateway Integration
- Implement secure payment processing
- Handle webhook verification
- Store payment data compliantly
- Implement refund and dispute handling

### Financial Compliance
- Implement PCI DSS compliance measures
- Handle sensitive financial data properly
- Implement audit trails for transactions
- Use encryption for stored payment data

## Data Migration & Seeding

### Database Migrations
- Use Flyway or Liquibase for schema versioning
- Implement rollback strategies
- Handle data migration scripts
- Test migrations in staging environments

### Data Seeding
- Create development data seeders
- Implement environment-specific seed data
- Handle data dependencies properly
- Use profiles for different data sets

## Internationalization (i18n)

### Multi-language Support
- Use Spring MessageSource for internationalization
- Implement locale detection and switching
- Handle currency and date formatting
- Example:
  ```java
  @Service
  public class MessageService {
      
      private final MessageSource messageSource;
      
      public String getMessage(String key, Locale locale, Object... args) {
          return messageSource.getMessage(key, args, locale);
      }
  }
  ```

## Code Quality

### Static Analysis
- Use SonarQube for code quality analysis
- Configure checkstyle for code formatting
- Use SpotBugs for bug detection
- Maintain high code coverage (>80%)

### Code Reviews
- Implement mandatory code reviews
- Use pull request templates
- Review for security vulnerabilities
- Check for performance implications

### Continuous Integration
- Implement CI/CD pipelines
- Run tests automatically on commits
- Deploy to staging environment automatically
- Use feature flags for production deployments

This document provides comprehensive guidelines for building robust, scalable, and maintainable Spring Boot applications. Follow these practices to ensure code quality, security, and performance.