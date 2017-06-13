.. _setup:

Environment setup
=================

Scala Native has the following minimum system requirements:

* Java 8+
* sbt 0.13.x
* LLVM 3.7 or newer
* Native libraries
    * Boehm GC 7.6.0
    * Re2 2017-01-01 (optional)
    * zlib 1.2.8 or newer (optional)

Installing sbt
--------------

Please refer to `this link <http://www.scala-sbt.org/release/docs/Setup.html>`_
for instructions for your operating system.

Installing LLVM, Clang and native libraries
-------------------------------------------

Scala Native requires Clang, which is part of the LLVM toolchain. The
recommended LLVM version is 3.7 or newer, however, the Scala Native sbt
plugin uses feature detection to discover the installed version of Clang
so older versions may also work.

In addition, the native Scala runtime and Java API implementation
require the Boehm garbage collector. Both the native library and header
files must be provided at build time.

To be able to use regular expressions, the RE2 library must be installed. You
will also need to install zlib if you use classes from the `java.util.zip`
package. If you don't use regular expressions or compression, you can skip
these dependencies.

.. note::

  Some package managers provide the library header files in separate
  `-dev` packages.

Here are install instructions for a number of operating systems Scala
Native has been used with:

**Ubuntu**
::

    $ sudo apt install clang libgc-dev libunwind-dev libre2-dev

*Note:* libre2-dev is available since Xenial (16.04 LTS). Refer to `travis.yml <https://github.com/scala-native/scala-native/blob/master/.travis.yml>`_ to install from source.

**Arch Linux**
::

    $ sudo pacman -S llvm gc
    $ sudo pacman -S re2 # if you use regular expressions

*Note:* A version of zlib that is sufficiently recent comes with the
installation of Arch Linux.

**macOS**
::

    $ brew install llvm bdw-gc
    $ brew install re2 # if you use regular expressions

*Note:* A version of zlib that is sufficiently recent comes with the
installation of macOS.

**FreeBSD**
::

    $ pkg install llvm38 boehm-gc libunwind
    $ pkg install re2 # if you use regular expressions

*Note:* A version of zlib that is sufficiently recent comes with the
installation of FreeBSD.

**Nix/NixOS**
::

    $ wget https://raw.githubusercontent.com/scala-native/scala-native/master/bin/scala-native.nix
    $ nix-shell scala-native.nix -A clangEnv

Continue to :ref:`sbt`.

.. _Boehm GC: http://www.hboehm.info/gc/
.. _LLVM: http://llvm.org
