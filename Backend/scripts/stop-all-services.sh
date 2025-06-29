#!/bin/bash

# Telepesa Stop All Services Script
# Stops all running microservices

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

echo -e "${BLUE}🛑 Stopping Telepesa All Services${NC}"

# Kill all Spring Boot processes
echo -e "${YELLOW}Stopping all Spring Boot services...${NC}"
pkill -f "spring-boot:run" || echo -e "${YELLOW}No Spring Boot processes found${NC}"

# Kill processes by PID files if they exist
if [ -d "logs" ]; then
    for pid_file in logs/*.pid; do
        if [ -f "$pid_file" ]; then
            pid=$(cat "$pid_file")
            echo -e "${YELLOW}Stopping process with PID: $pid${NC}"
            kill "$pid" 2>/dev/null || echo -e "${YELLOW}Process $pid not found${NC}"
            rm "$pid_file"
        fi
    done
fi

# Kill any remaining Java processes on our ports
echo -e "${YELLOW}Cleaning up any remaining processes on service ports...${NC}"
for port in 8081 8082 8083 8084 8085; do
    lsof -ti:$port | xargs kill -9 2>/dev/null || echo -e "${YELLOW}No processes on port $port${NC}"
done

echo -e "${GREEN}✅ All services stopped successfully!${NC}" 