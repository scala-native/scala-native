#!/usr/bin/env bash
# Run Scala Native programs and tests under memory checkers and report leaks.
#
# Usage: scripts/sanitizers/run.sh <asan|lsan|valgrind> [sbt-project]
#
#   asan      build with -fsanitize=address, run, fail on memory errors in
#             the C/C++ runtime and in C library calls (Scala code generated
#             through LLVM IR is not instrumented)
#   lsan      as asan, with LeakSanitizer enabled; summarises leaked bytes
#   valgrind  build without sanitizers, run the binary under memcheck.
#             Currently stops at startup: the stack overflow guards touch pages
#             far below the stack pointer, which memcheck's main stack rejects.
#
# sbt-project defaults to sandbox3. For asan/lsan, SBT_COMMAND overrides the
# sbt command, e.g. SBT_COMMAND="tests3/testOnly *ZoneTest" (any test binary
# built in the same sbt run inherits the sanitizer).
#
# Needs Linux with clang and valgrind; on macOS use scripts/sanitizers/docker.sh.
# Reports are written to target/sanitizers/<mode>.
set -euo pipefail

MODE="${1:?usage: $0 <asan|lsan|valgrind> [sbt-project]}"
PROJECT="${2:-sandbox3}"
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
HERE="$ROOT/scripts/sanitizers"
OUT="$ROOT/target/sanitizers/$MODE"
rm -rf "$OUT"
mkdir -p "$OUT"

export SCALANATIVE_MODE="${SCALANATIVE_MODE:-debug}"
export SCALANATIVE_OPTIMIZE="${SCALANATIVE_OPTIMIZE:-false}"
export GC_MAXIMUM_HEAP_SIZE="${GC_MAXIMUM_HEAP_SIZE:-1G}"

# The GC scans stacks conservatively, which ASan's fake stacks would hide.
ASAN_BASE="detect_stack_use_after_return=0:handle_segv=0:handle_sigbus=0"
ASAN_BASE+=":allocator_may_return_null=1:log_path=$OUT/report"

# Without this a failed build would be reported as "no leaks".
run_sbt() {
  local status=0
  sbt "$@" || status=$?
  if ! ls "$OUT"/report.* >/dev/null 2>&1 && [ $status -ne 0 ]; then
    echo >&2 "sbt failed (exit $status) before any sanitized program reported"
    exit $status
  fi
  return $status
}

summarise_lsan() {
  local leaks
  leaks=$(cat "$OUT"/report.* 2>/dev/null |
    grep -E '^SUMMARY: AddressSanitizer: [0-9]+ byte' || true)
  echo
  if [ -z "$leaks" ]; then
    echo "LeakSanitizer: no leaks detected"
    return 0
  fi
  echo "LeakSanitizer reports (per process):"
  echo "$leaks" | sed 's/^/  /'
  echo "$leaks" | awk '{b += $3; a += $7} END {
    printf "Total: %d byte(s) leaked in %d allocation(s)\n", b, a }'
  echo "Full reports: $OUT/report.*"
  return 1
}

summarise_asan_errors() {
  local errors
  errors=$(grep -l 'ERROR: AddressSanitizer' "$OUT"/report.* 2>/dev/null || true)
  if [ -n "$errors" ]; then
    echo
    echo "AddressSanitizer errors:"
    grep -h -A3 'ERROR: AddressSanitizer' $errors | sed 's/^/  /'
    return 1
  fi
}

case "$MODE" in
asan)
  export SCALANATIVE_SANITIZER=address
  export ASAN_OPTIONS="$ASAN_BASE:detect_leaks=0:halt_on_error=1"
  status=0
  run_sbt "${SBT_COMMAND:-$PROJECT/run}" || status=$?
  summarise_asan_errors || status=1
  exit $status
  ;;

lsan)
  export SCALANATIVE_SANITIZER=address
  export ASAN_OPTIONS="$ASAN_BASE:detect_leaks=1:halt_on_error=0"
  export LSAN_OPTIONS="suppressions=$HERE/lsan.supp:print_suppressions=0"
  status=0
  run_sbt "${SBT_COMMAND:-$PROJECT/run}" || status=$?
  summarise_asan_errors || status=1
  summarise_lsan || status=1
  exit $status
  ;;

valgrind)
  unset SCALANATIVE_SANITIZER
  marker="$OUT/.link-start"
  touch "$marker"
  sleep 1 # make sure the linked binary is strictly newer than the marker
  sbt "$PROJECT/nativeLink"
  binary=$(find "$ROOT" -path '*/target/*' -type f -perm -u+x \
    -newer "$marker" ! -name '*.so' ! -name '*.o' ! -name '*.ll' |
    head -1)
  if [ -z "$binary" ]; then
    echo >&2 "Could not find the binary linked by $PROJECT/nativeLink"
    exit 1
  fi
  echo "Running $binary under valgrind"
  # Uninitialised-value checks are disabled: the GC scans stacks and the
  # heap conservatively and reads memory memcheck considers undefined.
  # Objects in the GC heap come from mmap and are not tracked as leaks.
  status=0
  valgrind --tool=memcheck --leak-check=full \
    --show-leak-kinds=definite,indirect,possible \
    --errors-for-leak-kinds=definite,indirect \
    --undef-value-errors=no \
    --suppressions="$HERE/valgrind.supp" \
    --error-exitcode=99 \
    --log-file="$OUT/memcheck.%p.log" \
    "$binary" || status=$?
  echo
  for log in "$OUT"/memcheck.*.log; do
    echo "$log:"
    sed -n '/HEAP SUMMARY/,/ERROR SUMMARY/p' "$log" | sed 's/^==[0-9]*== /  /'
  done
  if grep -qs 'tryGrowStack' "$OUT"/memcheck.*.log &&
    grep -qs 'SIGSEGV' "$OUT"/memcheck.*.log; then
    echo >&2
    echo >&2 "valgrind cannot run Scala Native programs yet: tryGrowStack in"
    echo >&2 "stackOverflowGuards.c touches pages far below the stack pointer"
    echo >&2 "and memcheck terminates the process. The numbers above are from"
    echo >&2 "startup only, they say nothing about the program."
  fi
  exit $status
  ;;

*)
  echo >&2 "Unknown mode '$MODE', expected asan, lsan or valgrind"
  exit 2
  ;;
esac
