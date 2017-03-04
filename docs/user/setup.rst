.. _setup:

Environment setup
=================

This is what you will be doing, in a nutshell:

* installation of SBT
* installation of LLVM and Clang
* installation of Boehm GC

Installing SBT
--------------

Please refer to `this link <http://www.scala-sbt.org/release/docs/Setup.html>`_
for instructions for your OS.

Installing LLVM, Clang and Boehm GC
-----------------------------------

Boehm GC and LLVM (that includes Clang) are Scala Native's only external
dependencies. Here are install instructions for a number of operating
systems Scala Native has been used with:

Ubuntu::

    $ sudo apt-get install clang libgc-dev libunwind-dev

macOS::

    $ brew install llvm bdw-gc

FreeBSD::

    $ pkg install llvm38 boehm-gc libunwind

nix/nixOS::

    $ wget https://raw.githubusercontent.com/scala-native/scala-native/master/bin/scala-native.nix

    $ nix-shell scala-native.nix -A clangEnv

Continue to :ref:`sbt`.

.. _Boehm GC: http://www.hboehm.info/gc/
.. _LLVM: http://llvm.org
