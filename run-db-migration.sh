#!/bin/bash

# Quick database migration for Telepesa user service
# Adds the missing avatar_url and date_of_birth columns

set -e

echo "🗄️ Running Database Migration for Telepesa User Service"
echo "======================================================"

# Database connection details (adjust as needed)
DB_HOST="localhost"
DB_PORT="5432"
DB_NAME="telepesa_users"  # or whatever your user service database is named
DB_USER="telepesa"        # adjust to your database username

echo "Connecting to PostgreSQL database..."
echo "Host: $DB_HOST:$DB_PORT"
echo "Database: $DB_NAME"
echo "User: $DB_USER"
echo ""

# Run the migration
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME << 'EOF'
-- Add avatar_url column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'avatar_url'
    ) THEN
        ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500);
        RAISE NOTICE 'Added avatar_url column to users table';
    ELSE
        RAISE NOTICE 'avatar_url column already exists in users table';
    END IF;
END $$;

-- Add date_of_birth column if it doesn't exist  
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'users' AND column_name = 'date_of_birth'
    ) THEN
        ALTER TABLE users ADD COLUMN date_of_birth VARCHAR(10);
        RAISE NOTICE 'Added date_of_birth column to users table';
    ELSE
        RAISE NOTICE 'date_of_birth column already exists in users table';
    END IF;
END $$;

-- Create index on avatar_url for performance
CREATE INDEX IF NOT EXISTS idx_users_avatar ON users(avatar_url) WHERE avatar_url IS NOT NULL;

-- Verify the changes
SELECT 
    column_name, 
    data_type, 
    character_maximum_length,
    is_nullable
FROM information_schema.columns 
WHERE table_name = 'users' 
    AND column_name IN ('avatar_url', 'date_of_birth')
ORDER BY column_name;

EOF

echo ""
echo "✅ Database migration completed successfully!"
echo ""
echo "The following columns have been added to the users table:"
echo "- avatar_url VARCHAR(500)     - for storing profile picture URLs"
echo "- date_of_birth VARCHAR(10)   - for storing user birth dates"
echo ""
echo "Next steps:"
echo "1. Restart the user service to pick up the new code changes"
echo "2. Test the new avatar upload and profile update features"
