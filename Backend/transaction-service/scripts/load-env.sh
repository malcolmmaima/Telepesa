#!/bin/bash

# Load Environment Variables from .env files
# This script loads environment variables for the transaction service

# Function to load environment file
load_env_file() {
    local env_file="$1"
    if [ -f "$env_file" ]; then
        echo "📁 Loading environment from: $env_file"
        export $(cat "$env_file" | grep -v '^#' | grep -v '^$' | xargs)
        return 0
    else
        return 1
    fi
}

echo "🔧 Loading Transaction Service Environment..."

# Try to load environment files in order of preference
if load_env_file "../../.env.local"; then
    echo "✅ Loaded Backend/.env.local"
elif load_env_file "../../.env"; then
    echo "✅ Loaded Backend/.env"
elif load_env_file ".env.local"; then
    echo "✅ Loaded transaction-service/.env.local"
elif load_env_file ".env"; then
    echo "✅ Loaded transaction-service/.env"
else
    echo "⚠️  No environment file found. Using system environment variables only."
    echo "💡 Create Backend/.env.local from Backend/.env.example for local development"
fi

# Validate critical environment variables
echo ""
echo "🔍 Environment Variable Status:"

if [ -n "$NVD_API_KEY" ]; then
    echo "✅ NVD_API_KEY: ${NVD_API_KEY:0:8}..."
else
    echo "❌ NVD_API_KEY: Not set"
    echo "💡 Get your key at: https://nvd.nist.gov/developers/request-an-api-key"
fi

if [ -n "$JWT_SECRET" ]; then
    echo "✅ JWT_SECRET: ${JWT_SECRET:0:8}..."
else
    echo "⚠️  JWT_SECRET: Not set (will use default)"
fi

if [ -n "$SPRING_PROFILES_ACTIVE" ]; then
    echo "✅ SPRING_PROFILES_ACTIVE: $SPRING_PROFILES_ACTIVE"
else
    echo "ℹ️  SPRING_PROFILES_ACTIVE: Not set (will use default)"
fi

echo ""
echo "🎯 Environment loaded for Transaction Service"
