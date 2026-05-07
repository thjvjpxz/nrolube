#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if ! command -v mvn >/dev/null 2>&1; then
  echo "Error: maven is not installed. Install it with: sudo apt install maven"
  exit 1
fi

echo "Building jar via Maven..."
mvn -q -DskipTests clean package dependency:copy-dependencies -DincludeScope=runtime

echo "Done: target/Michelin_Boy-1.0.0.jar"
