# How to build?

This requires SBT to be installed on your machine. Refer to [this link](http://www.scala-sbt.org/release/docs/Setup.html) for instructions.

```
$ git clone https://github.com/scala-native/scala-native.git
$ cd scala-native/
$ sbt rtlib/publishLocal nscplugin/publishLocal
$ sbt package
```

If you face any issues with your development environment setup, refer to [developer guide](docs/develop.md)

## Resolving issue with gc.h

You will also need a version of the BOEHM GC either provided by the Operating System repositories or compiled from source. Refer to below instructions for your OS respectively.

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

or, compile yourself


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
