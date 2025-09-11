#!/bin/bash

# XyPay Microservices Shutdown Script
echo "🛑 Stopping XyPay Microservices..."

# Kill all Java processes (Spring Boot applications)
echo "Stopping all Spring Boot services..."
pkill -f "spring-boot:run"

# Stop Docker containers
echo "Stopping infrastructure containers..."
docker stop postgres redis kafka zookeeper 2>/dev/null || true

echo "✅ All services stopped!"
echo "💡 To start again, run: ./start-services.sh"
