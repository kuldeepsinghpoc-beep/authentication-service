@echo off
REM Authentication Service API Testing Script
REM Description: Batch script wrapper for API testing
REM Usage: test-api.bat [base_url] [environment]

setlocal enabledelayedexpansion

REM Set default values
set "BASE_URL=http://localhost:8080"
set "ENVIRONMENT=local"
set "VERBOSE=false"
set "STOP_ON_ERROR=false"

REM Parse command line arguments
if not "%1"=="" set "BASE_URL=%1"
if not "%2"=="" set "ENVIRONMENT=%2"
if "%3"=="--verbose" set "VERBOSE=true"
if "%3"=="--stop-on-error" set "STOP_ON_ERROR=true"
if "%4"=="--verbose" set "VERBOSE=true"
if "%4"=="--stop-on-error" set "STOP_ON_ERROR=true"

echo.
echo ====================================================================
echo  Authentication Service API Test Suite (Batch Version)
echo ====================================================================
echo  Base URL: %BASE_URL%
echo  Environment: %ENVIRONMENT%
echo  Verbose: %VERBOSE%
echo  Stop on Error: %STOP_ON_ERROR%
echo ====================================================================
echo.

REM Check if PowerShell is available and prefer it
where powershell >nul 2>&1
if %errorlevel% equ 0 (
    echo Using PowerShell for enhanced testing...
    if "%VERBOSE%"=="true" (
        if "%STOP_ON_ERROR%"=="true" (
            powershell.exe -ExecutionPolicy Bypass -File "test-api.ps1" -BaseUrl "%BASE_URL%" -Environment "%ENVIRONMENT%" -Verbose -StopOnError
        ) else (
            powershell.exe -ExecutionPolicy Bypass -File "test-api.ps1" -BaseUrl "%BASE_URL%" -Environment "%ENVIRONMENT%" -Verbose
        )
    ) else (
        if "%STOP_ON_ERROR%"=="true" (
            powershell.exe -ExecutionPolicy Bypass -File "test-api.ps1" -BaseUrl "%BASE_URL%" -Environment "%ENVIRONMENT%" -StopOnError
        ) else (
            powershell.exe -ExecutionPolicy Bypass -File "test-api.ps1" -BaseUrl "%BASE_URL%" -Environment "%ENVIRONMENT%"
        )
    )
    goto :end
)

REM Fallback to curl-based testing if PowerShell is not available
echo PowerShell not found, using curl-based testing...
echo.

REM Check if curl is available
where curl >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: curl is required for API testing but was not found.
    echo Please install curl or use PowerShell version: test-api.ps1
    exit /b 1
)

REM Initialize counters
set /a TOTAL_TESTS=0
set /a PASSED_TESTS=0
set /a FAILED_TESTS=0

REM Generate test data
set /a RANDOM_NUM=%RANDOM% %% 9000 + 1000
set "TEST_USERNAME=testuser_%RANDOM_NUM%"
set "TEST_EMAIL=test_%RANDOM_NUM%@example.com"
set "TEST_PASSWORD=password123"
set "ACCESS_TOKEN="
set "REFRESH_TOKEN="

echo ============================================================
echo  Health Check Tests
echo ============================================================

REM Test health endpoint
echo Testing health endpoint...
curl -s -w "%%{http_code}" -o temp_response.json "%BASE_URL%/api/auth/health" > temp_status.txt
set /p HTTP_STATUS=<temp_status.txt

set /a TOTAL_TESTS+=1
if "%HTTP_STATUS%"=="200" (
    echo [PASS] Health endpoint accessible
    set /a PASSED_TESTS+=1
) else (
    echo [FAIL] Health endpoint accessible - Status: %HTTP_STATUS%
    set /a FAILED_TESTS+=1
    if "%STOP_ON_ERROR%"=="true" goto :show_summary
)

echo.
echo ============================================================
echo  User Registration Tests
echo ============================================================

REM Test user registration
echo Testing user registration...
echo {"username":"%TEST_USERNAME%","email":"%TEST_EMAIL%","password":"%TEST_PASSWORD%","firstName":"Test","lastName":"User","phoneNumber":"+1234567890"} > temp_register.json

curl -s -w "%%{http_code}" -X POST -H "Content-Type: application/json" -d @temp_register.json -o temp_response.json "%BASE_URL%/api/auth/register" > temp_status.txt
set /p HTTP_STATUS=<temp_status.txt

set /a TOTAL_TESTS+=1
if "%HTTP_STATUS%"=="201" (
    echo [PASS] User registration successful
    set /a PASSED_TESTS+=1
) else (
    echo [FAIL] User registration - Status: %HTTP_STATUS%
    set /a FAILED_TESTS+=1
    if "%STOP_ON_ERROR%"=="true" goto :show_summary
)

REM Test duplicate username
echo Testing duplicate username...
echo {"username":"%TEST_USERNAME%","email":"different@example.com","password":"%TEST_PASSWORD%","firstName":"Different","lastName":"User"} > temp_duplicate.json

curl -s -w "%%{http_code}" -X POST -H "Content-Type: application/json" -d @temp_duplicate.json -o temp_response.json "%BASE_URL%/api/auth/register" > temp_status.txt
set /p HTTP_STATUS=<temp_status.txt

set /a TOTAL_TESTS+=1
if "%HTTP_STATUS%"=="409" (
    echo [PASS] Duplicate username returns 409
    set /a PASSED_TESTS+=1
) else (
    echo [FAIL] Duplicate username test - Status: %HTTP_STATUS%
    set /a FAILED_TESTS+=1
    if "%STOP_ON_ERROR%"=="true" goto :show_summary
)

echo.
echo ============================================================
echo  User Authentication Tests
echo ============================================================

REM Test user login
echo Testing user login...
echo {"username":"%TEST_USERNAME%","password":"%TEST_PASSWORD%"} > temp_login.json

curl -s -w "%%{http_code}" -X POST -H "Content-Type: application/json" -d @temp_login.json -o temp_response.json "%BASE_URL%/api/auth/login" > temp_status.txt
set /p HTTP_STATUS=<temp_status.txt

set /a TOTAL_TESTS+=1
if "%HTTP_STATUS%"=="200" (
    echo [PASS] User login successful
    set /a PASSED_TESTS+=1
    
    REM Extract tokens using findstr (basic JSON parsing)
    for /f "tokens=2 delims=:" %%a in ('findstr "accessToken" temp_response.json') do (
        set "TOKEN_LINE=%%a"
        set "TOKEN_LINE=!TOKEN_LINE:,=!"
        set "TOKEN_LINE=!TOKEN_LINE:"=!"
        set "ACCESS_TOKEN=!TOKEN_LINE: =!"
    )
) else (
    echo [FAIL] User login - Status: %HTTP_STATUS%
    set /a FAILED_TESTS+=1
    if "%STOP_ON_ERROR%"=="true" goto :show_summary
)

REM Test invalid credentials
echo Testing invalid credentials...
echo {"username":"%TEST_USERNAME%","password":"wrongpassword"} > temp_invalid.json

curl -s -w "%%{http_code}" -X POST -H "Content-Type: application/json" -d @temp_invalid.json -o temp_response.json "%BASE_URL%/api/auth/login" > temp_status.txt
set /p HTTP_STATUS=<temp_status.txt

set /a TOTAL_TESTS+=1
if "%HTTP_STATUS%"=="401" (
    echo [PASS] Invalid credentials return 401
    set /a PASSED_TESTS+=1
) else (
    echo [FAIL] Invalid credentials test - Status: %HTTP_STATUS%
    set /a FAILED_TESTS+=1
    if "%STOP_ON_ERROR%"=="true" goto :show_summary
)

echo.
echo ============================================================
echo  Protected Endpoints Tests
echo ============================================================

if not "%ACCESS_TOKEN%"=="" (
    REM Test protected endpoint with token
    echo Testing protected endpoint with token...
    curl -s -w "%%{http_code}" -H "Authorization: Bearer %ACCESS_TOKEN%" -o temp_response.json "%BASE_URL%/api/auth/me" > temp_status.txt
    set /p HTTP_STATUS=<temp_status.txt
    
    set /a TOTAL_TESTS+=1
    if "%HTTP_STATUS%"=="200" (
        echo [PASS] Get current user successful
        set /a PASSED_TESTS+=1
    ) else (
        echo [FAIL] Get current user - Status: %HTTP_STATUS%
        set /a FAILED_TESTS+=1
    )
) else (
    echo [SKIP] Skipping protected endpoint tests - no access token
)

REM Test protected endpoint without token
echo Testing protected endpoint without token...
curl -s -w "%%{http_code}" -o temp_response.json "%BASE_URL%/api/auth/me" > temp_status.txt
set /p HTTP_STATUS=<temp_status.txt

set /a TOTAL_TESTS+=1
if "%HTTP_STATUS%"=="401" (
    echo [PASS] Request without token returns 401
    set /a PASSED_TESTS+=1
) else (
    echo [FAIL] Request without token test - Status: %HTTP_STATUS%
    set /a FAILED_TESTS+=1
)

echo.
echo ============================================================
echo  Error Scenarios Tests
echo ============================================================

REM Test non-existent endpoint
echo Testing non-existent endpoint...
curl -s -w "%%{http_code}" -o temp_response.json "%BASE_URL%/api/auth/nonexistent" > temp_status.txt
set /p HTTP_STATUS=<temp_status.txt

set /a TOTAL_TESTS+=1
if "%HTTP_STATUS%"=="404" (
    echo [PASS] Non-existent endpoint returns 404
    set /a PASSED_TESTS+=1
) else (
    echo [FAIL] Non-existent endpoint test - Status: %HTTP_STATUS%
    set /a FAILED_TESTS+=1
)

:show_summary
echo.
echo ============================================================
echo  Test Summary
echo ============================================================
echo  Total Tests: %TOTAL_TESTS%
echo  Passed: %PASSED_TESTS%
echo  Failed: %FAILED_TESTS%

set /a SUCCESS_RATE=0
if %TOTAL_TESTS% gtr 0 (
    set /a SUCCESS_RATE=(%PASSED_TESTS% * 100) / %TOTAL_TESTS%
)
echo  Success Rate: %SUCCESS_RATE%%%

if %FAILED_TESTS% gtr 0 (
    echo.
    echo  Some tests failed. Check the output above for details.
    echo  For more detailed testing, use: test-api.ps1
)

echo.
echo ============================================================

REM Cleanup temporary files
if exist temp_*.json del temp_*.json
if exist temp_*.txt del temp_*.txt

:end
REM Exit with appropriate code
if %FAILED_TESTS% gtr 0 (
    exit /b 1
) else (
    exit /b 0
)