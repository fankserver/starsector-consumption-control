#!/usr/bin/env bash
set -euo pipefail
ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
STARSECTOR_DIR=${STARSECTOR_DIR:-/mnt/c/Program Files (x86)/Fractal Softworks/Starsector}
DEST="$STARSECTOR_DIR/mods/Consumption Control"
rm -rf "$DEST"
mkdir -p "$DEST"
cp -a "$ROOT/mod/." "$DEST/"
echo "Installed to $DEST"
