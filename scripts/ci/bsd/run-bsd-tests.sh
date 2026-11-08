#!/bin/sh
set -eu

export ENABLE_EXPERIMENTAL_COMPILER=true
export SCALANATIVE_TEST_PREFETCH_DEBUG_INFO=1
export SCALANATIVE_TEST_DEBUG_SIGNALS=1
# The test adapter starts one native test process per sbt test thread, and
# each Immix/Commix process defaults its max heap to physical memory. Cap it
# (as the Docker-based CI does) so several test processes and the sbt JVM fit
# in the VM's memory together.
export GC_MAXIMUM_HEAP_SIZE=1G
# Override -Xmx5G from .sbtopts; matches the Linux workflow.
SBT_MEM_OPTS="-J-Xmx3G"

# Show which process the OOM killer picked if a run is killed.
on_exit() {
  status=$?
  if [ "$status" -ne 0 ]; then
    echo "Tests failed with exit status $status; last kernel messages:"
    dmesg | tail -n 30 || true
  fi
}
trap on_exit EXIT

# The checkout retains the host runner's UID when copied into the root-run VM.
git config --global --add safe.directory "$PWD"
# The VM hostname is not necessarily published in DNS or its base hosts file.
printf '127.0.0.1 %s\n' "$(hostname)" >> /etc/hosts

uname -a
java -version
clang --version
# --server avoids the native thin client, which is not available for BSD.
sbt --server --batch $SBT_MEM_OPTS --sbt-version "$SBT_VERSION" \
  'set sandbox.forBinaryVersion("3") / nativeConfig ~= (_.withMultithreading(true))' \
  "test-gc 3"
# test-runtime also includes testsExt; keep the BSD runtime matrix explicit.
sbt --server --batch $SBT_MEM_OPTS --sbt-version "$SBT_VERSION" \
  "testsJVM3/test" \
  "tests3/test" "junitTestOutputsNative3/test" \
  "scalaPartestJunitTests3/test"
