#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

if ! command -v ant >/dev/null 2>&1; then
  echo "Error: ant is not installed. Install it with: sudo apt install ant"
  exit 1
fi

if [ -n "${JAVA_HOME:-}" ]; then
  JDK_HOME="$JAVA_HOME"
else
  JAVA_BIN="$(readlink -f "$(command -v java)")"
  JDK_HOME="$(dirname "$(dirname "$JAVA_BIN")")"
fi

if [ ! -x "$JDK_HOME/bin/javac" ]; then
  echo "Error: Could not detect a valid JDK home."
  echo "Set JAVA_HOME, for example: export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64"
  exit 1
fi

echo "Building jar via NetBeans project (nbproject)..."
ant -Dplatforms.JDK_21.home="$JDK_HOME" -f build.xml clean jar

echo "Done: dist/Michelin_Boy.jar"
