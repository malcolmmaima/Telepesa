# Telepesa Redis Caching Implementation

## Overview
This document outlines the comprehensive Redis caching implementation across all Telepesa microservices to improve performance and reduce database load.

## Architecture

### Redis Infrastructure
- **Redis Container**: `telepesa-redis` running on port 6379
- **Database Separation**: Each service uses a different Redis database
  - User Service: Database 0
  - Transaction Service: Database 1  
  - Loan Service: Database 2

### Cache Configuration
- **Shared Configuration**: `Backend/shared-libraries/common-models/src/main/java/com/maelcolium/telepesa/models/config/RedisCacheConfig.java`
- **Service-Specific Configs**: Each service has its own cache configuration
- **TTL Strategy**: Different cache names have different expiration times based on data volatility

## Service-by-Service Implementation

### 1. User Service (Database 0)

#### Dependencies ✅
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

#### Configuration ✅
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
  cache:
    type: redis
    redis:
      time-to-live: 1800000 # 30 minutes default
      cache-null-values: false
```

#### Cache Annotations ✅
```java
// User lookup caching
@Cacheable(value = "users", key = "#id")
public UserDto getUser(Long id)

@Cacheable(value = "users", key = "'username:' + #username")
public UserDto getUserByUsername(String username)

@Cacheable(value = "users", key = "'email:' + #email")
public UserDto getUserByEmail(String email)

// Cache eviction on updates
@CacheEvict(value = "users", allEntries = true)
public UserDto updateUser(Long id, CreateUserRequest request)

@CacheEvict(value = "users", allEntries = true)
public void deleteUser(Long id)

@CacheEvict(value = "users", allEntries = true)
public void verifyEmail(String token)

@CacheEvict(value = "users", allEntries = true)
public void resetPassword(String token, String newPassword)

@CacheEvict(value = "users", allEntries = true)
public void lockUserAccount(Long id)

@CacheEvict(value = "users", allEntries = true)
public void unlockUserAccount(Long id)
```

#### Cache Names and TTL
- `users`: 15 minutes (frequently accessed user data)
- `user-profiles`: 30 minutes (detailed user profiles)
- `user-sessions`: 2 hours (session data)
- `user-authentication`: 10 minutes (auth tokens)

### 2. Transaction Service (Database 1)

#### Dependencies ✅
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

#### Configuration ✅
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 1
  cache:
    type: redis
    redis:
      time-to-live: 600000 # 10 minutes default
      cache-null-values: false
```

#### Cache Configuration ✅
- **Location**: `Backend/transaction-service/src/main/java/com/maelcolium/telepesa/transaction/config/CacheConfig.java`
- **Features**: Service-specific TTL configuration

#### Cache Annotations ⚠️ (Partially Implemented)
```java
// TODO: Add these annotations to TransactionServiceImpl

// Transaction lookup caching
@Cacheable(value = "transactions", key = "#id")
public TransactionDto getTransaction(Long id)

@Cacheable(value = "transactions", key = "'transactionId:' + #transactionId")
public TransactionDto getTransactionByTransactionId(String transactionId)

// Transaction history caching
@Cacheable(value = "transaction-history", key = "'user:' + #userId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
public Page<TransactionDto> getTransactionsByUserId(Long userId, Pageable pageable)

@Cacheable(value = "transaction-history", key = "'account:' + #accountId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
public Page<TransactionDto> getTransactionsByAccountId(Long accountId, Pageable pageable)

// Account balance caching
@Cacheable(value = "account-balances", key = "#accountId")
public BigDecimal getAccountBalance(Long accountId)

// Cache eviction on transaction creation/updates
@CacheEvict(value = {"transactions", "transaction-history", "account-balances"}, allEntries = true)
public TransactionDto createTransaction(CreateTransactionRequest request)

@CacheEvict(value = {"transactions", "transaction-history", "account-balances"}, allEntries = true)
public TransactionDto updateTransactionStatus(Long id, TransactionStatus status)
```

#### Cache Names and TTL
- `transactions`: 10 minutes (individual transaction data)
- `transaction-history`: 5 minutes (transaction lists and history)
- `account-balances`: 2 minutes (account balance calculations)
- `transaction-limits`: 1 hour (transaction limits and totals)

### 3. Loan Service (Database 2)

#### Dependencies ✅
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

#### Configuration ✅
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 2
  cache:
    type: redis
```

#### Cache Annotations ⚠️ (Partially Implemented)
```java
// TODO: Add these annotations to LoanServiceImpl

// Loan lookup caching
@Cacheable(value = "loans", key = "#loanId")
public LoanDto getLoan(Long loanId)

@Cacheable(value = "loans", key = "'loanNumber:' + #loanNumber")
public LoanDto getLoanByNumber(String loanNumber)

// Loan lists caching
@Cacheable(value = "loan-applications", key = "'user:' + #userId + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
public Page<LoanDto> getLoansByUserId(Long userId, Pageable pageable)

@Cacheable(value = "loan-applications", key = "'status:' + #status + ':page:' + #pageable.pageNumber + ':size:' + #pageable.pageSize")
public Page<LoanDto> getLoansByStatus(LoanStatus status, Pageable pageable)

// Credit score caching
@Cacheable(value = "credit-scores", key = "#userId")
public Integer getCreditScore(Long userId)

// Loan calculations caching
@Cacheable(value = "loan-calculations", key = "'monthlyPayment:' + #principal + ':' + #interestRate + ':' + #termMonths")
public BigDecimal calculateMonthlyPayment(BigDecimal principal, BigDecimal interestRate, Integer termMonths)

// Cache eviction on loan updates
@CacheEvict(value = {"loans", "loan-applications", "credit-scores"}, allEntries = true)
public LoanDto approveLoan(Long loanId, Long approvedBy)

@CacheEvict(value = {"loans", "loan-applications", "credit-scores"}, allEntries = true)
public LoanDto updateLoanStatus(Long loanId, LoanStatus status)
```

#### Cache Names and TTL
- `loans`: 20 minutes (individual loan data)
- `loan-applications`: 15 minutes (loan lists and applications)
- `loan-calculations`: 1 hour (payment calculations)
- `credit-scores`: 6 hours (credit score data)
- `collaterals`: 30 minutes (collateral information)

## Performance Benefits

### Expected Improvements
1. **User Service**: 70-80% reduction in database queries for user lookups
2. **Transaction Service**: 60-70% reduction in balance calculation queries
3. **Loan Service**: 50-60% reduction in loan data retrieval queries

### Cache Hit Ratios
- **User Authentication**: Expected 85-90% cache hit ratio
- **Account Balances**: Expected 75-80% cache hit ratio
- **Transaction History**: Expected 60-70% cache hit ratio
- **Loan Data**: Expected 70-75% cache hit ratio

## Implementation Status

### ✅ Completed
1. **Infrastructure**: Redis container setup and configuration
2. **Dependencies**: All services have Redis and cache dependencies
3. **Configuration**: Redis and cache configuration in all services
4. **User Service**: Complete cache annotation implementation
5. **Shared Library**: Redis cache configuration in common-models

### ⚠️ Partially Implemented
1. **Transaction Service**: Cache configuration exists, annotations need to be added
2. **Loan Service**: Cache configuration exists, annotations need to be added

### 🔄 Next Steps
1. Add cache annotations to TransactionServiceImpl
2. Add cache annotations to LoanServiceImpl
3. Implement cache monitoring and metrics
4. Add cache warming strategies
5. Create cache performance tests

## Monitoring and Health Checks

### Health Endpoints
- All services expose Redis health checks via Spring Boot Actuator
- Endpoint: `/actuator/health`
- Includes Redis connection status

### Cache Statistics
- Cache hit/miss ratios available via Actuator metrics
- Endpoint: `/actuator/metrics/cache.gets`
- Endpoint: `/actuator/metrics/cache.miss`

## Best Practices Implemented

### Cache Key Strategy
- **Consistent Naming**: All cache keys follow consistent patterns
- **Namespace Separation**: Different cache names for different data types
- **Key Uniqueness**: Keys include relevant parameters to ensure uniqueness

### TTL Strategy
- **Short TTL**: Frequently changing data (account balances: 2 minutes)
- **Medium TTL**: Moderately stable data (transactions: 10 minutes)
- **Long TTL**: Stable data (credit scores: 6 hours)

### Cache Eviction Strategy
- **Time-based**: Automatic expiration based on TTL
- **Event-based**: Manual eviction on data updates
- **All-entries**: Clear entire cache when data changes significantly

## Testing

### Manual Testing
```bash
# Test Redis connectivity
docker exec -it telepesa-redis redis-cli ping

# Test cache functionality
curl -X GET "http://localhost:8081/api/v1/users/1"  # Should cache user data
curl -X GET "http://localhost:8081/api/v1/users/1"  # Should return cached data

# Check cache statistics
curl -X GET "http://localhost:8081/actuator/metrics/cache.gets"
```

### Automated Testing
- Cache configuration tests in each service
- Cache annotation tests for service methods
- Redis connectivity tests in integration tests

## Troubleshooting

### Common Issues
1. **Redis Connection Failed**: Check if Redis container is running
2. **Cache Not Working**: Verify cache annotations are properly applied
3. **Memory Issues**: Monitor Redis memory usage and adjust TTL settings

### Debug Commands
```bash
# Check Redis container status
docker ps | grep redis

# Check Redis logs
docker logs telepesa-redis

# Check cache statistics
docker exec -it telepesa-redis redis-cli info memory
```

## Future Enhancements

### Planned Improvements
1. **Cache Clustering**: Redis cluster for high availability
2. **Cache Warming**: Pre-populate frequently accessed data
3. **Cache Analytics**: Detailed cache performance dashboard
4. **Smart Eviction**: LRU-based eviction for better memory management

### Advanced Features
1. **Cache Invalidation**: Event-driven cache invalidation
2. **Cache Compression**: Compress cached data to save memory
3. **Cache Persistence**: Persist cache data across restarts
4. **Cache Replication**: Master-slave replication for read scaling

---

**Last Updated**: $(date)
**Version**: 1.0.0
**Status**: Partially Implemented (User Service Complete, Others In Progress) 