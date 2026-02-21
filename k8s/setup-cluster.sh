#!/bin/bash
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

CLUSTER_NAME="avaliator"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  ☸️  K8s Local Setup - Avaliator${NC}"
echo -e "${BLUE}========================================${NC}"

if ! command -v kubectl &> /dev/null; then
    echo -e "${YELLOW}📦 Installing kubectl...${NC}"
    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
    chmod +x kubectl
    sudo mv kubectl /usr/local/bin/
    echo -e "${GREEN}✅ kubectl installed!${NC}"
else
    echo -e "${GREEN}✅ kubectl already installed: $(kubectl version --client --short 2>/dev/null || kubectl version --client)${NC}"
fi

if ! command -v kind &> /dev/null; then
    echo -e "${YELLOW}📦 Installing kind...${NC}"
    curl -Lo kind https://kind.sigs.k8s.io/dl/v0.25.0/kind-linux-amd64
    chmod +x kind
    sudo mv kind /usr/local/bin/
    echo -e "${GREEN}✅ kind installed!${NC}"
else
    echo -e "${GREEN}✅ kind already installed: $(kind version)${NC}"
fi

if kind get clusters 2>/dev/null | grep -q "^${CLUSTER_NAME}$"; then
    echo -e "${YELLOW}⚠️  Cluster '${CLUSTER_NAME}' already exists. Skipping creation.${NC}"
    echo -e "${YELLOW}   (To recreate: kind delete cluster --name ${CLUSTER_NAME})${NC}"
else
    echo -e "${BLUE}🔨 Creating kind cluster '${CLUSTER_NAME}'...${NC}"
    kind create cluster --config k8s/kind-config.yaml --name ${CLUSTER_NAME}
    echo -e "${GREEN}✅ Cluster created!${NC}"

    echo -e "${BLUE}🔗 Connecting cluster to CI/CD network...${NC}"
    # Conecta todos os nós do cluster na rede do Docker Compose
    for node in $(kind get nodes --name ${CLUSTER_NAME}); do
        docker network connect ci-cd-network "${node}" || true
    done
    echo -e "${GREEN}✅ Connected to 'ci-cd-network'!${NC}"
fi

kubectl cluster-info --context kind-${CLUSTER_NAME}

echo -e "${BLUE}🌐 Installing NGINX Ingress Controller...${NC}"
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml

echo -e "${YELLOW}⏳ Waiting for Ingress Controller to be ready...${NC}"
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=120s
echo -e "${GREEN}✅ Ingress Controller ready!${NC}"

echo ""
echo -e "${BLUE}📋 Applying Kubernetes manifests...${NC}"

echo -e "${YELLOW}  → Namespace${NC}"
kubectl apply -f k8s/namespace.yaml

echo -e "${YELLOW}  → PostgreSQL (Secret + ConfigMap + PVC + Deployment)${NC}"
kubectl apply -f k8s/postgres/

echo -e "${YELLOW}  → Ingress Controller Resources${NC}"
kubectl apply -f k8s/ingress.yaml

echo -e "${YELLOW}  ⏳ Waiting for PostgreSQL to be ready...${NC}"
kubectl wait --namespace avaliator \
  --for=condition=ready pod \
  --selector=app=postgres \
  --timeout=120s
echo -e "${GREEN}  ✅ PostgreSQL ready!${NC}"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  ✅ Cluster Infrastructure Ready!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${BLUE}📊 Cluster status:${NC}"
kubectl get nodes
echo ""
echo -e "${BLUE}📌 Next steps:${NC}"
echo -e "  Run ${YELLOW}./start-dev.sh${NC} to deploy the applications."
echo -e "  Delete cluster:    ${YELLOW}kind delete cluster --name ${CLUSTER_NAME}${NC}"
echo ""
