#!/bin/sh
set -eu

ARCHIVE="${1:-data.tar.gz}"
TARGET_DIR="${2:-.}"

if [ ! -f "$ARCHIVE" ]; then
  echo "Error: archive '$ARCHIVE' not found." >&2
  exit 1
fi

mkdir -p "$TARGET_DIR"
tar -xzf "$ARCHIVE" -C "$TARGET_DIR"
echo "Extracted '$ARCHIVE' into '$TARGET_DIR'"
