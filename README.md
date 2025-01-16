# Charging Station Service

A reactive microservice for managing EV charging station data, featuring real-time data integration with Open Charge Map API and event-driven architecture using Apache Kafka.

## Features

- Real-time charging station data integration with Open Charge Map API
- Event-driven architecture using Apache Kafka for data processing
- Reactive REST API with Spring WebFlux
- Redis caching for improved performance
- PostgreSQL persistence for charging station data
- API Gateway for request routing
- Automatic data synchronization with external sources

## Technical Stack

- Java 17
- Spring Boot 3.2.1
- Spring Cloud Gateway
- Spring WebFlux
- Spring Data JPA
- Spring Kafka
- PostgreSQL 15
- Redis 7
- Apache Kafka 3.6
- Maven

## Prerequisites

1. Java 17 or higher
2. PostgreSQL 15
3. Redis 7
4. Apache Kafka 3.6
5. Maven

## Setup Instructions

### 1. Database Setup
```bash
# Create PostgreSQL database
createdb evcharging

# Database will be automatically initialized on first run
```

### 2. Redis Setup
```bash
# Install Redis (Windows)
# Download from https://github.com/microsoftarchive/redis/releases
# Start Redis
redis-server
```

### 3. Kafka Setup
```bash
# Download Kafka
wget https://downloads.apache.org/kafka/3.6.1/kafka_2.13-3.6.1.tgz
tar -xzf kafka_2.13-3.6.1.tgz
cd kafka_2.13-3.6.1

# Start Zookeeper
bin/windows/zookeeper-server-start.bat config/zookeeper.properties

# Start Kafka
bin/windows/kafka-server-start.bat config/server.properties

# Create topic
bin/windows/kafka-topics.bat --create --topic charging-stations-data --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

### 4. Application Configuration
Configure the application in `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/evcharging
spring.datasource.username=postgres
spring.datasource.password=postgres

# Redis
spring.redis.host=localhost
spring.redis.port=6379

# Kafka
spring.kafka.bootstrap-servers=localhost:9092

# Open Charge Map API
opencharge.api.key=your_api_key_here
opencharge.api.base-url=https://api.openchargemap.io/v3
```

### 5. Build and Run
```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

## API Endpoints

### Charging Stations
- GET `/stations` - List all charging stations (Cached)
- GET `/stations/{id}` - Get station by ID
- POST `/stations` - Create new station

## Data Flow

1. **External Data Integration**
   - Scheduled job fetches data from Open Charge Map API
   - Data is normalized to internal model
   - Fetched data is published to Kafka topic

2. **Message Processing**
   - Kafka consumer processes messages
   - Data is validated and stored in PostgreSQL
   - Cache is automatically updated

3. **API Access**
   - REST endpoints serve data from cache/database
   - Gateway routes requests appropriately
   - Redis caching improves response times

## Monitoring

The application includes detailed logging for:
- Kafka message processing
- API Gateway operations
- Redis cache operations
- Database transactions
- External API calls

## Configuration Properties

### Kafka Settings
- Topic: charging-stations-data
- Partitions: 3
- Replication Factor: 1
- Consumer Group: charging-stations-group

### Caching
- Provider: Redis
- TTL: 5 minutes
- Null Values: Not cached

### API Integration
- Fetch Interval: 60 seconds (configurable)
- Batch Size: 100 stations
- Retry: Exponential backoff

## Error Handling

- Kafka consumer retries with backoff
- API rate limiting compliance
- Database transaction management
- Cache eviction on updates
- Comprehensive error logging

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details 