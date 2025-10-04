# Spring Security Configuration Fix

## Issue Description

The Spring Boot application was failing to start with the following error:

```
org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration': Unsatisfied dependency expressed through method 'setFilterChains' parameter 0: Error creating bean with name 'filterChain' defined in class path resource [com/wipro/ai/demo/config/SecurityConfig.class]: Failed to instantiate [org.springframework.security.web.SecurityFilterChain]: Factory method 'filterChain' threw exception with message: This method cannot decide whether these patterns are Spring MVC patterns or not. If this endpoint is a Spring MVC endpoint, please use requestMatchers(MvcRequestMatcher); otherwise, please use requestMatchers(AntPathRequestMatcher).

This is because there is more than one mappable servlet in your servlet context: {org.h2.server.web.JakartaWebServlet=[/h2-console/*], ...
```

## Root Cause

The issue occurred because Spring Security 6.x introduced stricter pattern matching requirements when multiple servlets are present in the application context. In our case, we had both:

1. **Spring MVC DispatcherServlet** - for our REST API endpoints
2. **H2 Console Servlet** - for development database access

When Spring Security encountered multiple servlets, it couldn't automatically determine whether to use:
- `MvcRequestMatcher` (for Spring MVC endpoints)
- `AntPathRequestMatcher` (for general path matching)

## Solution Implemented

### 1. Added Required Import

```java
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
```

### 2. Updated Security Configuration

**Before (Problematic Code):**
```java
.authorizeHttpRequests(authz -> authz
    // Public endpoints - no authentication required
    .requestMatchers(
        "/api/auth/register",
        "/api/auth/login",
        "/api/auth/validate"
    ).permitAll()
    
    // Documentation and monitoring endpoints
    .requestMatchers(
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/webjars/**"
    ).permitAll()
    
    // H2 Console (development only)
    .requestMatchers("/h2-console/**").permitAll()
    
    // Actuator health endpoint
    .requestMatchers("/actuator/health").permitAll()
    
    // All other endpoints require authentication
    .anyRequest().authenticated()
)
```

**After (Fixed Code):**
```java
.authorizeHttpRequests(authz -> authz
    // Public endpoints - no authentication required
    .requestMatchers(
        new AntPathRequestMatcher("/api/auth/register"),
        new AntPathRequestMatcher("/api/auth/login"),
        new AntPathRequestMatcher("/api/auth/validate"),
        new AntPathRequestMatcher("/api/auth/health")
    ).permitAll()
    
    // Documentation and monitoring endpoints
    .requestMatchers(
        new AntPathRequestMatcher("/swagger-ui/**"),
        new AntPathRequestMatcher("/swagger-ui.html"),
        new AntPathRequestMatcher("/v3/api-docs/**"),
        new AntPathRequestMatcher("/swagger-resources/**"),
        new AntPathRequestMatcher("/webjars/**")
    ).permitAll()
    
    // H2 Console (development only)
    .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
    
    // Actuator health endpoint
    .requestMatchers(new AntPathRequestMatcher("/actuator/health")).permitAll()
    
    // All other endpoints require authentication
    .anyRequest().authenticated()
)
```

## Why This Fix Works

1. **Explicit Matcher Type**: By explicitly using `AntPathRequestMatcher`, we tell Spring Security exactly which type of pattern matching to use, eliminating ambiguity.

2. **Consistency**: All patterns now use the same matcher type, ensuring consistent behavior.

3. **Compatibility**: `AntPathRequestMatcher` works well for both API endpoints and static resources like H2 console.

## Alternative Solutions

### Option 1: Use MvcRequestMatcher (For API Endpoints Only)
```java
.requestMatchers(
    new MvcRequestMatcher(handlerMappingIntrospector, "/api/auth/register"),
    new MvcRequestMatcher(handlerMappingIntrospector, "/api/auth/login")
).permitAll()
```
**Note**: This would require injecting `HandlerMappingIntrospector` and wouldn't work for non-MVC paths like H2 console.

### Option 2: Disable H2 Console in Production
```java
// Only configure H2 console in development profiles
@Profile("!prod")
.requestMatchers("/h2-console/**").permitAll()
```

### Option 3: Use Separate Configuration Classes
Create separate security configurations for different servlet contexts.

## Testing the Fix

### 1. Compilation Test
The application should now compile without the Spring Security configuration error.

### 2. Runtime Test
```powershell
# Start the application
mvn spring-boot:run

# Test health endpoint
curl http://localhost:8080/api/auth/health

# Test registration endpoint
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123","firstName":"Test","lastName":"User"}'

# Test protected endpoint (should return 401)
curl http://localhost:8080/api/auth/me
```

### 3. Automated Testing
Use the provided PowerShell script:
```powershell
.\security-test.ps1
```

## Benefits of This Fix

1. **Application Startup**: Resolves the Spring Security configuration error that prevented application startup.

2. **Security Maintained**: All security rules remain intact and functional.

3. **H2 Console Access**: Development H2 console remains accessible.

4. **Future-Proof**: Compatible with Spring Security 6.x requirements.

5. **Clear Intent**: Explicit matcher usage makes the configuration more readable and maintainable.

## Considerations

1. **Performance**: `AntPathRequestMatcher` has slightly different performance characteristics than `MvcRequestMatcher`, but for most applications, the difference is negligible.

2. **Maintenance**: When adding new endpoints, remember to use explicit `AntPathRequestMatcher` instances.

3. **Production**: Consider disabling H2 console in production environments for security.

## Conclusion

The fix successfully resolves the Spring Security configuration ambiguity by explicitly specifying `AntPathRequestMatcher` for all path patterns. This approach maintains the intended security behavior while ensuring compatibility with Spring Security 6.x requirements when multiple servlets are present in the application context.

The application should now start successfully and maintain proper security for all endpoints.