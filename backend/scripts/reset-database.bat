@echo off
REM ============================================
REM XLCfi Platform - Database Reset Script (Windows)
REM ============================================
REM Purpose: Drop and recreate database (CAUTION!)
REM Usage: reset-database.bat
REM ============================================

echo ==========================================
echo XLCfi Platform - Database Reset
echo ==========================================
echo.
echo WARNING: This will DROP the existing database!
echo All data will be lost!
echo.
set /p confirm="Are you sure you want to continue? (yes/no): "

if not "%confirm%"=="yes" (
    echo Operation cancelled.
    exit /b 0
)

echo.
echo Resetting database...
echo.

REM PostgreSQL connection settings
if not defined DB_HOST set DB_HOST=localhost
if not defined DB_PORT set DB_PORT=5432
if not defined DB_NAME set DB_NAME=xlcfi_db
if not defined DB_USER set DB_USER=xlcfi_user
if not defined POSTGRES_USER set POSTGRES_USER=postgres

REM Drop existing database
echo 1. Dropping existing database...
psql -h %DB_HOST% -p %DB_PORT% -U %POSTGRES_USER% -c "DROP DATABASE IF EXISTS %DB_NAME%;"

REM Drop existing user
echo 2. Dropping existing user...
psql -h %DB_HOST% -p %DB_PORT% -U %POSTGRES_USER% -c "DROP USER IF EXISTS %DB_USER%;"

REM Run init script
echo 3. Running initialization script...
psql -h %DB_HOST% -p %DB_PORT% -U %POSTGRES_USER% -f init-database.sql

echo.
echo ==========================================
echo Database reset completed!
echo ==========================================
echo.
echo Next steps:
echo 1. Run Flyway migrations: cd .. ^&^& gradlew flywayMigrate
echo 2. Seed test data: psql -U %DB_USER% -d %DB_NAME% -f seed-data.sql
echo.

pause

