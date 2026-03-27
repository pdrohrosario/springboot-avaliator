#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-compose}"

case "${MODE}" in
  compose)
    docker compose -f compose-standalone.yaml down
    ;;
  k8s)
    kubectl delete -f k8s/feedbackservice/ --ignore-not-found=true
    kubectl delete -f k8s/catalogservice/ --ignore-not-found=true
    ;;
  *)
    echo "Usage: $0 [compose|k8s]"
    exit 1
    ;;
esac
