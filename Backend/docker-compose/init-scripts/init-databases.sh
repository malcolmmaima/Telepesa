#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE user_db;
    CREATE DATABASE account_db;
    CREATE DATABASE transaction_db;
    CREATE DATABASE loan_db;
    
    GRANT ALL PRIVILEGES ON DATABASE user_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE account_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE transaction_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE loan_db TO $POSTGRES_USER;
    
    \c user_db;
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    
    \c account_db;
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    
    \c transaction_db;
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    
    \c loan_db;
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
EOSQL 