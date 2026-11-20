#!/usr/bin/env bash
# Version: 1.0.0
# Builds the INATrace Spring Boot backend Docker image (optionally pushing to registry).
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
# shellcheck source=lib/common.sh
. "${SCRIPT_DIR}/lib/common.sh"

IMAGE_PREFIX="${IMAGE_PREFIX:-mi-organizacion}"
IMAGE_TAG="${IMAGE_TAG:-v1.0.0}"
PUSH="false"

usage() {
  cat <<'EOF'
Usage: 02-build-backend.sh [OPTIONS]

Builds the INATrace Java 17 / Spring Boot backend Docker image.

Options:
  --image-prefix <prefix>  Container registry/namespace prefix (default: mi-organizacion)
  --tag <tag>              Docker image tag (default: v1.0.0)
  --push                   Push built image to container registry after build
  -h, --help               Show this help message and exit

Example:
  ./02-build-backend.sh --image-prefix my-org --tag v1.0.0
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --image-prefix)
      [[ $# -ge 2 ]] || { log_error "--image-prefix requires a value"; exit 1; }
      IMAGE_PREFIX="$2"
      shift 2
      ;;
    --tag)
      [[ $# -ge 2 ]] || { log_error "--tag requires a value"; exit 1; }
      IMAGE_TAG="$2"
      shift 2
      ;;
    --push)
      PUSH="true"
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      log_error "Unknown option: $1"
      usage >&2
      exit 1
      ;;
  esac
done

require_cmd docker
[[ -f "${REPO_ROOT}/Dockerfile" ]] || { log_error "Dockerfile not found in ${REPO_ROOT}"; exit 1; }

FULL_IMAGE="${IMAGE_PREFIX}/inatrace-backend:${IMAGE_TAG}"
log_info "Building backend Docker image: ${FULL_IMAGE}"
docker build -t "${FULL_IMAGE}" "${REPO_ROOT}"
log_ok "Backend image built: ${FULL_IMAGE}"

if [[ "${PUSH}" == "true" ]]; then
  log_info "Pushing image: ${FULL_IMAGE}"
  docker push "${FULL_IMAGE}"
  log_ok "Backend image pushed: ${FULL_IMAGE}"
fi
