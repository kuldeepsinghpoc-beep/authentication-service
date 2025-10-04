# AWS ECS Deployment Script for Authentication Service (PowerShell)
param(
    [string]$AwsRegion = "us-east-1",
    [string]$ClusterName = "authentication-cluster",
    [string]$ServiceName = "authentication-service",
    [string]$EcrRepository = "authentication-service"
)

# Configuration
$ErrorActionPreference = "Stop"

Write-Host "🚀 Starting AWS ECS Deployment..." -ForegroundColor Green

# Check if AWS CLI is installed
try {
    aws --version | Out-Null
    Write-Host "✅ AWS CLI is available" -ForegroundColor Green
}
catch {
    Write-Host "❌ AWS CLI is not installed. Please install it first." -ForegroundColor Red
    exit 1
}

# Check if Docker is running
try {
    docker info | Out-Null
    Write-Host "✅ Docker is running" -ForegroundColor Green
}
catch {
    Write-Host "❌ Docker is not running. Please start Docker first." -ForegroundColor Red
    exit 1
}

# Get AWS Account ID
try {
    $AccountId = aws sts get-caller-identity --query Account --output text
    Write-Host "✅ AWS Account ID: $AccountId" -ForegroundColor Green
}
catch {
    Write-Host "❌ Unable to get AWS Account ID. Please check your AWS credentials." -ForegroundColor Red
    exit 1
}

# Set ECR URI
$EcrUri = "$AccountId.dkr.ecr.$AwsRegion.amazonaws.com/$EcrRepository"

# Login to ECR
Write-Host "🔐 Logging in to Amazon ECR..." -ForegroundColor Yellow
try {
    $LoginCommand = aws ecr get-login-password --region $AwsRegion
    $LoginCommand | docker login --username AWS --password-stdin $EcrUri
    Write-Host "✅ Successfully logged in to ECR" -ForegroundColor Green
}
catch {
    Write-Host "❌ Failed to login to ECR" -ForegroundColor Red
    exit 1
}

# Build Docker image
Write-Host "🏗️ Building Docker image..." -ForegroundColor Yellow
try {
    docker build -t "${EcrRepository}:latest" .
    Write-Host "✅ Docker image built successfully" -ForegroundColor Green
}
catch {
    Write-Host "❌ Failed to build Docker image" -ForegroundColor Red
    exit 1
}

# Get git commit hash for tagging
$GitHash = git rev-parse --short HEAD
if (-not $GitHash) {
    $GitHash = "latest"
}

# Tag image for ECR
Write-Host "🏷️ Tagging Docker image..." -ForegroundColor Yellow
docker tag "${EcrRepository}:latest" "${EcrUri}:latest"
docker tag "${EcrRepository}:latest" "${EcrUri}:$GitHash"

# Push to ECR
Write-Host "📤 Pushing image to ECR..." -ForegroundColor Yellow
try {
    docker push "${EcrUri}:latest"
    docker push "${EcrUri}:$GitHash"
    Write-Host "✅ Image pushed to ECR successfully" -ForegroundColor Green
}
catch {
    Write-Host "❌ Failed to push image to ECR" -ForegroundColor Red
    exit 1
}

# Update ECS service
Write-Host "🔄 Updating ECS service..." -ForegroundColor Yellow
try {
    aws ecs update-service `
        --cluster $ClusterName `
        --service $ServiceName `
        --force-new-deployment `
        --region $AwsRegion | Out-Null
    Write-Host "✅ ECS service update initiated" -ForegroundColor Green
}
catch {
    Write-Host "❌ Failed to update ECS service" -ForegroundColor Red
    exit 1
}

# Wait for deployment to complete
Write-Host "⏳ Waiting for deployment to complete..." -ForegroundColor Yellow
try {
    aws ecs wait services-stable `
        --cluster $ClusterName `
        --services $ServiceName `
        --region $AwsRegion
    
    Write-Host "✅ Deployment completed successfully!" -ForegroundColor Green
}
catch {
    Write-Host "❌ Deployment failed or timed out" -ForegroundColor Red
    exit 1
}

# Get service status
try {
    $ServiceStatus = aws ecs describe-services `
        --cluster $ClusterName `
        --services $ServiceName `
        --region $AwsRegion `
        --query 'services[0].deployments[0].status' `
        --output text
    
    if ($ServiceStatus -eq "PRIMARY") {
        Write-Host "✅ Service is running successfully!" -ForegroundColor Green
        
        # Try to get load balancer DNS name
        try {
            $AlbDns = aws elbv2 describe-load-balancers `
                --names "authentication-service-alb" `
                --region $AwsRegion `
                --query 'LoadBalancers[0].DNSName' `
                --output text 2>$null
            
            if ($AlbDns -and $AlbDns -ne "None") {
                Write-Host "🌐 Service URL: http://$AlbDns/api/v1/auth" -ForegroundColor Green
                Write-Host "🏥 Health Check: http://$AlbDns/actuator/health" -ForegroundColor Green
            }
        }
        catch {
            Write-Host "ℹ️ Load balancer DNS not available yet" -ForegroundColor Yellow
        }
    }
    else {
        Write-Host "❌ Service status: $ServiceStatus" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "❌ Failed to get service status" -ForegroundColor Red
    exit 1
}

Write-Host "🎉 Deployment completed successfully!" -ForegroundColor Green
Write-Host "📊 You can monitor your service in the AWS ECS Console" -ForegroundColor Cyan