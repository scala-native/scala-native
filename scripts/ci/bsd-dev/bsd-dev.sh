#!/bin/sh
# Persistent BSD VM inside Docker; the guest image matches vmactions CI.
set -eu
repo=$(CDPATH='' cd -- "$(dirname -- "$0")/../../.." && pwd)
command=${1:-help}
os=${2:-netbsd}
case "$os" in
  netbsd) release=10.1; builder=2.2.7; port=22250; network=e1000; vga=std ;;
  freebsd) release=14.4; builder=2.2.8; port=22251; network=virtio-net-pci; vga=virtio ;;
  *) echo "Unsupported OS: $os" >&2; exit 1 ;;
esac
state="$repo/target/bsd-dev/$os"
container="scala-native-$os-dev"
image=scala-native-bsd-dev
mkdir -p "$state"
case "$command" in
  start)
    docker build -t "$image" "$repo/scripts/ci/bsd-dev"
    base="https://github.com/anyvm-org/$os-builder/releases/download/v$builder/$os-$release"
    if [ ! -f "$state/disk.qcow2" ]; then
      available=$(df -Pk "$state" | awk 'NR == 2 { print $4 }')
      if [ "$available" -lt 6291456 ]; then
        echo "A fresh BSD guest needs at least 6 GiB of free host disk space." >&2
        exit 1
      fi
      curl -fL --retry 3 -o "$state/disk.qcow2.zst" "$base.qcow2.zst"
      docker run --rm -v "$state:/vm" "$image" \
        zstd -df --rm -o /vm/disk.qcow2.tmp /vm/disk.qcow2.zst
      mv "$state/disk.qcow2.tmp" "$state/disk.qcow2"
    fi
    if [ ! -f "$state/guest.key" ]; then
      curl -fL --retry 3 -o "$state/guest.key" "$base-host.id_rsa"
      chmod 600 "$state/guest.key"
    fi
    if docker container inspect "$container" >/dev/null 2>&1; then
      docker start "$container"
    else
      docker run -d --name "$container" -p "127.0.0.1:$port:2225" \
        -v "$state:/vm" -v "$repo:/source:ro" "$image" \
        qemu-system-x86_64 -machine pc,accel=tcg,hpet=off,smm=off \
        -cpu qemu64 -smp 1 -m "${BSD_DEV_MEMORY:-5120}" -vga "$vga" -display none \
        -chardev file,id=serial0,path=/vm/console.log -serial chardev:serial0 \
        -monitor unix:/vm/monitor.sock,server,nowait \
        -rtc base=utc,clock=host,driftfix=slew \
        -netdev user,id=net0,net=192.168.122.0/24,host=192.168.122.1,dhcpstart=192.168.122.254,ipv6=off,hostfwd=tcp:0.0.0.0:2225-192.168.122.254:22 \
        -device "$network,netdev=net0" -object rng-builtin,id=rng0 \
        -device virtio-rng-pci,rng=rng0 \
        -drive file=/vm/disk.qcow2,format=qcow2,if=virtio,discard=unmap
    fi
    echo "Guest is booting. Connect: $0 ssh $os [command]"
    ;;
  ssh)
    shift; shift || true
    exec ssh -p "$port" -i "$state/guest.key" \
      -o IdentitiesOnly=yes -o StrictHostKeyChecking=no \
      -o UserKnownHostsFile="$state/known_hosts" -o ConnectTimeout=10 \
      root@127.0.0.1 "$@"
    ;;
  sync)
    # Send sources only. Host build outputs cannot be reused on BSD.
    COPYFILE_DISABLE=1 tar --no-xattrs -C "$repo" --exclude='./.git' --exclude='./target' \
      --exclude='*/target' --exclude='./.bsp' --exclude='./.metals' \
      --exclude='./.scala-build' -cf - . | \
      "$0" ssh "$os" 'mkdir -p /root/scala-native && tar -xf - -C /root/scala-native'
    ;;
  stop)
    "$0" ssh "$os" '/sbin/shutdown -p now'
    ;;
  screen)
    docker exec "$container" sh -c 'printf "screendump /vm/screen.ppm\n" | socat - UNIX-CONNECT:/vm/monitor.sock' >/dev/null
    echo "$state/screen.ppm"
    ;;
  status)
    docker ps -a --filter "name=^/$container$"
    tail -n 20 "$state/console.log"
    ;;
  *)
    echo "Usage: $0 {start|ssh|sync|stop|status|screen} {netbsd|freebsd} [ssh command]"
    ;;
esac
