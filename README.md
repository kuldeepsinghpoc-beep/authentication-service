# Spring Boot Authentication Service

A comprehensive, enterprise-grade JWT-based authentication service built with Spring Boot 3.x and Java 17. This RESTful API provides secure user authentication, authorization, and user management capabilities with modern security practices.

## 🚀 Features

- **JWT Authentication**: Stateless authentication using JSON Web Tokens
- **User Management**: Complete user registration, login, and profile management
- **Security**: BCrypt password hashing, JWT token validation, and refresh mechanisms
- **Database Support**: H2 (development) and MySQL (production) compatibility
- **API Documentation**: Interactive Swagger/OpenAPI 3.0 documentation
- **Comprehensive Testing**: Unit, integration, and API testing with detailed coverage
- **Production Ready**: Security configurations, error handling, and monitoring endpoints

## 🛠 Technology Stack

- **Java 17** - Modern LTS Java version
- **Spring Boot 3.1.5** - Latest Spring Boot framework
- **Spring Security 6.x** - Advanced security configurations
- **JWT (JJWT 0.11.5)** - JSON Web Token implementation
- **Spring Data JPA** - Data persistence layer
- **H2/MySQL** - Database support
- **Maven** - Dependency management and build tool
- **SpringDoc OpenAPI** - API documentation
- **JUnit 5** - Testing framework

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Git
- IDE (IntelliJ IDEA, Eclipse, or VS Code recommended)

## 🚦 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/authentication-service.git
cd authentication-service
```

### 2. Build the Project

```bash
mvn clean compile
```

### 3. Run the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Access API Documentation

Navigate to `http://localhost:8080/swagger-ui.html` for interactive API documentation.

## 🔧 Configuration

### Application Properties

The application uses different configurations for different environments:

- `application.properties` - Default configuration
- `application-test.properties` - Test environment configuration

### Database Configuration

**Development (H2):**
```properties
spring.datasource.url=jdbc:h2:mem:authdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

**Production (MySQL):**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/auth_service
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
```

### JWT Configuration

```properties
jwt.secret=your-256-bit-secret
jwt.expiration=86400000
jwt.refresh-expiration=604800000
```

## 📚 API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/logout` | User logout |
| POST | `/api/auth/refresh` | Refresh JWT token |
| GET | `/api/auth/validate` | Validate JWT token |
| GET | `/api/auth/me` | Get current user profile |
| GET | `/api/auth/health` | Health check endpoint |

### Example Requests

**Register User:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Login:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123"
  }'
```

## 🧪 Testing

### Run All Tests

```bash
mvn test
```

### Run Specific Test Categories

```bash
# Unit tests only
mvn test -Dtest="*Test"

# Integration tests only
mvn test -Dtest="*IntegrationTest"
```

### API Testing

Use the provided PowerShell scripts for comprehensive API testing:

```powershell
# Basic API testing
.\security-test.ps1

# Comprehensive API testing
.\test-api.ps1 -Verbose

# Test with custom parameters
.\test-api.ps1 -BaseUrl "http://localhost:8080" -Environment "local" -Verbose
```

### Postman Collection

Import the `postman-collection.json` file into Postman for interactive API testing with pre-configured requests and automated test assertions.

## 📊 Test Coverage

The project includes comprehensive testing with multiple layers:

- **Unit Tests**: Service layer, repository layer, and utility classes
- **Integration Tests**: Full Spring context, database integration, and REST API testing
- **API Tests**: End-to-end testing with real HTTP requests

Current test coverage: **90%+** with detailed reporting.

## 🔒 Security Features

- **Password Hashing**: BCrypt with configurable strength
- **JWT Security**: RS256 or HS256 algorithms with token expiration
- **CORS Configuration**: Configurable cross-origin resource sharing
- **SQL Injection Protection**: Parameterized queries via JPA
- **Authentication Required**: Protected endpoints with proper authorization
- **Error Handling**: Secure error responses without sensitive information exposure

## 🏗 Project Structure

```
authentication-service/
├── src/
│   ├── main/
│   │   ├── java/com/wipro/ai/demo/
│   │   │   ├── AuthenticationServiceApplication.java
│   │   │   ├── config/          # Security and application configuration
│   │   │   ├── controller/      # REST API controllers
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── exception/      # Custom exception handling
│   │   │   ├── model/          # JPA entities
│   │   │   ├── repository/     # Data access layer
│   │   │   ├── security/       # Security components
│   │   │   └── service/        # Business logic layer
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/wipro/ai/demo/  # Test classes
├── postman-collection.json     # Postman API testing collection
├── test-api.ps1                # PowerShell API testing script
├── security-test.ps1           # Security validation script
├── API-Testing-Guide.md        # Comprehensive testing documentation
└── pom.xml                     # Maven configuration
```

## 🚀 Deployment

### Development Environment

```bash
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Production Environment

```bash
# Build the application
mvn clean package -DskipTests

# Run the JAR file
java -jar target/authentication-service-1.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### Docker Support (Future Enhancement)

```bash
# Build Docker image
docker build -t authentication-service .

# Run container
docker run -p 8080:8080 authentication-service
```

## 📖 Documentation

- **API Documentation**: Available at `/swagger-ui.html` when running
- **Testing Guide**: See `API-Testing-Guide.md` for comprehensive testing instructions
- **Security Documentation**: See `SECURITY-FIX-DOCUMENTATION.md` for security implementation details

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java coding conventions
- Write comprehensive tests for new features
- Update documentation for API changes
- Ensure all tests pass before submitting PRs

## 🐛 Issue Reporting

Please use the GitHub Issues tab to report bugs or request features. Include:

- Clear description of the issue
- Steps to reproduce
- Expected vs. actual behavior
- Environment details (Java version, OS, etc.)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔗 Related Projects

- [Spring Boot](https://spring.io/projects/spring-boot) - Framework
- [Spring Security](https://spring.io/projects/spring-security) - Security
- [JJWT](https://github.com/jwtk/jjwt) - JWT Library

## 📞 Support

For support and questions:

- Create an issue in this repository
- Review the API Testing Guide for common solutions
- Check the Swagger documentation for API details

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- JWT.io for JWT resources and tools
- The Spring Security team for comprehensive security features

---

**Built with ❤️ using Spring Boot and modern Java practices**