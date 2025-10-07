# Transfer Service

The Transfer Service is responsible for handling money transfers between accounts in the Telepesa banking system.

## Features

- **Multiple Transfer Types**: Internal, Mobile Money, Bank Transfer, Peer-to-Peer
- **Fee Calculation**: Automatic fee calculation based on transfer type
- **Transaction Processing**: Real-time processing with account service integration
- **Transfer Statistics**: Detailed analytics for account transfer activity
- **Caching**: Redis-based caching for improved performance
- **Monitoring**: Health checks and metrics via Actuator

## API Endpoints

### Transfer Management
- `POST /api/transfers` - Create a new transfer
- `GET /api/transfers/{transferId}` - Get transfer by ID
- `GET /api/transfers/reference/{reference}` - Get transfer by reference
- `POST /api/transfers/{transferId}/process` - Process pending transfer
- `POST /api/transfers/{transferId}/cancel` - Cancel transfer
- `POST /api/transfers/{transferId}/retry` - Retry failed transfer

### Transfer History
- `GET /api/transfers/account/{accountId}` - Get all transfers for account
- `GET /api/transfers/sent/{accountId}` - Get sent transfers
- `GET /api/transfers/received/{accountId}` - Get received transfers

### Analytics
- `GET /api/transfers/stats/{accountId}` - Get transfer statistics
- `GET /api/transfers/status/{status}` - Get transfers by status

### Utilities
- `GET /api/transfers/fee/calculate` - Calculate transfer fee
- `GET /api/transfers/health` - Health check

## Transfer Types and Fees

| Transfer Type | Fee | Min Fee | Max Fee |
|---------------|-----|---------|---------|
| INTERNAL | 0% | KES 0 | KES 0 |
| MOBILE_MONEY | 1% | KES 10 | KES 200 |
| BANK_TRANSFER | 0.5% | KES 20 | KES 500 |
| PEER_TO_PEER | 0.2% | KES 5 | KES 100 |

## Environment Variables

### Database
- `SPRING_DATASOURCE_URL` - PostgreSQL connection URL (default: jdbc:postgresql://localhost:5432/transfer_db)
- `SPRING_DATASOURCE_USERNAME` - Database username (default: telepesa_user)
- `SPRING_DATASOURCE_PASSWORD` - Database password (default: telepesa_password)

### Redis
- `SPRING_DATA_REDIS_HOST` - Redis host (default: localhost)
- `SPRING_DATA_REDIS_PORT` - Redis port (default: 6379)
- `SPRING_DATA_REDIS_PASSWORD` - Redis password

### Service Discovery
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` - Eureka server URL (default: http://localhost:8761/eureka/)

### Tracing
- `ZIPKIN_BASE_URL` - Zipkin server URL for distributed tracing

### Messaging
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka servers for event publishing

## Usage Examples

### Create Transfer
```bash
curl -X POST http://localhost:8086/api/transfers \
  -H "Content-Type: application/json" \
  -H "X-Account-Id: sender-account-123" \
  -d '{
    "recipientAccountId": "recipient-account-456",
    "amount": 1000.00,
    "transferType": "INTERNAL",
    "description": "Payment for services",
    "reference": "INV-2024-001"
  }'
```

### Calculate Fee
```bash
curl "http://localhost:8086/api/transfers/fee/calculate?amount=1000&transferType=MOBILE_MONEY"
```

### Get Transfer History
```bash
curl "http://localhost:8086/api/transfers/account/account-123?page=0&size=20"
```

## Docker Deployment

The service is configured for Docker deployment:

```bash
# Build image
docker build -t telepesa-transfer-service .

# Run container
docker run -p 8086:8086 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/transfer_db \
  telepesa-transfer-service
```

## Monitoring

### Health Check
```bash
curl http://localhost:8086/actuator/health
```

### Metrics
```bash
curl http://localhost:8086/actuator/metrics
```

### API Documentation
Visit `http://localhost:8086/swagger-ui.html` for interactive API documentation.

## Architecture Integration

- **Account Service**: For balance validation and account operations
- **User Service**: For user information and authentication
- **Transaction Service**: For transaction logging
- **Notification Service**: For transfer notifications
- **API Gateway**: Routes `/api/v1/transfers/**` to this service

## Development

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 13+
- Redis 6+

### Local Development
```bash
# Run locally with dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
./mvnw test

# Package
./mvnw clean package
```

## Database Schema

The service creates the following tables:
- `transfers` - Main transfer records with status tracking
- Indexed on: sender_account_id, recipient_account_id, transfer_reference, status, created_at

## Security

- JWT-based authentication via API Gateway
- Account ID validation through headers
- Circuit breaker patterns for external service calls
- Input validation and sanitization
