#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-compose}"

case "${MODE}" in
  compose)
    docker compose -f compose-standalone.yaml up -d --build
    ;;
  k8s)
    ./k8s/setup-cluster.sh

    for service in catalogservice feedbackservice; do
      (cd "${service}" && chmod +x mvnw && ./mvnw clean package -DskipTests)
      docker build -t "${service}:local" "./${service}"
      kind load docker-image "${service}:local" --name avaliator
    done

    kubectl apply -f k8s/catalogservice/
    kubectl apply -f k8s/feedbackservice/
    kubectl set image deployment/catalogservice \
      catalogservice=catalogservice:local -n avaliator
    kubectl set image deployment/feedbackservice \
      feedbackservice=feedbackservice:local -n avaliator

    kubectl rollout status deployment/catalogservice -n avaliator --timeout=180s
    kubectl rollout status deployment/feedbackservice -n avaliator --timeout=180s
    ;;
  *)
    echo "Usage: $0 [compose|k8s]"
    exit 1
    ;;
esac
