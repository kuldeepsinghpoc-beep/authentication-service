# Authentication Service API Testing Guide

## Overview

This document provides comprehensive guidelines for testing the Spring Boot Authentication Service API. It covers unit testing, integration testing, manual API testing, and automated testing strategies.

## Table of Contents

1. [Testing Architecture](#testing-architecture)
2. [Unit Testing](#unit-testing)
3. [Integration Testing](#integration-testing)
4. [API Testing with Postman](#api-testing-with-postman)
5. [Automated Testing Scripts](#automated-testing-scripts)
6. [Test Data Management](#test-data-management)
7. [Continuous Integration](#continuous-integration)
8. [Troubleshooting](#troubleshooting)

## Testing Architecture

### Test Pyramid Structure

```
    /\
   /  \     E2E Tests (Few)
  /____\    
 /      \   Integration Tests (Some)  
/________\  Unit Tests (Many)
```

### Test Categories

1. **Unit Tests**: Test individual components in isolation
2. **Integration Tests**: Test component interactions with real dependencies
3. **API Tests**: Test REST endpoints end-to-end
4. **Contract Tests**: Verify API contracts and schemas

## Unit Testing

### Framework Stack

- **JUnit 5**: Test framework with Jupiter engine
- **Mockito**: Mocking framework for dependencies
- **AssertJ**: Fluent assertions library
- **Spring Boot Test**: Spring testing support

### Running Unit Tests

```bash
# Run all unit tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=AuthServiceTest

# Run tests with coverage
./mvnw test jacoco:report
```

### Test Structure

#### AuthServiceTest.java
```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @InjectMocks
    private AuthService authService;
    
    @Nested
    @DisplayName("User Registration Tests")
    class UserRegistrationTests {
        // Registration test methods
    }
    
    @Nested
    @DisplayName("User Authentication Tests")
    class UserAuthenticationTests {
        // Authentication test methods
    }
}
```

### Best Practices

1. **Test Naming**: Use descriptive names that explain the scenario
2. **Arrange-Act-Assert**: Structure tests clearly
3. **Mock Dependencies**: Isolate the unit under test
4. **Test Edge Cases**: Include boundary conditions and error scenarios
5. **Use Test Data Builders**: Centralize test data creation

## Integration Testing

### Database Integration Tests

#### UserRepositoryTest.java
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldSaveAndFindUser() {
        // Test implementation
    }
}
```

### REST Controller Integration Tests

#### AuthControllerIntegrationTest.java
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @LocalServerPort
    private int port;
    
    @Test
    void shouldRegisterUser() {
        // Test implementation
    }
}
```

### Running Integration Tests

```bash
# Run integration tests
./mvnw test -Dtest="*IntegrationTest"

# Run with test profile
./mvnw test -Dspring.profiles.active=test
```

## API Testing with Postman

### Collection Structure

The `postman-collection.json` includes:

1. **Health Check**: Service availability tests
2. **User Registration**: Registration workflow tests
3. **User Authentication**: Login and token tests
4. **Token Management**: Validation and refresh tests
5. **Protected Endpoints**: Authenticated endpoint tests
6. **Error Scenarios**: Error handling tests

### Environment Setup

Create a Postman environment with:

```json
{
  "base_url": "http://localhost:8080",
  "access_token": "",
  "refresh_token": "",
  "test_username": "",
  "test_email": ""
}
```

### Running Collection

1. **Import Collection**: Import `postman-collection.json`
2. **Set Environment**: Configure base URL and environment variables
3. **Run Collection**: Execute entire collection or specific folders
4. **Review Results**: Check test results and response validations

### Test Scenarios

#### Registration Flow
```javascript
// Pre-request script
pm.globals.set('random_username', 'user_' + Math.random().toString(36).substr(2, 9));
pm.globals.set('random_email', 'test_' + Math.random().toString(36).substr(2, 9) + '@example.com');

// Test script
pm.test('Registration successful', function () {
    pm.response.to.have.status(201);
    const responseJson = pm.response.json();
    pm.expect(responseJson.data).to.have.property('username');
    pm.environment.set('test_username', responseJson.data.username);
});
```

#### Authentication Flow
```javascript
// Test script
pm.test('Login successful', function () {
    pm.response.to.have.status(200);
    const responseJson = pm.response.json();
    pm.expect(responseJson.data).to.have.property('accessToken');
    pm.environment.set('access_token', responseJson.data.accessToken);
});
```

## Automated Testing Scripts

### PowerShell Script (test-api.ps1)

#### Features
- Comprehensive API testing
- Colored output for better visibility
- Detailed error reporting
- Test result summaries
- JSON report generation

#### Usage
```powershell
# Basic usage
.\test-api.ps1

# With custom parameters
.\test-api.ps1 -BaseUrl "http://localhost:8080" -Environment "local" -Verbose

# Stop on first error
.\test-api.ps1 -StopOnError
```

#### Sample Output
```
=============================================================
 Authentication Service API Test Suite
=============================================================
Base URL: http://localhost:8080
Environment: local
Test User: testuser_1234
Test Email: test_1234@example.com

=============================================================
 Health Check Tests
=============================================================
✓ Health endpoint accessible
✓ Health status contains service message

=============================================================
 Test Summary
=============================================================
Total Tests: 15
Passed: 15
Failed: 0
Success Rate: 100%

Detailed report saved to: api-test-report-20241201-143022.json
```

### Batch Script (test-api.bat)

#### Features
- Cross-platform compatibility
- PowerShell detection and preference
- Fallback to curl-based testing
- Basic error handling

#### Usage
```cmd
REM Basic usage
test-api.bat

REM With parameters
test-api.bat http://localhost:8080 local --verbose

REM With stop on error
test-api.bat http://localhost:8080 local --stop-on-error
```

## Test Data Management

### TestDataConfiguration.java

Centralized test data factory providing:

```java
@TestConfiguration
public class TestDataConfiguration {
    
    public User createTestUser() {
        return User.builder()
            .username("testuser")
            .email("test@example.com")
            .password("encoded_password")
            .firstName("Test")
            .lastName("User")
            .active(true)
            .build();
    }
    
    public RegisterRequest createRegisterRequest() {
        // Implementation
    }
    
    public String generateValidJwtToken() {
        // Implementation
    }
}
```

### Test Data Patterns

1. **Builder Pattern**: For complex object creation
2. **Factory Methods**: For common test scenarios
3. **Test Fixtures**: For consistent test data
4. **Random Data**: For unique test instances

## Continuous Integration

### Maven Configuration

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>repackage</goal>
            </goals>
        </execution>
    </executions>
</plugin>

<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.7</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### GitHub Actions Workflow

```yaml
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Run tests
      run: ./mvnw clean test
    
    - name: Generate test report
      run: ./mvnw jacoco:report
    
    - name: Upload coverage reports
      uses: codecov/codecov-action@v3
```

## Troubleshooting

### Common Issues

#### 1. Test Database Connection Issues
```
Error: Unable to connect to H2 database

Solution:
- Check application-test.properties configuration
- Verify H2 dependency in pom.xml
- Ensure test profile is active
```

#### 2. JWT Token Validation Failures
```
Error: JWT signature does not match locally computed signature

Solution:
- Verify JWT secret in test configuration
- Check token expiration settings
- Ensure consistent secret between components
```

#### 3. Mock Configuration Issues
```
Error: Null pointer exception in mocked service

Solution:
- Verify @Mock and @InjectMocks annotations
- Check mock method stubbing
- Ensure proper test context setup
```

#### 4. Port Conflicts in Integration Tests
```
Error: Port 8080 already in use

Solution:
- Use @SpringBootTest(webEnvironment = RANDOM_PORT)
- Check for running Spring Boot applications
- Configure different test ports
```

### Debugging Tips

1. **Enable Debug Logging**: Set logging level to DEBUG in test properties
2. **Use @Sql for Data Setup**: Load test data with SQL scripts
3. **Profile-Specific Configuration**: Use different configs for test profiles
4. **Test Slices**: Use @WebMvcTest, @DataJpaTest for focused testing

### Performance Testing

#### Load Testing with JMeter

1. Create JMeter test plan
2. Configure thread groups for concurrent users
3. Add HTTP samplers for API endpoints
4. Set up assertions for response validation
5. Generate performance reports

#### Example Test Plan Structure
```
Test Plan
├── Thread Group (100 users, 10s ramp-up)
│   ├── HTTP Request - Register User
│   ├── HTTP Request - Login User
│   ├── HTTP Request - Get User Profile
│   └── HTTP Request - Logout User
├── Listeners
│   ├── View Results Tree
│   ├── Summary Report
│   └── Response Time Graph
```

## Security Testing

### Test Scenarios

1. **Authentication Bypass**: Attempt to access protected endpoints without tokens
2. **Token Manipulation**: Modify JWT tokens and verify rejection
3. **SQL Injection**: Test input validation on user data
4. **Cross-Site Scripting**: Verify output encoding
5. **Rate Limiting**: Test API throttling mechanisms

### Security Test Examples

```java
@Test
void shouldRejectTamperedToken() {
    String tamperedToken = validToken.substring(0, validToken.length() - 5) + "XXXXX";
    
    ResponseEntity<ApiResponse<Boolean>> response = restTemplate.exchange(
        "/api/auth/validate",
        HttpMethod.GET,
        new HttpEntity<>(createHeaders(tamperedToken)),
        new ParameterizedTypeReference<ApiResponse<Boolean>>() {}
    );
    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
}
```

## Conclusion

This comprehensive testing strategy ensures:

- **High Code Coverage**: Unit and integration tests cover critical paths
- **API Contract Validation**: Postman collections verify API behavior
- **Automated Testing**: Scripts enable continuous validation
- **Performance Assurance**: Load testing validates scalability
- **Security Validation**: Security tests protect against common vulnerabilities

Regular execution of these tests helps maintain code quality, API reliability, and system security throughout the development lifecycle.