# Telepesa Backend Microservices

This directory contains all the microservices for the Telepesa fintech application.

## Architecture Overview

```
┌─────────────────┐    ┌──────────────────┐
│   Mobile Apps   │    │   Web Dashboard  │
│ (Android/iOS)   │    │     (React)      │
└─────────────────┘    └──────────────────┘
          │                       │
          └───────────┬───────────┘
                      │
         ┌────────────▼────────────┐
         │      API Gateway        │
         │   (Spring Cloud Gateway)│
         └─────────────┬───────────┘
                       │
    ┌──────────────────┼──────────────────┐
    │                  │                  │
┌───▼───┐    ┌────▼────┐    ┌─────▼─────┐ │
│ User  │    │Account  │    │Transaction│ │
│Service│    │Service  │    │ Service   │ │
└───────┘    └─────────┘    └───────────┘ │
    │                                     │
    │        ┌────────▼──┐    ┌───────▼───┐
    └────────► Loan     │    │Notification│
             │ Service  │    │  Service   │
             └──────────┘    └────────────┘
```

## Microservices

### 1. API Gateway
- **Port**: 8080
- **Purpose**: Route requests, handle authentication, rate limiting
- **Technology**: Spring Cloud Gateway

### 2. User Service
- **Port**: 8081
- **Purpose**: User management, authentication, profiles
- **Database**: PostgreSQL
- **Features**:
  - User registration/login
  - Profile management
  - Password reset
  - Account verification

### 3. Account Service
- **Port**: 8082
- **Purpose**: Bank account management, balances
- **Database**: PostgreSQL
- **Features**:
  - Account creation
  - Balance tracking
  - Account linking
  - Transaction history

### 4. Transaction Service
- **Port**: 8083
- **Purpose**: Payment processing, money transfers
- **Database**: PostgreSQL
- **Features**:
  - Send/receive money
  - Payment processing
  - Transaction logging
  - Fee calculation

### 5. Loan Service
- **Port**: 8085
- **Purpose**: Loan management, credit scoring, repayments
- **Database**: PostgreSQL
- **Features**:
  - Loan applications
  - Credit scoring & assessment
  - Loan approval workflow
  - Repayment tracking
  - Interest calculations
  - Collateral management

### 6. Notification Service
- **Port**: 8084
- **Purpose**: Email, SMS, and push notifications
- **Database**: MongoDB
- **Features**:
  - Email notifications
  - SMS alerts
  - Push notifications
  - Notification templates

## Shared Libraries

### common-models
Common data models used across services

### security-utils
JWT utilities, encryption helpers, security configurations

### common-exceptions
Standard exception classes and error handling

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL
- MongoDB
- Redis

### Running Locally

1. **Start infrastructure services:**
   ```bash
   cd docker-compose
   docker-compose up -d postgres mongodb redis
   ```

2. **Build shared libraries:**
   ```bash
   cd shared-libraries
   mvn clean install
   ```

3. **Start services in order:**
   ```bash
   # Terminal 1 - User Service
   cd user-service
   mvn spring-boot:run

   # Terminal 2 - Account Service
   cd account-service
   mvn spring-boot:run

   # Terminal 3 - Transaction Service
   cd transaction-service
   mvn spring-boot:run

       # Terminal 4 - Loan Service
    cd loan-service
    mvn spring-boot:run

    # Terminal 5 - Notification Service
    cd notification-service
    mvn spring-boot:run

    # Terminal 6 - API Gateway
   cd api-gateway
   mvn spring-boot:run
   ```

### Using Docker Compose

```bash
cd docker-compose
docker-compose up --build
```

## Development Guidelines

Follow the [Spring Boot Cursor Rules](../Rules/springboot-cursor-rules.md) for:
- Code structure and organization
- Naming conventions
- Security implementation
- Testing strategies
- Documentation standards

## API Documentation

Each service exposes OpenAPI documentation at:
- API Gateway: http://localhost:8080/swagger-ui.html
- User Service: http://localhost:8081/swagger-ui.html
- Account Service: http://localhost:8082/swagger-ui.html
- Transaction Service: http://localhost:8083/swagger-ui.html
- Loan Service: http://localhost:8085/swagger-ui.html
- Notification Service: http://localhost:8084/swagger-ui.html

## Monitoring & Observability

- **Health Checks**: `/actuator/health` for each service
- **Metrics**: Prometheus endpoints at `/actuator/prometheus`
- **Logging**: Centralized logging with ELK stack
- **Tracing**: Distributed tracing with Zipkin

## Security

- JWT-based authentication
- OAuth2 integration
- Rate limiting
- CORS configuration
- Input validation
- Encryption for sensitive data

## Contributing

1. Follow the established package structure
2. Implement proper error handling
3. Add comprehensive tests
4. Document API endpoints
5. Update this README for new services 