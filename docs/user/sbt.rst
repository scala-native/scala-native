.. _sbt:

Building projects with sbt
==========================

If you have reached this section you probably have a system that is now able to compile and run Scala Native programs.

Minimal sbt project
-------------------

The easiest way to make a fresh project is to use our official gitter8 template::

    sbt new scala-native/scala-native.g8

This generates the following files:

* ``project/plugins.sbt`` to add a plugin dependency::

    addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.4.0-M2")

* ``project/build.properties`` to specify the sbt version::

    sbt.version = 1.4.1

* ``build.sbt`` to enable the plugin and specify Scala version::

    enablePlugins(ScalaNativePlugin)

    scalaVersion := "2.11.12"

* ``src/main/scala/Main.scala`` with minimal application::

    object Main {
      def main(args: Array[String]): Unit =
        println("Hello, world!")
    }

Now, simply run ``sbt run`` to get everything compiled and have the expected
output! Please refer to the :ref:`faq` if you encounter any problems.

Scala versions
--------------

Scala Native supports following Scala versions for corresponding releases:

========================== ========================
Scala Native Version       Scala Versions
========================== ========================
0.1.x                      2.11.8
0.2.x                      2.11.8, 2.11.11
0.3.0-0.3.3                2.11.8, 2.11.11
0.3.4+, 0.4.0-M1, 0.4.0-M2 2.11.8, 2.11.11, 2.11.12
========================== ========================

Sbt settings and tasks
----------------------

===== ======================== =============== =========================================================
Since Name                     Type            Description
===== ======================== =============== =========================================================
0.1   ``compile``              ``Analysis``    Compile Scala code to NIR
0.1   ``run``                  ``Unit``        Compile, link and run the generated binary
0.1   ``package``              ``File``        Similar to standard package with addition of NIR
0.1   ``publish``              ``Unit``        Similar to standard publish with addition of NIR (1)
0.1   ``nativeLink``           ``File``        Link NIR and generate native binary
0.1   ``nativeClang``          ``File``        Path to ``clang`` command
0.1   ``nativeClangPP``        ``File``        Path to ``clang++`` command
0.1   ``nativeCompileOptions`` ``Seq[String]`` Extra options passed to clang verbatim during compilation
0.1   ``nativeLinkingOptions`` ``Seq[String]`` Extra options passed to clang verbatim during linking
0.1   ``nativeMode``           ``String``      One of ``"debug"``, ``"release-fast"`` or ``"release-full"`` (2)
0.2   ``nativeGC``             ``String``      One of ``"none"``, ``"boehm"`` or ``"immix"`` (3)
0.3.3 ``nativeLinkStubs``      ``Boolean``     Whether to link ``@stub`` definitions, or to ignore them
0.4.0 ``nativeLTO``            ``String``      One of ``"none"``, ``"full"`` or ``"thin"`` (4)
0.4.0 ``nativeCheck``          ``Boolean``     Shall the linker check intermediate results for correctness?
0.4.0 ``nativeDump``           ``Boolean``     Shall the linker dump intermediate results to disk? 
===== ======================== =============== =========================================================

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

2. **boehm.** (default through 0.3.7)

   Conservative generational garbage collector. More information is available
   at the Github project "ivmai/bdgc" page.

3. **none.** (experimental, introduced in 0.2)

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

Including Native Code in your Application or Library
----------------------------------------------------

Scala Native uses native C and C++ code to interact with the underlying
platform and operating system. Since the tool chain compiles and links
the Scala Native system, it can also compile and link C and C++ code
included in an application project or a library that supports Scala
Native that includes C and/or C++ source code.

Supported file extensions for native code are `.c`, `.cpp`, and `.S`.

Note that `.S` files or assembly code is not portable across different CPU
architectures so conditional compilation would be needed to support
more than one architecture. You can also include header files with
the extensions `.h` and `.hpp`.

Applications with Native Code
-----------------------------

In order to create standalone native projects with native code use the
following procedure. You can start with the basic Scala Native template.

Add C/C++ code into `src/main/resources/scala-native`. The code can be put in
subdirectories as desired inside the `scala-native` directory. As an example,
create a file named `myapi.c` and put it into your `scala-native` directory
as described above.

.. code-block:: c

    long long add3(long long in) { return in + 3; }

Next, create a main file as follows:

.. code-block:: scala

    import scalanative.unsafe._

    @extern
    object myapi {
      def add3(in: CLongLong): CLongLong = extern
    }

    object Main {
      import myapi._
      def main(args: Array[String]): Unit = {
        val res = add3(-3L)
        assert(res == 0L)
        println(s"Add3 to -3 = $res")
      }
    }

Finally, compile and run this like a normal Scala Native application.


Using libraries with Native Code
------------------------------------------

Libraries developed to target the Scala Native platform
can have C, C++, or assembly files included in the dependency. The code is
added to `src/main/resources/scala-native` and is published like a normal
Scala library. The code can be put in subdirectories as desired inside the
`scala-native` directory. These libraries can also be cross built to
support Scala/JVM or Scala.js if the Native portions have replacement
code on the respective platforms.

The primary purpose of this feature is to allow libraries to support
Scala Native that need native "glue" code to operate. The current
C interopt does not allow direct access to macro defined constants and
functions or allow passing "struct"s from the stack to C functions.
Future versions of Scala Native may relax these restrictions making
this feature obsolete.

Note: This feature is not a replacement for developing or distributing
native C/C++ libraries and should not be used for this purpose.

If the dependency contains native code, Scala Native will identify the
library as a dependency that has native code and will unpack the library.
Next, it will compile, link, and optimize any native code along with the
Scala Native runtime and your application code. No additional information
is needed in the build file other than the normal dependency so it is
transparent to the library user.

This feature can be used in combination with the feature above that
allows native code in your application.

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

Continue to :ref:`lang`.
