#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

WAIT_SEC=60

echo "Restarting NRO SERVER"
sleep "$WAIT_SEC"
exec ./run.sh
