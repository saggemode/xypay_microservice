# XyPay Microservices Architecture

## Overview

XyPay has been transformed from a monolithic application to a microservices architecture. This document outlines the new structure, services, and deployment instructions.

## Architecture Diagram

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Web Browser   │    │   Mobile App    │    │   Admin Panel   │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────┴─────────────┐
                    │      API Gateway          │
                    │      (Port 8080)          │
                    └─────────────┬─────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
┌───────▼────────┐    ┌──────────▼──────────┐    ┌────────▼────────┐
│ Customer       │    │ Account Service     │    │ Transaction      │
│ Service        │    │ (Port 8083)         │    │ Service          │
│ (Port 8082)    │    │                     │    │ (Port 8084)      │
└────────────────┘    └─────────────────────┘    └─────────────────┘
        │                         │                         │
        └─────────────────────────┼─────────────────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
┌───────▼────────┐    ┌──────────▼──────────┐    ┌────────▼────────┐
│ Notification   │    │ Treasury Service    │    │ Analytics        │
│ Service        │    │ (Port 8085)         │    │ Service          │
│ (Port 8086)    │    │                     │    │ (Port 8087)      │
└────────────────┘    └─────────────────────┘    └─────────────────┘
        │                         │                         │
        └─────────────────────────┼─────────────────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │    Eureka Server          │
                    │    (Port 8761)            │
                    └───────────────────────────┘
                                  │
                    ┌─────────────▼─────────────┐
                    │    Shared Database        │
                    │    (PostgreSQL)           │
                    └───────────────────────────┘
```

## Services

### 1. Eureka Server (Port 8761)
- **Purpose**: Service discovery and registration
- **Technology**: Spring Cloud Netflix Eureka
- **Location**: `services/eureka-server/`

### 2. API Gateway (Port 8080)
- **Purpose**: Single entry point, routing, authentication
- **Technology**: Spring Cloud Gateway
- **Location**: `services/api-gateway/`
- **Routes**:
  - `/api/customers/**` → Customer Service
  - `/api/accounts/**` → Account Service
  - `/api/transactions/**` → Transaction Service
  - `/api/notifications/**` → Notification Service
  - `/api/treasury/**` → Treasury Service
  - `/api/analytics/**` → Analytics Service
  - `/**` → Web UI Service

### 3. Customer Service (Port 8082)
- **Purpose**: User management, KYC, profiles
- **Technology**: Spring Boot, JPA, PostgreSQL
- **Location**: `services/customer-service/`
- **Key Features**:
  - User registration and authentication
  - KYC profile management
  - OTP and verification
  - Transaction limits based on KYC level

### 4. Account Service (Port 8083)
- **Purpose**: Wallet and account management
- **Technology**: Spring Boot, JPA, PostgreSQL
- **Location**: `services/account-service/`
- **Key Features**:
  - Wallet creation and management
  - Account balance management
  - Hold/release functionality
  - Account freezing/unfreezing

### 5. Transaction Service (Port 8084)
- **Purpose**: Transaction processing
- **Technology**: Spring Boot, JPA, PostgreSQL
- **Location**: `services/transaction-service/`
- **Key Features**:
  - Transaction creation and processing
  - Transaction history
  - Transaction status management
  - Idempotency handling

### 6. Notification Service (Port 8086)
- **Purpose**: Email, SMS, push notifications
- **Technology**: Spring Boot, Kafka
- **Location**: `services/notification-service/`
- **Key Features**:
  - Email notifications
  - SMS notifications
  - Push notifications
  - In-app notifications

### 7. Treasury Service (Port 8085)
- **Purpose**: Treasury operations and liquidity management
- **Technology**: Spring Boot, JPA
- **Location**: `services/treasury-service/`
- **Key Features**:
  - Liquidity management
  - Treasury positions
  - Risk management

### 8. Analytics Service (Port 8087)
- **Purpose**: Data analytics and reporting
- **Technology**: Spring Boot, Kafka
- **Location**: `services/analytics-service/`
- **Key Features**:
  - Transaction analytics
  - Predictive analytics
  - Reporting dashboards

### 9. Web UI Service (Port 8081)
- **Purpose**: Admin panels and web interfaces
- **Technology**: Spring Boot, Thymeleaf
- **Location**: Main application (refactored)
- **Key Features**:
  - Admin dashboards
  - User management UI
  - Transaction monitoring
  - KYC management UI

## Database Schema

All services share the same PostgreSQL database with separate schemas or table prefixes:

- **Customer Service**: `users`, `user_profiles`, `kyc_profiles`
- **Account Service**: `wallets`, `accounts`
- **Transaction Service**: `transactions`
- **Notification Service**: `notifications`
- **Treasury Service**: `treasury_positions`, `liquidity_management`
- **Analytics Service**: `analytics_data`, `reports`

## Communication Patterns

### 1. Synchronous Communication
- **REST APIs**: Service-to-service communication
- **Feign Client**: Declarative REST client
- **Load Balancing**: Ribbon (via Eureka)

### 2. Asynchronous Communication
- **Kafka**: Event-driven communication
- **Events**: Transaction events, notification events

### 3. Service Discovery
- **Eureka**: Service registration and discovery
- **Load Balancing**: Client-side load balancing

## Deployment

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 13+
- Redis 6+
- Kafka 2.8+
- Docker (optional)

### Local Development

1. **Start Infrastructure**:
   ```bash
   # Start PostgreSQL
   docker run -d --name postgres -e POSTGRES_PASSWORD=password -e POSTGRES_DB=xypay -p 5432:5432 postgres:15
   
   # Start Redis
   docker run -d --name redis -p 6379:6379 redis:7-alpine
   
   # Start Kafka
   docker run -d --name zookeeper -p 2181:2181 confluentinc/cp-zookeeper:latest
   docker run -d --name kafka --link zookeeper -p 9092:9092 confluentinc/cp-kafka:latest
   ```

2. **Start Services** (in order):
   ```bash
   # 1. Eureka Server
   cd services/eureka-server
   mvn spring-boot:run
   
   # 2. Customer Service
   cd services/customer-service
   mvn spring-boot:run
   
   # 3. Account Service
   cd services/account-service
   mvn spring-boot:run
   
   # 4. Transaction Service
   cd services/transaction-service
   mvn spring-boot:run
   
   # 5. Notification Service
   cd services/notification-service
   mvn spring-boot:run
   
   # 6. Treasury Service
   cd services/treasury-service
   mvn spring-boot:run
   
   # 7. Analytics Service
   cd services/analytics-service
   mvn spring-boot:run
   
   # 8. API Gateway
   cd services/api-gateway
   mvn spring-boot:run
   
   # 9. Web UI Service
   cd ../../
   mvn spring-boot:run
   ```

### Kubernetes Deployment

1. **Apply Kubernetes configurations**:
   ```bash
   kubectl apply -f k8s/
   ```

2. **Check service status**:
   ```bash
   kubectl get pods -n xypay-banking
   kubectl get services -n xypay-banking
   ```

## API Endpoints

### Customer Service
- `GET /api/customers` - List all customers
- `POST /api/customers` - Create customer
- `GET /api/customers/{id}` - Get customer by ID
- `PUT /api/customers/{id}` - Update customer
- `POST /api/customers/{id}/kyc` - Create KYC profile
- `GET /api/customers/{id}/kyc` - Get KYC profile

### Account Service
- `GET /api/accounts` - List all accounts
- `POST /api/accounts` - Create account
- `GET /api/accounts/{accountNumber}` - Get account by number
- `POST /api/accounts/{id}/debit` - Debit account
- `POST /api/accounts/{id}/credit` - Credit account

### Transaction Service
- `GET /api/transactions` - List all transactions
- `POST /api/transactions` - Create transaction
- `GET /api/transactions/{id}` - Get transaction by ID
- `PUT /api/transactions/{id}/status` - Update transaction status

## Monitoring and Observability

### Health Checks
- Each service exposes health endpoints at `/actuator/health`
- Eureka dashboard at `http://localhost:8761`

### Metrics
- Prometheus metrics at `/actuator/prometheus`
- Grafana dashboards for visualization

### Logging
- Centralized logging with ELK stack (optional)
- Structured logging with correlation IDs

## Security

### Authentication
- JWT tokens for service-to-service communication
- OAuth2 for external API access

### Authorization
- Role-based access control (RBAC)
- Service-level permissions

### Data Protection
- Encryption at rest and in transit
- PCI DSS compliance for financial data

## Benefits of Microservices Architecture

1. **Scalability**: Each service can be scaled independently
2. **Fault Isolation**: Failure in one service doesn't affect others
3. **Technology Diversity**: Each service can use different technologies
4. **Team Autonomy**: Different teams can work on different services
5. **Deployment Independence**: Services can be deployed independently
6. **Maintainability**: Smaller, focused codebases are easier to maintain

## Migration Strategy

1. **Phase 1**: Extract Customer Service ✅
2. **Phase 2**: Extract Account Service ✅
3. **Phase 3**: Extract Transaction Service ✅
4. **Phase 4**: Extract Notification Service (Pending)
5. **Phase 5**: Refactor Main App to Web UI Service (Pending)
6. **Phase 6**: Add API Gateway and Service Discovery ✅
7. **Phase 7**: Update Kubernetes Configurations (Pending)

## Next Steps

1. Complete Notification Service extraction
2. Refactor main application to Web UI Service
3. Update Kubernetes configurations
4. Implement comprehensive monitoring
5. Add API documentation with Swagger
6. Implement circuit breakers and retry mechanisms
7. Add comprehensive testing suite
