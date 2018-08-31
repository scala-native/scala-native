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
    brew install llvm@6
    export PATH="/usr/local/opt/llvm@6/bin:$PATH"

else

    # add support for newer clang
    sudo wget -O - https://apt.llvm.org/llvm-snapshot.gpg.key | sudo apt-key add -
    sudo apt-key adv --recv-keys --keyserver keyserver.ubuntu.com 1E9377A2BA9EF27F
    echo "deb http://apt.llvm.org/trusty/ llvm-toolchain-trusty main" | sudo tee -a /etc/apt/sources.list
    echo "deb http://apt.llvm.org/trusty/ llvm-toolchain-trusty-6.0 main" | sudo tee -a /etc/apt/sources.list
    echo "deb http://apt.llvm.org/trusty/ llvm-toolchain-trusty-7 main" | sudo tee -a /etc/apt/sources.list
    echo "deb http://ppa.launchpad.net/ubuntu-toolchain-r/test/ubuntu trusty main" | sudo tee -a /etc/apt/sources.list

    sudo apt-get update

    # Remove pre-bundled libunwind
    sudo find /usr -name "*libunwind*" -delete

    # Use pre-bundled clang
    export PATH=/usr/local/clang-6.0.0/bin:$PATH
    export CXX=clang++

    # Install Boehm GC and libunwind
    sudo apt-get install libgc-dev libunwind8-dev clang-6.0

    # Build and install re2 from source
    git clone https://code.googlesource.com/re2
    pushd re2
    git checkout 2017-03-01
    make -j4 test
    sudo make install prefix=/usr
    make testinstall prefix=/usr
    popd

fi
