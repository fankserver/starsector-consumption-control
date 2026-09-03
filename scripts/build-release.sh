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

python3 - "$STAGE/Consumption Control/mod_info.json" "$STAGE/Consumption Control/consumptioncontrol.version" "$VERSION" "$ROOT/dist/$ASSET" "$STAGE" <<'PY'
import json
import re
import sys
import zipfile
from pathlib import Path

mod_info = Path(sys.argv[1])
version_file = Path(sys.argv[2])
version = sys.argv[3]
asset = Path(sys.argv[4])
stage = Path(sys.argv[5])

metadata = json.loads(mod_info.read_text(encoding="utf-8"))
metadata["version"] = version
mod_info.write_text(json.dumps(metadata, indent=2) + "\n", encoding="utf-8")

match = re.fullmatch(r"(\d+)\.(\d+)\.(\d+)(.*)", version)
if not match:
    raise SystemExit(f"unsupported Version Checker version: {version}")
version_metadata = json.loads(version_file.read_text(encoding="utf-8"))
patch = int(match.group(3)) if not match.group(4) else match.group(3) + match.group(4)
version_metadata["modVersion"] = {
    "major": int(match.group(1)),
    "minor": int(match.group(2)),
    "patch": patch,
}
version_file.write_text(json.dumps(version_metadata, indent=2) + "\n", encoding="utf-8")

with zipfile.ZipFile(asset, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
    for path in sorted(stage.rglob("*")):
        if path.is_file():
            archive.write(path, path.relative_to(stage))
PY

printf 'Built %s\n' "$ROOT/dist/$ASSET"
printf 'version=%s\nasset=%s\n' "$VERSION" "$ASSET"
