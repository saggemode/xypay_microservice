# XyPay Microservice Migration Guide

## Overview
This guide outlines the step-by-step process to migrate XyPay from a monolithic architecture to a microservices architecture.

## Current Status
✅ **Completed:**
- Eureka Server (Service Discovery)
- API Gateway (Routing & Load Balancing)
- Customer Service (Basic structure)
- Shared Libraries (Common entities)
- Docker Compose configuration
- Migration scripts

🔄 **In Progress:**
- Customer Service (Full implementation)

⏳ **Pending:**
- Account Service
- Transaction Service
- Notification Service
- Treasury Service
- Analytics Service
- Tenant Service

## Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Mobile App    │    │   Admin Panel   │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────▼─────────────┐
                    │      API Gateway          │
                    │    (Port 8080)            │
                    └─────────────┬─────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
┌───────▼────────┐    ┌──────────▼──────────┐    ┌────────▼────────┐
│ Customer Svc   │    │   Account Svc       │    │ Transaction Svc │
│ (Port 8082)    │    │   (Port 8083)       │    │   (Port 8084)   │
└────────────────┘    └─────────────────────┘    └─────────────────┘
        │                         │                         │
        └─────────────────────────┼─────────────────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
┌───────▼────────┐    ┌──────────▼──────────┐    ┌────────▼────────┐
│Notification Svc│    │   Treasury Svc      │    │ Analytics Svc   │
│ (Port 8086)    │    │   (Port 8085)       │    │   (Port 8087)   │
└────────────────┘    └─────────────────────┘    └─────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Eureka Server  │    │   PostgreSQL    │    │     Redis       │
│  (Port 8761)    │    │   (Port 5432)   │    │   (Port 6379)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Service Responsibilities

### 1. Eureka Server (Port 8761)
- **Purpose**: Service discovery and registration
- **Technology**: Spring Cloud Netflix Eureka
- **Dependencies**: None

### 2. API Gateway (Port 8080)
- **Purpose**: Single entry point, routing, load balancing, CORS
- **Technology**: Spring Cloud Gateway
- **Dependencies**: Eureka Server, Redis

### 3. Customer Service (Port 8082)
- **Purpose**: Customer management, KYC, user profiles
- **Controllers**: CustomerController, KYCController, UserProfileController
- **Services**: CustomerService, KYCService, UserRegistrationService
- **Dependencies**: Eureka Server, PostgreSQL, Kafka

### 4. Account Service (Port 8083)
- **Purpose**: Account management, balances, account types
- **Controllers**: AccountController, BalanceController
- **Services**: AccountService, BankAccountService, BalanceService
- **Dependencies**: Eureka Server, PostgreSQL, Redis, Kafka

### 5. Transaction Service (Port 8084)
- **Purpose**: Transaction processing, transfers, payments
- **Controllers**: TransactionController, BankingRestController, BankTransferController
- **Services**: TransactionService, TransferValidationService, PaymentService
- **Dependencies**: Eureka Server, PostgreSQL, Redis, Kafka

### 6. Notification Service (Port 8086)
- **Purpose**: Notifications, alerts, messaging
- **Controllers**: NotificationController
- **Services**: NotificationService, EmailService, SMSService, PushNotificationService
- **Dependencies**: Eureka Server, PostgreSQL, Kafka

### 7. Treasury Service (Port 8085)
- **Purpose**: Treasury operations, liquidity management
- **Controllers**: TreasuryController
- **Services**: TreasuryService, LiquidityManagementService
- **Dependencies**: Eureka Server, PostgreSQL, Kafka

### 8. Analytics Service (Port 8087)
- **Purpose**: Analytics, reporting, business intelligence
- **Controllers**: AnalyticsController, ReportingController
- **Services**: AnalyticsService, ReportingService
- **Dependencies**: Eureka Server, PostgreSQL, Kafka

### 9. Tenant Service (Port 8088)
- **Purpose**: Multi-tenancy, tenant management
- **Controllers**: TenantController
- **Services**: TenantService
- **Dependencies**: Eureka Server, PostgreSQL

## Migration Steps

### Phase 1: Infrastructure Setup ✅
1. ✅ Create service directories
2. ✅ Set up Eureka Server
3. ✅ Set up API Gateway
4. ✅ Create shared libraries
5. ✅ Update Docker Compose

### Phase 2: Core Services (In Progress)
1. ✅ Customer Service (Basic structure)
2. 🔄 Complete Customer Service implementation
3. ⏳ Extract Account Service
4. ⏳ Extract Transaction Service
5. ⏳ Extract Notification Service

### Phase 3: Advanced Services
1. ⏳ Extract Treasury Service
2. ⏳ Extract Analytics Service
3. ⏳ Extract Tenant Service

### Phase 4: Integration & Testing
1. ⏳ Update API Gateway routes
2. ⏳ Test inter-service communication
3. ⏳ Update frontend integration
4. ⏳ Performance testing

## Quick Start

### 1. Start Infrastructure
```bash
# Start database and messaging infrastructure
docker-compose -f docker-compose.microservices.yml up -d postgres redis zookeeper kafka

# Wait for services to be ready
timeout 30
```

### 2. Start Core Services
```bash
# Start Eureka Server
docker-compose -f docker-compose.microservices.yml up -d eureka-server

# Wait for Eureka to be ready
timeout 15

# Start API Gateway
docker-compose -f docker-compose.microservices.yml up -d api-gateway

# Start Customer Service
docker-compose -f docker-compose.microservices.yml up -d customer-service
```

### 3. Verify Services
- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Customer Service: http://localhost:8082

## Service Extraction Process

### For Each Service:

1. **Create Service Structure**
   ```bash
   mkdir services/[service-name]/src/main/java/com/xypay/[service-name]
   mkdir services/[service-name]/src/main/resources
   ```

2. **Create POM.xml**
   - Copy from existing service
   - Update artifact ID and name
   - Add required dependencies

3. **Create Application Class**
   ```java
   @SpringBootApplication
   @EnableEurekaClient
   public class [ServiceName]Application {
       public static void main(String[] args) {
           SpringApplication.run([ServiceName]Application.class, args);
       }
   }
   ```

4. **Create application.yml**
   - Set unique port
   - Configure database connection
   - Configure Eureka client
   - Configure Kafka (if needed)

5. **Extract Controllers**
   - Move relevant controllers from monolithic app
   - Update package names
   - Update service dependencies

6. **Extract Services**
   - Move relevant service classes
   - Update package names
   - Remove unused dependencies

7. **Extract Entities**
   - Move relevant domain classes
   - Update package names
   - Ensure JPA annotations are correct

8. **Create Dockerfile**
   ```dockerfile
   FROM openjdk:17-jdk-slim
   WORKDIR /app
   COPY target/[service-name]-0.0.1-SNAPSHOT.jar app.jar
   EXPOSE [port]
   ENTRYPOINT ["java", "-jar", "app.jar"]
   ```

9. **Test Service**
   - Build with Maven
   - Test locally
   - Deploy with Docker

## API Gateway Configuration

The API Gateway routes requests to appropriate services:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: customer-service
          uri: lb://customer-service
          predicates:
            - Path=/api/v1/customers/**
          filters:
            - StripPrefix=2
        - id: account-service
          uri: lb://account-service
          predicates:
            - Path=/api/v1/accounts/**
          filters:
            - StripPrefix=2
        # ... more routes
```

## Inter-Service Communication

### 1. Synchronous Communication
- Use OpenFeign for REST API calls
- Configure in each service's POM.xml
- Create Feign clients for each service

### 2. Asynchronous Communication
- Use Apache Kafka for event-driven communication
- Publish events when data changes
- Subscribe to relevant events

### 3. Service Discovery
- All services register with Eureka
- Use service names instead of IP addresses
- Load balancing handled by Ribbon

## Database Strategy

### Current Approach
- All services share the same PostgreSQL database
- Each service has its own schema or table prefix
- Shared entities in common-entities library

### Future Improvements
- Database per service (when needed)
- Event sourcing for critical entities
- CQRS pattern for read/write separation

## Monitoring & Observability

### Health Checks
- Spring Boot Actuator in each service
- Health endpoints: `/actuator/health`
- Custom health indicators

### Logging
- Structured logging with JSON format
- Correlation IDs for request tracing
- Centralized log aggregation

### Metrics
- Micrometer for application metrics
- Prometheus for metrics collection
- Grafana for visualization

## Security Considerations

### Service-to-Service Security
- JWT tokens for authentication
- Service mesh for mTLS (future)
- API Gateway as security boundary

### Data Security
- Encryption at rest
- Encryption in transit
- PII data handling compliance

## Performance Considerations

### Caching
- Redis for distributed caching
- Cache frequently accessed data
- Cache invalidation strategies

### Database Optimization
- Connection pooling
- Read replicas for read-heavy operations
- Database indexing

### Load Balancing
- API Gateway load balancing
- Service instance scaling
- Circuit breakers for resilience

## Troubleshooting

### Common Issues

1. **Service Registration Issues**
   - Check Eureka server is running
   - Verify service configuration
   - Check network connectivity

2. **Database Connection Issues**
   - Verify PostgreSQL is running
   - Check connection string
   - Verify credentials

3. **Kafka Connection Issues**
   - Check Kafka is running
   - Verify bootstrap servers
   - Check topic configuration

### Debug Commands

```bash
# Check service status
docker-compose -f docker-compose.microservices.yml ps

# View service logs
docker-compose -f docker-compose.microservices.yml logs -f [service-name]

# Check Eureka registrations
curl http://localhost:8761/eureka/apps

# Test API Gateway
curl http://localhost:8080/api/v1/customers/health
```

## Next Steps

1. **Complete Customer Service** - Extract all customer-related functionality
2. **Extract Account Service** - Move account management features
3. **Extract Transaction Service** - Move transaction processing
4. **Extract Notification Service** - Move notification features
5. **Update Frontend** - Modify frontend to use new API endpoints
6. **Performance Testing** - Load test the microservices
7. **Production Deployment** - Deploy to production environment

## Resources

- [Spring Cloud Documentation](https://spring.io/projects/spring-cloud)
- [Eureka Documentation](https://github.com/Netflix/eureka)
- [Spring Cloud Gateway Documentation](https://spring.io/projects/spring-cloud-gateway)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
