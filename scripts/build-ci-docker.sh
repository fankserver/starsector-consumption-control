#!/usr/bin/env bash
set -euo pipefail

ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
IMAGE=${JAVA_BUILD_IMAGE:-eclipse-temurin:17-jdk}

rm -rf "$ROOT/build/ci-stubs" "$ROOT/build/ci-classes"
mkdir -p "$ROOT/build/ci-stubs" "$ROOT/build/ci-classes" "$ROOT/mod/jars"

docker run --rm \
  --user "$(id -u):$(id -g)" \
  -v "$ROOT:/work" \
  -w /work \
  "$IMAGE" \
  sh -lc '
    javac --release 17 -d build/ci-stubs $(find ci/stubs/java -name "*.java" -print) &&
    javac --release 17 -cp build/ci-stubs -d build/ci-classes \
      $(find src/main/java -name "*.java" -print) &&
    jar --create --file mod/jars/ConsumptionControl.jar -C build/ci-classes .
  '

echo "Built $ROOT/mod/jars/ConsumptionControl.jar using compile-only API stubs"
