#!/usr/bin/env bash
set -euo pipefail

ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
STARSECTOR_DIR=${STARSECTOR_DIR:-/mnt/c/Program Files (x86)/Fractal Softworks/Starsector}
IMAGE=${JAVA_BUILD_IMAGE:-eclipse-temurin:17-jdk}

if [[ ! -f "$STARSECTOR_DIR/starsector-core/starfarer.api.jar" ]]; then
  echo "STARSECTOR_DIR does not point to a Starsector installation: $STARSECTOR_DIR" >&2
  exit 1
fi
if [[ ! -f "$STARSECTOR_DIR/mods/LunaLib/jars/LunaLib.jar" ]]; then
  echo "LunaLib.jar was not found below STARSECTOR_DIR: $STARSECTOR_DIR" >&2
  exit 1
fi

rm -rf "$ROOT/build/classes"
mkdir -p "$ROOT/build/classes" "$ROOT/mod/jars"

docker run --rm \
  --user "$(id -u):$(id -g)" \
  -v "$ROOT:/work" \
  -v "$STARSECTOR_DIR/starsector-core:/starsector-core:ro" \
  -v "$STARSECTOR_DIR/mods/LunaLib/jars/LunaLib.jar:/deps/LunaLib.jar:ro" \
  -w /work \
  "$IMAGE" \
  sh -lc '
    javac --release 17 \
      -cp /starsector-core/starfarer.api.jar:/deps/LunaLib.jar \
      -d build/classes \
      $(find src/main/java -name "*.java" -print) &&
    jar --create --file mod/jars/ConsumptionControl.jar -C build/classes .
  '

echo "Built $ROOT/mod/jars/ConsumptionControl.jar"
