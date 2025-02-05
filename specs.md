# EVCharging Microservice Specifications

## 1. System Architecture

### 1.1 Components
- API Gateway (Spring Cloud Gateway)
- Redis Cache Layer
- Reactor Cache Layer
- Core Service (Spring WebFlux)
- PostgreSQL Database
- Apache Kafka Message Broker
- External API Integration (Open Charge Map)

### 1.2 Caching Architecture
```properties
# Redis Cache Configuration
spring.cache.type=redis
spring.cache.redis.time-to-live=300000  # 5 minutes
spring.cache.redis.cache-null-values=false

# Redis Connection
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=2000

# Cache Keys
stations::all          # For all stations list
reactor::cache        # For individual station cache
```

## 2. Database Schema

### ChargingStation
```sql
CREATE TABLE charging_stations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    power_output DECIMAL(10,2) NOT NULL,
    connector_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 3. API Gateway Configuration

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: charging_stations_route
          uri: http://localhost:8080
          predicates:
            - Path=/stations/**
```

## 4. API Endpoints

### 4.1 GET /stations (Redis Cached)
- **Cache Implementation**: Redis
- **Cache Key**: `stations::all`
- **Cache TTL**: 5 minutes
- **Description**: Retrieve all charging stations
- **Response**: List of charging stations
- **Example Response**:
```json
[
  {
    "id": 1,
    "name": "Station A1",
    "location": "123 Main St",
    "status": "AVAILABLE",
    "powerOutput": 150.00,
    "connectorType": "CCS2",
    "createdAt": "2024-01-16T10:00:00Z",
    "updatedAt": "2024-01-16T10:00:00Z"
  }
]
```

### 4.2 GET /stations/{id} (Reactor Cached)
- **Cache Implementation**: Reactor cache() operator
- **Description**: Retrieve a specific charging station
- **Path Parameters**:
  - `id`: Station ID
- **Response**: Single charging station or 404 if not found
- **Example Response**:
```json
{
  "id": 1,
  "name": "Station A1",
  "location": "123 Main St",
  "status": "AVAILABLE",
  "powerOutput": 150.00,
  "connectorType": "CCS2",
  "createdAt": "2024-01-16T10:00:00Z",
  "updatedAt": "2024-01-16T10:00:00Z"
}
```

### 4.3 POST /stations (Cache Invalidating)
- **Cache Impact**: Invalidates Redis cache entries
- **Description**: Create a new charging station
- **Request Body**:
```json
{
  "name": "Station A1",
  "location": "123 Main St",
  "status": "AVAILABLE",
  "powerOutput": 150.00,
  "connectorType": "CCS2"
}
```
- **Response**: Created station object with status 201

## 5. External API Integration

### 5.1 Open Charge Map API
- **Base URL**: https://api.openchargemap.io/v3
- **Authentication**: API Key in header
- **Endpoints Used**:
  - GET /poi - Retrieve Points of Interest (charging stations)
- **Data Mapping**:
```java
// OpenChargeMap to ChargingStation mapping
{
    "ID": "id",
    "AddressInfo.Title": "name",
    "AddressInfo.AddressLine1": "location",
    "StatusType.IsOperational": "status",
    "Connections[0].PowerKW": "powerOutput",
    "Connections[0].ConnectionType.Title": "connectorType"
}
```

### 5.2 Scheduled Data Fetch
- Frequency: Every hour
- Fetch Limit: 100 stations per request
- Error Handling: Exponential backoff retry

## 6. Kafka Configuration

### 6.1 Topics
```properties
# Main topic for charging station data
Name: charging-stations-data
Partitions: 3
Replication Factor: 1
Retention: 7 days
```

### 6.2 Producer Configuration
```properties
spring.kafka.producer.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.producer.acks=all
```

### 6.3 Consumer Configuration
```properties
spring.kafka.consumer.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=charging-stations-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
```

### 6.4 Message Format
```json
{
    "messageId": "uuid",
    "timestamp": "ISO-8601",
    "source": "OPEN_CHARGE_MAP",
    "operation": "CREATE|UPDATE",
    "payload": {
        "id": "string",
        "name": "string",
        "location": "string",
        "status": "string",
        "powerOutput": "number",
        "connectorType": "string"
    }
}
```

## 7. Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── chargesquare/
│   │           ├── ChargingStationApplication.java
│   │           ├── config/
│   │           │   ├── RedisConfig.java
│   │           │   ├── GatewayConfig.java
│   │           │   └── KafkaConfig.java
│   │           ├── controller/
│   │           │   └── ChargingStationController.java
│   │           ├── model/
│   │           │   ├── ChargingStation.java
│   │           │   └── ChargingStationMessage.java
│   │           ├── service/
│   │           │   ├── OpenChargeMapService.java
│   │           │   └── KafkaMessageService.java
│   │           └── repository/
│   │               └── ChargingStationRepository.java
│   └── resources/
│       └── application.properties
```

## 8. Dependencies

```xml
<properties>
    <java.version>17</java.version>
    <spring-cloud.version>2023.0.0</spring-cloud.version>
</properties>

<dependencies>
    <!-- Spring WebFlux -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    
    <!-- Spring Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- Spring Cloud Gateway -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    
    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- Spring Cache -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    
    <!-- Jackson JSR310 -->
    <dependency>
        <groupId>com.fasterxml.jackson.datatype</groupId>
        <artifactId>jackson-datatype-jsr310</artifactId>
    </dependency>

    <!-- Kafka -->
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>

    <!-- HTTP Client -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
</dependencies>
```

## 9. Configuration

### 9.1 application.properties
```properties
# Server Configuration
server.port=8080
spring.main.web-application-type=reactive
spring.main.allow-bean-definition-overriding=true

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/evcharging
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.open-in-view=false

# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=2000

# API Gateway Configuration
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.discovery.locator.lower-case-service-id=true
spring.cloud.gateway.routes[0].id=charging_stations_route
spring.cloud.gateway.routes[0].uri=http://localhost:8080
spring.cloud.gateway.routes[0].predicates[0]=Path=/stations/**

# Caching Configuration
spring.cache.type=redis
spring.cache.redis.time-to-live=300000
spring.cache.redis.cache-null-values=false

# Logging
logging.level.com.chargesquare=DEBUG
logging.level.org.springframework.cloud.gateway=DEBUG
logging.level.org.springframework.data.redis=DEBUG
logging.level.org.springframework.orm.jpa=DEBUG
logging.level.org.hibernate.SQL=DEBUG

# Kafka Configuration
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.producer.acks=all
spring.kafka.consumer.group-id=charging-stations-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer

# External API Configuration
opencharge.api.key=${OPENCHARGE_API_KEY}
opencharge.api.base-url=https://api.openchargemap.io/v3
opencharge.api.fetch-interval=3600000
``` 