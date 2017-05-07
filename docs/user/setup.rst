.. _setup:

Environment setup
=================

Scala Native has the following system requirements:

* Java 8 or later
* sbt 0.13.15
* LLVM
* Boehm GC

Installing sbt
--------------

Please refer to `this link <http://www.scala-sbt.org/release/docs/Setup.html>`_
for instructions for your operating system.

Installing LLVM and Boehm GC
----------------------------

LLVM and Boehm GC are Scala Native's only external dependencies. Here are
installation instructions for a number of operating systems Scala Native has
been used with:

**Ubuntu**
::

    $ sudo apt install clang libgc-dev libunwind-dev libre2-dev

*Note:* libre2-dev is available since Xenial (16.04 LTS). Refer to `travis.yml <https://github.com/scala-native/scala-native/blob/master/.travis.yml>`_ to install from source.

**Arch Linux**
::

    $ sudo pacman -S gc llvm

**macOS**
::

    $ brew install llvm bdw-gc re2

**FreeBSD**
::

    $ pkg install llvm38 boehm-gc libunwind re2

**Nix/NixOS**
::

    $ wget https://raw.githubusercontent.com/scala-native/scala-native/master/bin/scala-native.nix
    $ nix-shell scala-native.nix -A clangEnv

Continue to :ref:`sbt`.

Troubleshooting
---------------
When compiling your Scala Native project, the linker ``ld`` may fail with the following message:

::

  relocation R_X86_64_32 against `.rodata.str1.1' can not be used when making a shared object; recompile with -fPIC

It is likely that the ``LDFLAGS`` environment variable enables hardening. For example, this occurs when the ``hardening-wrapper`` package is installed on Arch Linux. It can be safely removed.

.. _Boehm GC: http://www.hboehm.info/gc/
.. _LLVM: http://llvm.org
