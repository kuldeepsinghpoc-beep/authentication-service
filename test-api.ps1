# Authentication Service API Testing Script
# Description: Automated API testing script for Spring Boot Authentication Service
# Usage: .\test-api.ps1 [-BaseUrl "http://localhost:8080"] [-Environment "local"]

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Environment = "local",
    [switch]$Verbose,
    [switch]$StopOnError = $false
)

# Configuration
$script:TestResults = @()
$script:FailedTests = 0
$script:PassedTests = 0
$script:TotalTests = 0

# Colors for output
$Red = "Red"
$Green = "Green"
$Yellow = "Yellow"
$Cyan = "Cyan"
$White = "White"

# Test data
$global:TestUsername = "testuser_$(Get-Random -Minimum 1000 -Maximum 9999)"
$global:TestEmail = "test_$(Get-Random -Minimum 1000 -Maximum 9999)@example.com"
$global:TestPassword = "password123"
$global:AccessToken = ""
$global:RefreshToken = ""

function Write-TestHeader {
    param([string]$Title)
    Write-Host "`n" + "="*60 -ForegroundColor $Cyan
    Write-Host " $Title" -ForegroundColor $Cyan
    Write-Host "="*60 -ForegroundColor $Cyan
}

function Write-TestResult {
    param(
        [string]$TestName,
        [bool]$Passed,
        [string]$Message = "",
        [object]$Response = $null
    )
    
    $script:TotalTests++
    
    if ($Passed) {
        $script:PassedTests++
        Write-Host "✓ $TestName" -ForegroundColor $Green
    } else {
        $script:FailedTests++
        Write-Host "✗ $TestName" -ForegroundColor $Red
        if ($Message) {
            Write-Host "  Error: $Message" -ForegroundColor $Red
        }
        if ($Response -and $Verbose) {
            Write-Host "  Response: $($Response | ConvertTo-Json -Depth 3)" -ForegroundColor $Yellow
        }
    }
    
    $script:TestResults += [PSCustomObject]@{
        TestName = $TestName
        Passed = $Passed
        Message = $Message
        Timestamp = Get-Date
    }
    
    if ($StopOnError -and -not $Passed) {
        Write-Host "Stopping on first error as requested." -ForegroundColor $Red
        exit 1
    }
}

function Invoke-ApiCall {
    param(
        [string]$Method,
        [string]$Endpoint,
        [object]$Body = $null,
        [hashtable]$Headers = @{},
        [bool]$IncludeAuth = $false
    )
    
    $uri = "$BaseUrl$Endpoint"
    $requestHeaders = $Headers.Clone()
    
    if ($IncludeAuth -and $global:AccessToken) {
        $requestHeaders["Authorization"] = "Bearer $global:AccessToken"
    }
    
    $requestHeaders["Content-Type"] = "application/json"
    
    try {
        $params = @{
            Uri = $uri
            Method = $Method
            Headers = $requestHeaders
            UseBasicParsing = $true
        }
        
        if ($Body) {
            $params.Body = ($Body | ConvertTo-Json -Depth 10)
        }
        
        if ($Verbose) {
            Write-Host "Request: $Method $uri" -ForegroundColor $Yellow
            if ($Body) {
                Write-Host "Body: $($params.Body)" -ForegroundColor $Yellow
            }
        }
        
        $response = Invoke-RestMethod @params
        return @{
            Success = $true
            StatusCode = 200
            Data = $response
        }
    }
    catch {
        $statusCode = 500
        $errorMessage = $_.Exception.Message
        
        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
            try {
                $errorStream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($errorStream)
                $errorBody = $reader.ReadToEnd()
                $errorResponse = $errorBody | ConvertFrom-Json
                $errorMessage = $errorResponse.message
            }
            catch {
                # Keep original error message if parsing fails
            }
        }
        
        return @{
            Success = $false
            StatusCode = $statusCode
            Message = $errorMessage
            Data = $null
        }
    }
}

function Test-HealthCheck {
    Write-TestHeader "Health Check Tests"
    
    $response = Invoke-ApiCall -Method "GET" -Endpoint "/api/auth/health"
    
    if ($response.Success) {
        $healthData = $response.Data.data
        Write-TestResult -TestName "Health endpoint accessible" -Passed $true
        Write-TestResult -TestName "Health status contains service message" -Passed ($healthData -like "*Service is healthy*")
    } else {
        Write-TestResult -TestName "Health endpoint accessible" -Passed $false -Message $response.Message
    }
}

function Test-UserRegistration {
    Write-TestHeader "User Registration Tests"
    
    # Test successful registration
    $registerData = @{
        username = $global:TestUsername
        email = $global:TestEmail
        password = $global:TestPassword
        firstName = "Test"
        lastName = "User"
        phoneNumber = "+1234567890"
    }
    
    $response = Invoke-ApiCall -Method "POST" -Endpoint "/api/auth/register" -Body $registerData
    
    if ($response.Success -and $response.Data.data) {
        $userData = $response.Data.data
        Write-TestResult -TestName "User registration successful" -Passed $true
        Write-TestResult -TestName "Response contains username" -Passed ($userData.username -eq $global:TestUsername)
        Write-TestResult -TestName "Response contains email" -Passed ($userData.email -eq $global:TestEmail)
        Write-TestResult -TestName "User is active by default" -Passed ($userData.active -eq $true)
    } else {
        Write-TestResult -TestName "User registration successful" -Passed $false -Message $response.Message -Response $response
        return
    }
    
    # Test duplicate username
    $duplicateData = @{
        username = $global:TestUsername
        email = "different@example.com"
        password = $global:TestPassword
        firstName = "Different"
        lastName = "User"
    }
    
    $response = Invoke-ApiCall -Method "POST" -Endpoint "/api/auth/register" -Body $duplicateData
    Write-TestResult -TestName "Duplicate username returns 409" -Passed ($response.StatusCode -eq 409)
    
    # Test invalid data
    $invalidData = @{
        username = "ab"
        email = "invalid-email"
        password = "123"
        firstName = ""
        lastName = ""
    }
    
    $response = Invoke-ApiCall -Method "POST" -Endpoint "/api/auth/register" -Body $invalidData
    Write-TestResult -TestName "Invalid data returns 400" -Passed ($response.StatusCode -eq 400)
}

function Test-UserAuthentication {
    Write-TestHeader "User Authentication Tests"
    
    # Test successful login with username
    $loginData = @{
        username = $global:TestUsername
        password = $global:TestPassword
    }
    
    $response = Invoke-ApiCall -Method "POST" -Endpoint "/api/auth/login" -Body $loginData
    
    if ($response.Success -and $response.Data.data) {
        $authData = $response.Data.data
        Write-TestResult -TestName "Login with username successful" -Passed $true
        Write-TestResult -TestName "Response contains access token" -Passed ($authData.accessToken -ne $null)
        Write-TestResult -TestName "Response contains refresh token" -Passed ($authData.refreshToken -ne $null)
        Write-TestResult -TestName "Token type is Bearer" -Passed ($authData.tokenType -eq "Bearer")
        
        # Store tokens for subsequent tests
        $global:AccessToken = $authData.accessToken
        $global:RefreshToken = $authData.refreshToken
    } else {
        Write-TestResult -TestName "Login with username successful" -Passed $false -Message $response.Message -Response $response
        return
    }
    
    # Test login with email
    $emailLoginData = @{
        username = $global:TestEmail
        password = $global:TestPassword
    }
    
    $response = Invoke-ApiCall -Method "POST" -Endpoint "/api/auth/login" -Body $emailLoginData
    Write-TestResult -TestName "Login with email successful" -Passed $response.Success
    
    # Test invalid credentials
    $invalidLoginData = @{
        username = $global:TestUsername
        password = "wrongpassword"
    }
    
    $response = Invoke-ApiCall -Method "POST" -Endpoint "/api/auth/login" -Body $invalidLoginData
    Write-TestResult -TestName "Invalid credentials return 401" -Passed ($response.StatusCode -eq 401)
}

function Test-TokenManagement {
    Write-TestHeader "Token Management Tests"
    
    # Test token validation
    $response = Invoke-ApiCall -Method "GET" -Endpoint "/api/auth/validate" -IncludeAuth $true
    
    if ($response.Success) {
        Write-TestResult -TestName "Token validation successful" -Passed $true
        Write-TestResult -TestName "Token is valid" -Passed ($response.Data.data -eq $true)
    } else {
        Write-TestResult -TestName "Token validation successful" -Passed $false -Message $response.Message
    }
    
    # Test token refresh
    $refreshData = @{
        refreshToken = $global:RefreshToken
    }
    
    $response = Invoke-ApiCall -Method "POST" -Endpoint "/api/auth/refresh" -Body $refreshData
    
    if ($response.Success -and $response.Data.data) {
        $newTokenData = $response.Data.data
        Write-TestResult -TestName "Token refresh successful" -Passed $true
        Write-TestResult -TestName "New access token provided" -Passed ($newTokenData.accessToken -ne $null)
        Write-TestResult -TestName "New refresh token provided" -Passed ($newTokenData.refreshToken -ne $null)
        Write-TestResult -TestName "New token different from old" -Passed ($newTokenData.accessToken -ne $global:AccessToken)
        
        # Update tokens
        $global:AccessToken = $newTokenData.accessToken
        $global:RefreshToken = $newTokenData.refreshToken
    } else {
        Write-TestResult -TestName "Token refresh successful" -Passed $false -Message $response.Message
    }
    
    # Test invalid refresh token
    $invalidRefreshData = @{
        refreshToken = "invalid.refresh.token"
    }
    
    $response = Invoke-ApiCall -Method "POST" -Endpoint "/api/auth/refresh" -Body $invalidRefreshData
    Write-TestResult -TestName "Invalid refresh token returns 401" -Passed ($response.StatusCode -eq 401)
}

function Test-ProtectedEndpoints {
    Write-TestHeader "Protected Endpoints Tests"
    
    # Test get current user
    $response = Invoke-ApiCall -Method "GET" -Endpoint "/api/auth/me" -IncludeAuth $true
    
    if ($response.Success -and $response.Data.data) {
        $userData = $response.Data.data
        Write-TestResult -TestName "Get current user successful" -Passed $true
        Write-TestResult -TestName "Response contains user profile" -Passed ($userData.username -ne $null)
        Write-TestResult -TestName "User data matches registered user" -Passed ($userData.username -eq $global:TestUsername)
    } else {
        Write-TestResult -TestName "Get current user successful" -Passed $false -Message $response.Message
    }
    
    # Test protected endpoint without token
    $response = Invoke-ApiCall -Method "GET" -Endpoint "/api/auth/me"
    Write-TestResult -TestName "Request without token returns 401" -Passed ($response.StatusCode -eq 401)
    
    # Test logout
    $response = Invoke-ApiCall -Method "POST" -Endpoint "/api/auth/logout" -IncludeAuth $true
    
    if ($response.Success) {
        Write-TestResult -TestName "Logout successful" -Passed $true
        $global:AccessToken = ""
        $global:RefreshToken = ""
    } else {
        Write-TestResult -TestName "Logout successful" -Passed $false -Message $response.Message
    }
}

function Test-ErrorScenarios {
    Write-TestHeader "Error Scenarios Tests"
    
    # Test malformed JSON
    try {
        $uri = "$BaseUrl/api/auth/register"
        $headers = @{"Content-Type" = "application/json"}
        $response = Invoke-RestMethod -Uri $uri -Method "POST" -Headers $headers -Body "{invalid json}" -UseBasicParsing
        Write-TestResult -TestName "Malformed JSON returns 400" -Passed $false -Message "Expected error but got success"
    }
    catch {
        $statusCode = [int]$_.Exception.Response.StatusCode
        Write-TestResult -TestName "Malformed JSON returns 400" -Passed ($statusCode -eq 400)
    }
    
    # Test non-existent endpoint
    $response = Invoke-ApiCall -Method "GET" -Endpoint "/api/auth/nonexistent"
    Write-TestResult -TestName "Non-existent endpoint returns 404" -Passed ($response.StatusCode -eq 404)
}

function Show-TestSummary {
    Write-TestHeader "Test Summary"
    
    Write-Host "Total Tests: $script:TotalTests" -ForegroundColor $White
    Write-Host "Passed: $script:PassedTests" -ForegroundColor $Green
    Write-Host "Failed: $script:FailedTests" -ForegroundColor $Red
    
    $successRate = if ($script:TotalTests -gt 0) { 
        [math]::Round(($script:PassedTests / $script:TotalTests) * 100, 2) 
    } else { 
        0 
    }
    
    Write-Host "Success Rate: $successRate%" -ForegroundColor $(if ($successRate -ge 90) { $Green } elseif ($successRate -ge 70) { $Yellow } else { $Red })
    
    if ($script:FailedTests -gt 0) {
        Write-Host "`nFailed Tests:" -ForegroundColor $Red
        $script:TestResults | Where-Object { -not $_.Passed } | ForEach-Object {
            Write-Host "  • $($_.TestName): $($_.Message)" -ForegroundColor $Red
        }
    }
    
    # Generate report file
    $reportFile = "api-test-report-$(Get-Date -Format 'yyyyMMdd-HHmmss').json"
    $reportData = @{
        Environment = $Environment
        BaseUrl = $BaseUrl
        TestRunDate = Get-Date
        Summary = @{
            TotalTests = $script:TotalTests
            PassedTests = $script:PassedTests
            FailedTests = $script:FailedTests
            SuccessRate = $successRate
        }
        Results = $script:TestResults
    }
    
    $reportData | ConvertTo-Json -Depth 10 | Out-File -FilePath $reportFile -Encoding UTF8
    Write-Host "`nDetailed report saved to: $reportFile" -ForegroundColor $Cyan
}

# Main execution
Write-Host "Authentication Service API Test Suite" -ForegroundColor $Cyan
Write-Host "Base URL: $BaseUrl" -ForegroundColor $White
Write-Host "Environment: $Environment" -ForegroundColor $White
Write-Host "Test User: $global:TestUsername" -ForegroundColor $White
Write-Host "Test Email: $global:TestEmail" -ForegroundColor $White

try {
    Test-HealthCheck
    Test-UserRegistration
    Test-UserAuthentication
    Test-TokenManagement
    Test-ProtectedEndpoints
    Test-ErrorScenarios
}
catch {
    Write-Host "Unexpected error during test execution: $($_.Exception.Message)" -ForegroundColor $Red
    exit 1
}
finally {
    Show-TestSummary
}

# Exit with appropriate code
exit $(if ($script:FailedTests -eq 0) { 0 } else { 1 })