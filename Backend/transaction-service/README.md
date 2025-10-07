# Transaction Service

The Transaction Service is a core component of the Telepesa banking platform, responsible for processing financial transactions, managing transaction history, and providing transaction-related APIs.

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 13+ (for production) or H2 (for local development)

### Local Development Setup

#### Option 1: Automated Setup (Recommended)
```bash
# Navigate to transaction service
cd Backend/transaction-service

# Run the setup script
./scripts/setup-local-dev.sh
```

#### Option 2: Manual Setup
```bash
# 1. Source environment variables
source scripts/env-local.sh

# 2. Build shared libraries
cd ../shared-libraries
mvn clean install -DskipTests

# 3. Return to transaction service
cd ../transaction-service

# 4. Run tests
mvn clean test

# 5. Start the service
mvn spring-boot:run -Dspring.profiles.active=local
```

### Environment Variables

For local development, configure the following environment variables:

```bash
# Security Scanning (Get your own free API key)
# Visit: https://nvd.nist.gov/developers/request-an-api-key
NVD_API_KEY=your-nvd-api-key-here

# Spring Boot
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8083

# Database (H2 for local development)
DB_URL=jdbc:h2:mem:telepesa_local;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false
DB_USERNAME=sa
DB_PASSWORD=

# JWT (for local development only)
JWT_SECRET=local-dev-secret-key-for-testing-only
JWT_EXPIRATION=86400000
```

**Getting Your NVD API Key:**
1. Visit: https://nvd.nist.gov/developers/request-an-api-key
2. Fill out the simple form with your email
3. Receive your API key via email (usually within minutes)
4. Export: `export NVD_API_KEY="your-key-here"`
5. Add to `~/.bashrc` or `~/.zshrc` for persistence

## Service Information

- **Port**: 8083
- **Health Check**: http://localhost:8083/actuator/health
- **API Documentation**: http://localhost:8083/swagger-ui.html
- **H2 Console**: http://localhost:8083/h2-console (local development only)

## Testing

### Running Tests
```bash
# Run all tests
mvn test

# Run tests with coverage
mvn test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

### Test Coverage Requirements
- **Line Coverage**: ≥ 80%
- **Branch Coverage**: ≥ 54%
- **Current Status**: 104/104 tests passing (100% success rate)

### Test Categories
- **Unit Tests**: Service layer, DTOs, entities, mappers
- **Integration Tests**: Repository layer, controller endpoints
- **Security Tests**: Authentication, authorization, input validation

## Security

### OWASP Dependency Check
```bash
# Run security scan (with API key for faster execution)
mvn dependency-check:check -DnvdApiKey=$NVD_API_KEY

# Run security scan with custom CVSS threshold
mvn dependency-check:check -DnvdApiKey=$NVD_API_KEY -DfailBuildOnCVSS=7
```

### Security Features
- JWT-based authentication
- Input validation and sanitization
- SQL injection prevention
- CORS configuration
- Rate limiting support
- Audit logging

## Build & Deployment

### Local Build
```bash
# Clean build
mvn clean compile

# Package JAR
mvn clean package

# Skip tests (not recommended)
mvn clean package -DskipTests
```

### Docker Build
```bash
# Build Docker image
docker build -t telepesa/transaction-service:latest .

# Run with Docker
docker run -p 8083:8083 telepesa/transaction-service:latest
```

## Configuration

### Application Profiles

#### Local Development (`application-local.yml`)
- H2 in-memory database
- Debug logging enabled
- H2 console accessible
- Relaxed security for development

#### Test (`application-test.yml`)
- H2 in-memory database
- Minimal logging
- Fast startup configuration

#### Production (`application-prod.yml`)
- PostgreSQL database
- Optimized logging
- Security hardening
- Performance tuning

### Database Configuration

#### Local Development (H2)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:telepesa_local
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
```

#### Production (PostgreSQL)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/telepesa
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
```

## API Endpoints

### Transaction Management
- `POST /api/v1/transactions` - Create transaction
- `GET /api/v1/transactions/{id}` - Get transaction by ID
- `GET /api/v1/transactions` - List transactions with pagination
- `PUT /api/v1/transactions/{id}/status` - Update transaction status

### Transaction Queries
- `GET /api/v1/transactions/user/{userId}` - Get user transactions
- `GET /api/v1/transactions/account/{accountId}` - Get account transactions
- `GET /api/v1/transactions/search` - Search transactions

### Statistics
- `GET /api/v1/transactions/stats/user/{userId}` - User transaction statistics
- `GET /api/v1/transactions/stats/account/{accountId}` - Account statistics

## Monitoring

### Health Endpoints
- `/actuator/health` - Service health status
- `/actuator/info` - Service information
- `/actuator/metrics` - Application metrics
- `/actuator/env` - Environment properties

### Logging
```bash
# View logs in real-time
tail -f logs/transaction-service.log

# Search for specific patterns
grep "ERROR" logs/transaction-service.log
grep "Transaction created" logs/transaction-service.log
```

## Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# Find process using port 8083
lsof -i :8083

# Kill the process
kill -9 <PID>
```

#### Database Connection Issues
```bash
# Check if PostgreSQL is running
pg_isready -h localhost -p 5432

# Start PostgreSQL (macOS with Homebrew)
brew services start postgresql

# Start PostgreSQL (Linux)
sudo systemctl start postgresql
```

#### Maven Build Issues
```bash
# Clean Maven cache
rm -rf ~/.m2/repository

# Rebuild shared libraries
cd ../shared-libraries && mvn clean install -DskipTests
```

#### OWASP Security Scan Issues
```bash
# If you encounter parsing errors or slow scans
./scripts/fix-nvd-issues.sh

# Ensure NVD API key is set for faster scans
echo $NVD_API_KEY

# If not set, source the environment script
source scripts/env-local.sh

# For persistent issues, clear cache manually
rm -rf ~/.m2/repository/org/owasp/dependency-check-data
```

## Dependencies

### Shared Libraries
- `common-models` - Shared entity models and enums
- `common-exceptions` - Common exception classes
- `security-utils` - JWT utilities and security helpers

### External Dependencies
- Spring Boot 3.4.4
- Spring Security 6.2.5
- Spring Data JPA
- PostgreSQL Driver
- H2 Database (for testing)
- JUnit 5 & Mockito
- JaCoCo (code coverage)
- OWASP Dependency Check

## Development Guidelines

### Code Style
- Follow Spring Boot best practices
- Use Lombok for boilerplate reduction
- Implement comprehensive test coverage
- Document public APIs with JavaDoc

### Testing Requirements
- Write tests for all new features
- Maintain minimum 80% line coverage
- Include integration tests for controllers
- Test error scenarios and edge cases

### Security Guidelines
- Validate all input parameters
- Use parameterized queries
- Implement proper error handling
- Log security-relevant events

## Contributing

1. Create a feature branch
2. Write comprehensive tests
3. Ensure all tests pass
4. Run security scan
5. Submit pull request

### Pre-commit Checklist
- [ ] All tests pass (`mvn test`)
- [ ] Coverage requirements met (`mvn jacoco:report`)
- [ ] Security scan clean (`mvn dependency-check:check`)
- [ ] Code follows style guidelines
- [ ] Documentation updated

---

For more information about the overall Telepesa platform, see the [main README](../../README.md). 