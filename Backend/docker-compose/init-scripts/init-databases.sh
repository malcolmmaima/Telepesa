#!/bin/bash
set -e

# Create multiple databases for different services
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE user_db;
    CREATE DATABASE account_db;
    CREATE DATABASE transaction_db;
    CREATE DATABASE loan_db;
    
    GRANT ALL PRIVILEGES ON DATABASE user_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE account_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE transaction_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE loan_db TO $POSTGRES_USER;
EOSQL

# Add UUID extension to each database
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "user_db" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "account_db" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "transaction_db" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "loan_db" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
EOSQL