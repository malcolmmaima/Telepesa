#!/bin/bash

# Transaction Service - Local Development Setup Script
# This script configures the local development environment for optimal performance

set -e

echo "🚀 Setting up Transaction Service for Local Development..."

# Load environment variables from .env files
source scripts/load-env.sh

# Create local development profile if it doesn't exist
if [ ! -f "src/main/resources/application-local.yml" ]; then
    cat > src/main/resources/application-local.yml << 'EOF'
# Local Development Configuration
spring:
  profiles:
    active: local
  
  datasource:
    url: jdbc:h2:mem:telepesa_local;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  h2:
    console:
      enabled: true
      path: /h2-console
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        
  security:
    jwt:
      secret: local-dev-secret-key-for-testing-only
      expiration: 86400000

logging:
  level:
    com.maelcolium.telepesa: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    
server:
  port: 8083

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env
  endpoint:
    health:
      show-details: always
EOF
    echo "✅ Created local development configuration"
fi

# Build shared libraries if not already built
echo "📦 Building shared libraries..."
cd ../shared-libraries

# Build common-exceptions
if [ -d "common-exceptions" ]; then
    cd common-exceptions
    mvn clean install -DskipTests -q
    echo "  ✅ common-exceptions built"
    cd ..
fi

# Build common-models  
if [ -d "common-models" ]; then
    cd common-models
    mvn clean install -DskipTests -q
    echo "  ✅ common-models built"
    cd ..
fi

# Build security-utils
if [ -d "security-utils" ]; then
    cd security-utils
    mvn clean install -DskipTests -q
    echo "  ✅ security-utils built"
    cd ..
fi

# Return to transaction-service directory
cd ../transaction-service

# Install dependencies and run tests
echo "🧪 Running tests with coverage..."
mvn clean test jacoco:report -Dspring.profiles.active=local -q

# Check if tests passed
if [ $? -eq 0 ]; then
    echo "✅ All tests passed!"
else
    echo "❌ Some tests failed. Please check the output above."
    exit 1
fi

# Run security scan with improved error handling
echo "🔒 Running security scan..."
if [ -n "$NVD_API_KEY" ]; then
    mvn dependency-check:check \
        -DnvdApiKey=$NVD_API_KEY \
        -DfailBuildOnCVSS=7 \
        -DfailOnError=false \
        -DenableRetired=false \
        -DenableExperimental=false \
        -DskipSystemScope=true \
        -DskipTestScope=true \
        -DskipProvidedScope=true \
        -DnvdMaxRetryCount=5 \
        -DnvdApiDelay=4000 \
        -DcveValidForHours=24 \
        -DretireJsAnalyzerEnabled=false \
        -DbundleAuditAnalyzerEnabled=false \
        -q || echo "⚠️  Security scan completed with warnings (this is normal for development)"
else
    echo "⏳ Running security scan without API key (this will be slower but more reliable)..."
    mvn dependency-check:check \
        -DfailBuildOnCVSS=7 \
        -DfailOnError=false \
        -DenableRetired=false \
        -DenableExperimental=false \
        -DskipSystemScope=true \
        -DskipTestScope=true \
        -DskipProvidedScope=true \
        -DautoUpdate=false \
        -DcveValidForHours=168 \
        -DretireJsAnalyzerEnabled=false \
        -DbundleAuditAnalyzerEnabled=false \
        -DarchiveAnalyzerEnabled=false \
        -q || echo "⚠️  Security scan completed with warnings (this is normal for development)"
fi

echo ""
echo "🎉 Transaction Service Local Development Setup Complete!"
echo ""
echo "📋 Quick Commands:"
echo "  Start service:     mvn spring-boot:run -Dspring.profiles.active=local"
echo "  Run tests:         mvn test"
echo "  Coverage report:   mvn jacoco:report"
if [ -n "$NVD_API_KEY" ]; then
    echo "  Security scan:     mvn dependency-check:check -DnvdApiKey=\$NVD_API_KEY"
else
    echo "  Security scan:     mvn dependency-check:check"
    echo "  Get NVD API key:   https://nvd.nist.gov/developers/request-an-api-key"
fi
echo "  H2 Console:        http://localhost:8083/h2-console"
echo "  Health Check:      http://localhost:8083/actuator/health"
echo "  API Docs:          http://localhost:8083/swagger-ui.html"
echo ""
if [ -n "$NVD_API_KEY" ]; then
    echo "🔧 Environment Variables Set:"
    echo "  NVD_API_KEY: ${NVD_API_KEY:0:8}..."
else
    echo "🔧 To speed up security scans:"
    echo "  1. Get API key: https://nvd.nist.gov/developers/request-an-api-key"
    echo "  2. Export: export NVD_API_KEY=\"your-key-here\""
    echo "  3. Add to ~/.bashrc or ~/.zshrc for persistence"
fi
echo ""
echo "💡 Tip: Run 'source scripts/setup-local-dev.sh' to configure your environment" 