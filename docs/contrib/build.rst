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
but may perform better than `release`. The `release-size` mode optimizes for reduced size.

The `optimize` setting is controlled via the `SCALANATIVE_OPTIMIZE` environment
variable. Valid values are `true` and `false`. The default value is `true`.
This setting controls whether the Interflow optimizer is enabled or not.

The path to used include and library dirs is controlled via environment variables
the `SCALANATIVE_INCLUDE_DIRS` and `SCALANATIVE_LIB_DIRS`.

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

Use the custom sbt command to publish all projects for a specific Scala version (`x,y,z`):

.. code-block:: text

    > publish-local-dev x.y.z

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

6. External tests and its dependencies. Sources of these tests are not stored
   in this project, but fetched from external sources, e.g.: Scala compiler repository.
   Sources in this project define interface used by Scala Native and tests filters.

    - ``scalaPartest (scala-partest)`` (JVM project, uses Scala Native artifacts)

    - ``scalaPartestRuntime (scala-partest-runtime)`` (Scala native project)

    - ``scalaPartestTests (scala-partest-tests)`` (JVM project)

    - ``scalaPartestJunitTests (scala-partest-junit-tests)`` (Scala Native project)

7. JUnit plugin, its tests and dependencies. Following sources define JUnit compiler
   for Scala Native and its runtime, as well as compliance tests and internal stubs.

    - ``junitPlugin (junit-plugin)``

    - ``junitRuntime (junit-runtime)``

    - ``junitTestOutputsJVM (junit-test/output-jvm)``

    - ``junitTestOutputsNative (junit-test/output-native)``

    - ``junitAsyncJVM (junit-async/jvm)``

    - ``junitAsyncNative (junit-async/native)``

Apart from those mentioned sub-projects it is possible to notice project-like directory ``testInterfaceCommon (test-interface-common)``.
Its content is shared as unmanaged source dependency between JVM and Native sides of test interface.

Working with scalalib overrides
-------------------------------
Scalalib project does not introduce any new classes but provides overrides
for the existing Scala standard library. Some of these overrides exist to improve
the performance of Scala Native, eg. by explicit inlining of some methods. 
When running `scalalib/compile` it will automatically use existing `*.scala` files defined in `overrides` directories. To reduce the number of changes between overrides and 
original Scala sources, we have introduced a patching mechanism. 
Each file defined with the name `*.scala.patch` contains generated patch, which would be applied
onto source defined for the current Scala version inside its standard library.
In case `overrides*` directory contains both `*.scala` file and its corresponding patch file,
only `*.scala` file would be added to the compilation sources.  

To operate with patches it is recommended to use ScalaCLI script `scripts/scalalib-patch-tool.sc`. 
It takes 2 mandatory arguments: command to use and Scala version. There are currently 3 supported commands defined:
* recreate - creates `*.scala` files based on original sources with applied patches corresponding to their name;
* create - creates `*.scala.patch` files from defined `*.scala` files in overrides directory with corresponding name;
* prune - deletes all `*.scala` files which does not have corresponding `*.scala.patch` file;

(e.g. `scala-cli scripts/scalalib-patch-tool.sc -- recreate 2.13.10`)

Each of these commands is applied to all files defined in the overrides directory. 
By default override directory is selected based on the used scala version, 
if it's not the present script will try to use directory with corresponding Scala binary version, 
or it would try to use Scala epoch version or `overrides` directory. If none of these directories exists it will fail. 
It is also possible to define explicitly overrides directory to use by passing it as the third argument to the script.


Locally publish docs
---------------------------------------
Follow the steps after cloning the `scalanative <https://github.com/scala-native/scala-native>`_ repo and changing to `scala-native` directory.

1. First time building the docs. This command will setup & build the docs.

.. code-block:: text

    $ bash scripts/makedocs setup

2. If setup is already done. This command will only build the docs assuming setup is already done.

.. code-block:: text

    $ bash scripts/makedocs 

3. Navigate to ``docs/_build/html`` directory and open ``index.html`` file in your browser.

The next section has more build and development information for those wanting
to work on :ref:`compiler`.

.. [1] http://www.scala-native.org/en/latest/user/setup.html
.. [2] http://www.scala-native.org/en/latest/user/sbt.html
