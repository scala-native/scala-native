.. _sbt:

Building projects with sbt
==========================

If you have reached this section you probably have a system that is now able to compile and run Scala Native programs.

Minimal sbt project
-------------------

The easiest way to make a fresh project is to use our official gitter8
template.  In an empty working directory, execute::

    sbt new scala-native/scala-native.g8

.. Note:: On Windows, new project should not be created in `mounted`
  directories, like external storage devices.

  In the case of WSL2 (Windows Subsystem Linux), Windows file system drives like `C` or `D` are perceived as `mounted`. So creating new projects in these locations will not work.

  In the WSL2 environment, it is recommended to create projects in the user files path, e.g /home/<USER>/sn-projects.

This will:

* start sbt.

* prompt for a project name

* use the `.g8 template
  <https://github.com/scala-native/scala-native.g8/tree/main/src/main/g8>`_.
  to generate a basic project with that name.

* create a project sub-directory with the project name.

* copy the contents at these template links to the corresponding location
  in this new project sub-directory.

  * `project/plugins.sbt
    <https://github.com/scala-native/scala-native.g8/blob/main/src/main/g8/project/plugins.sbt>`_
    adds the Scala Native plugin dependency and its version.

  * `project/build.properties
    <https://github.com/scala-native/scala-native.g8/blob/main/src/main/g8/project/build.properties>`_
    specifies the sbt version.

  * `build.sbt
    <https://github.com/scala-native/scala-native.g8/blob/main/src/main/g8/build.sbt>`_
    enables the plugin and specifies the Scala version.

  * `src/main/scala/Main.scala
    <https://github.com/scala-native/scala-native.g8/blob/main/src/main/g8/src/main/scala/Main.scala>`_
    is a minimal application.
    ::
     
      object Main {
        def main(args: Array[String]): Unit =
          println("Hello, world!")
      }
      

To use the new project:

* Change the current working directory to the new project directory.

   - For example, on linux with a project named AnswerToProjectNamePrompt,
     type ``cd AnswerToProjectNamePrompt``.

* Type ``sbt run``.

This will get everything compiled and should have the expected output!

Please refer to the :ref:`faq` if you encounter any problems.

The generated project is a starting point. After the first run, you
should review the software versions in the generated files and, possibly,
update or customize them. `Scaladex <https://index.scala-lang.org/>`_
is a useful resource for software versions.

Scala versions
--------------

Scala Native supports following Scala versions for corresponding releases:

========================== ===============================================
Scala Native Version       Scala Versions
========================== ===============================================
0.1.x                      2.11.8
0.2.x                      2.11.8, 2.11.11
0.3.0-0.3.3                2.11.8, 2.11.11
0.3.4+, 0.4.0-M1, 0.4.0-M2 2.11.8, 2.11.11, 2.11.12
0.4.0                      2.11.12, 2.12.13, 2.13.4
0.4.1                      2.11.12, 2.12.13, 2.13.4, 2.13.5
0.4.2                      2.11.12, 2.12.{13..15}, 2.13.{4..8}
0.4.3-RC1, 0.4.3-RC2       2.11.12, 2.12.{13..15}, 2.13.{4..8}, 3.1.0
0.4.3                      2.11.12, 2.12.{13..15}, 2.13.{4..8}, 3.1.{0..1}
========================== ===============================================

Sbt settings and tasks
----------------------

Use ``nativeConfig`` in `sbt` to provide settings. This is often
done in a project's `build.sbt`.

**Warning**: If you change settings you should clean your project to remove generated
code to be safe.

Scala Native starts execution with a NativeConfig object, called nativeConfig,
filled with default values::

  show ThisBuild / nativeConfig

Each ``withX()`` method creates a new
NativeConfig with the indicated ``X`` value set.  All other settings are taken
from the Config object being accessed. 

To show nativeConfig values active in current scope at any point in time:

  sbt> show nativeConfig

To set a new value and replace any previous setting:

.. code-block:: scala

    import scala.scalanative.build._

    nativeConfig ~= {
      _.withLTO(LTO.thin)
        .withMode(Mode.releaseFast)
        .withGC(GC.commix)
    }

To append a value to the right of any previous setting:

.. code-block:: scala

    import scala.scalanative.build._

    // Enable verbose reporting during compilation
    nativeConfig ~= { c =>
      c.withCompileOptions(_ :+ "-v")
    }

    // Use an alternate linker
    nativeConfig ~= { c =>
      c.withLinkingOptions(c.linkingOptions ++ Seq("-fuse-ld=mold"))
    }

    /* The keen observer will note that "-fuse-ld=mold" could also have been
     *  set using "withCompileOptions". 
     */

===== ======================== ================ =========================================================
Since Name                     Type             Description
===== ======================== ================ =========================================================
0.1   ``compile``              ``Analysis``     Compile Scala code to NIR
0.1   ``run``                  ``Unit``         Compile, link and run the generated binary
0.1   ``package``              ``File``         Similar to standard package with addition of NIR
0.1   ``publish``              ``Unit``         Similar to standard publish with addition of NIR (1)
0.1   ``nativeLink``           ``File``         Link NIR and generate native binary
0.1   ``nativeClang``          ``File``         Path to ``clang`` command
0.1   ``nativeClangPP``        ``File``         Path to ``clang++`` command
0.1   ``nativeCompileOptions`` ``Seq[String]``  Extra options passed to clang verbatim during compilation
0.1   ``nativeLinkingOptions`` ``Seq[String]``  Extra options passed to clang verbatim during linking
0.1   ``nativeMode``           ``String``       One of ``"debug"``, ``"release-fast"``, ``"release-size"`` or ``"release-full"`` (2)
0.2   ``nativeGC``             ``String``       One of ``"none"``, ``"boehm"``, ``"immix"`` or ``"commix"`` (3)
0.3.3 ``nativeLinkStubs``      ``Boolean``      Whether to link ``@stub`` definitions, or to ignore them
0.4.0 ``nativeConfig``         ``NativeConfig`` Configuration of the Scala Native plugin
0.4.0 ``nativeLTO``            ``String``       One of ``"none"``, ``"full"`` or ``"thin"`` (4)
0.4.0 ``targetTriple``         ``String``       The platform LLVM target triple
0.4.0 ``nativeCheck``          ``Boolean``      Shall the linker check intermediate results for correctness?
0.4.0 ``nativeDump``           ``Boolean``      Shall the linker dump intermediate results to disk?
===== ======================== ================ =========================================================

1. See `Publishing`_ and `Cross compilation`_ for details.
2. See `Compilation modes`_ for details.
3. See `Garbage collectors`_ for details.
4. See `Link-Time Optimization (LTO)`_ for details.

Compilation modes
-----------------

Scala Native supports three distinct linking modes:

1. **debug.** (default)

   Default mode. Optimized for shortest compilation time. Runs fewer
   optimizations and is much more suited for iterative development workflow.
   Similar to clang's ``-O0``.

2. **release.** (deprecated since 0.4.0)

   Aliases to **release-full**.

2. **release-fast.** (introduced in 0.4.0)

   Optimize for runtime performance while still trying to keep
   quick compilation time and small emitted code size.
   Similar to clang's ``-O2`` with addition of link-time optimization over
   the whole application code.

2. **release-size.** (introduced in 0.5.0)

   Optimize for reduced output size while still trying to keep
   quick compilation time and relatively fast runtime performance.
   Similar to clang's ``-Oz`` with addition of link-time optimization over
   the whole application code.

3. **release-full.** (introduced in 0.4.0)

   Optimized for best runtime performance, even if hurts compilation
   time and code size. This modes includes a number of more aggresive optimizations
   such type-driven method duplication and more aggresive inliner.
   Similar to clang's ``-O3`` with addition of link-time optimization over
   the whole application code.

Garbage collectors
------------------

1. **immix.** (default since 0.3.8, introduced in 0.3)

   Immix is a mostly-precise mark-region tracing garbage collector.
   More information about the collector is available as part of the original
   `0.3.0 announcement <https://github.com/scala-native/scala-native/releases/tag/v0.3.0>`_.

2. **commix.** (introduced in 0.4)

   Commix is parallel mark and concurrent sweep garbage collector based on Immix

3. **boehm.** (default through 0.3.7)

   Conservative generational garbage collector. More information is available
   at the Github project "ivmai/bdgc" page.

4. **none.** (experimental, introduced in 0.2)

   Garbage collector that allocates things without ever freeing them. Useful
   for short-running command-line applications or applications where garbage
   collections pauses are not acceptable.

Link-Time Optimization (LTO)
----------------------------

Scala Native relies on link-time optimization to maximize runtime performance
of release builds. There are three possible modes that are currently supported:

1. **none.** (default)

   Does not inline across Scala/C boundary. Scala to Scala calls
   are still optimized.

2. **full.** (available on Clang 3.8 or older)

   Inlines across Scala/C boundary using legacy FullLTO mode.

3. **thin.** (recommended on Clang 3.9 or newer)

   Inlines across Scala/C boundary using LLVM's latest
   `ThinLTO mode <https://clang.llvm.org/docs/ThinLTO.html>`_.
   Offers both better compilation speed and
   better runtime performance of the generated code
   than the legacy FullLTO mode.

Cross compilation using target triple
-------------------------------------

The target triple can be set to allow cross compilation (introduced in 0.4.0).
Use the following approach in `sbt` to set the target triple:

.. code-block:: scala

    nativeConfig ~= { _.withTargetTriple("x86_64-apple-macosx10.14.0") }

you may create a few dedicated projects with different target triples. If you
have multiple project definitions for different macOS architectures, eg:

.. code-block:: scala

    lazy val sandbox64 = project.in(file("sandbox"))
        .settings(nativeConfig ~= { _.withTargetTriple("arm64-apple-darwin20.6.0") })

    lazy val sandboxM1 = project.in(file("sandbox"))
        .settings(nativeConfig ~= { _.withTargetTriple("x86_64-apple-darwin20.6.0") })

These project definitions allow to produce different binaries - one dedicated
for the `x86_64` platform and another one for `arm64`. You may easily combine
them to one so called fat binary or universal binary via lipo:

.. code-block:: sh

     lipo -create sandbox64/target/scala-2.12/sandbox64-out sandboxM1/target/scala-2.12/sandboxM1-out -output sandbox-out

which produces `sandbox-out` that can be used at any platform.

You may use `FatELF https://icculus.org/fatelf/` to build fat binaries for Linux.

Build target
------------

Setting build target allows you to specify to what type of object your project should be linked to.
As an example, to link it as dynamic library use the following command:

.. code-block:: scala

    nativeConfig ~= { _.withBuildTarget(BuildTarget.libraryDynamic) }

1. **application** (default)

   Results in creating ready to use executable program.

2. **libraryDynamic**

   Results in dynamic library being built based on entry point methods annotated with `@exported`,
   for details see :ref:`interop`.

3. **libraryStatic**

    Results in building static library using the same semantincs as in the libraryDynamic. 
    Exported methods should handle exceptions, as they might not be able to be catched in the program using static library.
    Building static library requires LLVM 14 or newer.

Publishing
----------

Scala Native supports sbt's standard workflow for the package distribution:

1. Compile your code.
2. Generate a jar with all of the class files and NIR files.
3. Publish the jar to `sonatype`_, `bintray`_ or any other 3rd party hosting service.

Once the jar has been published, it can be resolved through sbt's standard
package resolution system.

.. _sonatype: https://github.com/xerial/sbt-sonatype
.. _bintray: https://github.com/sbt/sbt-bintray

Cross compilation
-----------------

`sbt-crossproject <https://github.com/portable-scala/sbt-crossproject>`_ is an
sbt plugin that lets you cross-compile your projects against all three major
platforms in Scala: JVM, JavaScript via Scala.js, and native via Scala Native.
It is based on the original cross-project idea from Scala.js and supports the
same syntax for existing JVM/JavaScript cross-projects. Please refer to the
project's
`README <https://github.com/portable-scala/sbt-crossproject/blob/master/README.md>`_
for details.
