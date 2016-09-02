# How to build?

This requires SBT to be installed on your machine. Refer to [this link](http://www.scala-sbt.org/release/docs/Setup.html) for instructions.

```
$ git clone https://github.com/scala-native/scala-native.git
$ cd scala-native/
$ sbt 'nscplugin/publishLocal' 'nativelib/publishLocal' 'publishLocal'
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

## Nix / NixOS

```
nix-env -i clang-wrapper-3.7.1 boehm-gc-7.2f
```

to run [scala-native-example](https://github.com/scala-native/scala-native-example) you need to configure library and headers include path.

`nix-build '<nixpkgs>' -A boehmgc --no-out-link`

example output:

```
boehm-gc-7.2f
dev=/nix/store/rxvzdlp5x3r60b02fk95v404y3mhs2in-boehm-gc-7.2f-dev;
doc=/nix/store/jpcng9dyid9002ry4h3rm3z1r5irdgqz-boehm-gc-7.2f-doc;
/nix/store/bw1p8rairfwv2yif2g1cc0yg8hv25mnl-boehm-gc-7.2f

```

```
$ cd scala-native-example
$ sbt
> set nativeClangOptions := Stream(
  "-I/nix/store/rxvzdlp5x3r60b02fk95v404y3mhs2in-boehm-gc-7.2f-dev/include",
  "-L/nix/store/bw1p8rairfwv2yif2g1cc0yg8hv25mnl-boehm-gc-7.2f/lib"
)
```

## OSX

You can use brew as shown below

```
brew install bdw-gc
```

### OS X Yosemite or older

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
