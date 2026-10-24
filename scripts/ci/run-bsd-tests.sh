#!/bin/sh
set -eu

export ENABLE_EXPERIMENTAL_COMPILER=true
export SCALANATIVE_TEST_PREFETCH_DEBUG_INFO=1
export SCALANATIVE_TEST_DEBUG_SIGNALS=1

# The checkout retains the host runner's UID when copied into the root-run VM.
git config --global --add safe.directory "$PWD"
# The VM hostname is not necessarily published in DNS or its base hosts file.
printf '127.0.0.1 %s\n' "$(hostname)" >> /etc/hosts

# Use the portable JVM launcher: coursier and sbt native runners do not
# distribute executables for these BSD targets.
sbt_version=$(sed -n 's/^sbt.version *= *//p' project/build.properties)
test -n "$sbt_version"
launcher_dir=$(mktemp -d)
trap 'rm -rf "$launcher_dir"' EXIT HUP INT TERM
curl -fL --retry 3 -o "$launcher_dir/sbt-launch.jar" \
  "https://repo.maven.apache.org/maven2/org/scala-sbt/sbt-launch/$sbt_version/sbt-launch-$sbt_version.jar"

uname -a
java -version
clang --version
java -Xmx5G -jar "$launcher_dir/sbt-launch.jar" "test-gc 3"
# Keep extended tests out of the initial BSD bring-up. test-runtime also
# includes testsExt, so spell out the intended runtime projects here.
java -Xmx5G -jar "$launcher_dir/sbt-launch.jar" \
  "testsJVM3/test" "tests3/test" "junitTestOutputsNative3/test" \
  "scalaPartestJunitTests3/test"
