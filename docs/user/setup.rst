.. _setup:

Environment setup
=================

This is what you will be doing, in a nutshell:

* installation of sbt
* installation of LLVM and Clang
* installation of native libraries

Installing sbt
--------------

Please refer to `this link <http://www.scala-sbt.org/release/docs/Setup.html>`_
for instructions for your OS.

Please note that you'll need Java 8 or more recent to use the Scala Native
toolchain.

Installing LLVM, Clang and native libraries
-------------------------------------------

Scala Native requires Clang, which is part of the LLVM toolchain. The
recommended LLVM version is 3.7 or newer, however, the Scala Native sbt
plugin uses feature detection to discover the installed version of Clang
so older versions may also work.

In addition, the native Scala runtime and Java API implementation
require the Boehm garbage collector and the RE2 regular expression
engine. Both the native library and header files must be provided at
build time.

.. note::

  Some package managers provide the library header files in separate
  `-dev` packages.

Here are install instructions for a number of operating systems Scala
Native has been used with:

Ubuntu::

    $ sudo apt-get install clang libgc-dev libunwind-dev libre2-dev

Note: libre2-dev is available since Xenial (16.04 LTS). Refer to `travis.yml <https://github.com/scala-native/scala-native/blob/master/.travis.yml>`_ to install from source.

macOS::

    $ brew install llvm bdw-gc re2

FreeBSD::

    $ pkg install llvm38 boehm-gc libunwind re2

nix/nixOS::

    $ wget https://raw.githubusercontent.com/scala-native/scala-native/master/bin/scala-native.nix

    $ nix-shell scala-native.nix -A clangEnv

Continue to :ref:`sbt`.

.. _Boehm GC: http://www.hboehm.info/gc/
.. _LLVM: http://llvm.org
