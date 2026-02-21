#!/bin/bash
set -e

# Terminal colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}🚀 Step 1: Starting CI/CD Infrastructure...${NC}"
docker compose -f ../ci-cd/compose.yaml up -d

echo -e "${BLUE}☸️  Step 2: Preparing Kubernetes Cluster...${NC}"
./k8s/setup-cluster.sh

echo -e "${BLUE}🔨 Step 3: Compiling Services...${NC}"
(cd catalogservice && chmod +x mvnw && ./mvnw clean package -DskipTests)
(cd feedbackservice && chmod +x mvnw && ./mvnw clean package -DskipTests)

echo -e "${BLUE}📦 Step 4: Building & Loading Docker Images...${NC}"
docker build -t localhost:5001/catalogservice:latest ./catalogservice
docker build -t localhost:5001/feedbackservice:latest ./feedbackservice

kind load docker-image localhost:5001/catalogservice:latest --name avaliator
kind load docker-image localhost:5001/feedbackservice:latest --name avaliator

echo -e "${BLUE}� Step 5: Deploying Applications to K8s...${NC}"
kubectl apply -f k8s/catalogservice/
kubectl apply -f k8s/feedbackservice/

echo -e "${YELLOW}⏳ Waiting for applications to be ready...${NC}"
kubectl wait --namespace avaliator \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/part-of=avaliator \
  --timeout=120s

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}✨ ALL SERVICES ARE UP AND RUNNING!${NC}"
echo -e "Access urls:"
echo -e "  - Catalog Service:  http://localhost/catalog/"
echo -e "  - Feedback Service: http://localhost/feedback/"
echo -e "  - Jenkins:          http://localhost:8080"
echo -e "  - Grafana:          http://localhost:3000"
echo -e "${GREEN}========================================${NC}"
