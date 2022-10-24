#!/usr/bin/env bash
set -e
set -x

workDir=$PWD

# Zlib
zlibDir=$workDir/zlib
git clone https://github.com/madler/zlib $zlibDir
cd $zlibDir
git checkout v1.2.13
./configure
make install prefix=$QEMU_LD_PREFIX
rm -rf $zlibDir

# Boehm GC
bdwgcDir=$workDir/bdwgc
cd $workDir
git clone https://github.com/ivmai/bdwgc $bdwgcDir
cd $bdwgcDir
git checkout v8.2.2
git clone https://github.com/ivmai/libatomic_ops
cd libatomic_ops/
git checkout v7.6.14
cd $bdwgcDir
./autogen.sh
./configure --host $CROSS_TRIPLE
make install prefix=$QEMU_LD_PREFIX
rm -rf $bdwgcDir
