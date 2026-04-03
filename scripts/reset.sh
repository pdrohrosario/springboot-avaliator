#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-compose}"

case "${MODE}" in
  compose)
    docker compose -f compose-standalone.yaml down -v
    ;;
  k8s)
    kind delete cluster --name avaliator
    ;;
  *)
    echo "Usage: $0 [compose|k8s]"
    exit 1
    ;;
esac
