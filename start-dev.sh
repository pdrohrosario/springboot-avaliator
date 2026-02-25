#!/bin/bash
set -e

# Terminal colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}🚀 Step 1: Starting CI/CD Infrastructure...${NC}"
docker compose -f ../ci-cd/compose.yaml up -d

echo -e "${YELLOW}⏳ Waiting for Local Registry to be ready...${NC}"
until curl -s http://localhost:5001/v2/ > /dev/null; do
  echo -n "."
  sleep 2
done
echo -e "\n${GREEN}✅ Registry ready!${NC}"

echo -e "${BLUE}☸️  Step 2: Preparing Kubernetes Cluster...${NC}"
./k8s/setup-cluster.sh

echo -e "${BLUE}🔨 Step 3: Compiling Services...${NC}"
for service in catalogservice feedbackservice; do
    echo -e "${YELLOW}  → Building ${service}...${NC}"
    (cd $service && chmod +x mvnw && ./mvnw clean package -DskipTests)
done

echo -e "${BLUE}📦 Step 4: Building & Loading Docker Images...${NC}"
for service in catalogservice feedbackservice; do
    echo -e "${YELLOW}  → Docker build: ${service}${NC}"
    docker build -t localhost:5001/${service}:latest ./${service}
    echo -e "${YELLOW}  → Loading into Kind: ${service}${NC}"
    kind load docker-image localhost:5001/${service}:latest --name avaliator
done

echo -e "${BLUE}🚀 Step 5: Deploying Applications to K8s...${NC}"
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
echo -e "  - Catalog Service (Ingress):  http://localhost/catalog/"
echo -e "  - Feedback Service (Ingress): http://localhost/feedback/"
echo -e "  - Jenkins:                   http://localhost:8080"
echo -e "  - Grafana:                   http://localhost:3000"
echo -e "  - Prometheus:                http://localhost:9090"
echo -e "${GREEN}========================================${NC}"
