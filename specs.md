# EVCharging Microservice Specifications

## 1. Database Schema

### ChargingStation
```sql
CREATE TABLE charging_station (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL, -- AVAILABLE, OCCUPIED, MAINTENANCE
    power_output DECIMAL(10,2) NOT NULL, -- in kW
    connector_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 2. API Endpoints

### 2.1 GET /stations
- **Description**: Retrieve all charging stations
- **Query Parameters**:
  - `status` (optional): Filter by station status
  - `page` (optional, default=0): Page number
  - `size` (optional, default=10): Page size
- **Response**:
  ```json
  {
    "content": [
      {
        "id": 1,
        "name": "Station A1",
        "location": "123 Main St",
        "status": "AVAILABLE",
        "powerOutput": 150.00,
        "connectorType": "CCS2",
        "createdAt": "2024-03-15T10:00:00Z",
        "updatedAt": "2024-03-15T10:00:00Z"
      }
    ],
    "totalElements": 100,
    "totalPages": 10,
    "currentPage": 0
  }
  ```

### 2.2 GET /stations/{id}
- **Description**: Retrieve a specific charging station
- **Path Parameters**:
  - `id`: Station ID
- **Response**:
  ```json
  {
    "id": 1,
    "name": "Station A1",
    "location": "123 Main St",
    "status": "AVAILABLE",
    "powerOutput": 150.00,
    "connectorType": "CCS2",
    "createdAt": "2024-03-15T10:00:00Z",
    "updatedAt": "2024-03-15T10:00:00Z"
  }
  ```

### 2.3 POST /stations
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

## 3. Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── chargesquare/
│   │           ├── controller/
│   │           │   └── ChargingStationController.java
│   │           ├── model/
│   │           │   └── ChargingStation.java
│   │           ├── repository/
│   │           │   └── ChargingStationRepository.java
│   │           ├── service/
│   │           │   └── ChargingStationService.java
│   │           └── ChargingStationApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/
            └── chargesquare/
                └── controller/
                    └── ChargingStationControllerTest.java

```

## 4. Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <!-- PostgreSQL Driver -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## 5. Configuration

### application.properties
```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/evcharging
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Logging
logging.level.com.chargesquare=DEBUG
``` 