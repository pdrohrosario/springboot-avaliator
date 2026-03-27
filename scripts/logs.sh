#!/usr/bin/env bash
set -euo pipefail

MODE="${1:-compose}"
SERVICE="${2:-}"

case "${MODE}" in
  compose)
    if [[ -n "${SERVICE}" ]]; then
      docker compose -f compose-standalone.yaml logs -f "${SERVICE}"
    else
      docker compose -f compose-standalone.yaml logs -f
    fi
    ;;
  k8s)
    if [[ -n "${SERVICE}" ]]; then
      kubectl logs -n avaliator deploy/"${SERVICE}" -f
    else
      kubectl logs -n avaliator -l app.kubernetes.io/part-of=avaliator -f
    fi
    ;;
  *)
    echo "Usage: $0 [compose|k8s] [service]"
    exit 1
    ;;
esac
