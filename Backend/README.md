# Telepesa Backend Services

## Quick Start

The Telepesa backend is built with Spring Boot microservices architecture, providing secure, scalable banking services for African financial institutions.

### Prerequisites
- **Java 17+** (OpenJDK recommended)
- **Maven 3.8+** 
- **PostgreSQL 13+** (or Docker for easy setup)
- **Docker** (optional, for containerized development)
- **NVD API Key** (for faster OWASP security scans) - [Generate your own](https://nvd.nist.gov/developers/request-an-api-key)

### Getting Started

1. **Clone and Setup**
   ```bash
   git clone https://github.com/malcolmmaima/Telepesa.git
   cd Telepesa/Backend
   ```

2. **Start Database** (using Docker)
   ```bash
   docker run --name telepesa-db -e POSTGRES_PASSWORD=password \
     -e POSTGRES_USER=telepesa -e POSTGRES_DB=telepesa \
     -p 5432:5432 -d postgres:15
   ```

3. **Build Shared Libraries**
   ```bash
   cd shared-libraries
   mvn clean install
   ```

4. **Setup Environment Variables** (Recommended)
   ```bash
   # Setup local environment file
   ./scripts/setup-env.sh
   
   # Edit .env.local with your actual API keys
   nano .env.local
   ```

5. **Start User Service**
   ```bash
   cd ../user-service
   mvn spring-boot:run
   ```
   Service available at: http://localhost:8081

6. **Test the API**
   ```bash
   # Quick health check
   curl http://localhost:8081/actuator/health
   
   # Run comprehensive tests
   cd ..
   ./scripts/quick-api-test.sh
   ```

## Services Overview

### Active Services
| Service | Port | Status | Description |
|---------|------|--------|-------------|
| **User Service** | 8081 | **LIVE** | Authentication, user management, security |

### Planned Services  
| Service | Port | Status | Description |
|---------|------|--------|-------------|
| **Account Service** | 8082 | Planned | Account creation and management |
| **Transaction Service** | 8083 | Planned | Payment processing and transfers |
| **Loan Service** | 8084 | Planned | Loan applications and management |
| **Notification Service** | 8085 | Planned | Email, SMS, and push notifications |

## Testing & Quality

### Current Status
- **Unit Tests**: 81/81 passing (100% success rate)
- **Coverage**: 81% line coverage, 35% branch coverage  
- **E2E Tests**: 24/25 passing (96% success rate)
- **Overall**: **PRODUCTION READY**

### Quick Testing
```bash
# Run all tests for user service
cd user-service && mvn test

# Quick API validation (no Postman needed)
cd .. && ./scripts/quick-api-test.sh

# Comprehensive API testing with Postman
# Import: Telepesa_API_Collection.postman_collection.json
# Environment: Telepesa_Development.postman_environment.json
```

## Security Features

**JWT Authentication** - Stateless token-based auth  
**Rate Limiting** - Protection against abuse  
**Input Validation** - Comprehensive data validation  
**Audit Logging** - Complete action tracking  
**Device Fingerprinting** - Enhanced security  
**Password Security** - BCrypt + complexity rules  
**CORS Configuration** - Secure cross-origin requests  
**OWASP Security Scanning** - Automated vulnerability detection

### Security Scanning Setup
For faster OWASP dependency vulnerability scans, get your own NVD API key:

**Quick Setup:**
1. **Setup Environment**: `./scripts/setup-env.sh`
2. **Get API Key**: Visit https://nvd.nist.gov/developers/request-an-api-key
3. **Configure**: Edit `.env.local` with your API key
4. **Run Scan**: `mvn dependency-check:check -DnvdApiKey=$NVD_API_KEY`

**Benefits:**
- **10x faster** vulnerability scanning (2-3 minutes vs 30+ minutes)
- **Reliable API access** without rate limiting
- **Complete vulnerability database** access
- **Optimized CI/CD pipelines**

**Detailed Guide**: See [NVD API Setup Guide](docs/NVD_API_SETUP.md) for complete instructions.

**Per-Service Setup:**
- **Transaction Service**: Use `./scripts/setup-local-dev.sh` for automated setup
- **User Service**: Export NVD_API_KEY before running security scans
- **Other Services**: Add NVD_API_KEY to environment before Maven commands

## API Documentation

### Interactive Documentation
- **User Service**: http://localhost:8081/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8081/v3/api-docs

### Testing Resources
- **Postman Collection**: `Telepesa_API_Collection.postman_collection.json`
- **Test Environment**: `Telepesa_Development.postman_environment.json`
- **Quick Test Script**: `scripts/quick-api-test.sh`

## Development

### Technology Stack
- **Framework**: Spring Boot 3.2.0 + Java 17
- **Security**: Spring Security 6.2 + JWT
- **Database**: PostgreSQL 15 (Production), H2 (Testing)
- **Testing**: JUnit 5, Mockito, TestContainers
- **Documentation**: OpenAPI 3.0 (Swagger)

### Project Structure
```
Backend/
├── README.md                     # This file - Overview & setup
├── docs/                         # Detailed documentation
│   ├── API_TESTING_GUIDE.md      # Comprehensive testing guide
│   ├── END_TO_END_TEST_REPORT.md # Test execution results
│   └── SECURITY_*.md             # Security documentation
├── scripts/                      # All backend scripts
│   ├── quick-api-test.sh         # Quick validation script
│   ├── end-to-end-test.sh        # E2E testing script
│   ├── build-shared-libs.sh      # Build utilities
│   ├── test-enhanced-security.sh # Security testing
│   ├── setup-env.sh              # Environment setup
│   └── test-all-services.sh      # Service testing
├── user-service/                # User management service
├── account-service/             # Account management (planned)
├── transaction-service/         # Transaction processing (scripts available)
├── shared-libraries/            # Common utilities
├── Telepesa_API_Collection.postman_collection.json
└── Telepesa_Development.postman_environment.json
```

### Adding New Services
1. **Copy user-service structure** as template
2. **Update ports and configuration** (8082, 8083, etc.)
3. **Implement business logic** following existing patterns
4. **Create comprehensive tests** (80%+ coverage required)
5. **Update Postman collection** with new endpoints
6. **Update documentation** in docs folder

## Documentation

### Detailed Documentation (in docs/)
- [API Testing Guide](docs/API_TESTING_GUIDE.md) - Comprehensive testing guide
- [End-to-End Test Report](docs/END_TO_END_TEST_REPORT.md) - Latest test results
- [Security Implementation](docs/SECURITY_IMPLEMENTATION.md) - Security details
- [Security Features](docs/SECURITY_FEATURES.md) - Feature documentation
- [NVD API Setup Guide](docs/NVD_API_SETUP.md) - Fast security scanning setup
### External Links
- [User Service Testing Guide](user-service/README-TESTING.md)
- [Transaction Service Setup](transaction-service/README.md) - Local development with NVD API
- [Docker Compose Setup](docker-compose/docker-compose.yml)
- [Shared Libraries](shared-libraries/)

## Contributing

### Development Workflow
1. **Create feature branch** from main
2. **Implement changes** following Spring Boot rules
3. **Write comprehensive tests** (80% coverage minimum)
4. **Update documentation** in docs folder
5. **Update Postman collection** with new endpoints
6. **Submit pull request** with tests passing

### Quality Requirements
- All tests must pass (100%)
- Minimum 80% line coverage
- OWASP security scans pass
- API documentation updated
- Postman collection updated

## Deployment

### Local Development
```bash
# Start all services with Docker Compose
docker-compose up

# Or start individual services
cd user-service && mvn spring-boot:run
```

### Production Deployment
- **Container Registry**: Docker images ready
- **Environment Variables**: Configure via application.yml
- **Database**: PostgreSQL with proper migrations
- **Security**: JWT secrets, SSL certificates
- **Monitoring**: Actuator endpoints enabled

---

## Support

### Quick Help
- **Setup Issues**: Check Prerequisites and Database connection
- **Test Failures**: Run `mvn clean test` in service directory
- **API Issues**: Use `./scripts/quick-api-test.sh` for diagnosis
- **Documentation**: See `docs/` folder for detailed guides
- **Security Scans**: Use provided NVD API key for faster execution

### Development Resources
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Transaction Service README](transaction-service/README.md) - Complete local setup guide

---

*Building the future of African banking, one microservice at a time.* 