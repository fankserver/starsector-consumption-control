#!/usr/bin/env bash
set -euo pipefail

ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
TAG=${1:?usage: build-release.sh vMAJOR.MINOR.PATCH}
VERSION=$("$ROOT/scripts/semver.py" "$TAG")
ASSET="Consumption-Control-${VERSION}.zip"
STAGE="$ROOT/build/release"

"$ROOT/scripts/build-ci-docker.sh"
rm -rf "$STAGE" "$ROOT/dist"
mkdir -p "$STAGE/Consumption Control" "$ROOT/dist"
cp -a "$ROOT/mod/." "$STAGE/Consumption Control/"

python3 - "$STAGE/Consumption Control/mod_info.json" "$VERSION" "$ROOT/dist/$ASSET" "$STAGE" <<'PY'
import json
import sys
import zipfile
from pathlib import Path

mod_info = Path(sys.argv[1])
version = sys.argv[2]
asset = Path(sys.argv[3])
stage = Path(sys.argv[4])

metadata = json.loads(mod_info.read_text(encoding="utf-8"))
metadata["version"] = version
mod_info.write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")

with zipfile.ZipFile(asset, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
    for path in sorted(stage.rglob("*")):
        if path.is_file():
            archive.write(path, path.relative_to(stage))
PY

printf 'Built %s\n' "$ROOT/dist/$ASSET"
printf 'version=%s\nasset=%s\n' "$VERSION" "$ASSET"
