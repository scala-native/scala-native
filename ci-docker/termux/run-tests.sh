#!/usr/bin/env bash
set -euo pipefail

java -version
clang --version

if [[ $# -eq 0 ]]; then
  set -- \
    "sandbox3/run" \
    "junitTestOutputsJVM3/test" \
    "junitTestOutputsNative3/test" \
    "testsJVM3/test" \
    "tests3/test" \
    "testsExtJVM3/test" \
    "testsExt3/test" \
    "scalaPartestJunitTests3/test"
fi

# Run the JVM directly through sbt's runner: the native thin client needs glibc.
# The runner also reads the repository's .sbtopts.
exec sbt --server --batch \
  "-J-Xmx${SBT_MAX_HEAP_SIZE:-5G}" \
  'set Global / nativeConfig ~= { config =>
    config
      .withTargetTriple("aarch64-unknown-linux-android28")
      .withMultithreading(true)
      .withLinkingOptions(_ ++ Seq("-landroid-posix-semaphore", "-landroid-wordexp"))
  }' \
  'set ThisBuild / Test / parallelExecution := false' \
  "$@"
