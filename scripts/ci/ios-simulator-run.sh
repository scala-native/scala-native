#!/usr/bin/env bash
# Launcher of Scala Native binaries built for the iOS simulator
# (target triple `<arch>-apple-ios<version>-simulator`).
#
# Usage: CROSSCOMPILING_EMULATOR=<path-to-this-script> sbt tests3/test
#
# The binary is spawned inside of a booted simulator device. The device can be
# selected using IOS_SIMULATOR_UDID env variable, defaults to `booted`.
# `simctl spawn` does not propagate the environment of the caller, only the
# variables prefixed with `SIMCTL_CHILD_` are passed to the spawned process.
set -euo pipefail

while IFS= read -r name; do
  # e.g. GitHub Actions allows env variables with names rejected by bash
  [[ "$name" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] || continue
  case "$name" in
    SIMCTL_* | DYLD_* | XPC_* | __CF* | _ | SHLVL | OLDPWD | IFS | BASH* | name) ;;
    # Used by the simulator runtime to locate its own root directories
    HOME | TMPDIR | PATH | CFFIXED_USER_HOME | IPHONE_* ) ;;
    *) export "SIMCTL_CHILD_${name}=${!name}" ;;
  esac
done < <(compgen -e)

exec xcrun simctl spawn "${IOS_SIMULATOR_UDID:-booted}" "$@"
