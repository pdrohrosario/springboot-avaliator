#!/bin/bash
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

CLUSTER_NAME="avaliator"
LOCAL_REGISTRY_CONTAINER_NAME="${LOCAL_REGISTRY_CONTAINER_NAME:-local-registry}"

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

    echo -e "${BLUE}🔗 Cluster created (no local CI network coupling).${NC}"
fi

CURRENT_SERVER="$(kubectl config view --minify -o jsonpath='{.clusters[0].cluster.server}' 2>/dev/null || true)"
if echo "${CURRENT_SERVER}" | grep -q "https://0.0.0.0:"; then
    CONTROL_PLANE_CONTAINER="${CLUSTER_NAME}-control-plane"
    CONTROL_PLANE_IP="$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' "${CONTROL_PLANE_CONTAINER}" 2>/dev/null || true)"
    if [ -n "${CONTROL_PLANE_IP}" ]; then
        echo -e "${YELLOW}⚠️  Fixing kubeconfig server from 0.0.0.0 to ${CONTROL_PLANE_IP}...${NC}"
        kubectl config set-cluster "kind-${CLUSTER_NAME}" --server="https://${CONTROL_PLANE_IP}:6443" >/dev/null
        echo -e "${GREEN}✅ kubeconfig server fixed.${NC}"
    fi
fi

for CONTAINER_NAME in "${LOCAL_REGISTRY_CONTAINER_NAME}" "jenkins"; do
    if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
        if [ "$(docker inspect -f '{{json .NetworkSettings.Networks.kind}}' "${CONTAINER_NAME}")" = "null" ]; then
            echo -e "${BLUE}🔗 Connecting ${CONTAINER_NAME} to kind network...${NC}"
            docker network connect "kind" "${CONTAINER_NAME}"
            echo -e "${GREEN}✅ ${CONTAINER_NAME} connected to kind network!${NC}"
        else
            echo -e "${GREEN}✅ ${CONTAINER_NAME} already connected to kind network.${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  Container '${CONTAINER_NAME}' not found — start CI/CD compose first.${NC}"
    fi
done

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
echo -e "  Run ${YELLOW}./scripts/up.sh k8s${NC} to deploy the applications."
echo -e "  Delete cluster:    ${YELLOW}kind delete cluster --name ${CLUSTER_NAME}${NC}"
echo ""
