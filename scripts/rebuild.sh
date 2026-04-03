#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-compose}"

case "${MODE}" in
  compose)
    docker compose -f compose-standalone.yaml build --no-cache
    docker compose -f compose-standalone.yaml up -d
    ;;
  k8s)
    for service in catalogservice feedbackservice; do
      (cd "${service}" && chmod +x mvnw && ./mvnw clean package -DskipTests)
      docker build -t "${service}:local" "./${service}"
      kind load docker-image "${service}:local" --name avaliator
    done
    kubectl rollout restart deployment/catalogservice -n avaliator
    kubectl rollout restart deployment/feedbackservice -n avaliator
    ;;
  *)
    echo "Usage: $0 [compose|k8s]"
    exit 1
    ;;
esac
