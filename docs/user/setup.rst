.. _setup:

Environment setup
=================

Scala Native has the following build dependencies:

* Java 8 or newer
* sbt 0.13.13 or newer
* LLVM/Clang 3.7 or newer

And following runtime library dependencies:

* libunwind 0.99 or newer (built-in on macOS)
* Boehm GC 7.6.0 (optional)
* Re2 2017-01-01 (optional)
* zlib 1.2.8 or newer (optional)

Most of the runtime dependencies are completely optional and are
only required if you use the corresponding feature.

Installing sbt
--------------

Please refer to `this link <http://www.scala-sbt.org/release/docs/Setup.html>`_
for instructions for your operating system.

Installing clang and runtime dependencies
-----------------------------------------

Scala Native requires Clang, which is part of the LLVM toolchain. The
recommended LLVM version is 3.7 or newer, however, the Scala Native sbt
plugin uses feature detection to discover the installed version of Clang
so older versions may also work.

Scala Native uses Boehm garbage collector by default. Both the native
library and header files must be provided at build time. One may use opt-in
to use new experimental garbage collector called Immix to avoid this dependency.

To be able to use regular expressions, the RE2 library must be installed. You
will also need to install zlib if you use classes from the `java.util.zip`
package. If you don't use regular expressions or compression, you can skip
these dependencies.

.. note::

  Some package managers provide the library header files in separate
  `-dev` packages.

Here are install instructions for a number of operating systems Scala
Native has been used with:

**macOS**

.. code-block:: shell

    $ brew install llvm
    $ brew install bdw-gc re2 # optional

*Note:* A version of zlib that is sufficiently recent comes with the
installation of macOS.

**Ubuntu**

.. code-block:: shell

    $ sudo apt install clang libunwind-dev
    $ sudo apt install libgc-dev libre2-dev # optional

*Note:* libre2-dev is available since Ubuntu 16.04. Please refer to
`our travis environment setup script <https://github.com/scala-native/scala-native/blob/master/bin/travis_setup.sh#L29-L39>`_
to install from source.

**Arch Linux**

.. code-block:: shell

    $ sudo pacman -S llvm
    $ sudo pacman -S gc re2 # optional

*Note:* A version of zlib that is sufficiently recent comes with the
installation of Arch Linux.

**FreeBSD**

.. code-block:: shell

    $ pkg install llvm38 libunwind
    $ pkg install boehm-gc re2 # optional

*Note:* A version of zlib that is sufficiently recent comes with the
installation of FreeBSD.

**Nix/NixOS**

.. code-block:: shell

    $ wget https://raw.githubusercontent.com/scala-native/scala-native/master/bin/scala-native.nix
    $ nix-shell scala-native.nix -A clangEnv

Continue to :ref:`sbt`.

.. _Boehm GC: http://www.hboehm.info/gc/
.. _LLVM: http://llvm.org
