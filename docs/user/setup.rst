.. _setup:

Environment setup
=================

Scala Native has the following build dependencies:

* Java 8 or newer
* sbt 1.1.6 or newer
* LLVM/Clang 6.0 or newer

And following completely optional runtime library dependencies:

* Boehm GC 7.6.0 (optional)
* zlib 1.2.8 or newer (optional)

These are only required if you use the corresponding feature.

Installing sbt
--------------

**macOS and Linux**

Please refer to `this link <https://www.scala-sbt.org/release/docs/Setup.html>`_
for instructions for your operating system.

**FreeBSD**

.. code-block:: shell

    $ pkg install sbt

Installing clang and runtime dependencies
-----------------------------------------

Scala Native requires Clang, which is part of the `LLVM`_ toolchain. The
recommended LLVM version is the most recent available for your system
provided that it works with Scala Native. The Scala Native sbt
plugin checks to ensure that `clang` is at least the minimum version
shown above.

Scala Native uses the `immix`_ garbage collector by default.
You can use the `Boehm`_ garbage collector instead.
If you chose to use that alternate garbage collector both the native library
and header files must be provided at build time.

If you use classes from the `java.util.zip` for compression
zlib needs to be installed.

.. note::

  Some package managers provide the library header files in separate
  `-dev` packages.

Here are install instructions for a number of operating systems Scala
Native has been used with:

**macOS**

.. code-block:: shell

    $ brew install llvm
    $ brew install bdw-gc # optional

*Note 1:* Xcode should work as an alternative if preferred: 
https://apps.apple.com/us/app/xcode/id497799835

*Note 2:* A version of zlib that is sufficiently recent comes with the
installation of macOS.

**Ubuntu**

.. code-block:: shell

    $ sudo apt install clang
    $ sudo apt install libgc-dev # optional

**Arch Linux**

.. code-block:: shell

    $ sudo pacman -S llvm clang build-essential
    $ sudo pacman -S gc # optional

*Note:* A version of zlib that is sufficiently recent comes with the
installation of Arch Linux.

**Fedora 33**

.. code-block:: shell

    $ sudo dnf install llvm clang
    $ sudo dnf groupinstall "Development Tools"
    $ sudo dnf install gc-devel zlib-devel # both optional

**FreeBSD 12.2 and later**

.. code-block:: shell

    $ pkg install llvm10
    $ pkg install boehm-gc # optional

*Note:* A version of zlib that is sufficiently recent comes with the
installation of FreeBSD.

**Nix/NixOS**

.. code-block:: shell

    $ wget https://raw.githubusercontent.com/scala-native/scala-native/master/scripts/scala-native.nix
    $ nix-shell scala-native.nix -A clangEnv

Continue to :ref:`sbt`.

.. Comment - Sphinx linkcheck fails both http: and https://www.hboehm.info/gc 
.. Comment - so use the roughly equivalent GitHub URL.
.. _Boehm: https://github.com/ivmai/bdwgc
.. _immix: https://www.cs.utexas.edu/users/speedway/DaCapo/papers/immix-pldi-2008.pdf
.. _LLVM: https://llvm.org
.. _here: :ref:`Sbt settings and tasks`
