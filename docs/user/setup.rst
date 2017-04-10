.. _setup:

Environment setup
=================

This is what you will be doing, in a nutshell:

* installation of sbt
* installation of LLVM and Clang
* installation of Boehm GC

Installing sbt
--------------

Please refer to `this link <http://www.scala-sbt.org/release/docs/Setup.html>`_
for instructions for your OS.

Please note that you'll need Java 8 or more recent to use the Scala Native
toolchain.

Installing LLVM, Clang and Boehm GC
-----------------------------------

Boehm GC and LLVM (that includes Clang) are Scala Native's only external
dependencies. Here are install instructions for a number of operating
systems Scala Native has been used with:

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
