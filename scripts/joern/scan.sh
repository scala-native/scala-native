#!/usr/bin/env bash
# Static analysis of the native runtime (C/C++ sources shipped as resources)
# with Joern: https://joern.io
#
# Usage: scripts/joern/scan.sh [output-dir]
#
# Runs the bundled joern-scan queries (minus code metrics) and the custom
# queries in scripts/joern/runtime-checks.sc. Reports are written to
# output-dir (default: target/joern).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
OUT="$(mkdir -p "${1:-$ROOT/target/joern}" && cd "${1:-$ROOT/target/joern}" && pwd)"
SRC="$OUT/src"
CPG="$OUT/cpg.bin"
TAGS="${JOERN_TAGS:-default,uaf,badfn,integers,strings,race-condition}"

# Homebrew only links the joern binary, the other tools stay in libexec.
if command -v brew >/dev/null && [ -d "$(brew --prefix)/opt/joern/libexec" ]; then
  PATH="$PATH:$(brew --prefix)/opt/joern/libexec"
fi

for tool in joern joern-parse joern-scan; do
  command -v "$tool" >/dev/null || {
    echo >&2 "$tool not found, install Joern (e.g. brew install joern)"
    exit 1
  }
done

# Stage only the native sources so the CPG does not contain Scala/Java code.
rm -rf "$SRC"
mkdir -p "$SRC"
for module in nativelib clib posixlib javalib windowslib; do
  dir="$ROOT/$module/src/main/resources/scala-native"
  [ -d "$dir" ] && rsync -a --include='*/' \
    --include='*.c' --include='*.cpp' --include='*.h' --include='*.hpp' \
    --exclude='*' "$dir/" "$SRC/$module/"
done

joern-parse "$SRC" --output "$CPG" --language newc

if [ -z "$(joern-scan --list-query-names 2>/dev/null | grep -v '^Writing')" ]; then
  joern-scan --updatedb
fi

# joern-scan re-parses the source dir, it cannot reuse an existing CPG.
(cd "$OUT" && joern-scan --overwrite --tags "$TAGS" "$SRC") 2>&1 |
  { grep '^Result:' || true; } | sed "s|$SRC/||" | tee "$OUT/joern-scan.txt"

# joern creates its workspace/ in the current directory.
(cd "$OUT" && joern --script "$ROOT/scripts/joern/runtime-checks.sc" \
  --param cpgFile="$CPG" --param outFile="$OUT/runtime-checks.txt" \
  --param srcRoot="$SRC/") |
  { grep -E '^(unchecked|realloc|possible|[0-9]+ findings)' || true; }

echo
echo "Reports:"
echo "  $OUT/joern-scan.txt"
echo "  $OUT/runtime-checks.txt"
