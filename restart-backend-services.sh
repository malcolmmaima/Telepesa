#!/bin/bash

# Script to rebuild and restart Telepesa backend services with new changes
# Run this from the Telepesa project root directory

set -e

echo "🔄 Restarting Telepesa Backend Services with New Changes"
echo "================================================"

# Check if we're in the right directory
if [ ! -d "Backend" ]; then
    echo "❌ Error: Backend directory not found. Please run this script from the Telepesa project root."
    exit 1
fi

cd Backend

# Check if docker-compose.yml exists
if [ ! -f "docker-compose.yml" ]; then
    echo "❌ Error: docker-compose.yml not found in Backend directory."
    exit 1
fi

echo "📋 Current running containers:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep telepesa || echo "No telepesa containers found"

echo ""
echo "🛑 Stopping services that need updates..."

# Stop services that need the new endpoints
echo "Stopping user-service..."
docker-compose stop user-service

echo "Stopping notification-service..."
docker-compose stop notification-service

echo ""
echo "🔨 Rebuilding services with new changes..."

# Rebuild the services with new code
echo "Rebuilding user-service..."
docker-compose build --no-cache user-service

echo "Rebuilding notification-service..."
docker-compose build --no-cache notification-service

echo ""
echo "🚀 Starting updated services..."

# Start the services
echo "Starting user-service..."
docker-compose up -d user-service

echo "Starting notification-service..."
docker-compose up -d notification-service

echo ""
echo "⏳ Waiting for services to be healthy..."

# Wait for services to be ready
sleep 10

echo ""
echo "🏥 Health check - Testing endpoints..."

# Test the API Gateway
echo "Testing API Gateway..."
curl -s -I http://localhost:8080/health | head -1 || echo "API Gateway not responding"

# Test User Service directly
echo "Testing User Service..."
curl -s -I http://localhost:8081/actuator/health | head -1 || echo "User Service not responding"

# Test Notification Service directly  
echo "Testing Notification Service..."
curl -s -I http://localhost:8085/actuator/health | head -1 || echo "Notification Service not responding"

echo ""
echo "📊 Final status:"
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep -E "(telepesa-user-service|telepesa-notification-service|telepesa-api-gateway)"

echo ""
echo "✅ Services have been restarted with new changes!"
echo ""
echo "🔧 Next steps:"
echo "1. Check the logs if any service is unhealthy:"
echo "   docker logs telepesa-user-service"
echo "   docker logs telepesa-notification-service"
echo ""
echo "2. Test the new endpoints:"
echo "   - Avatar upload: POST /api/v1/users/avatar"
echo "   - Profile update: PUT /api/v1/users/profile"
echo "   - Change password: PUT /api/v1/users/change-password"
echo ""
echo "3. If you still get 500 errors, check the database schema:"
echo "   The users table needs these new columns:"
echo "   - avatar_url VARCHAR(500)"
echo "   - date_of_birth VARCHAR(10)"
echo ""
echo "4. Monitor the frontend - errors should now be resolved!"
