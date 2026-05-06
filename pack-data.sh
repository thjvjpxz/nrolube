#!/bin/sh
set -eu

DATA_DIR="${1:-data}"
OUTPUT="${2:-data.tar.gz}"

if [ ! -d "$DATA_DIR" ]; then
  echo "Error: directory '$DATA_DIR' not found." >&2
  exit 1
fi

tar -czf "$OUTPUT" "$DATA_DIR"
echo "Created archive: $OUTPUT"
