# How to build?

This requires SBT to be installed on your machine. Refer to [this link](http://www.scala-sbt.org/release/docs/Setup.html) for instructions.

```
$ git clone https://github.com/scala-native/scala-native.git
$ cd scala-native/
$ sbt rtlib/publishLocal nscplugin/publishLocal
$ sbt package
```

If you face any issues with your development environment setup, refer to below instructions for your OS respectively, you will need clang and BOEHM GC, too.

## Setup the environment

## Fedora

```
sudo yum install gc-devel
```

## Ubuntu

```
sudo apt-get install libgc-dev
```

## OSX

You can use brew as shown below

```
brew install bdw-gc
```

### OS X Yosemite

OS X Yosemite uses outdated version of `clang` so you might have to install a newer version via brew:

```
brew install homebrew/versions/llvm38
```

## FreeBSD

```
pkg install sbt llvm38 boehm-gc
```

## Source code

```
git clone git://github.com/ivmai/bdwgc.git
cd bdwgc
git clone git://github.com/ivmai/libatomic_ops.git
autoreconf -vif
automake --add-missing
./configure
make
make check
sudo make install

```
