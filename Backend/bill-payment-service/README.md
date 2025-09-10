# Bill Payment Service

The Bill Payment Service handles utility bill payments for various service providers in Kenya through the Telepesa banking system.

## Features

- 💡 **Multiple Bill Types**: Electricity, Water, Internet, TV, Mobile, Insurance, School Fees, Government services
- ⚡ **Provider Integration**: Extensible provider pattern for different utility companies
- 💰 **Smart Fee Calculation**: Provider-specific fee structures
- 🔄 **Real-time Processing**: Immediate bill validation and payment processing
- 📊 **Payment History**: Comprehensive payment tracking and analytics
- 🏢 **KPLC Integration**: Kenya Power electricity bill payments (with mock API)
- ⚡ **Caching**: Redis-based caching for improved performance

## Supported Bill Types

| Bill Type | Example Providers | Service Fee |
|-----------|------------------|-------------|
| ELECTRICITY | KPLC, Kenya Power | KES 20-30 |
| WATER | Nairobi Water, Regional Water Companies | KES 20 |
| INTERNET | Safaricom Home, JTL, Zuku | KES 15 |
| TV_SUBSCRIPTION | DSTV, Gotv, Startimes | KES 10 |
| MOBILE_POSTPAID | Safaricom Postpaid, Airtel | KES 25 |
| INSURANCE | NHIF, Private Insurance | KES 30 |
| SCHOOL_FEES | Schools, Universities | KES 30 |
| GOVERNMENT | KRA, Government Services | KES 30 |

## API Endpoints

### Bill Payment Management
- `POST /api/bills` - Create a new bill payment
- `GET /api/bills/{paymentId}` - Get payment by ID
- `GET /api/bills/reference/{reference}` - Get payment by reference
- `POST /api/bills/{paymentId}/cancel` - Cancel payment
- `POST /api/bills/{paymentId}/retry` - Retry failed payment

### Payment History
- `GET /api/bills/account/{accountId}` - Get payments for account
- `GET /api/bills/type/{billType}` - Get payments by bill type

### Utilities
- `GET /api/bills/fee/calculate` - Calculate service fee
- `GET /api/bills/health` - Health check

## Payment Provider Pattern

The service uses a flexible provider pattern to support multiple utility companies:

```java
@Service
public class KPLCPaymentProvider implements PaymentProvider {
    
    public boolean supports(BillType billType, String serviceProvider) {
        return billType == ELECTRICITY && "KPLC".equalsIgnoreCase(serviceProvider);
    }
    
    public PaymentResult processPayment(PaymentRequest request) {
        // Integration with KPLC payment API
    }
}
```

## Environment Variables

### Database
- `SPRING_DATASOURCE_URL` - PostgreSQL connection URL (default: jdbc:postgresql://localhost:5432/bill_payment_db)
- `SPRING_DATASOURCE_USERNAME` - Database username (default: telepesa_user)
- `SPRING_DATASOURCE_PASSWORD` - Database password (default: telepesa_password)

### Redis
- `SPRING_DATA_REDIS_HOST` - Redis host (default: localhost)
- `SPRING_DATA_REDIS_PORT` - Redis port (default: 6379)
- `SPRING_DATA_REDIS_PASSWORD` - Redis password

### Service Discovery
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE` - Eureka server URL (default: http://localhost:8761/eureka/)

### Tracing & Messaging
- `ZIPKIN_BASE_URL` - Zipkin server URL for distributed tracing
- `KAFKA_BOOTSTRAP_SERVERS` - Kafka servers for event publishing

## Usage Examples

### Create Bill Payment
```bash
curl -X POST http://localhost:8087/api/bills \
  -H "Content-Type: application/json" \
  -H "X-Account-Id: account-123" \
  -d '{
    "billNumber": "12345678901",
    "customerName": "John Doe",
    "billType": "ELECTRICITY",
    "serviceProvider": "KPLC",
    "amount": 2500.00,
    "meterNumber": "87654321",
    "description": "Monthly electricity bill"
  }'
```

### Calculate Service Fee
```bash
curl "http://localhost:8087/api/bills/fee/calculate?amount=2500&billType=ELECTRICITY&provider=KPLC"
```

### Get Payment History
```bash
curl "http://localhost:8087/api/bills/account/account-123?page=0&size=20"
```

## Docker Deployment

The service is configured for Docker deployment on port 8087:

```bash
# Build image
docker build -t telepesa-bill-payment-service .

# Run container
docker run -p 8087:8087 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/bill_payment_db \
  telepesa-bill-payment-service
```

## Provider Integration

### Adding New Providers

1. Implement the `PaymentProvider` interface
2. Add provider-specific logic for bill validation and payment processing
3. Register as a Spring `@Service` component
4. The service will automatically discover and use the provider

Example:
```java
@Service
public class NairobiWaterPaymentProvider implements PaymentProvider {
    
    @Override
    public boolean supports(BillType billType, String serviceProvider) {
        return billType == BillType.WATER && 
               "Nairobi Water".equalsIgnoreCase(serviceProvider);
    }
    
    // Implement other methods...
}
```

## Architecture Integration

- **Account Service**: For balance validation and account debits (future integration)
- **Transaction Service**: For payment transaction logging
- **Notification Service**: For payment confirmations and receipts
- **API Gateway**: Routes `/api/v1/bills/**` to this service

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
- `bill_payments` - Main bill payment records with status tracking
- Indexed on: accountId, billNumber, paymentReference, status, billType, createdAt

## Security

- JWT-based authentication via API Gateway
- Account ID validation through headers
- Input validation and sanitization
- Provider-specific security measures

## Monitoring

### Health Check
```bash
curl http://localhost:8087/actuator/health
```

### Metrics
```bash
curl http://localhost:8087/actuator/metrics
```

### API Documentation
Visit `http://localhost:8087/swagger-ui.html` for interactive API documentation.
