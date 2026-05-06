#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

case "${1:-up}" in
  up)
    docker compose up -d mysql
    echo "MySQL is starting on localhost:3306"
    echo "user: root | pass: root | db: tomahoc_db"
    ;;
  logs)
    docker compose logs -f mysql
    ;;
  down)
    docker compose down
    ;;
  reset)
    docker compose down -v
    docker compose up -d mysql
    echo "Database reset and re-import triggered."
    ;;
  *)
    echo "Usage: $0 {up|logs|down|reset}"
    exit 1
    ;;
esac
