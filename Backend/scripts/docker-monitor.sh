#!/bin/bash

# Telepesa Docker Monitor Script
# Real-time monitoring dashboard for all services

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to clear screen and show header
show_header() {
    clear
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
║                    🐳 Docker Services Monitor 🐳                            ║
║                     📊 Real-time Dashboard 📊                               ║
║                                                                              ║
╚══════════════════════════════════════════════════════════════════════════════╝
EOF
    echo -e "${NC}"
    echo -e "${BLUE}$(date '+%Y-%m-%d %H:%M:%S') - Telepesa Services Monitoring Dashboard${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
}

# Function to get service status
get_service_status() {
    local service_name="$1"
    local port="$2"
    local endpoint="$3"
    
    # Check Docker container status
    local container_status=$(docker-compose ps -q $service_name 2>/dev/null)
    
    if [[ -z "$container_status" ]]; then
        echo -e "${RED}⚫ STOPPED${NC}"
        return
    fi
    
    local container_health=$(docker inspect --format='{{.State.Health.Status}}' "telepesa-$service_name" 2>/dev/null || echo "unknown")
    local container_running=$(docker inspect --format='{{.State.Running}}' "telepesa-$service_name" 2>/dev/null || echo "false")
    
    if [[ "$container_running" == "true" ]]; then
        if [[ "$container_health" == "healthy" ]]; then
            # Double-check with HTTP endpoint if provided
            if [[ -n "$endpoint" ]]; then
                if curl -s -f "http://localhost:$port$endpoint" > /dev/null 2>&1; then
                    echo -e "${GREEN}🟢 HEALTHY${NC}"
                else
                    echo -e "${YELLOW}🟡 RUNNING (HTTP FAIL)${NC}"
                fi
            else
                echo -e "${GREEN}🟢 HEALTHY${NC}"
            fi
        elif [[ "$container_health" == "unhealthy" ]]; then
            echo -e "${RED}🔴 UNHEALTHY${NC}"
        elif [[ "$container_health" == "starting" ]]; then
            echo -e "${YELLOW}🟡 STARTING${NC}"
        else
            echo -e "${BLUE}🔵 RUNNING${NC}"
        fi
    else
        echo -e "${RED}⚫ STOPPED${NC}"
    fi
}

# Function to get container resource usage
get_container_stats() {
    local service_name="$1"
    local container_id=$(docker-compose ps -q $service_name 2>/dev/null)
    
    if [[ -n "$container_id" ]]; then
        local stats=$(docker stats --no-stream --format "table {{.CPUPerc}},{{.MemUsage}}" $container_id 2>/dev/null | tail -n 1)
        if [[ -n "$stats" ]]; then
            echo "$stats"
        else
            echo "N/A,N/A"
        fi
    else
        echo "N/A,N/A"
    fi
}

# Function to display service status table
display_services() {
    echo -e "${GREEN}🏗️  Infrastructure Services${NC}"
    echo "┌─────────────────┬──────────────┬─────────────┬─────────────┬─────────────────┐"
    echo "│ Service         │ Status       │ Port        │ CPU         │ Memory          │"
    echo "├─────────────────┼──────────────┼─────────────┼─────────────┼─────────────────┤"
    
    # Infrastructure services
    local services=(
        "postgres:5432:/health"
        "mongodb:27017:"
        "redis:6379:"
        "kafka:29092:"
        "zipkin:9411:/health"
    )
    
    for service_info in "${services[@]}"; do
        IFS=':' read -r service port endpoint <<< "$service_info"
        local status=$(get_service_status "$service" "$port" "$endpoint")
        local stats=$(get_container_stats "$service")
        IFS=',' read -r cpu memory <<< "$stats"
        printf "│ %-15s │ %-12s │ %-11s │ %-11s │ %-15s │\n" "$service" "$status" "$port" "$cpu" "$memory"
    done
    
    echo "└─────────────────┴──────────────┴─────────────┴─────────────┴─────────────────┘"
    echo ""
    
    echo -e "${BLUE}🔍 Core Services${NC}"
    echo "┌─────────────────┬──────────────┬─────────────┬─────────────┬─────────────────┐"
    echo "│ Service         │ Status       │ Port        │ CPU         │ Memory          │"
    echo "├─────────────────┼──────────────┼─────────────┼─────────────┼─────────────────┤"
    
    # Core services
    local core_services=(
        "eureka-server:8761:/actuator/health"
        "api-gateway:8080:/actuator/health"
    )
    
    for service_info in "${core_services[@]}"; do
        IFS=':' read -r service port endpoint <<< "$service_info"
        local status=$(get_service_status "$service" "$port" "$endpoint")
        local stats=$(get_container_stats "$service")
        IFS=',' read -r cpu memory <<< "$stats"
        printf "│ %-15s │ %-12s │ %-11s │ %-11s │ %-15s │\n" "$service" "$status" "$port" "$cpu" "$memory"
    done
    
    echo "└─────────────────┴──────────────┴─────────────┴─────────────┴─────────────────┘"
    echo ""
    
    echo -e "${PURPLE}🏦 Microservices${NC}"
    echo "┌─────────────────┬──────────────┬─────────────┬─────────────┬─────────────────┐"
    echo "│ Service         │ Status       │ Port        │ CPU         │ Memory          │"
    echo "├─────────────────┼──────────────┼─────────────┼─────────────┼─────────────────┤"
    
    # Microservices
    local microservices=(
        "user-service:8081:/actuator/health"
        "account-service:8082:/actuator/health"
        "transaction-service:8083:/actuator/health"
        "loan-service:8084:/actuator/health"
        "notification-service:8085:/actuator/health"
    )
    
    for service_info in "${microservices[@]}"; do
        IFS=':' read -r service port endpoint <<< "$service_info"
        local status=$(get_service_status "$service" "$port" "$endpoint")
        local stats=$(get_container_stats "$service")
        IFS=',' read -r cpu memory <<< "$stats"
        printf "│ %-15s │ %-12s │ %-11s │ %-11s │ %-15s │\n" "$service" "$status" "$port" "$cpu" "$memory"
    done
    
    echo "└─────────────────┴──────────────┴─────────────┴─────────────┴─────────────────┘"
}

# Function to display system overview
display_system_overview() {
    echo ""
    echo -e "${CYAN}📊 System Overview${NC}"
    echo "┌────────────────────────────────────────────────────────────────────────────┐"
    
    # Docker system info
    local total_containers=$(docker ps -a | wc -l)
    local running_containers=$(docker ps | wc -l)
    local images_count=$(docker images | wc -l)
    
    echo "│ Docker Containers: $((running_containers - 1)) running / $((total_containers - 1)) total"
    echo "│ Docker Images:     $((images_count - 1)) total"
    
    # System resources
    if command -v free &> /dev/null; then
        local mem_info=$(free -h | grep '^Mem:')
        local mem_used=$(echo $mem_info | awk '{print $3}')
        local mem_total=$(echo $mem_info | awk '{print $2}')
        echo "│ System Memory:     $mem_used / $mem_total"
    fi
    
    if command -v df &> /dev/null; then
        local disk_info=$(df -h / | tail -1)
        local disk_used=$(echo $disk_info | awk '{print $3}')
        local disk_total=$(echo $disk_info | awk '{print $2}')
        local disk_percent=$(echo $disk_info | awk '{print $5}')
        echo "│ Disk Usage:        $disk_used / $disk_total ($disk_percent)"
    fi
    
    echo "└────────────────────────────────────────────────────────────────────────────┘"
}

# Function to display recent logs
display_recent_activity() {
    echo ""
    echo -e "${YELLOW}📝 Recent Activity (Last 5 log entries)${NC}"
    echo "┌────────────────────────────────────────────────────────────────────────────┐"
    
    # Get recent logs from all services
    docker-compose logs --tail=5 2>/dev/null | tail -10 | while read line; do
        # Truncate long lines
        if [[ ${#line} -gt 76 ]]; then
            line="${line:0:73}..."
        fi
        echo "│ $line"
    done
    
    echo "└────────────────────────────────────────────────────────────────────────────┘"
}

# Function to display service discovery status
display_eureka_status() {
    echo ""
    echo -e "${GREEN}🔍 Service Discovery Status${NC}"
    echo "┌────────────────────────────────────────────────────────────────────────────┐"
    
    # Check Eureka registration
    if curl -s "http://localhost:8761/eureka/apps" > /dev/null 2>&1; then
        local registered_services=$(curl -s "http://localhost:8761/eureka/apps" | grep -o '<name>[^<]*</name>' | sed 's/<name>//g' | sed 's/<\/name>//g' | sort | uniq | wc -l)
        echo "│ Eureka Server:     🟢 Running"
        echo "│ Registered Apps:   $registered_services services"
        
        # List registered services
        local services_list=$(curl -s "http://localhost:8761/eureka/apps" | grep -o '<name>[^<]*</name>' | sed 's/<name>//g' | sed 's/<\/name>//g' | sort | uniq | tr '\n' ' ')
        echo "│ Services:          $services_list"
    else
        echo "│ Eureka Server:     🔴 Not accessible"
    fi
    
    echo "└────────────────────────────────────────────────────────────────────────────┘"
}

# Function to display interactive menu
display_menu() {
    echo ""
    echo -e "${CYAN}🎛️  Interactive Commands${NC}"
    echo "┌────────────────────────────────────────────────────────────────────────────┐"
    echo "│ [l] View logs for a service    [r] Restart a service                      │"
    echo "│ [s] Stop a service            [t] Run gateway tests                       │"
    echo "│ [h] Show health endpoints     [c] Cleanup system                          │"
    echo "│ [q] Quit monitor              [Enter] Refresh dashboard                   │"
    echo "└────────────────────────────────────────────────────────────────────────────┘"
    echo -n "Enter command: "
}

# Function to handle user input
handle_input() {
    local input="$1"
    
    case "$input" in
        "l"|"logs")
            echo -n "Enter service name (postgres, eureka-server, api-gateway, user-service, etc.): "
            read service_name
            if [[ -n "$service_name" ]]; then
                echo "Showing logs for $service_name (press Ctrl+C to return)..."
                docker-compose logs -f "$service_name"
            fi
            ;;
        "r"|"restart")
            echo -n "Enter service name to restart: "
            read service_name
            if [[ -n "$service_name" ]]; then
                echo "Restarting $service_name..."
                docker-compose restart "$service_name"
                echo "Service restarted. Press Enter to continue..."
                read
            fi
            ;;
        "s"|"stop")
            echo -n "Enter service name to stop: "
            read service_name
            if [[ -n "$service_name" ]]; then
                echo "Stopping $service_name..."
                docker-compose stop "$service_name"
                echo "Service stopped. Press Enter to continue..."
                read
            fi
            ;;
        "t"|"test")
            echo "Running gateway end-to-end tests..."
            if [[ -f "../scripts/gateway-e2e-test.sh" ]]; then
                ../scripts/gateway-e2e-test.sh
            else
                echo "Test script not found!"
            fi
            echo "Press Enter to continue..."
            read
            ;;
        "h"|"health")
            echo "Health endpoints:"
            echo "• API Gateway:        http://localhost:8080/actuator/health"
            echo "• Eureka Server:      http://localhost:8761/actuator/health"
            echo "• User Service:       http://localhost:8081/actuator/health"
            echo "• Account Service:    http://localhost:8082/actuator/health"
            echo "• Transaction Service: http://localhost:8083/actuator/health"
            echo "• Loan Service:       http://localhost:8084/actuator/health"
            echo "• Notification Service: http://localhost:8085/actuator/health"
            echo "• Zipkin:            http://localhost:9411/health"
            echo "Press Enter to continue..."
            read
            ;;
        "c"|"cleanup")
            echo "Are you sure you want to cleanup the system? (y/N): "
            read confirm
            if [[ "$confirm" == "y" || "$confirm" == "Y" ]]; then
                if [[ -f "../scripts/docker-cleanup.sh" ]]; then
                    ../scripts/docker-cleanup.sh
                else
                    echo "Cleanup script not found!"
                fi
            fi
            echo "Press Enter to continue..."
            read
            ;;
        "q"|"quit")
            echo "Exiting monitor..."
            exit 0
            ;;
        "")
            # Just refresh - do nothing
            ;;
        *)
            echo "Unknown command: $input"
            echo "Press Enter to continue..."
            read
            ;;
    esac
}

# Main monitoring loop
main() {
    # Change to docker-compose directory
    if [[ ! -f "docker-compose.yml" ]]; then
        if [[ -f "../docker-compose/docker-compose.yml" ]]; then
            cd ../docker-compose
        else
            echo "Error: Cannot find docker-compose.yml"
            exit 1
        fi
    fi
    
    while true; do
        show_header
        display_services
        display_system_overview
        display_recent_activity
        display_eureka_status
        display_menu
        
        # Read user input with timeout
        if read -t 10 user_input; then
            handle_input "$user_input"
        else
            # Auto-refresh after 10 seconds
            continue
        fi
    done
}

# Handle interrupts gracefully
trap 'echo -e "\n${YELLOW}Monitor stopped.${NC}"; exit 0' INT TERM

# Execute main function
main "$@" 