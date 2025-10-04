#!/bin/bash

# AWS ECS Deployment Script for Authentication Service
set -e

# Configuration
AWS_REGION=${AWS_REGION:-us-east-1}
CLUSTER_NAME="authentication-cluster"
SERVICE_NAME="authentication-service"
ECR_REPOSITORY="authentication-service"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Starting AWS ECS Deployment...${NC}"

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "${RED}❌ AWS CLI is not installed. Please install it first.${NC}"
    exit 1
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}❌ Docker is not running. Please start Docker first.${NC}"
    exit 1
fi

# Get AWS Account ID
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
if [ -z "$ACCOUNT_ID" ]; then
    echo -e "${RED}❌ Unable to get AWS Account ID. Please check your AWS credentials.${NC}"
    exit 1
fi

echo -e "${GREEN}✅ AWS Account ID: $ACCOUNT_ID${NC}"

# Set ECR URI
ECR_URI="$ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPOSITORY"

# Login to ECR
echo -e "${YELLOW}🔐 Logging in to Amazon ECR...${NC}"
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_URI

# Build Docker image
echo -e "${YELLOW}🏗️  Building Docker image...${NC}"
docker build -t $ECR_REPOSITORY:latest .

# Tag image for ECR
docker tag $ECR_REPOSITORY:latest $ECR_URI:latest
docker tag $ECR_REPOSITORY:latest $ECR_URI:$(git rev-parse --short HEAD)

# Push to ECR
echo -e "${YELLOW}📤 Pushing image to ECR...${NC}"
docker push $ECR_URI:latest
docker push $ECR_URI:$(git rev-parse --short HEAD)

# Update ECS service
echo -e "${YELLOW}🔄 Updating ECS service...${NC}"
aws ecs update-service \
    --cluster $CLUSTER_NAME \
    --service $SERVICE_NAME \
    --force-new-deployment \
    --region $AWS_REGION

# Wait for deployment to complete
echo -e "${YELLOW}⏳ Waiting for deployment to complete...${NC}"
aws ecs wait services-stable \
    --cluster $CLUSTER_NAME \
    --services $SERVICE_NAME \
    --region $AWS_REGION

# Get service status
SERVICE_STATUS=$(aws ecs describe-services \
    --cluster $CLUSTER_NAME \
    --services $SERVICE_NAME \
    --region $AWS_REGION \
    --query 'services[0].deployments[0].status' \
    --output text)

if [ "$SERVICE_STATUS" = "PRIMARY" ]; then
    echo -e "${GREEN}✅ Deployment completed successfully!${NC}"
    
    # Get load balancer DNS name
    ALB_DNS=$(aws elbv2 describe-load-balancers \
        --names "authentication-service-alb" \
        --region $AWS_REGION \
        --query 'LoadBalancers[0].DNSName' \
        --output text 2>/dev/null || echo "Load balancer not found")
    
    if [ "$ALB_DNS" != "Load balancer not found" ]; then
        echo -e "${GREEN}🌐 Service URL: http://$ALB_DNS/api/v1/auth${NC}"
        echo -e "${GREEN}🏥 Health Check: http://$ALB_DNS/actuator/health${NC}"
    fi
else
    echo -e "${RED}❌ Deployment failed. Service status: $SERVICE_STATUS${NC}"
    exit 1
fi

echo -e "${GREEN}🎉 Deployment completed successfully!${NC}"