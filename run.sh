#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "NRO SERVER"
java -Xms1G -Xmx1G -Xss512k -XX:+UseZGC -cp "dist/Michelin_Boy.jar:lib/*" server.ServerManager
