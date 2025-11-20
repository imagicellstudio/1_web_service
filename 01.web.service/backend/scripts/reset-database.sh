#!/bin/bash

# ============================================
# XLCfi Platform - Database Reset Script
# ============================================
# Purpose: Drop and recreate database (CAUTION!)
# Usage: ./reset-database.sh
# ============================================

set -e

echo "=========================================="
echo "XLCfi Platform - Database Reset"
echo "=========================================="
echo ""
echo "WARNING: This will DROP the existing database!"
echo "All data will be lost!"
echo ""
read -p "Are you sure you want to continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "Operation cancelled."
    exit 0
fi

echo ""
echo "Resetting database..."
echo ""

# PostgreSQL connection settings
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-xlcfi_db}"
DB_USER="${DB_USER:-xlcfi_user}"
POSTGRES_USER="${POSTGRES_USER:-postgres}"

# Drop existing database
echo "1. Dropping existing database..."
psql -h $DB_HOST -p $DB_PORT -U $POSTGRES_USER -c "DROP DATABASE IF EXISTS $DB_NAME;"

# Drop existing user
echo "2. Dropping existing user..."
psql -h $DB_HOST -p $DB_PORT -U $POSTGRES_USER -c "DROP USER IF EXISTS $DB_USER;"

# Run init script
echo "3. Running initialization script..."
psql -h $DB_HOST -p $DB_PORT -U $POSTGRES_USER -f init-database.sql

echo ""
echo "=========================================="
echo "Database reset completed!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Run Flyway migrations: cd .. && ./gradlew flywayMigrate"
echo "2. Seed test data: psql -U $DB_USER -d $DB_NAME -f seed-data.sql"
echo ""

