#!/bin/bash

# Start User Service with PostgreSQL Configuration
echo "🚀 Starting User Service with PostgreSQL..."

# Set environment variables for PostgreSQL
export DATABASE_URL="jdbc:postgresql://localhost:5432/telepesa"
export DATABASE_USERNAME="telepesa"
export DATABASE_PASSWORD="password"
export DATABASE_DRIVER="org.postgresql.Driver"
export JPA_DIALECT="org.hibernate.dialect.PostgreSQLDialect"
export DDL_AUTO="update"
export SPRING_PROFILES_ACTIVE="dev"

# Navigate to user service directory
cd user-service

# Start the service
echo "📊 Database URL: $DATABASE_URL"
echo "👤 Username: $DATABASE_USERNAME"
echo "🔑 Password: $DATABASE_PASSWORD"
echo "🏗️ DDL Auto: $DDL_AUTO"
echo ""

mvn spring-boot:run 