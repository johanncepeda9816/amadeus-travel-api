# Amadeus Travel API

A comprehensive REST API for flight search and management built with Spring Boot. This application provides endpoints for searching flights, user authentication, and administrative flight management.

## 🚀 Features

- **Flight Search**: Search for flights by origin, destination, and departure date
- **User Authentication**: JWT-based authentication with role-based access control
- **Flight Management**: Complete CRUD operations for flights (Admin only)
- **Location Services**: Get available origins and destinations
- **Multi-Environment Support**: Development (H2) and Production (PostgreSQL) profiles
- **API Documentation**: Interactive Swagger UI documentation
- **Security**: Spring Security with JWT tokens and CORS support

## 🛠 Technology Stack

- **Java 17**
- **Spring Boot 3.5.5**
- **Spring Security 6**
- **Spring Data JPA**
- **JWT (JSON Web Tokens)**
- **H2 Database** (Development)
- **PostgreSQL** (Production)
- **Maven** (Dependency Management)
- **Lombok** (Code Generation)
- **Swagger/OpenAPI 3** (API Documentation)

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL (for production environment)

## 🚀 Quick Start

### 1. Clone the repository

```bash
git clone <repository-url>
cd amadeus-travel-api
```

### 2. Run the application (Development)

```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using Maven directly
mvn spring-boot:run
```

The application will start on `http://localhost:8080` with the context path `/api`.

### 3. Access the application

- **API Base URL**: `http://localhost:8080/api`
- **Swagger UI**: `http://localhost:8080/api/swagger-ui.html`
- **H2 Console**: `http://localhost:8080/api/h2-console`
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (empty)

## 🔐 Authentication

The API uses JWT-based authentication. Two test users are automatically seeded:

### Admin User

- **Email**: `admin@amadeus.com`
- **Password**: `password123`
- **Role**: `ADMIN`
- **Permissions**: Full access to all endpoints including flight management

### Regular User

- **Email**: `user@amadeus.com`
- **Password**: `password123`
- **Role**: `USER`
- **Permissions**: Access to flight search and personal endpoints

### Login Process

1. Send POST request to `/api/auth/login` with credentials
2. Receive JWT token in response
3. Include token in `Authorization` header as `Bearer <token>` for protected endpoints

## 📚 API Endpoints

### Authentication

| Method | Endpoint       | Description           | Auth Required |
| ------ | -------------- | --------------------- | ------------- |
| POST   | `/auth/login`  | User login            | No            |
| POST   | `/auth/logout` | User logout           | Yes           |
| GET    | `/auth/me`     | Get current user info | Yes           |

### Flight Search (Public)

| Method | Endpoint                          | Description                | Auth Required |
| ------ | --------------------------------- | -------------------------- | ------------- |
| POST   | `/flights/search`                 | Search flights             | No            |
| GET    | `/flights/locations`              | Get all locations          | No            |
| GET    | `/flights/locations/origins`      | Get available origins      | No            |
| GET    | `/flights/locations/destinations` | Get available destinations | No            |
| GET    | `/flights/upcoming`               | Get upcoming flights       | No            |

### Flight Management (Admin Only)

| Method | Endpoint                | Description                 | Auth Required |
| ------ | ----------------------- | --------------------------- | ------------- |
| POST   | `/flights/admin`        | Create flight               | Yes (Admin)   |
| GET    | `/flights/admin/{id}`   | Get flight by ID            | Yes (Admin)   |
| GET    | `/flights/admin`        | Get all flights (paginated) | Yes (Admin)   |
| GET    | `/flights/search/admin` | Advanced flight search      | Yes (Admin)   |
| PUT    | `/flights/admin/{id}`   | Update flight               | Yes (Admin)   |
| DELETE | `/flights/admin/{id}`   | Delete flight               | Yes (Admin)   |

## 🔍 Usage Examples

### Search Flights

```bash
curl -X POST "http://localhost:8080/api/flights/search" \
  -H "Content-Type: application/json" \
  -d '{
    "origin": "BOGOTA",
    "destination": "MEDELLIN",
    "departureDate": "2024-12-25",
    "passengers": 1,
    "cabinClass": "Economy"
  }'
```

### Login

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@amadeus.com",
    "password": "password123"
  }'
```

### Create Flight (Admin)

```bash
curl -X POST "http://localhost:8080/api/flights/admin" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{
    "flightNumber": "AV1234",
    "airline": "Avianca",
    "origin": "BOGOTA",
    "destination": "MIAMI",
    "departureTime": "2024-12-25T10:00:00",
    "arrivalTime": "2024-12-25T15:00:00",
    "duration": "5h 0m",
    "price": 1500000,
    "aircraftType": "Boeing 787",
    "availableSeats": 200,
    "cabinClass": "Economy"
  }'
```

## 🏗 Architecture

### Project Structure

```
src/main/java/com/amadeus/api/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/            # Data Transfer Objects
│   ├── request/    # Request DTOs
│   └── response/   # Response DTOs
├── entity/         # JPA entities
├── exception/      # Custom exceptions
├── repository/     # Data repositories
├── security/       # Security configuration
├── service/        # Business logic
│   └── impl/       # Service implementations
└── util/           # Utility classes
```

### Key Components

- **SecurityConfig**: JWT authentication and authorization
- **DataSeeder**: Automatic data seeding for development
- **FlightController**: Flight search and management endpoints
- **AuthController**: Authentication endpoints
- **JwtTokenProvider**: JWT token generation and validation
- **GlobalExceptionHandler**: Centralized exception handling

## 🌍 Environment Configuration

### Development Profile (default)

- **Database**: H2 in-memory
- **Port**: 8080
- **Logging**: DEBUG level
- **H2 Console**: Enabled

## 📊 Sample Data

The application automatically seeds the database with:

- **Users**: Admin and regular user accounts
- **Flights**: Over 1000+ flights with:
  - Domestic Colombian routes
  - International routes from major Colombian cities
  - Multiple airlines and aircraft types
  - Realistic pricing and schedules

### Available Locations

**Colombian Cities**: Bogotá, Medellín, Cali, Cartagena, Barranquilla, Bucaramanga, Pereira, Santa Marta, Manizales, Villavicencio

**International Cities**: Madrid, Paris, London, Miami, New York, Mexico City, Lima, Quito

## 🧪 Testing

### Run Tests

```bash
./mvnw test
```

### Manual Testing

Use the Swagger UI at `http://localhost:8080/api/swagger-ui.html` for interactive API testing.

## 📝 API Response Format

All API responses follow a consistent format:

### Success Response

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Error Response

```json
{
  "success": false,
  "message": "Error description",
  "error": "ERROR_CODE",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## 🔧 Configuration

### JWT Configuration

```yaml
jwt:
  secret: amadeus-travel-api-secret-key-2024-jwt-token-generation
  expiration: 86400000 # 24 hours
  refresh-expiration: 604800000 # 7 days
```

### CORS Configuration

```yaml
cors:
  allowed-origins: http://localhost:3000,http://localhost:3001,http://localhost:5173,http://localhost:5174
  allowed-methods: GET,POST,PUT,DELETE,OPTIONS
  allowed-headers: "*"
  allow-credentials: true
```
