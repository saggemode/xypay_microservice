#!/bin/bash

# XyPay Microservices Startup Script
echo "🚀 Starting XyPay Microservices Architecture..."

# Function to start a service
start_service() {
    local service_name=$1
    local service_path=$2
    local port=$3
    
    echo "Starting $service_name on port $port..."
    cd "$service_path"
    mvn spring-boot:run &
    echo "✅ $service_name started (PID: $!)"
    cd - > /dev/null
    sleep 5
}

# Wait for infrastructure
echo "⏳ Waiting for infrastructure services to be ready..."
sleep 10

# Start services in order
echo "1️⃣ Starting Eureka Server..."
start_service "Eureka Server" "services/eureka-server" "8761"

echo "2️⃣ Starting Customer Service..."
start_service "Customer Service" "services/customer-service" "8082"

echo "3️⃣ Starting Account Service..."
start_service "Account Service" "services/account-service" "8083"

echo "4️⃣ Starting Transaction Service..."
start_service "Transaction Service" "services/transaction-service" "8084"

echo "5️⃣ Starting Treasury Service..."
start_service "Treasury Service" "services/treasury-service" "8085"

echo "6️⃣ Starting Notification Service..."
start_service "Notification Service" "services/notification-service" "8086"

echo "7️⃣ Starting Analytics Service..."
start_service "Analytics Service" "services/analytics-service" "8087"

echo "8️⃣ Starting API Gateway..."
start_service "API Gateway" "services/api-gateway" "8080"

echo "9️⃣ Starting Web UI Service..."
cd .
mvn spring-boot:run -Dspring.profiles.active=webui &
echo "✅ Web UI Service started (PID: $!)"

echo ""
echo "🎉 All services started!"
echo ""
echo "📋 Service URLs:"
echo "   🌐 Web UI:           http://localhost:8081"
echo "   🚪 API Gateway:      http://localhost:8080"
echo "   🔍 Eureka Dashboard: http://localhost:8761"
echo "   👥 Customer API:     http://localhost:8082/api/customers"
echo "   💳 Account API:      http://localhost:8083/api/accounts"
echo "   💰 Transaction API:  http://localhost:8084/api/transactions"
echo "   📧 Notification API: http://localhost:8086/api/notifications"
echo "   🏦 Treasury API:     http://localhost:8085/api/treasury"
echo "   📊 Analytics API:    http://localhost:8087/api/analytics"
echo ""
echo "💡 To stop all services: Ctrl+C or run ./stop-services.sh"
