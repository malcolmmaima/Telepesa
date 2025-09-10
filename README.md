# Telepesa - Modern Banking Platform for Africa

<div align="center">

![Telepesa Logo](https://img.shields.io/badge/Telepesa-Banking%20Platform-blue?style=for-the-badge)

[![Build Status](https://img.shields.io/github/actions/workflow/status/malcolmmaima/Telepesa/ci.yml?branch=main&style=flat-square)](https://github.com/malcolmmaima/Telepesa/actions)
[![Coverage](https://img.shields.io/codecov/c/github/malcolmmaima/Telepesa?style=flat-square)](https://codecov.io/gh/malcolmmaima/Telepesa)
[![License](https://img.shields.io/badge/license-MIT-green?style=flat-square)](LICENSE)
[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=flat-square)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue?style=flat-square)](https://www.docker.com/)

**Enterprise-grade fintech platform empowering African cooperatives and microfinance institutions with comprehensive digital banking solutions.**

[📱 Features](#-features) • [🏗️ Architecture](#️-architecture) • [🚀 Quick Start](#-quick-start) • [📚 Documentation](#-documentation) • [🤝 Contributing](#-contributing)

</div>

## 🌟 Overview

Telepesa is a comprehensive digital banking platform designed specifically for African financial institutions. Built with modern microservices architecture, it provides secure, scalable, and compliant banking solutions with mobile money integration, advanced security features, and comprehensive audit trails.

### 🎯 Mission
To enhance financial inclusion across Africa by providing cooperatives and MFIs with enterprise-grade digital banking infrastructure that's accessible, secure, and culturally relevant.

## 📱 Features

### 🏦 Core Banking
- **Account Management**: Multiple account types (Savings, Checking, Business, Fixed Deposit)
- **Transaction Processing**: Real-time payments, transfers, and mobile money integration
- **Loan Management**: Complete loan lifecycle (origination, approval, disbursement, repayment, collateral management)
- **User Management**: Comprehensive user profiles, authentication, and authorization

### 🔒 Security & Compliance
- **Enterprise Security**: JWT-based authentication, rate limiting, device fingerprinting
- **Session Tokens (New)**: Short-lived access tokens + long-lived refresh tokens with rotation
- **Banking Compliance**: Comprehensive audit logging, transaction monitoring
- **Data Protection**: AES-256 encryption, secure password policies
- **Fraud Detection**: Real-time suspicious activity detection and alerting

### 📊 Analytics & Reporting
- **Real-time Dashboards**: Account balances, transaction history, performance metrics
- **Compliance Reports**: Regulatory reporting, audit trails, risk assessments
- **Business Intelligence**: Customer insights, transaction patterns, growth analytics

### 🌐 Multi-Platform Support
- **Android**: Native Kotlin app with Jetpack Compose
- **iOS**: Native Swift app with SwiftUI
- **Web Dashboard**: React-based admin and customer portals
- **API-First**: RESTful APIs with OpenAPI documentation

## 🏗️ Architecture

### Microservices Architecture
```
                    ┌─────────────────────────────────────────────────────┐
                    │                      Clients                        │
                    │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │
                    │  │   Android   │ │     iOS     │ │ Web Dashboard│   │
                    │  │    App      │ │    App      │ │   (React)    │   │
                    │  └─────────────┘ └─────────────┘ └─────────────┘   │
                    └─────────────────────────────────────────────────────┘
                                              │
                    ┌─────────────────────────▼─────────────────────────────┐
                    │                     API Gateway                        │
                    │                    (8080) ✅                           │
                    │   - Centralized routing (all external calls)          │
                    │   - JWT auth, refresh token endpoint                   │
                    │   - Rate limiting, security headers, docs proxy        │
                    └─────────────────────────┬─────────────────────────────┘
                                              │
                    ┌─────────────────────────▼─────────────────────────────┐
                    │             Service Discovery (Eureka)                 │
                    │                    (8761) ✅                           │
                    └─────┬─────────┬─────────┬──────────────┬──────────────┘
                          │         │         │              │
                     ┌────▼────┐┌───▼────┐┌───▼──────────┐┌───▼───────┐
                     │  User   ││Account ││  Transaction ││  Loan     │
                     │ Service ││Service ││   Service    ││  Service  │
                     │ (8081)  ││ (8082) ││    (8083)    ││  (8084)   │
                     └─────────┘└────────┘└──────────────┘└───────────┘
                          │                               │
                          │         ┌─────────────────────▼────────────┐
                          │         │         Notification Service      │
                          │         │               (8085)              │
                          │         └───────────────────────────────────┘
                          │
                     ┌────▼─────────▼─────────▼─────────▼─────────▼────┐
                     │                 Shared Libraries                  │
                     │     (Common Models, Exceptions, Security Utils)   │
                     └─────────────────────┬────────────────────────────┘
                                           │
                    ┌──────────────────────▼─────────────────────────────┐
                    │        Cross‑Cutting Infra (Observability/MQ)      │
                    │  ┌──────────────┐  ┌─────────┐   ┌──────────────┐ │
                    │  │  Zipkin      │  │  Redis  │   │   Kafka       │ │
                    │  │  Tracing     │  │  Cache  │   │  Messaging    │ │
                    │  │    ✅        │  │   ✅     │   │     ✅        │ │
                    │  └──────────────┘  └─────────┘   └──────────────┘ │
                    └──────────────────────┬─────────────────────────────┘
                                           │
                    ┌──────────────────────▼─────────────────────────────┐
                    │                     Data Layer                      │
                    │  ┌──────────────┐  ┌─────────┐   ┌──────────────┐ │
                    │  │ PostgreSQL   │  │  Redis  │   │   MongoDB     │ │
                    │  │  (Primary)   │  │ (Cache) │   │(Notifications)│ │
                    │  │     ✅       │  │   ✅     │   │      ✅       │ │
                    │  │      H2 (Tests)  ✅                         │ │
                    │  └──────────────┘  └─────────┘   └──────────────┘ │
                    └────────────────────────────────────────────────────┘

📊 Service Status Summary (Local Dev):
┌─────────────────────┬──────────┬─────────────┬──────────────────┐
│ Service             │ Status   │ Port        │ Test Coverage    │
├─────────────────────┼──────────┼─────────────┼──────────────────┤
│ Eureka Server       │ ✅ LIVE  │ 8761        │ Service Discovery│
│ API Gateway         │ ✅ LIVE  │ 8080        │ Route Proxying   │
│ User Service        │ ✅ LIVE  │ 8081        │ Auth + Refresh    │
│ Account Service     │ ✅ LIVE  │ 8082        │ Gateway proxied   │
│ Transaction Service │ ✅ LIVE  │ 8083        │ Gateway proxied   │
│ Loan Service        │ ✅ LIVE  │ 8084        │ Gateway proxied   │
│ Notification Service│ ✅ LIVE  │ 8085        │ Mongo + Gateway   │
└─────────────────────┴──────────┴─────────────┴──────────────────┘

🔗 Inter-Service Communication:
• API Gateway ←→ All Services: Centralized routing and security
• Eureka Server ←→ All Services: Service discovery and registration
• User ←→ Account: User account management and authentication
• Account ←→ Transaction: Account balance updates and validation  
• Transaction ←→ Notification: Real-time payment notifications
• Loan ←→ Account: Loan disbursement and repayment processing
• Loan ←→ Notification: Loan status updates and reminders
• All Services ←→ Shared Libraries: Common security, models, utilities
```

### 🛠️ Technology Stack

#### Backend
- **Framework**: Spring Boot 3.2.0 with Java 17
- **API Gateway**: Spring Cloud Gateway with WebClient (reactive)
- **Service Discovery**: Netflix Eureka Server
- **Security**: Spring Security 6.2.0 + JWT
- **Database**: PostgreSQL 15 (Production), H2 (Testing)
- **Cache**: Redis 7.0 (Loan service caching)
- **ORM**: Spring Data JPA with Hibernate
- **Testing**: JUnit 5, Mockito, TestContainers
- **Documentation**: OpenAPI 3.0 (Swagger)
- **Monitoring**: Spring Actuator, Micrometer

#### Frontend
- **Android**: Kotlin + Jetpack Compose + Android Architecture Components
- **iOS**: Swift + SwiftUI + Core Data
- **Web**: React + TypeScript + Material-UI
- **State Management**: Redux (Web), ViewModel (Mobile)

#### DevOps & Infrastructure
- **CI/CD**: GitHub Actions with automated testing and deployment
- **Containerization**: Docker with multi-stage builds
- **Security Scanning**: OWASP Dependency Check, Codecov
- **Code Quality**: JaCoCo (80% coverage requirement), SonarQube
- **Monitoring**: Actuator endpoints, health checks

## 🚀 Quick Start

### Prerequisites
- **Java 17+** (OpenJDK recommended)
- **Maven 3.8+**
- **PostgreSQL 13+** (or H2 for testing)
- **Docker** (optional, for containerized setup)
- **Node.js 18+** (for frontend development)

### 🎉 Recent Achievements
- **✅ Refresh Tokens**: Added secure refresh token issuance and rotation in user-service
- **✅ Gateway-Only API Testing**: Postman collection updated to route via API Gateway
- **✅ API Gateway**: Successfully implemented with WebClient, rate limiting, and documentation proxying
- **✅ Eureka Server**: Service discovery fully operational with all services registered
- **✅ CI/CD Fixes**: Resolved dependency management issues in notification-service
- **✅ PostgreSQL Integration**: All services successfully migrated from H2 to PostgreSQL
- **✅ Docker Infrastructure**: Complete Docker setup with PostgreSQL and Redis containers
- **✅ Comprehensive API Testing**: Complete user service testing with 100% success rate
- **✅ Security Validation**: All security controls tested and verified
- **✅ Audit Logging**: Complete audit trail implementation tested
- **✅ Postman Collection**: Comprehensive API test collection created
- **✅ Test Automation**: Automated test scripts for complete system flow
- **✅ Loan Service**: Complete implementation with 179 passing tests and comprehensive loan management features
- **✅ Collateral Management**: Full collateral lifecycle management with 15+ repository tests
- **✅ Test Infrastructure**: Robust test configuration with environment-independent testing
- **✅ API Documentation**: Complete OpenAPI documentation for all loan endpoints
- **✅ Test Coverage**: Project-wide achievement of 85% line coverage across all services
- **✅ Production-Ready**: All services running with PostgreSQL in production-like environment

### 🏃‍♂️ Running the Backend

1. **Clone the Repository**
   ```bash
   git clone https://github.com/malcolmmaima/Telepesa.git
   cd Telepesa
   ```

2. **Database Setup (Docker)**
   ```bash
   # Start PostgreSQL container
   docker run --name telepesa-postgres -e POSTGRES_PASSWORD=password \
     -e POSTGRES_USER=telepesa -e POSTGRES_DB=telepesa \
     -p 5432:5432 -d postgres:15
   
   # Start Redis container (for loan service caching)
   docker run --name telepesa-redis -p 6379:6379 -d redis:7-alpine
   
   # Create service-specific databases
   docker exec -it telepesa-postgres psql -U telepesa -c "CREATE DATABASE user_service;"
   docker exec -it telepesa-postgres psql -U telepesa -c "CREATE DATABASE account_service;"
   docker exec -it telepesa-postgres psql -U telepesa -c "CREATE DATABASE transaction_service;"
   docker exec -it telepesa-postgres psql -U telepesa -c "CREATE DATABASE loan_service;"
   docker exec -it telepesa-postgres psql -U telepesa -c "CREATE DATABASE notification_service;"
   ```

3. **Build Shared Libraries**
   ```bash
   cd Backend/shared-libraries
   mvn clean install
   ```

4. **Start All Services**
   ```bash
   # Start Eureka Server (Service Discovery)
   cd Backend/eureka-server
   mvn spring-boot:run -Dspring.profiles.active=dev
   
   # Start API Gateway
   cd Backend/api-gateway
   mvn spring-boot:run -Dspring.profiles.active=dev
   
   # Start Microservices (in separate terminals)
   cd Backend/user-service
   mvn spring-boot:run -Dspring.profiles.active=dev
   
   cd Backend/account-service
   mvn spring-boot:run -Dspring.profiles.active=dev
   
   cd Backend/transaction-service
   mvn spring-boot:run -Dspring.profiles.active=dev
   
   cd Backend/loan-service
   mvn spring-boot:run -Dspring.profiles.active=dev
   
   cd Backend/notification-service
   mvn spring-boot:run -Dspring.profiles.active=dev
   ```

5. **Verify Services**
   ```bash
   # Check Eureka Dashboard
   curl http://localhost:8761
   
   # Check API Gateway Health
   curl http://localhost:8080/actuator/health
   
   # Check Service Routes
   curl http://localhost:8080/actuator/gateway/routes

   # Example Auth via Gateway (recommended)
   # Login → returns accessToken and refreshToken
   curl -X POST \
     "http://localhost:8080/api/v1/users/login" \
     -H "Content-Type: application/json" \
     -d '{"usernameOrEmail":"<username>","password":"<password>"}'

   # Refresh → rotates refreshToken and returns new pair
   curl -X POST \
     "http://localhost:8080/api/v1/users/refresh" \
     -H "Content-Type: application/json" \
     -d '{"refreshToken":"<refreshToken>"}'
   ```

### 🧪 Testing

1. **Run All Tests**
   ```bash
   cd Backend
   mvn clean test
   ```

2. **API Testing with Postman**
   - Import `Backend/Telepesa_API_Collection_Complete.postman_collection.json`
   - Run the complete test suite

3. **Individual Service Testing**
   ```bash
   # Test User Service
   cd Backend/user-service
   mvn test
   
   # Test Loan Service
   cd Backend/loan-service
   mvn test
   ```

## 📚 Documentation

### 📁 Documentation Structure
```
Backend/
├── README.md                    # ✅ Main entry point (this file)
├── docs/                        # 📁 All detailed documentation
│   ├── API_TESTING_GUIDE.md     # Complete API testing guide
│   ├── END_TO_END_TEST_REPORT.md # Comprehensive test results
│   ├── SECURITY_IMPLEMENTATION.md # Security features documentation
│   ├── ARCHITECTURE.md          # Detailed architecture guide
│   ├── CI_CD_STATUS.md          # CI/CD pipeline status
│   └── *.md                     # Other documentation files
├── Telepesa_API_Collection_Complete.postman_collection.json
└── quick-api-test.sh
```

### 🔗 Quick Links
- **[API Testing Guide](Backend/docs/API_TESTING_GUIDE.md)** - Complete guide for testing all APIs
- **[End-to-End Test Report](Backend/docs/END_TO_END_TEST_REPORT.md)** - Comprehensive test results and coverage
- **[Security Implementation](Backend/docs/SECURITY_IMPLEMENTATION.md)** - Security features and compliance
- **[Architecture Guide](Backend/docs/ARCHITECTURE.md)** - Detailed system architecture
- **[CI/CD Status](Backend/docs/CI_CD_STATUS.md)** - Pipeline status and deployment info

### 🆕 API Gateway Features
- **Documentation Proxying**: Access all service Swagger UI through `/api/v1/docs/{service-name}/swagger-ui.html`
- **Rate Limiting**: Configurable rate limiting per IP and user
- **Security**: JWT authentication and authorization
- **Load Balancing**: Automatic load balancing across service instances
- **Health Checks**: Centralized health monitoring

### 🔍 Service Endpoints

#### API Gateway (Port 8080)
- **Health**: `GET /actuator/health`
- **Routes**: `GET /actuator/gateway/routes`
- **Documentation**: `GET /api/v1/docs/{service-name}/swagger-ui.html`

#### Eureka Server (Port 8761)
- **Dashboard**: `GET /` (Web UI)
- **Applications**: `GET /eureka/apps`

#### User Service (Port 8081)
- **Health**: `GET /actuator/health`
- **API Docs**: `GET /swagger-ui.html`
- **Users**: `GET /api/v1/users`

#### Account Service (Port 8082)
- **Health**: `GET /actuator/health`
- **API Docs**: `GET /swagger-ui.html`
- **Accounts**: `GET /api/v1/accounts`

#### Transaction Service (Port 8083)
- **Health**: `GET /actuator/health`
- **API Docs**: `GET /swagger-ui.html`
- **Transactions**: `GET /api/v1/transactions`

#### Loan Service (Port 8084)
- **Health**: `GET /actuator/health`
- **API Docs**: `GET /swagger-ui.html`
- **Loans**: `GET /api/v1/loans`

#### Notification Service (Port 8085)
- **Health**: `GET /actuator/health`
- **API Docs**: `GET /swagger-ui.html`
- **Notifications**: `GET /api/v1/notifications`

## 🤝 Contributing

### Development Guidelines
- **Testing**: 80% minimum code coverage required
- **Documentation**: All new features must be documented
- **Code Quality**: Follow Spring Boot best practices
- **Security**: All security features must be tested

### Testing Requirements
- **Unit Tests**: Required for all service methods
- **Integration Tests**: Required for all controllers
- **End-to-End Tests**: Required for critical user flows
- **Security Tests**: Required for all authentication/authorization

### Code Review Process
1. Create feature branch from `main`
2. Implement feature with tests
3. Ensure all tests pass
4. Update documentation
5. Submit pull request
6. Code review and approval
7. Merge to main

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: Check the [docs](Backend/docs/) folder
- **Issues**: Report bugs via GitHub Issues
- **Discussions**: Use GitHub Discussions for questions
- **Security**: Report security issues privately

---

<div align="center">

**Built with ❤️ for African Financial Inclusion**

[Back to Top](#telepesa---modern-banking-platform-for-africa)

</div>