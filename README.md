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

## Web Application Screenshots

The Telepesa web application provides a modern, responsive interface for all banking operations. Here are screenshots showcasing the key features:

<div align="center">

### Authentication & Dashboard
<table>
  <tr>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.32.45.png" width="400" alt="Login Page"/>
      <br/><b>Login Page</b>
    </td>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.32.58.png" width="400" alt="Dashboard"/>
      <br/><b>Register Account</b>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.33.44.png" width="400" alt="Account Overview"/>
      <br/><b>Dashboard Home</b>
    </td>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.34.05.png" width="400" alt="Transfer Page"/>
      <br/><b>Accounts Interface</b>
    </td>
  </tr>
</table>

### Transfer & Payment Features
<table>
  <tr>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.34.24.png" width="400" alt="Transfer Form"/>
      <br/><b>Transfer Form</b>
    </td>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.34.38.png" width="400" alt="Payment Options"/>
      <br/><b>Bill Payments</b>
    </td>
  </tr>
</table>

### Loan Management
<table>
  <tr>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.34.45.png" width="400" alt="Transaction History"/>
      <br/><b>Bill Payments</b>
    </td>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.34.54.png" width="400" alt="Transaction Details"/>
      <br/><b>Loan Details</b>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.35.02.png" width="400" alt="Transaction Filters"/>
      <br/><b>Loan Details</b>
    </td>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.35.09.png" width="400" alt="Account Settings"/>
      <br/><b>My Loan Status</b>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.35.22.png" width="400" alt="Profile Management"/>
      <br/><b>Loan Calculator</b>
    </td>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.35.34.png" width="400" alt="Security Settings"/>
      <br/><b>Profile Settings</b>
    </td>
  </tr>
</table>

### Settings & Support
<table>
  <tr>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.35.39.png" width="400" alt="Notifications"/>
      <br/><b>Profile Settings</b>
    </td>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.35.46.png" width="400" alt="Bill Payments"/>
      <br/><b>Security Settings</b>
    </td>
  </tr>
  <tr>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.35.55.png" width="400" alt="Reports"/>
      <br/><b>Security Settings</b>
    </td>
    <td align="center">
      <img src="Screenshots/Web/Screenshot 2025-09-16 at 16.36.07.png" width="400" alt="Mobile Responsive"/>
      <br/><b>Help & Support</b>
    </td>
  </tr>
</table>

</div>

### Key Features Demonstrated

- **🔐 Secure Authentication**: JWT-based login with session management
- **📊 Real-time Dashboard**: Account balances, recent transactions, and quick actions
- **💸 Money Transfers**: Internal and external transfers with real-time processing
- **💳 Bill Payments**: Utility bills, telecom, and entertainment payments
- **📱 Responsive Design**: Optimized for desktop, tablet, and mobile devices
- **🔍 Transaction Search**: Advanced filtering and search capabilities
- **🔔 Notifications**: Real-time alerts and notification management
- **⚙️ Account Management**: Profile settings, security preferences, and preferences

## Documentation

- Backend/docs contains detailed guides for architecture, security, and testing.
- Each service exposes Swagger UI at `http://localhost:<port>/swagger-ui.html`.
- Frontend web app: http://localhost:5174/ (React + TypeScript)

## License

MIT License. See LICENSE for details.
