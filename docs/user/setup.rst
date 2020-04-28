.. _setup:

Environment setup
=================

Scala Native has the following build dependencies:

* Java 8 or newer
* sbt 0.13.13 or newer
* LLVM/Clang 3.7 or newer

And following completely optional runtime library dependencies:

* Boehm GC 7.6.0 (optional)
* zlib 1.2.8 or newer (optional)

These are only required if you use the corresponding feature.

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

Scala Native uses the immix garbage collector by default.
You can use the Boehm__ garbage collector instead, as described here.
If you chose to use the Boehm garbage collector both the native library
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

*Note:* A version of zlib that is sufficiently recent comes with the
installation of macOS.

**Ubuntu**

.. code-block:: shell

    $ sudo apt install clang
    $ sudo apt install libgc-dev # optional

**Arch Linux**

.. code-block:: shell

    $ sudo pacman -S llvm clang
    $ sudo pacman -S gc # optional

*Note:* A version of zlib that is sufficiently recent comes with the
installation of Arch Linux.

**Fedora 26**

.. code-block:: shell

    $ sudo dnf install llvm clang
    $ sudo dnf install gc-devel zlib-devel # both optional

**FreeBSD**

.. code-block:: shell

    $ pkg install llvm38
    $ pkg install boehm-gc # optional

*Note:* A version of zlib that is sufficiently recent comes with the
installation of FreeBSD.

**Nix/NixOS**

.. code-block:: shell

    $ wget https://raw.githubusercontent.com/scala-native/scala-native/master/scripts/scala-native.nix
    $ nix-shell scala-native.nix -A clangEnv

IntelliJ IDEA
-------------

First, check "use sbt shell for import and build" when opening the SN project.
Then, once the project is imported, you have to fix some dependencies:

 * first, remove or mark as ignored the ``scalalib`` module. It is not required for the SN compilation itself, only for runtime. IntelliJ gets confused when ``scalalib`` is present, because it thinks that it should use that as the source of the scala library.
 * then, add some dependencies to the ``nscplugin`` module: go to project structure, select ``nscplugin`` and then "Dependencies". Add "JARs or directories" and select the ``scala-native/nir`` directory. Repeat and select the ``scala-native/util`` directory.

At some point, the process could be automated for IDEA in the same way that ``scala/scala`` does (i.e. with an SBT task that generates the proper project definition).

Continue to :ref:`sbt`.


.. _Boehm GC: http://www.hboehm.info/gc/
__ 'Boehm GC'_
.. _immix: http://www.cs.utexas.edu/users/speedway/DaCapo/papers/immix-pldi-2008.pdf
.. _LLVM: http://llvm.org
.. _here: :ref:`Sbt settings and tasks`
