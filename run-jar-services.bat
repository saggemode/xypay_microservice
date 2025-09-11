@echo off
echo 🚀 Starting XyPay Microservices using JAR files...

echo.
echo Make sure PostgreSQL, Redis, Kafka and Zookeeper are running!
echo.

echo 1️⃣ Starting Eureka Server...
start "Eureka Server" cmd /k "cd services\eureka-server && java -jar target\eureka-server-0.0.1-SNAPSHOT.jar"

echo 2️⃣ Starting Customer Service...
timeout /t 10 /nobreak > nul
start "Customer Service" cmd /k "cd services\customer-service && java -jar target\customer-service-0.0.1-SNAPSHOT.jar"

echo 3️⃣ Starting Account Service...
timeout /t 10 /nobreak > nul
start "Account Service" cmd /k "cd services\account-service && java -jar target\account-service-0.0.1-SNAPSHOT.jar"

echo 4️⃣ Starting Transaction Service...
timeout /t 10 /nobreak > nul
start "Transaction Service" cmd /k "cd services\transaction-service && java -jar target\transaction-service-0.0.1-SNAPSHOT.jar"

echo 5️⃣ Starting Treasury Service...
timeout /t 10 /nobreak > nul
start "Treasury Service" cmd /k "cd services\treasury-service && java -jar target\treasury-service-0.0.1-SNAPSHOT.jar"

echo 6️⃣ Starting Notification Service...
timeout /t 10 /nobreak > nul
start "Notification Service" cmd /k "cd services\notification-service && java -jar target\notification-service-0.0.1-SNAPSHOT.jar"

echo 7️⃣ Starting Analytics Service...
timeout /t 10 /nobreak > nul
start "Analytics Service" cmd /k "cd services\analytics-service && java -jar target\analytics-service-0.0.1-SNAPSHOT.jar"

echo 8️⃣ Starting API Gateway...
timeout /t 10 /nobreak > nul
start "API Gateway" cmd /k "cd services\api-gateway && java -jar target\api-gateway-0.0.1-SNAPSHOT.jar"

echo 9️⃣ Starting Web UI Service...
timeout /t 10 /nobreak > nul
start "Web UI Service" cmd /k "cd . && java -jar target\web-ui-service-0.0.1-SNAPSHOT.jar"

echo.
echo 🎉 All services started!
echo.
echo 📋 Service URLs:
echo    🌐 Web UI:           http://localhost:8081
echo    🚪 API Gateway:      http://localhost:8080
echo    🔍 Eureka Dashboard: http://localhost:8761
echo.
echo 💡 Each service runs in its own window. Close windows to stop services.