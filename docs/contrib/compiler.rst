.. _compiler:

The compiler plugin and code generator
======================================

Compilation to native code happens in two steps. First, Scala code is compiled
into :ref:`nir` by nscplugin, the Scala compiler plugin. It runs as one of the
later phases of the Scala compiler and inspects the AST and generates ``.nir``
files. Finally, the ``.nir`` files are compiled into ``.ll`` files and passed
to LLVM by the native compiler.

.. figure:: compilation.png

   High-level overview of the compilation process.

Tips for working on the compiler
--------------------------------

When adding a new intrinsic, the first thing to check is how clang would compile
it in C. Write a small program with the behavior you are trying to add and
compile it to ``.ll`` using::

    clang -S -emit-llvm foo.c

Now write the equivalent Scala code for the new intrinsic in the sandbox
project.
This project contains a minimal amount of code and has all the toolchain set up
which makes it fast to iterate and inspect the output of the compilation.

The following directions are using the Scala 3 project. To use other Scala
versions first find the project name and then use that instead of "sandbox3",
say "sandbox2_13"::

    sbt> sandbox<TAB>
    
To compile the sandbox project run the following in the sbt shell::

    sbt> sandbox3/clean; sandbox3/nativeLink

After compiling the sandbox project you can inspect the ``.ll`` files below
``sandbox/.3/target/``. Because Scala Native is under active development,
the directory layout, names of files and their specific content may change.

A Linux example, where the actual file names found may vary::

    $ # on command line, with project root as current working directory.
    $ find sandbox/.3/target -name "*.ll"
    sandbox/.3/target/scala-3.1.3/native/3.ll
    sandbox/.3/target/scala-3.1.3/native/2.ll
    sandbox/.3/target/scala-3.1.3/native/1.ll
    sandbox/.3/target/scala-3.1.3/native/0.ll

Locating the code you are interested in will require that
you are familiar with the `LLVM assembly language <http://llvm.org/docs/LangRef.html>`_.

When working on the compiler plugin you'll need to clean the sandbox (or other
Scala Native projects) if you want it to be recompiled with the newer version
of the compiler plugin. This can be achieved with::

    sbt> sandbox3/clean; sandbox3/run

Certain intrinsics might require adding new primitives to the compiler plugin.
This can be done in ``NirPrimitives`` with an accompanying definition in
``NirDefinitions``. Ensure that new primitives are correctly registered.

The NIR code generation uses a builder to maintain the generated instructions.
This allows to inspect the instructions before and after the part of the compilation
you are working on has generated code.
