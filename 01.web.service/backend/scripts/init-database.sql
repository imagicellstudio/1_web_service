-- ============================================
-- XLCfi Platform - Database Initialization Script
-- ============================================
-- Purpose: Create database, user, and grant permissions
-- Usage: Run this script as PostgreSQL superuser
-- Example: psql -U postgres -f init-database.sql
-- ============================================

-- Create database
CREATE DATABASE xlcfi_db
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE xlcfi_db IS 'XLCfi K-Food Platform Database';

-- Connect to the database
\c xlcfi_db

-- Create user
CREATE USER xlcfi_user WITH PASSWORD 'xlcfi_password';

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE xlcfi_db TO xlcfi_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO xlcfi_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO xlcfi_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO xlcfi_user;

-- Default privileges for future objects
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT ALL PRIVILEGES ON TABLES TO xlcfi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT ALL PRIVILEGES ON SEQUENCES TO xlcfi_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public 
    GRANT ALL PRIVILEGES ON FUNCTIONS TO xlcfi_user;

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Display success message
SELECT 'Database xlcfi_db initialized successfully!' AS status;

