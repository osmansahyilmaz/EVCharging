# Charging Station Microservice with API Gateway and Redis Caching

A Spring Boot microservice for managing EV charging stations, featuring API Gateway routing and Redis caching for improved performance.

## Prerequisites

- Java 17 or higher (JDK, not JRE)
- Maven 3.6+
- PostgreSQL 15+
- Redis 7+

## Setup Instructions

### 1. Database Setup
```sql
CREATE DATABASE evcharging;
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

### 2. Redis Setup
1. Download Redis for Windows from: https://github.com/microsoftarchive/redis/releases
2. Extract to a location (e.g., C:\Redis)
3. Start Redis server:
   ```bash
   # In Command Prompt (as Administrator)
   cd C:\Redis
   redis-server.exe
   ```

### 3. Configuration
Configure database and Redis connection in `src/main/resources/application.properties`:
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/evcharging
spring.datasource.username=postgres
spring.datasource.password=postgres

# Redis
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.timeout=2000

# Cache
spring.cache.type=redis
spring.cache.redis.time-to-live=300000
```

### 4. Build and Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The service will be available at `http://localhost:8080`

## Architecture

The service implements:
1. Spring Cloud Gateway for routing
2. Redis caching for frequently accessed endpoints (5-minute TTL)
3. PostgreSQL for persistent storage
4. Reactive programming with WebFlux

## API Documentation

### 1. List All Stations (Redis Cached)
- **Endpoint**: GET /stations
- **Description**: Retrieves all charging stations (cached for 5 minutes)
- **Response**: Array of charging stations
- **Example Response**:
```json
[
    {
        "id": 1,
        "name": "Station Alpha",
        "location": "123 Main Street, Istanbul",
        "status": "AVAILABLE",
        "powerOutput": 150.0,
        "connectorType": "CCS2",
        "createdAt": "2024-01-16T10:00:00",
        "updatedAt": "2024-01-16T10:00:00"
    }
]
```

### 2. Get Station by ID (Reactor Cached)
- **Endpoint**: GET /stations/{id}
- **Description**: Retrieves a specific charging station (cached using Reactor)
- **Parameters**: id - Station ID
- **Response**: Single charging station or 404 if not found

### 3. Create New Station
- **Endpoint**: POST /stations
- **Description**: Creates a new charging station (invalidates Redis cache)
- **Request Body**: Charging station details
- **Headers**: Content-Type: application/json
- **Example Request**:
```json
{
    "name": "Station Alpha",
    "location": "123 Main Street, Istanbul",
    "status": "AVAILABLE",
    "powerOutput": 150.0,
    "connectorType": "CCS2"
}
```

## Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── chargesquare/
│   │           ├── ChargingStationApplication.java
│   │           ├── config/
│   │           │   ├── RedisConfig.java
│   │           │   └── GatewayConfig.java
│   │           ├── controller/
│   │           │   └── ChargingStationController.java
│   │           ├── model/
│   │           │   └── ChargingStation.java
│   │           └── repository/
│   │               └── ChargingStationRepository.java
│   └── resources/
│       └── application.properties
```

## Technologies Used

- Spring Boot 3.2.1
- Spring Cloud Gateway
- Spring WebFlux
- Spring Data Redis
- Spring Data JPA/Hibernate
- PostgreSQL 15
- Redis 7
- Maven
- Java 17

## Caching Strategy

The service implements two types of caching:
1. Redis Caching:
   - GET /stations endpoint (5 minutes TTL)
   - Automatically invalidated on new station creation

2. Reactor Caching:
   - GET /stations/{id} endpoint
   - In-memory caching using Reactor's cache() operator

## Build Output

The build process generates:
- Main JAR: target/charging-station-service-1.0.0.jar
- Original JAR: target/charging-station-service-1.0.0.jar.original

## Development

For VS Code users, basic settings are provided in `.vscode/settings.json`. 