#!/bin/bash

# Telepesa Docker Startup Script
# Comprehensive startup with monitoring and health checks

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ASCII Banner
echo -e "${CYAN}"
cat << "EOF"
╔══════════════════════════════════════════════════════════════════════════════╗
║                                                                              ║
║  ████████╗███████╗██╗     ███████╗██████╗ ███████╗███████╗ █████╗             ║
║  ╚══██╔══╝██╔════╝██║     ██╔════╝██╔══██╗██╔════╝██╔════╝██╔══██╗            ║
║     ██║   █████╗  ██║     █████╗  ██████╔╝█████╗  ███████╗███████║            ║
║     ██║   ██╔══╝  ██║     ██╔══╝  ██╔═══╝ ██╔══╝  ╚════██║██╔══██║            ║
║     ██║   ███████╗███████╗███████╗██║     ███████╗███████║██║  ██║            ║
║     ╚═╝   ╚══════╝╚══════╝╚══════╝╚═╝     ╚══════╝╚══════╝╚═╝  ╚═╝            ║
║                                                                              ║
║                   🏦 Enterprise Banking Platform 🏦                          ║
║                      🐳 Docker Deployment System 🐳                         ║
║                                                                              ║
╚══════════════════════════════════════════════════════════════════════════════╝
EOF
echo -e "${NC}"

# Function to print status messages
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_step() {
    echo -e "${PURPLE}[STEP]${NC} $1"
}

# Function to check prerequisites
check_prerequisites() {
    print_step "Checking prerequisites..."
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed or not in PATH"
        exit 1
    fi
    
    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed or not in PATH"
        exit 1
    fi
    
    # Check if Docker daemon is running
    if ! docker info &> /dev/null; then
        print_error "Docker daemon is not running"
        exit 1
    fi
    
    print_success "All prerequisites met"
}

# Function to build shared libraries
build_shared_libraries() {
    print_step "Building shared libraries..."
    
    cd shared-libraries
    
    if ./build-shared-libs.sh; then
        print_success "Shared libraries built successfully"
    else
        print_error "Failed to build shared libraries"
        exit 1
    fi
    
    cd ..
}

# Function to start infrastructure services
start_infrastructure() {
    print_step "Starting infrastructure services..."
    
    cd docker-compose
    
    # Start infrastructure services first
    docker-compose up -d postgres mongodb redis zookeeper kafka zipkin
    
    print_status "Waiting for infrastructure services to be healthy..."
    
    # Wait for each service to be healthy
    local services=("postgres" "mongodb" "redis" "zookeeper" "kafka" "zipkin")
    
    for service in "${services[@]}"; do
        print_status "Waiting for $service to be healthy..."
        local timeout=180
        local count=0
        
        while [ $count -lt $timeout ]; do
            if docker-compose ps $service | grep -q "healthy"; then
                print_success "$service is healthy"
                break
            fi
            
            if [ $count -eq $timeout ]; then
                print_error "$service failed to become healthy within ${timeout}s"
                docker-compose logs $service | tail -20
                exit 1
            fi
            
            sleep 2
            count=$((count + 2))
        done
    done
    
    cd ..
}

# Function to start core services
start_core_services() {
    print_step "Starting core services (Eureka & API Gateway)..."
    
    cd docker-compose
    
    # Start Eureka Server
    docker-compose up -d eureka-server
    
    print_status "Waiting for Eureka Server to be healthy..."
    local timeout=120
    local count=0
    
    while [ $count -lt $timeout ]; do
        if docker-compose ps eureka-server | grep -q "healthy"; then
            print_success "Eureka Server is healthy"
            break
        fi
        
        if [ $count -eq $timeout ]; then
            print_error "Eureka Server failed to become healthy within ${timeout}s"
            docker-compose logs eureka-server | tail -20
            exit 1
        fi
        
        sleep 3
        count=$((count + 3))
    done
    
    # Start API Gateway
    docker-compose up -d api-gateway
    
    print_status "Waiting for API Gateway to be healthy..."
    count=0
    
    while [ $count -lt $timeout ]; do
        if docker-compose ps api-gateway | grep -q "healthy"; then
            print_success "API Gateway is healthy"
            break
        fi
        
        if [ $count -eq $timeout ]; then
            print_error "API Gateway failed to become healthy within ${timeout}s"
            docker-compose logs api-gateway | tail -20
            exit 1
        fi
        
        sleep 3
        count=$((count + 3))
    done
    
    cd ..
}

# Function to start microservices
start_microservices() {
    print_step "Starting microservices..."
    
    cd docker-compose
    
    # Start all microservices
    docker-compose up -d user-service account-service transaction-service loan-service notification-service
    
    print_status "Waiting for microservices to be healthy..."
    
    local services=("user-service" "account-service" "transaction-service" "loan-service" "notification-service")
    local timeout=120
    
    for service in "${services[@]}"; do
        print_status "Waiting for $service to be healthy..."
        local count=0
        
        while [ $count -lt $timeout ]; do
            if docker-compose ps $service | grep -q "healthy"; then
                print_success "$service is healthy"
                break
            fi
            
            if [ $count -eq $timeout ]; then
                print_error "$service failed to become healthy within ${timeout}s"
                docker-compose logs $service | tail -20
                exit 1
            fi
            
            sleep 3
            count=$((count + 3))
        done
    done
    
    cd ..
}

# Function to verify services
verify_services() {
    print_step "Verifying all services..."
    
    # Service endpoints
    local endpoints=(
        "http://localhost:8761/actuator/health:Eureka Server"
        "http://localhost:8080/actuator/health:API Gateway"
        "http://localhost:8081/actuator/health:User Service"
        "http://localhost:8082/actuator/health:Account Service"
        "http://localhost:8083/actuator/health:Transaction Service"
        "http://localhost:8084/actuator/health:Loan Service"
        "http://localhost:8085/actuator/health:Notification Service"
    )
    
    for endpoint_info in "${endpoints[@]}"; do
        IFS=':' read -r endpoint name <<< "$endpoint_info"
        
        if curl -s -f "$endpoint" > /dev/null; then
            print_success "$name is responding"
        else
            print_warning "$name is not responding properly"
        fi
    done
}

# Function to display service status
display_status() {
    print_step "Service Status Dashboard"
    
    echo -e "${CYAN}"
    cat << "EOF"
╔══════════════════════════════════════════════════════════════════════════════╗
║                            SERVICE STATUS DASHBOARD                         ║
╚══════════════════════════════════════════════════════════════════════════════╝
EOF
    echo -e "${NC}"
    
    echo -e "${GREEN}🏗️  Infrastructure Services:${NC}"
    echo "   • PostgreSQL:     http://localhost:5432  (Multi-database)"
    echo "   • MongoDB:        http://localhost:27017 (Document storage)"
    echo "   • Redis:          http://localhost:6379  (Cache & sessions)"
    echo "   • Kafka:          http://localhost:29092 (Message streaming)"
    echo "   • Zipkin:         http://localhost:9411  (Distributed tracing)"
    echo ""
    
    echo -e "${BLUE}🔍 Service Discovery:${NC}"
    echo "   • Eureka Server:  http://localhost:8761"
    echo ""
    
    echo -e "${PURPLE}🚪 API Gateway:${NC}"
    echo "   • Gateway:        http://localhost:8080"
    echo "   • Health Check:   http://localhost:8080/actuator/health"
    echo "   • Routes:         http://localhost:8080/actuator/gateway/routes"
    echo ""
    
    echo -e "${GREEN}🏦 Microservices:${NC}"
    echo "   • User Service:         http://localhost:8081/swagger-ui.html"
    echo "   • Account Service:      http://localhost:8082/swagger-ui.html"
    echo "   • Transaction Service:  http://localhost:8083/swagger-ui.html"
    echo "   • Loan Service:         http://localhost:8084/swagger-ui.html"
    echo "   • Notification Service: http://localhost:8085/swagger-ui.html"
    echo ""
    
    echo -e "${YELLOW}📊 Monitoring & Management:${NC}"
    echo "   • Monitor Services:     ./scripts/docker-monitor.sh"
    echo "   • View Logs:           docker-compose logs -f [service-name]"
    echo "   • Cleanup:             ./scripts/docker-cleanup.sh"
    echo ""
    
    echo -e "${CYAN}🧪 Testing:${NC}"
    echo "   • Gateway E2E Tests:   ./scripts/gateway-e2e-test.sh"
    echo "   • All Services Test:   ./scripts/comprehensive-system-test.sh"
    echo "   • Individual Tests:    curl http://localhost:8080/actuator/health"
    echo ""
    
    echo -e "${GREEN}✅ All services are running and ready for testing!${NC}"
}

# Main execution
main() {
    print_status "Starting Telepesa Docker deployment..."
    
    # Change to Backend directory if not already there
    if [[ ! -f "docker-compose/docker-compose.yml" ]]; then
        if [[ -f "../docker-compose/docker-compose.yml" ]]; then
            cd ..
        else
            print_error "Cannot find docker-compose.yml. Please run from Backend directory."
            exit 1
        fi
    fi
    
    check_prerequisites
    build_shared_libraries
    start_infrastructure
    start_core_services
    start_microservices
    verify_services
    display_status
    
    print_success "🎉 Telepesa deployment completed successfully!"
    print_status "Use './scripts/docker-monitor.sh' for real-time monitoring"
}

# Handle interrupts gracefully
trap 'print_warning "Deployment interrupted. Run ./scripts/docker-cleanup.sh to clean up."; exit 1' INT TERM

# Execute main function
main "$@" 