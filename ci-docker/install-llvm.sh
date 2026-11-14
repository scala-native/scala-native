#!/usr/bin/env bash
# Installs clang/lld <version> from apt.llvm.org, retrying with backoff to ride
# out its outages, then falls back to the distribution packages if available.
# Usage: install-llvm.sh <llvm-version> (as root)
set -euo pipefail

version=${1:?Usage: install-llvm.sh <llvm-version>}
attempts=${LLVM_INSTALL_ATTEMPTS:-6}

retry() {
  local n=1 delay=15
  until "$@"; do
    if ((n >= attempts)); then
      return 1
    fi
    echo "Attempt $n/$attempts failed: $*; retrying in ${delay}s" >&2
    sleep "$delay"
    n=$((n + 1))
    delay=$((delay * 2))
  done
}

echo 'Acquire::Retries "5";' >/etc/apt/apt.conf.d/80-retries

workdir=$(mktemp -d)
trap 'rm -rf "$workdir"' EXIT

# Force IPv4: GitHub runners often have no IPv6, and apt.llvm.org prefers AAAA.
fetch() {
  curl -4 -fsSL --connect-timeout 20 --max-time 30 -o "$2" "$1"
}

install_from_apt_llvm_org() {
  local key=/etc/apt/trusted.gpg.d/apt.llvm.org.asc
  if [[ ! -s $key ]]; then
    fetch https://apt.llvm.org/llvm-snapshot.gpg.key "$workdir/key" &&
      mv "$workdir/key" "$key"
  fi &&
    fetch https://apt.llvm.org/llvm.sh "$workdir/llvm.sh" &&
    bash "$workdir/llvm.sh" "$version" </dev/null
}

if retry install_from_apt_llvm_org; then
  exit 0
fi

echo "apt.llvm.org unavailable, trying distribution packages of LLVM $version" >&2
rm -f /etc/apt/sources.list.d/*apt_llvm_org*
retry apt-get update
apt-get install -y "clang-$version" "lld-$version"
