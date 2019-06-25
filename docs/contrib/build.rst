.. _build:

Guide to the sbt build
======================================

This section gives some basic information and tips about the build system. The
``sbt`` build system is quite complex and effectively brings together all the
components of Scala Native. The ``build.sbt`` file is at the root of the project
along with the sub-projects that make up the system.

Overview
--------------------------------
In order to effectively work with Scala Native, a knowledge of the build system
is very helpful. In general the code is built and published to your local Ivy
repository using the `sbt` `publishLocal` command so that other components in the
system can depend on each other via normal `sbt` dependencies. Although the
``build.sbt`` file and other code in the system is the way to learn the system
thoroughly, the following sections will give information that should be helpful
to get started.

The build has roughly four groups of sub-projects as follows:

1.  The Native Scala Compiler plugin and libraries. Each of these projects depend
    on the next project in the list.

    - `nscplugin`

    - `nativelib`

    - `clib`

    - `posixlib`

    - `javalib`

    - `auxlib`

    - `scalalib`

2.  The Scala Native plugin and dependencies (directory names are in parentheses).

    - `sbtScalaNative (sbt-scala-native)`

    - `tools`

    - `nir`, `util`

    - `nirparser`

3.  The Scala Native test interface and dependencies.

    - `testInterface (test-interface)`

    - `testInterfaceSerialization (test-interface-serialization)`

    - `testInterfaceSbtDefs (test-interface-sbt-defs)`

4.  Tests and benchmarks (no dependencies on each other).

    - `tests (unit-tests)`

    - `tools` This has tests within the project

    - `(scripted-tests)`

    - `benchmarks`

Each of the groups above also depend on the previous group being compiled and
published locally. The sbt plugin ``sbtScalaNative`` is used inside Scala Native
exactly as it is used in a project using Scala Native. The plugin is needed
by the `testInterface` and also the `tests` that use the `testInterface`
to compile native code. Please refer to the `build.sbt` file as the final
authority.

Building Scala Native
---------------------
Once you have cloned Scala Native from git, `cd` into the base directory. Inside
this directory is the `build.sbt` file which is used to build Scala Native. This
file has `sbt` command aliases which are used to help build the system. In order
to build Scala Native for the first time you should run the following commands:

.. code-block:: text

    $ sbt
    > rebuild

If you want to run all the tests and benchmarks, which takes awhile, you can run
the `test-all` command after the systems builds.

Normal development workflow
---------------------------
Let us suppose that you wish to work on the `javalib` project to add some code
or fix a bug. Once you make a change to the code, run the following command
at the sbt prompt to compile the code and run the tests:

.. code-block:: text

    > javalib/publishLocal
    > tests/test

You can run only the test of interest by using one of the following commands:

.. code-block:: text

    > tests/testOnly java.lang.StringSuite
    > tests/testOnly *StringSuite

Some additional tips are as follows.

- If you change anything in `tools` (linker, optimizer, codegen), you need to
  `reload`, not `rebuild`. It's possible because we textually include code of
  the `sbt` plugin and the toolchain.

- If you change `nscplugin`, `rebuild` is the only option. This is because
  the Scala compiler uses this plugin to generate the code that Scala Native uses.

Using Metals for development
--------------------------------------------------
The Scala community has been working on
`Metals <https://scalameta.org/metals/>`_ which uses the
`Language Server Protocol(LSP) <https://microsoft.github.io/language-server-protocol/>`_
to communicate with an editor of your choice such as
`VSCode. <https://code.visualstudio.com/>`_

The default build uses sbt `0.13.18` for the Scala Native plugin which uses
Scala `2.10` which is not supported by Metals. Switching the build to
the newer `sbt` version will change the plugin code to compile with
Scala `2.12` so the Metals restriction does not apply. When Metals starts it
sets an environment variable `METALS_ENABLED=true` but Metals is not able to
run the required `rebuild` step so we need to open `sbt` in our own terminal.

You can set the environment variable before running `sbt` which will switch
the build to use the newer `sbt`. The exact version is specified in the build.

.. code-block:: text

    $ export METALS_ENABLED=true
    $ sbt
    > rebuild

If you do not wish to set an environment variable, you can set the `sbt` version
at the `sbt` prompt as follows:

.. code-block:: text

    $ sbt
    > ^^1.2.8
    > rebuild

Once these steps are completed, you can open the Scala Native project with VSCode
and Metals provides a very good IDE experience. Metals also allows you to run
`scalafmt` from within the editor. Please refer to the link above for more
information or use the `Gitter chat here. <https://gitter.im/scalameta/metals>`_

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
to compile.

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


The next section has more build and development information for those wanting
to work on :ref:`compiler`.

.. [1] http://www.scala-native.org/en/latest/user/setup.html
.. [2] http://www.scala-native.org/en/latest/user/sbt.html

