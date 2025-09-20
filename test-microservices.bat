@echo off
echo ========================================
echo XyPay Microservices Test Script
echo ========================================
echo.

echo Testing service endpoints...
echo.

echo [1/5] Testing Eureka Server...
curl -s http://localhost:8761/actuator/health
if %errorlevel% neq 0 (
    echo ❌ Eureka Server is not responding
) else (
    echo ✅ Eureka Server is healthy
)
echo.

echo [2/5] Testing API Gateway...
curl -s http://localhost:8080/actuator/health
if %errorlevel% neq 0 (
    echo ❌ API Gateway is not responding
) else (
    echo ✅ API Gateway is healthy
)
echo.

echo [3/6] Testing Customer Service...
curl -s http://localhost:8082/actuator/health
if %errorlevel% neq 0 (
    echo ❌ Customer Service is not responding
) else (
    echo ✅ Customer Service is healthy
)
echo.

echo [4/6] Testing Account Service...
curl -s http://localhost:8083/actuator/health
if %errorlevel% neq 0 (
    echo ❌ Account Service is not responding
) else (
    echo ✅ Account Service is healthy
)
echo.

echo [5/6] Testing service discovery...
curl -s http://localhost:8761/eureka/apps
if %errorlevel% neq 0 (
    echo ❌ Service discovery is not working
) else (
    echo ✅ Service discovery is working
)
echo.

echo [6/6] Testing API Gateway routing...
curl -s http://localhost:8080/api/v1/customers/health
if %errorlevel% neq 0 (
    echo ❌ Customer Service routing is not working
) else (
    echo ✅ Customer Service routing is working
)

curl -s http://localhost:8080/api/v1/accounts/health
if %errorlevel% neq 0 (
    echo ❌ Account Service routing is not working
) else (
    echo ✅ Account Service routing is working
)
echo.

echo ========================================
echo Test Summary
echo ========================================
echo Eureka Server: http://localhost:8761
echo API Gateway: http://localhost:8080
echo Customer Service: http://localhost:8082
echo Account Service: http://localhost:8083
echo.
echo To view service logs:
echo docker-compose -f docker-compose.microservices.yml logs -f [service-name]
echo.
pause
