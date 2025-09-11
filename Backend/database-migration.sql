-- Database Migration Script for Telepesa User Service
-- Add new columns for avatar upload and profile management features
-- Run this against your PostgreSQL database

-- Connect to the user service database
\c telepesa_users;

-- Check current table structure
\d users;

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

-- Create index on avatar_url for performance (optional)
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

-- Show sample of updated table structure
SELECT 
    column_name, 
    data_type, 
    character_maximum_length,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'users' 
ORDER BY ordinal_position;

COMMIT;
