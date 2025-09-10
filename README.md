# Telepesa - Modern Banking Platform for Africa

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![codecov](https://codecov.io/gh/malcolmmaima/Telepesa/branch/main/graph/badge.svg)](https://codecov.io/gh/malcolmmaima/Telepesa)
[![Java](https://img.shields.io/badge/Java-17+-blue.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-green.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18+-blue.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5+-blue.svg)](https://www.typescriptlang.org/)

Telepesa is a modular, microservices-based digital banking platform. It provides secure, scalable services for user management, accounts, transactions, loans, notifications, transfers, and bill payments, routed through a centralized API Gateway and discovered via Eureka.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────────────┐
│                                TELEPESA ARCHITECTURE                                   │
└─────────────────────────────────────────────────────────────────────────────────────┘

                              ┌─────────────────────┐
                              │   Web Frontend      │
                              │   (React/TypeScript)│
                              │   localhost:5174    │
                              └──────────┬──────────┘
                                         │ HTTPS/CORS
                                         │
                              ┌──────────▼──────────┐
                              │    API Gateway      │
                              │  (Spring Gateway)   │
                              │   localhost:8080    │
                              └──────────┬──────────┘
                                         │
                    ┌────────────────────┼────────────────────┐
                    │                    │                    │
         ┌──────────▼─────────┐ ┌────────▼────────┐ ┌────────▼────────┐
         │   Eureka Server    │ │   Infrastructure │ │   Monitoring    │
         │ (Service Discovery)│ │                  │ │                 │
         │  localhost:8761    │ │ PostgreSQL:5432  │ │  Zipkin:9411   │
         └────────────────────┘ │   Redis:6379     │ │  Kafka:9092    │
                                │  MongoDB:27017   │ └─────────────────┘
                                │ ZooKeeper:2181   │
                                └──────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────────┐
│                               MICROSERVICES LAYER                                      │
├─────────────────────────────────────────────────────────────────────────────────────┤
│                                                                                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │  User Service   │  │ Account Service │  │Transaction Svc  │  │  Loan Service   │ │
│  │   Port: 8081    │  │   Port: 8082    │  │   Port: 8083    │  │   Port: 8084    │ │
│  │                 │  │                 │  │                 │  │                 │ │
│  │ • Authentication│  │ • Account CRUD  │  │ • Txn Processing│  │ • Loan Apps     │ │
│  │ • JWT Tokens    │  │ • Balance Mgmt  │  │ • History       │  │ • Credit Checks │ │
│  │ • User Profiles │  │ • Account Types │  │ • Audit Logs    │  │ • Repayment     │ │
│  │ • Security      │  │ • Statements    │  │ • Reconciliation│  │ • Collateral    │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
│                                                                                     │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐                     │
│  │Notification Svc │  │ Transfer Service│  │Bill Payment Svc │                     │
│  │   Port: 8085    │  │   Port: 8086    │  │   Port: 8087    │                     │
│  │                 │  │                 │  │                 │                     │
│  │ • Email/SMS     │  │ • Fund Transfers│  │ • Utility Bills │                     │
│  │ • Push Notify   │  │ • Inter-bank    │  │ • Telecom       │                     │
│  │ • Templates     │  │ • Fee Calc      │  │ • Entertainment │                     │
│  │ • Audit Trail   │  │ • Limits        │  │ • Biller Mgmt   │                     │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘                     │
│                                                                                     │
└─────────────────────────────────────────────────────────────────────────────────────┘

                           ┌─────────────────────────────┐
                           │        DATA LAYER           │
                           ├─────────────────────────────┤
                           │                             │
                           │ • PostgreSQL (Primary)      │
                           │   - User data               │
                           │   - Account data            │
                           │   - Transaction records     │
                           │   - Loan information        │
                           │                             │
                           │ • Redis (Cache & Sessions) │
                           │   - JWT tokens              │
                           │   - Rate limiting           │
                           │   - Temporary data          │
                           │                             │
                           │ • MongoDB (Notifications)   │
                           │   - Email templates         │
                           │   - Notification history    │
                           │   - Message queues          │
                           │                             │
                           │ • Kafka (Event Streaming)   │
                           │   - Transaction events      │
                           │   - Audit trails            │
                           │   - Inter-service comms     │
                           │                             │
                           └─────────────────────────────┘
```

## Tech Stack

- **Language/Framework**: Java 17, Spring Boot 3.4
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Datastores**: PostgreSQL (primary), Redis (cache), MongoDB (notifications)
- **Messaging/Tracing**: Kafka, Zipkin
- **Auth**: Spring Security + JWT
- **Docs**: OpenAPI/Swagger
- **Containers**: Docker
- **Frontend**: React + TypeScript + Vite

## Services and Ports

- API Gateway: 8080
- Eureka Server: 8761
- User Service: 8081
- Account Service: 8082
- Transaction Service: 8083
- Loan Service: 8084
- Notification Service: 8085
- Transfer Service: 8086
- Bill Payment Service: 8087

## Quick Start (Docker Compose)

Prerequisites: Docker Desktop or Docker Engine.

1) Build shared libraries (first time only)
```bash
cd Backend/shared-libraries
mvn -q -DskipTests clean install
```

2) Start core infrastructure and services
```bash
cd Backend/docker-compose
# Starts infra + gateway + Eureka + transfer-service + bill-payment-service
docker compose -f docker-compose.yml up -d \
  zookeeper kafka postgres redis zipkin eureka-server api-gateway \
  transfer-service bill-payment-service
```

3) Verify health
```bash
# Eureka UI
open http://localhost:8761

# API Gateway health
curl -fsS http://localhost:8080/actuator/health | jq

# Transfer Service health (via service)
curl -fsS http://localhost:8086/actuator/health | jq

# Bill Payment Service health (via service)
curl -fsS http://localhost:8087/actuator/health | jq
```

Notes
- All services register with Eureka and are routed via the API Gateway under /api/v1/**.
- Per-service PostgreSQL databases are created by docker-compose init scripts.

## API Surface (Gateway paths)

- Users:            /api/v1/users/**
- Accounts:         /api/v1/accounts/**
- Transactions:     /api/v1/transactions/**
- Loans:            /api/v1/loans/**
- Notifications:    /api/v1/notifications/**
- Transfers:        /api/v1/transfers/**
- Bills:            /api/v1/bills/**

## Local Development (without Docker)

- Each service can be run with: `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
- Recommended to run Eureka and API Gateway first, then dependent services.

## Testing

```bash
cd Backend
mvn -q clean test

# Or run realistic E2E infra/gateway tests
bash Backend/scripts/realistic-e2e-test.sh
```

Postman collection: `Backend/Telepesa_API_Collection_Complete.postman_collection.json`

## CI/CD Pipeline Overview

### 🔄 Automated Pipelines

| Pipeline | Status | Coverage | Security | Docker |
|----------|---------|----------|----------|---------|
| **Backend Services** | [![Backend CI](https://github.com/malcolmmiyare/Telepesa/actions/workflows/ci.yml/badge.svg)](https://github.com/malcolmmiyare/Telepesa/actions/workflows/ci.yml) | JaCoCo Reports | Security Audit | ✅ Multi-service |
| **Web Frontend** | [![Frontend CI](https://github.com/malcolmmiyare/Telepesa/actions/workflows/web-frontend.yml/badge.svg)](https://github.com/malcolmmiyare/Telepesa/actions/workflows/web-frontend.yml) | Vitest Coverage | npm audit | ✅ Nginx + SPA |

### 📊 Pipeline Statistics

#### Backend CI (`ci.yml`)
- **Services Tested**: 7 microservices + API Gateway + Eureka + Transfer Service
- **Test Types**: Unit, Integration, E2E Infrastructure
- **Quality Gates**: Code coverage, security audit, dependency check
- **Deployment**: Docker multi-service build and push (includes transfer-service)
- **Triggers**: Push to `main`/`develop`, PRs

#### Frontend CI (`web-frontend.yml`)
- **Framework**: React 18 + TypeScript 5 + Vite
- **Test Suite**: ESLint, Prettier, Vitest unit tests
- **Coverage**: HTML/LCOV reports with Codecov integration
- **Bundle Analysis**: Size limits and optimization checks
- **Deployment**: Nginx-based Docker container
- **Triggers**: Changes to `Frontend/Web/**`


### 🚀 Container Registry Setup

**Docker Hub** (Recommended):
```bash
# Repository Secrets Required:
DOCKER_USERNAME=<your-dockerhub-username>
DOCKER_PASSWORD=<your-dockerhub-token>
```

**GitHub Container Registry** (Alternative):
```bash
# Repository Secret Required:
GHCR_PAT=<github-personal-access-token>
# Token needs: write:packages, read:packages
```

### 📊 Codecov Integration Setup

**Coverage Reports** (Optional but Recommended):
```bash
# Repository Secret Required:
CODECOV_TOKEN=<your-codecov-upload-token>
# Get token from: https://codecov.io/gh/YOUR_USERNAME/YOUR_REPO
```

**Setup Steps:**
1. Visit [codecov.io](https://codecov.io) and sign up with GitHub
2. Add your repository to Codecov
3. Copy the upload token from repository settings
4. Add `CODECOV_TOKEN` to your GitHub repository secrets
5. Coverage reports will be automatically uploaded on each CI run

### 📈 Quality Metrics

- **Code Coverage**: >80% target across all services
- **Security Scanning**: Automated vulnerability detection
- **Bundle Size**: Frontend <5MB JavaScript bundle limit
- **Build Time**: Average <10 minutes per pipeline
- **Deployment**: Zero-downtime with health checks

## End-to-End Testing Status

✅ **Infrastructure**: All core services healthy and accessible  
✅ **Service Discovery**: All services registered with Eureka  
✅ **Authentication**: User login/JWT working (tested with valid credentials)  
✅ **Account Management**: Account creation and retrieval working  
✅ **API Gateway**: CORS fixed, routing functional, security working  
✅ **Frontend**: Complete web UI with all features implemented  

## Documentation

- Backend/docs contains detailed guides for architecture, security, and testing.
- Each service exposes Swagger UI at `http://localhost:<port>/swagger-ui.html`.
- Frontend web app: http://localhost:5174/ (React + TypeScript)

## License

MIT License. See LICENSE for details.
