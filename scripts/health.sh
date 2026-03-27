#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-compose}"

case "${MODE}" in
  compose)
    docker compose -f compose-standalone.yaml ps
    curl -fsS http://localhost:8081/actuator/health >/dev/null && echo "catalogservice: OK"
    curl -fsS http://localhost:8882/actuator/health >/dev/null && echo "feedbackservice: OK"
    ;;
  k8s)
    kubectl get pods -n avaliator
    kubectl get ingress -n avaliator
    ;;
  *)
    echo "Usage: $0 [compose|k8s]"
    exit 1
    ;;
esac
