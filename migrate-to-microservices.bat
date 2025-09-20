@echo off
echo ========================================
echo XyPay Microservices Migration Script
echo ========================================
echo.

echo [1/10] Building shared libraries...
cd shared-libraries\common-entities
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build common-entities
    exit /b 1
)
cd ..\..

echo.
echo [2/10] Building Eureka Server...
cd services\eureka-server
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build eureka-server
    exit /b 1
)
cd ..\..

echo.
echo [3/10] Building API Gateway...
cd services\api-gateway
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build api-gateway
    exit /b 1
)
cd ..\..

echo.
echo [4/10] Building Customer Service...
cd services\customer-service
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build customer-service
    exit /b 1
)
cd ..\..

echo.
echo [5/10] Building Account Service...
cd services\account-service
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Failed to build account-service
    exit /b 1
)
cd ..\..

echo.
echo [6/10] Starting infrastructure services...
docker-compose -f docker-compose.microservices.yml up -d postgres redis zookeeper kafka
timeout /t 30 /nobreak > nul

echo.
echo [7/10] Starting Eureka Server...
docker-compose -f docker-compose.microservices.yml up -d eureka-server
timeout /t 15 /nobreak > nul

echo.
echo [8/10] Starting API Gateway...
docker-compose -f docker-compose.microservices.yml up -d api-gateway
timeout /t 10 /nobreak > nul

echo.
echo [9/10] Starting Customer and Account Services...
docker-compose -f docker-compose.microservices.yml up -d customer-service account-service
timeout /t 10 /nobreak > nul

echo.
echo [10/10] Checking service health...
echo Eureka Server: http://localhost:8761
echo API Gateway: http://localhost:8080
echo Customer Service: http://localhost:8082
echo Account Service: http://localhost:8083

echo.
echo Migration completed!
echo.
echo Next steps:
echo 1. Extract remaining services (account, transaction, notification, etc.)
echo 2. Update service configurations
echo 3. Test inter-service communication
echo 4. Update frontend to use new API endpoints
echo.
echo To stop all services: docker-compose -f docker-compose.microservices.yml down
echo To view logs: docker-compose -f docker-compose.microservices.yml logs -f [service-name]
echo.
pause
