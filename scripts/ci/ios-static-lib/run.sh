#!/usr/bin/env bash
# Builds Scala Native code as a static library for the iOS simulator, links it
# into a host program written in C and runs it inside of a booted simulator.
# Covers https://github.com/scala-native/scala-native/issues/4334
#
# Usage: scripts/ci/ios-static-lib/run.sh <scala-binary-version, e.g. 3>
# Expects SCALANATIVE_TARGET_TRIPLE to point to the iOS simulator.
# Replaces sources of the sandbox project, intended to be used only in the CI.
set -euo pipefail

version="${1:?missing Scala binary version}"
triple="${SCALANATIVE_TARGET_TRIPLE:?missing SCALANATIVE_TARGET_TRIPLE}"
here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
root="$(cd "$here/../.." && pwd)"
cd "$root"

rm -rf sandbox/src/main/scala*
mkdir -p sandbox/src/main/scala
cp "$here/StaticLib.scala" sandbox/src/main/scala/

project="sandbox${version/./_}"
sbt "set $project/nativeConfig ~= { _.withBuildTarget(scala.scalanative.build.BuildTarget.libraryStatic) }" \
  "$project/nativeLink"

lib="$(find "$root/target" "$root/sandbox" -name '*sandbox*.a' -newer "$here/run.sh" 2>/dev/null | head -n 1)"
test -n "$lib" || { echo "Static library not found" >&2; exit 1; }
echo "Using static library: $lib"

out="$(mktemp -d)"
xcrun --sdk iphonesimulator clang -target "$triple" -c "$here/host.c" -o "$out/host.o"
xcrun --sdk iphonesimulator clang++ -target "$triple" "$out/host.o" "$lib" -o "$out/host"

"$root/scripts/ci/ios-simulator-run.sh" "$out/host"
