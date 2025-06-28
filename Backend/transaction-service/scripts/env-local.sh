#!/bin/bash

# Transaction Service - Local Environment Variables
# Source this file to set up your local development environment
# Usage: source scripts/env-local.sh

# NVD API Key for OWASP dependency checks
# Get your own free API key at: https://nvd.nist.gov/developers/request-an-api-key
if [ -z "$NVD_API_KEY" ]; then
    echo "⚠️  NVD_API_KEY not set. Get your free API key at:"
    echo "   https://nvd.nist.gov/developers/request-an-api-key"
    echo "   Then add: export NVD_API_KEY=\"your-key-here\" to your ~/.bashrc or ~/.zshrc"
else
    echo "✅ NVD API Key configured: ${NVD_API_KEY:0:8}..."
fi

# Spring Boot configuration
export SPRING_PROFILES_ACTIVE="local"
export SERVER_PORT="8083"

# Database configuration (using H2 for local development)
export DB_URL="jdbc:h2:mem:telepesa_local;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false"
export DB_USERNAME="sa"
export DB_PASSWORD=""

# JWT configuration for local development
export JWT_SECRET="local-dev-secret-key-for-testing-only"
export JWT_EXPIRATION="86400000"

# Logging configuration
export LOGGING_LEVEL_ROOT="INFO"
export LOGGING_LEVEL_TELEPESA="DEBUG"

echo "✅ Local environment variables configured for Transaction Service"
echo "📍 Service will run on: http://localhost:$SERVER_PORT"
echo "🏷️  Active Profile: $SPRING_PROFILES_ACTIVE" 