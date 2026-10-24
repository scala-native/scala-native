# Local BSD development

This rig runs the x86_64 images used by vmactions CI inside a Linux Docker
container. It works on Intel and ARM hosts through QEMU TCG. Emulation is
slower than CI; use focused tests rather than the full GC/runtime matrix.

Requires Docker, curl, tar and an SSH client on the host. Allow at least
10 GiB of free host disk space per guest for packages and focused builds.
VM disks, public
image SSH keys, logs and guest build caches live under `target/bsd-dev/`.
The source checkout is mounted read-only in the container. Guest SSH is
published only on host loopback: NetBSD port 22250, FreeBSD port 22251.
The SSH keys are the public test-image keys distributed by the image builder,
not personal credentials. Do not expose these guests to the network.

From the repository root:

```sh
scripts/ci/bsd-dev/bsd-dev.sh start netbsd
# Wait until SSH responds; a TCG boot can take a few minutes.
scripts/ci/bsd-dev/bsd-dev.sh ssh netbsd 'uname -a'
scripts/ci/bsd-dev/bsd-dev.sh sync netbsd
scripts/ci/bsd-dev/bsd-dev.sh ssh netbsd \
  'cd /root/scala-native && sh scripts/ci/bsd-dev/prepare.sh'
```

Replace `netbsd` with `freebsd` for the FreeBSD 14.4 image. Each VM defaults
to 5 GiB RAM and one emulated CPU. The conservative `qemu64` model
avoids FreeBSD boot panics seen with TCG, `max` and multiple CPUs. Set `BSD_DEV_MEMORY` (MiB) before the
first start to choose a different allocation. Shut down unused guests:

```sh
scripts/ci/bsd-dev/bsd-dev.sh stop netbsd
```

`sync` updates the guest's `/root/scala-native` checkout, excluding host build
outputs and Git metadata. It preserves the guest's existing build outputs.
Run it again after local changes. `status` shows the container and serial log;
`screen` writes a VGA screenshot to `target/bsd-dev/<os>/screen.ppm`.

## Socket ABI probe

This uses the base system compiler, so package installation is unnecessary:

```sh
scripts/ci/bsd-dev/bsd-dev.sh ssh netbsd \
  'cd /root/scala-native && cc -Wall -Wextra -o /root/socket-probe scripts/ci/bsd-dev/socket-probe.c && /root/socket-probe'
```

The four cases compare the raw libc `socket` symbol used by Scala externs
with the header-selected symbol, with zero and populated `sin_len` fields.
Each case binds, listens, connects, accepts and transfers data over loopback.
NetBSD 10.1 passes all four. The raw call emits an expected linker warning
about NetBSD's compatibility `socket` symbol.

## Boehm signal reproducer

After installing the threaded Boehm package in FreeBSD:

```sh
scripts/ci/bsd-dev/bsd-dev.sh ssh freebsd \
  'cd /root/scala-native && cc -Wall -Wextra -I/usr/local/include -L/usr/local/lib -o /root/boehm-signal-probe scripts/ci/bsd-dev/boehm-signal-probe.c -lgc-threaded -pthread && /root/boehm-signal-probe'
scripts/ci/bsd-dev/bsd-dev.sh ssh freebsd '/root/boehm-signal-probe overwrite'
```

The first command collects with a worker thread and exits successfully.
The second deliberately overwrites Boehm's SIGUSR1/SIGUSR2 handlers, matching
the old test-interface diagnostic setup, and exits with status 158 on FreeBSD
(128 + SIGUSR1). This also reproduces on Linux with `-lgc`, where SIGUSR1 is
10 and the failing status is 138. The probe selects these signals explicitly
before initializing Boehm, allowing the same test on both platforms.

## Scala Native tests

Inside the guest, download the portable launcher once:

```sh
cd /root/scala-native
sbt_version=$(sed -n 's/^sbt.version *= *//p' project/build.properties)
curl -fL -o /root/sbt-launch.jar \
  "https://repo.maven.apache.org/maven2/org/scala-sbt/sbt-launch/$sbt_version/sbt-launch-$sbt_version.jar"
```

Set the environment for the selected guest:

```sh
# NetBSD
export JAVA_HOME=/usr/pkg/java/openjdk21
export PATH="$JAVA_HOME/bin:/usr/pkg/bin:/usr/pkg/sbin:$PATH"
export SCALANATIVE_INCLUDE_DIRS=/usr/pkg/include
export SCALANATIVE_LIB_DIRS=/usr/pkg/lib

# FreeBSD (use this block instead)
export JAVA_HOME=/usr/local/openjdk21
export LLVM_BIN=/usr/local/llvm18/bin
export PATH="$JAVA_HOME/bin:$LLVM_BIN:/usr/local/bin:$PATH"
```

Then run a focused test with diagnostic signals enabled:

```sh
export ENABLE_EXPERIMENTAL_COMPILER=true
export SCALANATIVE_TEST_DEBUG_SIGNALS=1
java -Xmx3G -jar /root/sbt-launch.jar \
  'set testInterface.forBinaryVersion("3") / nativeConfig ~= (_.withGC(scala.scalanative.build.GC.boehm).withSourceLevelDebuggingConfig(_.disableAll))' \
  'testInterface3/test'
```

Do not set `CI`: this source sync excludes Git metadata, which CI version
calculation would require. Normal local builds use the snapshot version.

If the host filesystem fills, QEMU can pause disk I/O while its container
still appears running, and SSH stops responding. Check host `df -h` as well
as the guest's virtual disk space. After freeing host space, resume the VM:

```sh
docker exec scala-native-netbsd-dev sh -c \
  'printf "cont\n" | socat - UNIX-CONNECT:/vm/monitor.sock'
```

Use `scala-native-freebsd-dev` for the FreeBSD guest.
