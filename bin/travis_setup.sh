#!/usr/bin/env bash

# Enable strict mode and fail the script on non-zero exit code,
# unresolved variable or pipe failure.
set -euo pipefail
IFS=$'\n\t'

if [ "$(uname)" == "Darwin" ]; then

    brew update
    brew install sbt
    brew install bdw-gc
    brew link bdw-gc
    brew install jq
    brew install re2
    brew install llvm@4
    export PATH="/usr/local/opt/llvm@4/bin:$PATH"

else

    # Install LLVM/Clang, Boehm GC, libunwind
    sudo add-apt-repository --yes ppa:ubuntu-toolchain-r/test
    sudo apt-get -qq update
    sudo apt-get install -y -qq \
      clang++-3.8 \
      libgc-dev \
      libunwind8-dev

    # Install re2
    # Starting from Ubuntu 16.04 LTS, it'll be available as http://packages.ubuntu.com/xenial/libre2-dev
    sudo apt-get install -y make
    export CXX=clang++-3.8
    git clone https://code.googlesource.com/re2
    pushd re2
    git checkout 2017-03-01
    make -j4 test
    sudo make install prefix=/usr
    make testinstall prefix=/usr
    popd

fi
