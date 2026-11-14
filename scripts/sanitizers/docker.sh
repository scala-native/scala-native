#!/usr/bin/env bash
# Run scripts/sanitizers/run.sh inside a Linux container (for macOS hosts).
#
# Usage: scripts/sanitizers/docker.sh <asan|lsan|valgrind> [sbt-project]
#
# Pass-through env: SBT_COMMAND, SCALANATIVE_MODE, SCALANATIVE_GC,
# GC_MAXIMUM_HEAP_SIZE. Set DOCKER_CONTEXT to pick the docker daemon.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
IMAGE=scala-native-sanitizers

docker build -q -t "$IMAGE" "$ROOT/scripts/sanitizers" >/dev/null

# Separate caches so the container does not reuse macOS-built artifacts.
CACHE="$ROOT/target/sanitizers-cache"
mkdir -p "$CACHE/cache" "$CACHE/sbt" "$CACHE/ivy"

# The build runs git; in a worktree .git points into the main repository,
# so mount both at their host paths.
GIT_COMMON="$(cd "$ROOT" && cd "$(git rev-parse --git-common-dir)" && pwd)"
GIT_MOUNT=()
case "$GIT_COMMON" in
"$ROOT"/*) ;;
*) GIT_MOUNT=(--mount type=bind,source="$GIT_COMMON",target="$GIT_COMMON") ;;
esac

# LeakSanitizer needs ptrace to stop the world.
docker run --rm -t \
  --cap-add SYS_PTRACE --security-opt seccomp=unconfined \
  --mount type=bind,source="$ROOT",target="$ROOT" -w "$ROOT" \
  ${GIT_MOUNT[@]+"${GIT_MOUNT[@]}"} \
  --mount type=bind,source="$CACHE/cache",target=/home/scala-native/.cache \
  --mount type=bind,source="$CACHE/sbt",target=/home/scala-native/.sbt \
  --mount type=bind,source="$CACHE/ivy",target=/home/scala-native/.ivy2 \
  -e SBT_COMMAND -e SCALANATIVE_MODE -e SCALANATIVE_GC -e GC_MAXIMUM_HEAP_SIZE \
  -e SBT_OPTS="${SBT_OPTS:--Xmx6G}" \
  "$IMAGE" scripts/sanitizers/run.sh "$@"
