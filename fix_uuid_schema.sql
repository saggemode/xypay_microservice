-- =====================================================
-- UUID Schema Migration Script for XyPay Database
-- =====================================================
-- This script migrates the database from bigint IDs to UUID IDs
-- Run this script in your PostgreSQL database before starting the application

-- Step 1: Create backup tables (optional but recommended)
-- Uncomment the following lines if you want to backup existing data
-- CREATE TABLE users_backup AS SELECT * FROM users;
-- CREATE TABLE xysave_settings_backup AS SELECT * FROM xysave_settings;
-- CREATE TABLE xysave_goals_backup AS SELECT * FROM xysave_goals;

-- Step 2: Drop existing foreign key constraints
-- This prevents constraint violations during the migration
ALTER TABLE xysave_settings DROP CONSTRAINT IF EXISTS fkq9uy93adb7mtdsqure2j8bu65;
ALTER TABLE xysave_goals DROP CONSTRAINT IF EXISTS fkkvubebh9bd0ocsdy5anuofxdc;
ALTER TABLE xysave_goals DROP CONSTRAINT IF EXISTS fk_xysave_goals_xysave_account_id;

-- Step 3: Add new UUID columns
-- Add temporary UUID columns to store the new values
ALTER TABLE users ADD COLUMN IF NOT EXISTS new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE xysave_settings ADD COLUMN IF NOT EXISTS new_user_id UUID;
ALTER TABLE xysave_goals ADD COLUMN IF NOT EXISTS new_user_id UUID;
ALTER TABLE xysave_goals ADD COLUMN IF NOT EXISTS new_xysave_account_id UUID;

-- Step 4: Generate UUIDs for existing records
-- Update the new columns with generated UUIDs
UPDATE users SET new_id = gen_random_uuid() WHERE new_id IS NULL;

-- Step 5: Map foreign key relationships
-- Update foreign key columns to reference the new UUIDs
UPDATE xysave_settings 
SET new_user_id = u.new_id 
FROM users u 
WHERE xysave_settings.user_id = u.id;

UPDATE xysave_goals 
SET new_user_id = u.new_id 
FROM users u 
WHERE xysave_goals.user_id = u.id;

-- Step 6: Drop old columns and rename new ones
-- Remove the old bigint columns and rename UUID columns
ALTER TABLE xysave_settings DROP COLUMN IF EXISTS user_id;
ALTER TABLE xysave_settings RENAME COLUMN new_user_id TO user_id;

ALTER TABLE xysave_goals DROP COLUMN IF EXISTS user_id;
ALTER TABLE xysave_goals RENAME COLUMN new_user_id TO user_id;

ALTER TABLE users DROP COLUMN IF EXISTS id;
ALTER TABLE users RENAME COLUMN new_id TO id;

-- Step 7: Set primary key and constraints
-- Make the UUID columns primary keys and add constraints
ALTER TABLE users ADD PRIMARY KEY (id);
ALTER TABLE xysave_settings ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE xysave_goals ALTER COLUMN user_id SET NOT NULL;

-- Step 8: Recreate foreign key constraints
-- Add back the foreign key constraints with proper names
ALTER TABLE xysave_settings 
ADD CONSTRAINT fk_xysave_settings_user_id 
FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE xysave_goals 
ADD CONSTRAINT fk_xysave_goals_user_id 
FOREIGN KEY (user_id) REFERENCES users(id);

-- Step 9: Update any other tables that might reference users.id
-- Check if there are other tables with user_id foreign keys
-- You may need to add similar migrations for other tables

-- Step 10: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_xysave_settings_user_id ON xysave_settings(user_id);
CREATE INDEX IF NOT EXISTS idx_xysave_goals_user_id ON xysave_goals(user_id);

-- Step 11: Verify the migration
-- Run these queries to verify the migration was successful
SELECT 'Users table' as table_name, count(*) as record_count FROM users
UNION ALL
SELECT 'XySave Settings table' as table_name, count(*) as record_count FROM xysave_settings
UNION ALL
SELECT 'XySave Goals table' as table_name, count(*) as record_count FROM xysave_goals;

-- Check data types
SELECT 
    table_name, 
    column_name, 
    data_type 
FROM information_schema.columns 
WHERE table_name IN ('users', 'xysave_settings', 'xysave_goals') 
    AND column_name IN ('id', 'user_id')
ORDER BY table_name, column_name;

-- =====================================================
-- Migration Complete!
-- =====================================================
-- Your database schema has been successfully migrated to use UUIDs
-- You can now start your Spring Boot application