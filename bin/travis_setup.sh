#!/usr/bin/env bash

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

    # Install LLVM/Clang 3.7, Boehm GC, libunwind
    sudo apt-get -qq update
    sudo sh -c "echo 'deb http://apt.llvm.org/precise/ llvm-toolchain-precise-3.7 main' >> /etc/apt/sources.list"
    sudo sh -c "echo 'deb http://apt.llvm.org/precise/ llvm-toolchain-precise main' >> /etc/apt/sources.list"
    wget -O - http://apt.llvm.org/llvm-snapshot.gpg.key | sudo apt-key add -
    sudo add-apt-repository --yes ppa:ubuntu-toolchain-r/test
    sudo apt-get -qq update
    sudo apt-get install -y \
      clang++-3.7 \
      llvm-3.7 \
      llvm-3.7-dev \
      llvm-3.7-runtime \
      llvm-3.7-tool \
      libgc-dev \
      libunwind7-dev

    # Install re2
    # Starting from Ubuntu 16.04 LTS, it'll be available as http://packages.ubuntu.com/xenial/libre2-dev
    sudo apt-get install -y make
    export CXX=clang++-3.7
    git clone https://code.googlesource.com/re2
    pushd re2
    git checkout 2017-03-01
    make -j4 test
    sudo make install prefix=/usr
    make testinstall prefix=/usr
    popd

fi
