#!/bin/sh
# Run inside the guest after syncing the checkout.
set -eu
case "$(uname -s)" in
  NetBSD)
    export PATH="/usr/pkg/bin:/usr/pkg/sbin:$PATH"
    export PKG_PATH=https://cdn.NetBSD.org/pub/pkgsrc/packages/NetBSD/x86_64/10.0_2026Q3/All
    /usr/sbin/pkg_add -u pcre2
    /usr/sbin/pkg_add clang boehm-gc zlib git curl bash openjdk21 mozilla-rootcerts
    ;;
  FreeBSD)
    pkg update -f
    pkg install -y llvm18 boehm-gc-threaded git curl bash openjdk21
    ;;
  *) echo 'Run this script inside the BSD guest.' >&2; exit 1 ;;
esac
