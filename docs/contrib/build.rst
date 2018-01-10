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
repository so that other components in the system can depend on each other.
Although the ``build.sbt`` file and other code in the system is the way to learn the system
thoroughly, the following sections will give information that should be helpful.

The build has roughly four groups of sub-projects as follows:

1.  The Native Scala Compiler plugin and libraries. Each of these depend on the next project
    in the list.

    - `nscplugin`

    - `nativelib`

    - `javalib`

    - `auxlib`

    - `scalalib`


2.  The Scala Native plugin and dependencies (directory names are in parentheses).

    - `sbtScalaNative (sbt-scala-native)`

    - `tools`

    - `nir`

    - `util`

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
exactly as it is used in a project using Scala Native. The plugin is needed to
by the ``testInterface`` and also the `tests` that use the ``testInterface``
to compile native code.

Helpful Hints and Tips
--------------------------------



The next section has more build and development information for those wanting
to work on :ref:`compiler`.

