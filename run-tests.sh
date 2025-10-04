#!/bin/bash

# Authentication Service Test Runner
# Description: Unified test runner for all testing scenarios
# Usage: ./run-tests.sh [test-type] [options]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Default configuration
TEST_TYPE="all"
BASE_URL="http://localhost:8080"
ENVIRONMENT="local"
VERBOSE=false
STOP_ON_ERROR=false
GENERATE_REPORT=true

# Function to display usage
show_usage() {
    echo -e "${CYAN}Authentication Service Test Runner${NC}"
    echo ""
    echo "Usage: $0 [test-type] [options]"
    echo ""
    echo "Test Types:"
    echo "  unit              Run unit tests only"
    echo "  integration       Run integration tests only"
    echo "  api               Run API tests only"
    echo "  all               Run all tests (default)"
    echo ""
    echo "Options:"
    echo "  --base-url URL    Base URL for API tests (default: http://localhost:8080)"
    echo "  --env ENV         Environment name (default: local)"
    echo "  --verbose         Enable verbose output"
    echo "  --stop-on-error   Stop on first test failure"
    echo "  --no-report       Skip report generation"
    echo "  --help            Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 unit"
    echo "  $0 api --base-url http://localhost:8081 --verbose"
    echo "  $0 all --env staging --stop-on-error"
}

# Function to print colored output
print_status() {
    local color=$1
    local message=$2
    echo -e "${color}$message${NC}"
}

# Function to print section header
print_header() {
    local title=$1
    echo ""
    echo -e "${CYAN}=================================================================${NC}"
    echo -e "${CYAN} $title${NC}"
    echo -e "${CYAN}=================================================================${NC}"
}

# Function to run unit tests
run_unit_tests() {
    print_header "Running Unit Tests"
    
    if command -v mvn &> /dev/null; then
        print_status $BLUE "Using Maven to run unit tests..."
        mvn test -Dtest="*Test" -DexcludedGroups="integration"
    elif command -v ./mvnw &> /dev/null; then
        print_status $BLUE "Using Maven Wrapper to run unit tests..."
        ./mvnw test -Dtest="*Test" -DexcludedGroups="integration"
    else
        print_status $RED "Maven not found. Please install Maven or use Maven Wrapper."
        return 1
    fi
}

# Function to run integration tests
run_integration_tests() {
    print_header "Running Integration Tests"
    
    if command -v mvn &> /dev/null; then
        print_status $BLUE "Using Maven to run integration tests..."
        mvn test -Dtest="*IntegrationTest" -Dspring.profiles.active=test
    elif command -v ./mvnw &> /dev/null; then
        print_status $BLUE "Using Maven Wrapper to run integration tests..."
        ./mvnw test -Dtest="*IntegrationTest" -Dspring.profiles.active=test
    else
        print_status $RED "Maven not found. Please install Maven or use Maven Wrapper."
        return 1
    fi
}

# Function to run API tests
run_api_tests() {
    print_header "Running API Tests"
    
    # Check if Spring Boot application is running
    if ! curl -s "$BASE_URL/api/auth/health" > /dev/null 2>&1; then
        print_status $YELLOW "Spring Boot application not detected at $BASE_URL"
        print_status $YELLOW "Starting Spring Boot application..."
        
        # Start Spring Boot application in background
        if command -v mvn &> /dev/null; then
            mvn spring-boot:run > app.log 2>&1 &
        elif command -v ./mvnw &> /dev/null; then
            ./mvnw spring-boot:run > app.log 2>&1 &
        else
            print_status $RED "Cannot start Spring Boot application. Maven not found."
            return 1
        fi
        
        APP_PID=$!
        print_status $BLUE "Waiting for application to start..."
        
        # Wait for application to be ready
        for i in {1..30}; do
            if curl -s "$BASE_URL/api/auth/health" > /dev/null 2>&1; then
                print_status $GREEN "Application is ready!"
                break
            fi
            sleep 2
            if [ $i -eq 30 ]; then
                print_status $RED "Application failed to start within 60 seconds"
                kill $APP_PID 2>/dev/null || true
                return 1
            fi
        done
    else
        print_status $GREEN "Spring Boot application is already running at $BASE_URL"
        APP_PID=""
    fi
    
    # Run API tests using PowerShell if available, otherwise use curl
    if command -v powershell &> /dev/null; then
        print_status $BLUE "Using PowerShell for API testing..."
        if [ "$VERBOSE" = true ]; then
            if [ "$STOP_ON_ERROR" = true ]; then
                powershell -ExecutionPolicy Bypass -File "test-api.ps1" -BaseUrl "$BASE_URL" -Environment "$ENVIRONMENT" -Verbose -StopOnError
            else
                powershell -ExecutionPolicy Bypass -File "test-api.ps1" -BaseUrl "$BASE_URL" -Environment "$ENVIRONMENT" -Verbose
            fi
        else
            if [ "$STOP_ON_ERROR" = true ]; then
                powershell -ExecutionPolicy Bypass -File "test-api.ps1" -BaseUrl "$BASE_URL" -Environment "$ENVIRONMENT" -StopOnError
            else
                powershell -ExecutionPolicy Bypass -File "test-api.ps1" -BaseUrl "$BASE_URL" -Environment "$ENVIRONMENT"
            fi
        fi
    elif command -v curl &> /dev/null; then
        print_status $BLUE "Using curl for basic API testing..."
        ./test-api.bat "$BASE_URL" "$ENVIRONMENT"
    else
        print_status $RED "Neither PowerShell nor curl found. Cannot run API tests."
        return 1
    fi
    
    # Cleanup: stop application if we started it
    if [ ! -z "$APP_PID" ]; then
        print_status $BLUE "Stopping Spring Boot application..."
        kill $APP_PID 2>/dev/null || true
        wait $APP_PID 2>/dev/null || true
    fi
}

# Function to generate test report
generate_test_report() {
    if [ "$GENERATE_REPORT" = true ]; then
        print_header "Generating Test Report"
        
        REPORT_DIR="test-reports"
        TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
        
        mkdir -p "$REPORT_DIR"
        
        # Generate Maven Surefire report if available
        if [ -d "target/surefire-reports" ]; then
            print_status $BLUE "Copying Maven test reports..."
            cp -r target/surefire-reports "$REPORT_DIR/surefire-$TIMESTAMP"
        fi
        
        # Generate JaCoCo coverage report if available
        if [ -d "target/site/jacoco" ]; then
            print_status $BLUE "Copying JaCoCo coverage reports..."
            cp -r target/site/jacoco "$REPORT_DIR/jacoco-$TIMESTAMP"
        fi
        
        # Copy API test reports if available
        if ls api-test-report-*.json 1> /dev/null 2>&1; then
            print_status $BLUE "Copying API test reports..."
            cp api-test-report-*.json "$REPORT_DIR/"
        fi
        
        print_status $GREEN "Test reports generated in $REPORT_DIR/"
    fi
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        unit|integration|api|all)
            TEST_TYPE="$1"
            shift
            ;;
        --base-url)
            BASE_URL="$2"
            shift 2
            ;;
        --env)
            ENVIRONMENT="$2"
            shift 2
            ;;
        --verbose)
            VERBOSE=true
            shift
            ;;
        --stop-on-error)
            STOP_ON_ERROR=true
            shift
            ;;
        --no-report)
            GENERATE_REPORT=false
            shift
            ;;
        --help)
            show_usage
            exit 0
            ;;
        *)
            print_status $RED "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Main execution
print_header "Authentication Service Test Suite"
print_status $BLUE "Test Type: $TEST_TYPE"
print_status $BLUE "Base URL: $BASE_URL"
print_status $BLUE "Environment: $ENVIRONMENT"
print_status $BLUE "Verbose: $VERBOSE"
print_status $BLUE "Stop on Error: $STOP_ON_ERROR"

# Track overall success
OVERALL_SUCCESS=true

# Run tests based on type
case $TEST_TYPE in
    unit)
        run_unit_tests || OVERALL_SUCCESS=false
        ;;
    integration)
        run_integration_tests || OVERALL_SUCCESS=false
        ;;
    api)
        run_api_tests || OVERALL_SUCCESS=false
        ;;
    all)
        run_unit_tests || OVERALL_SUCCESS=false
        if [ "$OVERALL_SUCCESS" = true ] || [ "$STOP_ON_ERROR" = false ]; then
            run_integration_tests || OVERALL_SUCCESS=false
        fi
        if [ "$OVERALL_SUCCESS" = true ] || [ "$STOP_ON_ERROR" = false ]; then
            run_api_tests || OVERALL_SUCCESS=false
        fi
        ;;
    *)
        print_status $RED "Invalid test type: $TEST_TYPE"
        show_usage
        exit 1
        ;;
esac

# Generate reports
generate_test_report

# Final status
print_header "Test Execution Summary"
if [ "$OVERALL_SUCCESS" = true ]; then
    print_status $GREEN "✓ All tests completed successfully!"
    exit 0
else
    print_status $RED "✗ Some tests failed. Check the output above for details."
    exit 1
fi