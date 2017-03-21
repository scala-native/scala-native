.. _sbt:

Building projects with SBT
==========================

If you have reached this section you probably have a system that is now able to compile and run Scala Native programs.

Minimal sbt project
-------------------

You need sbt 0.13.0 or later.

```
sbt new scala-native/scala-native.g8 && cd scala-native-example

name [scala-native-example]: <Press ENTER>
```
now simply run ``sbt run`` to get everything compiled and have the expected output!

Sbt settings
------------

======================== =============== =======================================
Name                     Type            Description
======================== =============== =======================================
``nativeClang``          ``File``        Path to ``clang`` command
``nativeClangPP``        ``File``        Path to ``clang++`` command
``nativeCompileOptions`` ``Seq[String]`` Extra options passed to clang verbatim
``nativeMode``           ``String``      Either ``"debug"`` or ``"release"`` (1)
======================== =============== =======================================

See `Compilation modes`_ for details.

Sbt tasks
---------

============== ====================================================
Name           Description
============== ====================================================
``compile``    Compile Scala code to NIR
``nativeLink`` Link NIR and generate native binary
``run``        Compile, link and run the generated binary
``package``    Similar to standard package with addition of NIR
``publish``    Similar to standard publish with addition of NIR (1)
============== ====================================================

See `Publishing`_ for details.

Compilation modes
-----------------

Scala Native supports two distinct linking modes:

1. **debug.**

   Default mode. Optimized for shortest compilation time. Runs fewer
   optimizations and is much more suited for iterative development workflow.
   Similar to clang's ``-O0``.

2. **release.**

   Optimized for best runtime performance at expense of longer compilation time.
   Similar to clang's ``-O2`` with addition of link-time optimisation over
   the whole application code.

Publishing
----------

Scala Native supports sbt's standard workflow for the package distribution:

1. Compile your code.
2. Generate a jar with all of the classfiles and NIR files.
3. Publish the jar to `sonatype`_, `bintray`_ or any other 3rd party hosting service.

Once the jar has been published, it can be resolved through sbt's standard
package resolution system.

.. _sonatype: https://github.com/xerial/sbt-sonatype
.. _bintray: https://github.com/sbt/sbt-bintray

Cross compilation between JS, JVM and Native
--------------------------------------------

We created `sbt-crossproject <https://github.com/scala-native/sbt-crossproject>`_
to be a drop-in replacement of Scala.js' crossProject. Please refer to its documentation
in the README.

Continue to :ref:`lang`.
