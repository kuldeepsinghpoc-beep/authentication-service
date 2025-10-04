# AWS ECS DevOps Pipeline Setup Guide

This guide will help you set up a complete DevOps pipeline for deploying your Spring Boot Authentication Service to AWS ECS.

## 🏗️ Infrastructure Overview

### Architecture Components:
- **GitHub Actions**: CI/CD pipeline for automated testing and deployment
- **Amazon ECR**: Docker container registry
- **Amazon ECS**: Container orchestration service
- **Application Load Balancer**: Load balancing and health checks
- **AWS Fargate**: Serverless container platform
- **CloudWatch**: Logging and monitoring
- **Systems Manager Parameter Store**: Secure configuration management

## 📋 Prerequisites

### 1. AWS Account Setup
- AWS Account with appropriate permissions
- AWS CLI installed and configured
- Docker installed locally

### 2. GitHub Repository
- Repository with your Spring Boot application
- GitHub Secrets configured for AWS credentials

### 3. Required Tools
- Terraform (optional, for infrastructure as code)
- Git
- Maven
- Java 17

## 🚀 Step-by-Step Setup

### Step 1: Configure GitHub Secrets

Add the following secrets to your GitHub repository:

```bash
# Go to your GitHub repository
# Settings > Secrets and variables > Actions > New repository secret

AWS_ACCESS_KEY_ID=your-access-key-id
AWS_SECRET_ACCESS_KEY=your-secret-access-key
```

### Step 2: Create AWS Infrastructure

#### Option A: Using Terraform (Recommended)

```bash
# Navigate to the aws directory
cd aws

# Initialize Terraform
terraform init

# Plan the deployment
terraform plan

# Apply the infrastructure
terraform apply
```

#### Option B: Manual Setup via AWS Console

1. **Create ECR Repository**
   - Go to Amazon ECR in AWS Console
   - Create repository named `authentication-service`

2. **Create ECS Cluster**
   - Go to Amazon ECS in AWS Console
   - Create cluster named `authentication-cluster`
   - Use Fargate launch type

3. **Create Application Load Balancer**
   - Go to EC2 > Load Balancers
   - Create Application Load Balancer
   - Configure target group for port 8080

4. **Create IAM Roles**
   - `ecsTaskExecutionRole` with `AmazonECSTaskExecutionRolePolicy`
   - `ecsTaskRole` for application-specific permissions

### Step 3: Update Configuration Files

1. **Update task-definition.json**
   ```json
   # Replace YOUR_ACCOUNT_ID with your actual AWS Account ID
   # Update region if different from us-east-1
   ```

2. **Update GitHub Actions workflow**
   ```yaml
   # Verify region and service names match your setup
   ```

### Step 4: Deploy the Application

#### Automatic Deployment (Recommended)
```bash
# Push to main/master branch
git add .
git commit -m "Add DevOps pipeline configuration"
git push origin main
```

#### Manual Deployment
```bash
# Using PowerShell (Windows)
./deploy.ps1

# Using Bash (Linux/Mac)
./deploy.sh
```

## 🔧 Configuration Management

### Environment Variables
Configure the following in your ECS task definition:

```json
{
  "environment": [
    {
      "name": "SPRING_PROFILES_ACTIVE",
      "value": "prod"
    },
    {
      "name": "SERVER_PORT",
      "value": "8080"
    }
  ]
}
```

### Secrets Management
Store sensitive data in AWS Systems Manager Parameter Store:

```bash
# JWT Secret Key
aws ssm put-parameter \
  --name "/auth-service/jwt-secret" \
  --value "your-secure-jwt-secret-key" \
  --type "SecureString"

# Database Password (if using external DB)
aws ssm put-parameter \
  --name "/auth-service/db-password" \
  --value "your-database-password" \
  --type "SecureString"
```

## 📊 Monitoring and Logging

### CloudWatch Logs
- Application logs are automatically sent to CloudWatch
- Log group: `/ecs/authentication-service`

### Health Checks
- Application Load Balancer health check: `/actuator/health`
- Container health check: Built into Docker image

### Metrics
- CPU and memory utilization
- Request count and response times
- Error rates

## 🔄 Auto Scaling

The service is configured with auto scaling based on CPU utilization:
- Minimum instances: 1
- Maximum instances: 4
- Target CPU utilization: 70%

## 🛡️ Security Features

### Network Security
- Private subnets for ECS tasks
- Security groups with minimal required access
- Application Load Balancer in public subnets

### Container Security
- Non-root user in Docker container
- Regular security scanning with ECR
- Secrets managed through AWS Parameter Store

### Application Security
- JWT-based authentication
- HTTPS termination at load balancer
- CORS configuration

## 🔧 Troubleshooting

### Common Issues

1. **Deployment Fails**
   ```bash
   # Check ECS service events
   aws ecs describe-services --cluster authentication-cluster --services authentication-service
   ```

2. **Health Check Failures**
   ```bash
   # Check application logs
   aws logs tail /ecs/authentication-service --follow
   ```

3. **Task Won't Start**
   ```bash
   # Check task definition and resource allocation
   aws ecs describe-task-definition --task-definition authentication-service
   ```

### Useful Commands

```bash
# View service status
aws ecs describe-services --cluster authentication-cluster --services authentication-service

# View running tasks
aws ecs list-tasks --cluster authentication-cluster --service-name authentication-service

# View logs
aws logs tail /ecs/authentication-service --follow

# Force new deployment
aws ecs update-service --cluster authentication-cluster --service authentication-service --force-new-deployment
```

## 📈 Scaling and Performance

### Vertical Scaling
Adjust CPU and memory in task definition:
```json
{
  "cpu": "1024",
  "memory": "2048"
}
```

### Horizontal Scaling
Update auto scaling policies or desired count:
```bash
aws ecs update-service --cluster authentication-cluster --service authentication-service --desired-count 3
```

## 🎯 Next Steps

1. **Set up monitoring alerts** in CloudWatch
2. **Configure SSL certificate** for HTTPS
3. **Set up blue-green deployments** for zero-downtime updates
4. **Implement database integration** if needed
5. **Set up staging environment** for testing

## 📞 Support

For issues or questions:
1. Check CloudWatch logs
2. Review AWS ECS service events
3. Verify GitHub Actions workflow logs
4. Check IAM permissions

## 🔗 Useful Links

- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [GitHub Actions AWS Deployment](https://github.com/aws-actions)
- [Spring Boot on AWS](https://spring.io/guides/gs/spring-boot-docker/)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)