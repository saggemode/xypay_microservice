# 🚀 XyPay Microservices - Quick Start Guide

## **Easy Way to Run (Windows)**

### **Step 1: Start Infrastructure**
Open Command Prompt and run:
```bash
# Start PostgreSQL
docker run -d --name postgres -e POSTGRES_PASSWORD=password -e POSTGRES_DB=xypay -p 5432:5432 postgres:15

# Start Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# Start Kafka
docker run -d --name zookeeper -p 2181:2181 confluentinc/cp-zookeeper:latest
docker run -d --name kafka --link zookeeper -p 9092:9092 confluentinc/cp-kafka:latest
```

### **Step 2: Start Services (9 Terminal Windows)**

**Open 9 separate Command Prompt windows and run these commands:**

#### **Window 1 - Eureka Server:**
```bash
cd services\eureka-server
mvn spring-boot:run
```

#### **Window 2 - Customer Service:**
```bash
cd services\customer-service
mvn spring-boot:run
```

#### **Window 3 - Account Service:**
```bash
cd services\account-service
mvn spring-boot:run
```

#### **Window 4 - Transaction Service:**
```bash
cd services\transaction-service
mvn spring-boot:run
```

#### **Window 5 - Treasury Service:**
```bash
cd services\treasury-service
mvn spring-boot:run
```

#### **Window 6 - Notification Service:**
```bash
cd services\notification-service
mvn spring-boot:run
```

#### **Window 7 - Analytics Service:**
```bash
cd services\analytics-service
mvn spring-boot:run
```

#### **Window 8 - API Gateway:**
```bash
cd services\api-gateway
mvn spring-boot:run
```

#### **Window 9 - Web UI Service:**
```bash
mvn spring-boot:run -Dspring.profiles.active=webui
```

### **Step 3: Access Your Application**

Once all services are running, you can access:

- **🌐 Main Application:** http://localhost:8081
- **🚪 API Gateway:** http://localhost:8080
- **🔍 Service Discovery:** http://localhost:8761
- **📊 Health Checks:** http://localhost:8080/actuator/health

## **Alternative: Use Batch Files**

### **Quick Start (All Services):**
```bash
start-services.bat
```

### **Stop All Services:**
```bash
stop-services.bat
```

## **Service URLs**

| Service | URL | Port |
|---------|-----|------|
| Web UI | http://localhost:8081 | 8081 |
| API Gateway | http://localhost:8080 | 8080 |
| Eureka Dashboard | http://localhost:8761 | 8761 |
| Customer API | http://localhost:8082/api/customers | 8082 |
| Account API | http://localhost:8083/api/accounts | 8083 |
| Transaction API | http://localhost:8084/api/transactions | 8084 |
| Treasury API | http://localhost:8085/api/treasury | 8085 |
| Notification API | http://localhost:8086/api/notifications | 8086 |
| Analytics API | http://localhost:8087/api/analytics | 8087 |

## **Troubleshooting**

### **If services fail to start:**
1. Check if Docker containers are running: `docker ps`
2. Check if ports are available: `netstat -an | findstr :8080`
3. Check logs in each service window

### **If you get connection errors:**
1. Wait 2-3 minutes for all services to fully start
2. Check Eureka dashboard: http://localhost:8761
3. Ensure all services are registered

### **To stop everything:**
```bash
# Stop all Java processes
taskkill /f /im java.exe

# Stop Docker containers
docker stop postgres redis kafka zookeeper
```

## **What You'll See**

1. **Eureka Dashboard** - Shows all registered services
2. **Web UI** - Your main banking application interface
3. **API Gateway** - Routes all API requests
4. **Individual Services** - Each running independently

## **Next Steps**

Once running, you can:
- Test the APIs using Postman
- View metrics at `/actuator/prometheus`
- Check health at `/actuator/health`
- Monitor circuit breakers at `/actuator/circuitbreakers`

🎉 **Your microservices architecture is now running!**
