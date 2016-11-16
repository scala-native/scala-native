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

    $ sudo apt-get install libgc-dev

macOS::

    $ brew install llvm bdw-gc

FreeBSD::

    $ pkg install llvm38 boehm-gc

nixOS::

    $ nix-env -i clang-wrapper-3.7.1 boehm-gc-7.2f

    # clang.nix
    let
      pkgs = import <nixpkgs> {};
      stdenv = pkgs.stdenv;
    in rec {
      clangEnv = stdenv.mkDerivation rec {
        name = "clang-env";
        buildInputs = [
          stdenv
          pkgs.boehmgc
        ];
      };
    }

    $ nix-shell clang.nix -A clangEnv

Continue to :ref:`sbt`.

.. _Boehm GC: http://www.hboehm.info/gc/
.. _LLVM: http://llvm.org
