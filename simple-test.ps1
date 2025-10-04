# Simple test to verify Spring Security configuration fix
# Usage: .\simple-test.ps1

Write-Host "Testing Spring Security Configuration Fix" -ForegroundColor Cyan

# Check if we can compile the application
Write-Host "Checking if Maven is available..." -ForegroundColor Yellow

# Try to find Maven or use a simple approach
$mavenFound = $false

# Check for Maven in common locations
$mavenPaths = @(
    "mvn",
    "C:\Program Files\Apache\maven\bin\mvn.cmd",
    "C:\apache-maven\bin\mvn.cmd",
    ".\mvnw.cmd"
)

foreach ($path in $mavenPaths) {
    try {
        if (Get-Command $path -ErrorAction SilentlyContinue) {
            Write-Host "Found Maven at: $path" -ForegroundColor Green
            $mavenFound = $true
            $mavenCommand = $path
            break
        }
    }
    catch {
        # Continue searching
    }
}

if (-not $mavenFound) {
    Write-Host "Maven not found. Checking if we can run the application directly..." -ForegroundColor Yellow
    
    # Check if there are compiled classes
    if (Test-Path "target\classes") {
        Write-Host "Found compiled classes. Attempting to run with java directly..." -ForegroundColor Yellow
        
        # Try to run with java -cp
        try {
            $classpath = "target\classes;target\dependency\*"
            $mainClass = "com.wipro.ai.demo.AuthenticationServiceApplication"
            
            Write-Host "Starting Spring Boot application..." -ForegroundColor Yellow
            Start-Process -FilePath "java" -ArgumentList "-cp", $classpath, $mainClass -WindowStyle Hidden -PassThru
            
            # Wait a bit for the application to start
            Start-Sleep -Seconds 10
            
            # Test if the application is running
            try {
                $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/health" -Method GET -TimeoutSec 5
                Write-Host "✓ Application started successfully!" -ForegroundColor Green
                Write-Host "✓ Health endpoint accessible" -ForegroundColor Green
                Write-Host "✓ Spring Security configuration appears to be working" -ForegroundColor Green
                
                # Stop the application
                Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -like "*AuthenticationServiceApplication*" } | Stop-Process -Force
                
                Write-Host "Test completed successfully. The Spring Security configuration fix appears to be working." -ForegroundColor Green
                exit 0
            }
            catch {
                Write-Host "✗ Application may not have started properly: $($_.Exception.Message)" -ForegroundColor Red
                Write-Host "This could be due to missing dependencies or the security configuration issue persisting." -ForegroundColor Yellow
            }
        }
        catch {
            Write-Host "✗ Could not start application with java directly: $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    else {
        Write-Host "✗ No compiled classes found. The application needs to be compiled first." -ForegroundColor Red
    }
}
else {
    Write-Host "Attempting to compile and test the application..." -ForegroundColor Yellow
    
    try {
        # Compile the application
        & $mavenCommand "clean" "compile"
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ Compilation successful!" -ForegroundColor Green
            
            # Try to run the application
            Write-Host "Starting Spring Boot application..." -ForegroundColor Yellow
            $process = Start-Process -FilePath $mavenCommand -ArgumentList "spring-boot:run" -WindowStyle Hidden -PassThru
            
            # Wait for the application to start
            Start-Sleep -Seconds 15
            
            # Test the health endpoint
            try {
                $response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/health" -Method GET -TimeoutSec 5
                Write-Host "✓ Application started successfully!" -ForegroundColor Green
                Write-Host "✓ Health endpoint accessible" -ForegroundColor Green
                Write-Host "✓ Spring Security configuration fix successful!" -ForegroundColor Green
                
                # Stop the application
                Stop-Process -Id $process.Id -Force
                
                Write-Host "Test completed successfully. The Spring Security configuration fix is working." -ForegroundColor Green
                exit 0
            }
            catch {
                Write-Host "✗ Could not access health endpoint: $($_.Exception.Message)" -ForegroundColor Red
                Write-Host "The Spring Security configuration issue may still exist." -ForegroundColor Yellow
                
                # Stop the application
                Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
            }
        }
        else {
            Write-Host "✗ Compilation failed. Please check the error messages above." -ForegroundColor Red
        }
    }
    catch {
        Write-Host "✗ Error during Maven execution: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "Test completed. Please review the results above." -ForegroundColor Cyan