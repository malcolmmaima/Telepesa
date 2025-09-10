# Telepesa - Modern Banking Platform for Africa

Telepesa is a modular, microservices-based digital banking platform. It provides secure, scalable services for user management, accounts, transactions, loans, notifications, transfers, and bill payments, routed through a centralized API Gateway and discovered via Eureka.

## Overview

- Language/Framework: Java 17, Spring Boot 3.2
- Service Discovery: Netflix Eureka
- API Gateway: Spring Cloud Gateway
- Datastores: PostgreSQL (primary), Redis (cache), MongoDB (notifications)
- Messaging/Tracing: Kafka, Zipkin
- Auth: Spring Security + JWT
- Docs: OpenAPI/Swagger
- Containers: Docker

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
```

Postman collection: `Backend/Telepesa_API_Collection_Complete.postman_collection.json`

## Documentation

- Backend/docs contains detailed guides for architecture, security, and testing.
- Each service exposes Swagger UI at `http://localhost:<port>/swagger-ui.html`.

## License

MIT License. See LICENSE for details.
