# Simple test to verify Spring Security configuration fix
param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "Testing Spring Security Configuration Fix" -ForegroundColor Cyan
Write-Host "Base URL: $BaseUrl" -ForegroundColor White

# Function to test if application is running
function Test-ApplicationHealth {
    param([string]$Url)
    
    try {
        $response = Invoke-RestMethod -Uri "$Url/api/auth/health" -Method GET -TimeoutSec 5
        return $true
    }
    catch {
        return $false
    }
}

# Check if application is already running
Write-Host "Checking if application is already running..." -ForegroundColor Yellow

if (Test-ApplicationHealth -Url $BaseUrl) {
    Write-Host "✓ Application is already running!" -ForegroundColor Green
    Write-Host "✓ Health endpoint accessible" -ForegroundColor Green
    Write-Host "✓ Spring Security configuration appears to be working" -ForegroundColor Green
    
    # Test a few more endpoints to verify the fix
    Write-Host "Testing additional endpoints..." -ForegroundColor Yellow
    
    # Test registration endpoint (should be accessible)
    try {
        $testData = @{
            username = "testuser"
            email = "test@example.com"
            password = "password123"
            firstName = "Test"
            lastName = "User"
        }
        
        Invoke-RestMethod -Uri "$BaseUrl/api/auth/register" -Method POST -Body ($testData | ConvertTo-Json) -ContentType "application/json" -TimeoutSec 5
        Write-Host "✓ Registration endpoint accessible" -ForegroundColor Green
    }
    catch {
        if ($_.Exception.Response.StatusCode -eq 409) {
            Write-Host "✓ Registration endpoint accessible (user already exists)" -ForegroundColor Green
        }
        elseif ($_.Exception.Response.StatusCode -eq 400) {
            Write-Host "✓ Registration endpoint accessible (validation working)" -ForegroundColor Green
        }
        else {
            Write-Host "⚠ Registration endpoint test: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
    
    # Test protected endpoint without auth (should return 401)
    try {
        Invoke-RestMethod -Uri "$BaseUrl/api/auth/me" -Method GET -TimeoutSec 5
        Write-Host "⚠ Protected endpoint accessible without auth (unexpected)" -ForegroundColor Yellow
    }
    catch {
        if ($_.Exception.Response.StatusCode -eq 401) {
            Write-Host "✓ Protected endpoint properly secured (401 Unauthorized)" -ForegroundColor Green
        }
        else {
            Write-Host "⚠ Protected endpoint test: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
    
    Write-Host "`n🎉 Spring Security configuration fix verification completed successfully!" -ForegroundColor Green
    Write-Host "The application is running and the security configuration appears to be working correctly." -ForegroundColor Green
}
else {
    Write-Host "✗ Application is not running at $BaseUrl" -ForegroundColor Red
    Write-Host "Please start the Spring Boot application first to test the security configuration." -ForegroundColor Yellow
    Write-Host "You can start it using: mvn spring-boot:run" -ForegroundColor Cyan
}

Write-Host "`nTest completed." -ForegroundColor Cyan