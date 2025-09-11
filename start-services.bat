@echo off
echo 🚀 Starting XyPay Microservices Architecture...

echo ⏳ Starting infrastructure services...
echo Starting PostgreSQL...
docker run -d --name postgres -e POSTGRES_PASSWORD=password -e POSTGRES_DB=xypay -p 5432:5432 postgres:15

echo Starting Redis...
docker run -d --name redis -p 6379:6379 redis:7-alpine

echo Starting Zookeeper...
docker run -d --name zookeeper -p 2181:2181 confluentinc/cp-zookeeper:latest

echo Starting Kafka...
docker run -d --name kafka --link zookeeper -p 9092:9092 confluentinc/cp-kafka:latest

echo ⏳ Waiting for infrastructure to be ready...
timeout /t 15 /nobreak > nul

echo.
echo Starting microservices...
echo.

echo 1️⃣ Starting Eureka Server...
start "Eureka Server" cmd /k "cd services\eureka-server && mvn spring-boot:run"

echo 2️⃣ Starting Customer Service...
timeout /t 10 /nobreak > nul
start "Customer Service" cmd /k "cd services\customer-service && mvn spring-boot:run"

echo 3️⃣ Starting Account Service...
timeout /t 10 /nobreak > nul
start "Account Service" cmd /k "cd services\account-service && mvn spring-boot:run"

echo 4️⃣ Starting Transaction Service...
timeout /t 10 /nobreak > nul
start "Transaction Service" cmd /k "cd services\transaction-service && mvn spring-boot:run"

echo 5️⃣ Starting Treasury Service...
timeout /t 10 /nobreak > nul
start "Treasury Service" cmd /k "cd services\treasury-service && mvn spring-boot:run"

echo 6️⃣ Starting Notification Service...
timeout /t 10 /nobreak > nul
start "Notification Service" cmd /k "cd services\notification-service && mvn spring-boot:run"

echo 7️⃣ Starting Analytics Service...
timeout /t 10 /nobreak > nul
start "Analytics Service" cmd /k "cd services\analytics-service && mvn spring-boot:run"

echo 8️⃣ Starting API Gateway...
timeout /t 10 /nobreak > nul
start "API Gateway" cmd /k "cd services\api-gateway && mvn spring-boot:run"

echo 9️⃣ Starting Web UI Service...
timeout /t 10 /nobreak > nul
start "Web UI Service" cmd /k "mvn spring-boot:run -Dspring.profiles.active=webui"

echo.
echo 🎉 All services started!
echo.
echo 📋 Service URLs:
echo    🌐 Web UI:           http://localhost:8081
echo    🚪 API Gateway:      http://localhost:8080
echo    🔍 Eureka Dashboard: http://localhost:8761
echo    👥 Customer API:     http://localhost:8082/api/customers
echo    💳 Account API:      http://localhost:8083/api/accounts
echo    💰 Transaction API:  http://localhost:8084/api/transactions
echo    📧 Notification API: http://localhost:8086/api/notifications
echo    🏦 Treasury API:     http://localhost:8085/api/treasury
echo    📊 Analytics API:    http://localhost:8087/api/analytics
echo.
echo 💡 Each service runs in its own window. Close windows to stop services.
echo 💡 To stop infrastructure: docker stop postgres redis kafka zookeeper
pause
