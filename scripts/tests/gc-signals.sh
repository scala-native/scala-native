#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."
test_dir=$(mktemp -d)
trap 'rm -f "$test_dir/gc-signals"; rmdir "$test_dir"' EXIT
runtime=nativelib/src/main/resources/scala-native
if [[ $(uname -s) == Darwin ]]; then
  dead_strip=-Wl,-dead_strip
  extra_libs=(-pthread)
else
  dead_strip=-Wl,--gc-sections
  extra_libs=(-ldl)
fi
for gc in IMMIX COMMIX; do
"${CC:-clang}" -std=gnu11 -O2 -pthread -Wall -Wextra \
  -Wno-unused-parameter -Wno-unused-function -Wno-sign-compare \
  -ffunction-sections -fdata-sections "$dead_strip" \
  -DSCALANATIVE_GC_USE_YIELDPOINT_TRAPS -D"SCALANATIVE_GC_$gc" \
  -DSCALANATIVE_MULTITHREADING_ENABLED -I"$runtime" -I"$runtime/gc" \
  scripts/tests/gc-signals.c "$runtime/SignalDiagnostics.c" \
  "$runtime/gc/shared/YieldPointTrap.c" "${extra_libs[@]}" -o "$test_dir/gc-signals"
"$test_dir/gc-signals"
done
