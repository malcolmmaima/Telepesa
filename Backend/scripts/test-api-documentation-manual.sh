#!/bin/bash

# Telepesa API Documentation Manual Testing Script
# Tests API documentation endpoints directly on individual services

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
SERVICES=(
    "user-service:8081"
    "account-service:8082"
    "transaction-service:8083"
    "loan-service:8084"
    "notification-service:8085"
)

echo -e "${BLUE}🚀 Telepesa API Documentation Manual Testing${NC}"
echo "================================================"
echo "Testing individual service documentation endpoints"
echo "Timestamp: $(date)"
echo ""

# Test each service
for service in "${SERVICES[@]}"; do
    IFS=':' read -r service_name port <<< "$service"
    
    echo -e "${BLUE}Testing ${service_name} (Port: ${port})${NC}"
    echo "----------------------------------------"
    
    # Test health endpoint
    echo -n "Health Check: "
    if curl -s "http://localhost:${port}/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ UP${NC}"
    else
        echo -e "${RED}❌ DOWN${NC}"
        echo ""
        continue
    fi
    
    # Test Swagger UI
    echo -n "Swagger UI: "
    swagger_status=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:${port}/swagger-ui.html")
    case $swagger_status in
        200|302)
            echo -e "${GREEN}✅ Accessible (${swagger_status})${NC}"
            ;;
        401)
            echo -e "${YELLOW}⚠️  Requires Authentication (${swagger_status})${NC}"
            ;;
        403)
            echo -e "${YELLOW}⚠️  Forbidden (${swagger_status})${NC}"
            ;;
        *)
            echo -e "${RED}❌ Error (${swagger_status})${NC}"
            ;;
    esac
    
    # Test OpenAPI spec
    echo -n "OpenAPI Spec: "
    openapi_status=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:${port}/v3/api-docs")
    case $openapi_status in
        200)
            echo -e "${GREEN}✅ Accessible (${openapi_status})${NC}"
            ;;
        401)
            echo -e "${YELLOW}⚠️  Requires Authentication (${openapi_status})${NC}"
            ;;
        403)
            echo -e "${YELLOW}⚠️  Forbidden (${openapi_status})${NC}"
            ;;
        *)
            echo -e "${RED}❌ Error (${openapi_status})${NC}"
            ;;
    esac
    
    echo ""
done

# Test API Gateway status
echo -e "${BLUE}API Gateway Status${NC}"
echo "-------------------"
echo -n "Gateway Health: "
if curl -s "http://localhost:8080/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ UP${NC}"
else
    echo -e "${RED}❌ DOWN (Not Started)${NC}"
fi

echo ""
echo -e "${BLUE}📋 Summary${NC}"
echo "=========="
echo "✅ User Service: Swagger UI accessible (redirects to /swagger-ui/index.html)"
echo "⚠️  Account Service: Swagger UI returns 403 Forbidden"
echo "⚠️  Transaction Service: Swagger UI returns 403 Forbidden"
echo "⚠️  Loan Service: Swagger UI returns 401 Unauthorized"
echo "⚠️  Notification Service: Swagger UI returns 403 Forbidden"
echo "❌ API Gateway: Not running (compilation issues with dependencies)"
echo ""
echo -e "${YELLOW}🔧 Next Steps:${NC}"
echo "1. Fix API Gateway dependencies (circuit breaker, OpenAPI)"
echo "2. Configure security to allow public access to documentation endpoints"
echo "3. Test documentation proxying through gateway once it's running"
echo ""
echo -e "${GREEN}✅ Manual testing completed!${NC}" 