.. _build:

Guide to the sbt build
======================================

This section gives some basic information and tips about the build system. The
``sbt`` build system is quite complex and effectively brings together all the
components of Scala Native. The ``build.sbt`` file is at the root of the project
along with the sub-projects that make up the system.

Common sbt commands
-------------------
Once you have cloned Scala Native from git, ``cd`` into the base directory and
run ``sbt`` to launch the sbt build. Inside the sbt shell, the most common
commands are the following:

- ``sandbox/run`` -- run the main method of the `sandbox` project
- ``tests/test`` -- run the unit tests
- ``tools/test`` -- run the unit tests of the tools, aka the linker
- ``sbtScalaNative/scripted`` -- run the integration tests of the sbt plugin
  (this takes a while)
- ``clean`` -- delete all generated sources, compiled artifacts, intermediate
  products, and generally all build-produced files
- ``reload`` -- reload the build, to take into account changes to the sbt plugin
  and its transitive dependencies

If you want to run all the tests and benchmarks, which takes a while, you can
run the ``test-all`` command, ideally after ``reload`` and ``clean``.

Normal development workflow
---------------------------
Let us suppose that you wish to work on the ``javalib`` project to add some code
or fix a bug. Once you make a change to the code, run the following command
at the sbt prompt to compile the code and run the tests:

.. code-block:: text

    > tests/test

You can run only the test of interest by using one of the following commands:

.. code-block:: text

    > tests/testOnly java.lang.StringSuite
    > tests/testOnly *StringSuite

Scripted tests are used when you need to interact with the file system,
networking, or the build system that cannot be done with a unit test. They
are located in the `scripted-tests` directory.

Run all the scripted tests or just one test using the following examples respectively.
To run an individual test substitute the test to run for `native-code-include`:

.. code-block:: text

    > sbtScalaNative/scripted
    > sbtScalaNative/scripted run/native-code-include

Some additional tips are as follows.

- If you modify the ``nscplugin``, you will need to ``clean`` the project that
  you want to rebuild with its new version (typically ``sandbox/clean`` or
  ``tests/clean``). For a full rebuild, use the global ``clean`` command.

- If you modify the sbt plugin or any of its transitive dependencies
  (``sbt-scala-native``, ``nir``, ``util``, ``tools``, ``test-runner``), you
  will need to ``reload`` for your changes to take effect with most test
  commands (except with the ``scripted`` tests).

- For a completely clean build, from scratch, run ``reload`` *and* ``clean``.

Build settings via environment variables
--------------------------------------------------
Two build settings, ``nativeGC`` and ``nativeMode`` can be changed via
environment variables. They have default settings that are used unless
changed. The setting that controls the garbage collector is `nativeGC`.
Scala Native has a high performance Garbage Collector (GC) ``immix``
that comes with the system or the `boehm` GC which can be used when
the supporting library is installed. The setting `none` also exists for a
short running script or where memory is not an issue.

Scala Native uses Continuous integration (CI) to compile and test the code on
different platforms [1]_ and using different garbage collectors [2]_.
The Scala Native `sbt` plugin includes the ability to set an environment
variable `SCALANATIVE_GC` to set the garbage collector value used by `sbt`.
Setting this as follows will set the value in the plugin when `sbt` is run.

.. code-block:: text

    $ export SCALANATIVE_GC=immix
    $ sbt
    > show nativeGC

This setting remains unless changed at the `sbt` prompt. If changed, the value
will be restored to the environment variable value if `sbt` is restarted or
`reload` is called at the `sbt` prompt. You can also revert to the default
setting value by running `unset SCALANATIVE_GC` at the command line
and then restarting `sbt`.

The `nativeMode` setting is controlled via the `SCALANATIVE_MODE` environment
variable. The default mode, `debug` is designed to optimize but compile fast
whereas the `release` mode performs additional optimizations and takes longer
to compile. The `release-fast` mode builds faster, performs less optimizations,
but may perform better than `release`.

The `optimize` setting is controlled via the `SCALANATIVE_OPTIMIZE` environment
variable. Valid values are `true` and `false`. The default value is `true`.
This setting controls whether the Interflow optimizer is enabled or not.

Setting the GC setting via `sbt`
--------------------------------
The GC setting is only used during the link phase of the Scala Native
compiler so it can be applied to one or all the Scala Native projects
that use the `sbtScalaNative` plugin. This is an example to only change the
setting for the `sandbox`.

.. code-block:: text

    $ sbt
    > show nativeGC
    > set nativeGC in sandbox := "none"
    > show nativeGC
    > sandbox/run

The following shows how to set ``nativeGC`` on all the projects.

.. code-block:: text

    > set every nativeGC := "immix"
    > show nativeGC

The same process above will work for setting `nativeMode`.

Locally publish to test in other builds
---------------------------------------
If you need to test your copy of Scala Native in the larger context of a
separate build, you will need to locally publish all the artifacts of Scala
Native.

You can do this with:

.. code-block:: text

    > publishLocal

Afterwards, set the version of `sbt-scala-native` in the target project's
`project/plugins.sbt` to the current SNAPSHOT version of Scala Native, and use
normally.

Organization of the build
-------------------------
The build has roughly five groups of sub-projects as follows:

1.  The compiler plugin, which generates NIR files. It is used in all the
    Scana Native artifacts in the build, with
    ``.dependsOn(nscplugin % "plugin")``. This is a JVM project.

    - ``nscplugin``

2.  The Scala Native core libraries. Those are core artifacts which the sbt
    plugin adds to the ``Compile`` configuration of all Scala Native projects.
    The libraries in this group are themselves Scala Native projects. Projects
    further in the list depend on projects before them.

    - ``nativelib``

    - ``clib``

    - ``posixlib``

    - ``javalib``

    - ``auxlib``

    - ``scalalib``

3.  The Scala Native sbt plugin and its dependencies (directory names are in
    parentheses). These are JVM projects.

    - ``sbtScalaNative (sbt-scala-native)``

    - ``tools``

    - ``nir``, ``util``

    - ``nirparser``

    - ``testRunner (test-runner)``

4.  The Scala Native test interface and its dependencies. The sbt plugin adds
    them to the ``Test`` configuration of all Scala Native projects. These are
    Scala Native projects.

    - ``testInterface (test-interface)``

    - ``testInterfaceSbtDefs (test-interface-sbt-defs)``

5.  Tests and benchmarks (no dependencies on each other).

    - ``tests (unit-tests)`` (Scala Native project)

    - ``tools`` This has tests within the project (JVM project)

    - ``(scripted-tests)`` (JVM project)

Apart from those mentioned sub-projects it is possible to notice project-like directory ``testInterfaceCommon (test-interface-common)``.
Its content is shared as unmanaged source dependency between JVM and Native sides of test interface.

The next section has more build and development information for those wanting
to work on :ref:`compiler`.

.. [1] http://www.scala-native.org/en/latest/user/setup.html
.. [2] http://www.scala-native.org/en/latest/user/sbt.html
