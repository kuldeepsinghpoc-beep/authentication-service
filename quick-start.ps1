# Quick Start AWS ECS Deployment Script
param(
    [Parameter(Mandatory=$true)]
    [string]$AwsAccountId,
    
    [string]$AwsRegion = "us-east-1",
    [string]$AppName = "authentication-service"
)

Write-Host "🚀 Quick Start AWS ECS Setup for $AppName" -ForegroundColor Green
Write-Host "Account ID: $AwsAccountId" -ForegroundColor Cyan
Write-Host "Region: $AwsRegion" -ForegroundColor Cyan

# Step 1: Create ECR Repository
Write-Host "`n📦 Step 1: Creating ECR Repository..." -ForegroundColor Yellow
try {
    aws ecr create-repository --repository-name $AppName --region $AwsRegion | Out-Null
    Write-Host "✅ ECR Repository created successfully" -ForegroundColor Green
}
catch {
    Write-Host "ℹ️ ECR Repository might already exist" -ForegroundColor Yellow
}

# Step 2: Update task definition with account ID
Write-Host "`n🔧 Step 2: Updating task definition..." -ForegroundColor Yellow
$taskDefinitionPath = "aws/task-definition.json"
if (Test-Path $taskDefinitionPath) {
    $content = Get-Content $taskDefinitionPath -Raw
    $content = $content -replace "YOUR_ACCOUNT_ID", $AwsAccountId
    $content = $content -replace "us-east-1", $AwsRegion
    Set-Content $taskDefinitionPath $content
    Write-Host "✅ Task definition updated" -ForegroundColor Green
}

# Step 3: Build and push initial image
Write-Host "`n🏗️ Step 3: Building and pushing initial Docker image..." -ForegroundColor Yellow
$ecrUri = "$AwsAccountId.dkr.ecr.$AwsRegion.amazonaws.com/$AppName"

# Login to ECR
aws ecr get-login-password --region $AwsRegion | docker login --username AWS --password-stdin $ecrUri

# Build and push
docker build -t $AppName .
docker tag "$AppName`:latest" "$ecrUri`:latest"
docker push "$ecrUri`:latest"
Write-Host "✅ Initial image pushed to ECR" -ForegroundColor Green

# Step 4: Deploy infrastructure using Terraform
Write-Host "`n🏗️ Step 4: Deploying infrastructure..." -ForegroundColor Yellow
if (Test-Path "aws/main.tf") {
    Set-Location aws
    terraform init
    terraform plan -var="aws_region=$AwsRegion"
    $deploy = Read-Host "Do you want to deploy the infrastructure? (y/N)"
    if ($deploy -eq "y" -or $deploy -eq "Y") {
        terraform apply -var="aws_region=$AwsRegion" -auto-approve
        Write-Host "✅ Infrastructure deployed successfully" -ForegroundColor Green
    }
    Set-Location ..
}

# Step 5: Create ECS Service
Write-Host "`n🎯 Step 5: Creating ECS Service..." -ForegroundColor Yellow
try {
    aws ecs create-service `
        --cluster "$AppName-cluster" `
        --service-name $AppName `
        --task-definition $AppName `
        --desired-count 1 `
        --launch-type FARGATE `
        --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx,subnet-yyy],securityGroups=[sg-xxx],assignPublicIp=ENABLED}" `
        --region $AwsRegion | Out-Null
    Write-Host "✅ ECS Service created successfully" -ForegroundColor Green
}
catch {
    Write-Host "ℹ️ ECS Service might already exist or need manual configuration" -ForegroundColor Yellow
}

Write-Host "`n🎉 Quick setup completed!" -ForegroundColor Green
Write-Host "`nNext steps:" -ForegroundColor Cyan
Write-Host "1. Configure GitHub Secrets with your AWS credentials" -ForegroundColor White
Write-Host "2. Push to main branch to trigger automatic deployment" -ForegroundColor White
Write-Host "3. Monitor deployment in AWS ECS Console" -ForegroundColor White
Write-Host "`nFor detailed setup, see AWS-SETUP-GUIDE.md" -ForegroundColor Yellow