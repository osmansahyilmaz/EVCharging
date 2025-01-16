# Charging Station Microservice

A Spring Boot microservice for managing EV charging stations. This service provides RESTful APIs for creating and retrieving charging station information.

## Prerequisites

- Java 17 or higher (JDK, not JRE)
- Maven 3.6+
- PostgreSQL 15+

## Setup Instructions

### 1. Database Setup
```sql
CREATE DATABASE evcharging;
```

### 2. Configuration
Configure database connection in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/evcharging
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### 3. Build and Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The service will be available at `http://localhost:8080`

## API Documentation

### 1. List All Stations
- **Endpoint**: GET /stations
- **Description**: Retrieves all charging stations
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

### 2. Get Station by ID
- **Endpoint**: GET /stations/{id}
- **Description**: Retrieves a specific charging station
- **Parameters**: id - Station ID
- **Response**: Single charging station or 404 if not found
- **Example**: `GET /stations/1`

### 3. Create New Station
- **Endpoint**: POST /stations
- **Description**: Creates a new charging station
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

## Testing with Postman

1. **List All Stations**:
   - Method: GET
   - URL: `http://localhost:8080/stations`

2. **Get Station by ID**:
   - Method: GET
   - URL: `http://localhost:8080/stations/1`

3. **Create New Station**:
   - Method: POST
   - URL: `http://localhost:8080/stations`
   - Headers: Content-Type: application/json
   - Body: Use the example request JSON above

## Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── chargesquare/
│   │           ├── ChargingStationApplication.java
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
- Spring Data JPA/Hibernate
- PostgreSQL 15
- Maven
- Java 17

## Data Model

The charging station entity includes:
- `id`: Unique identifier (auto-generated)
- `name`: Station name
- `location`: Physical location
- `status`: AVAILABLE, OCCUPIED, or MAINTENANCE
- `powerOutput`: Charging power in kW
- `connectorType`: Type of charging connector
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp 