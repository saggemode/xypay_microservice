@echo off
echo Running UUID schema migration for XyPay database...
echo.

REM Check if psql is available
where psql >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo ERROR: psql command not found. Please ensure PostgreSQL is installed and in your PATH.
    echo.
    echo You can also run the migration manually by:
    echo 1. Open pgAdmin or any PostgreSQL client
    echo 2. Connect to your database (localhost:5432/xypay_db)
    echo 3. Run the SQL commands from fix_uuid_schema.sql
    echo.
    pause
    exit /b 1
)

echo Connecting to PostgreSQL database...
echo Database: xypay_db
echo Host: localhost:5432
echo.

REM Run the migration script
psql -h localhost -p 5432 -U postgres -d xypay_db -f fix_uuid_schema.sql

if %ERRORLEVEL% equ 0 (
    echo.
    echo =====================================================
    echo Migration completed successfully!
    echo =====================================================
    echo You can now start your Spring Boot application.
) else (
    echo.
    echo =====================================================
    echo Migration failed! Please check the error messages above.
    echo =====================================================
)

echo.
pause
