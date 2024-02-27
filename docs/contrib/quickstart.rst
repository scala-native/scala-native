.. _quickstart:

Quick Start Guide
=================

Document built: |today|

Requirements
------------

- Java 8 or newer
- LLVM/Clang 15 or newer
- sbt

Project Structure Overview
--------------------------

See :ref:`build`

Project suffix
--------------

Most projects in ScalaNative cross-build against Scala ``2.12``, ``2.13`` and ``3``, and these projects have a suffix like ``2_12``, ``2_13`` or ``3`` to differentiate the Scala version.
For example, ``sandbox`` has ``sandbox2_12``, ``sandbox2_13`` and ``sandbox3``.

In the following we will use suffix ``3``, but remember that you can build and test for different versions using different suffixes.

Build / Manual Testing on Sandbox
---------------------------------

``sandbox3/run`` to compile, link and run the main method of the sandbox project defined in ``sandbox/src/main/scala/Test.scala``.

It's convenient to run the ``sandbox`` project to verify the build works as expected.

Test
----

**Common Test Commands**

- ``tests3/test`` - run the unit tests for libraries on native build
- ``tests3/testOnly org.scalanative.testsuite.javalib.util.RandomTest`` - run only the test of interest
- ``tests3/testOnly *.RandomTest`` - run only the test of interest using wildcard
- ``testsExt3/test`` - run the unit tests on native build, this module contains tests that requires dummy javalib implementation defined in ``javalibExtDummies``.
- ``nirJVM3/test`` - run the unit tests for NIR
- ``toolsJVM3/test`` - run the unit tests of the tools: ScalaNative backend
- ``sbtScalaNative/scripted`` - run all `scripted tests <https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html>`_ of the sbt plugin (this takes a while).
- ``sbtScalaNative/scripted <test directory to run>`` - run specific scripted tests of the sbt plugin. e.g. ``sbtScalaNative/scripted run/backtrace``
    - Scripted tests are used when you need to interact with the file system, networking, or the build system that cannot be done with a unit test.
    - ``set ThisBuild / scriptedBufferLog := false`` disables buffer log in scripted test and get more verbose output

**Other Test Commands**

- ``testsJVM3/test`` - run ``tests3/test`` on JVM
- ``testsExtJVM3/test`` - run ``testsExt3/test`` on JVM
- ``test-all`` - run all tests, ideally after ``reload`` and ``clean``

**Some additional tips**

- If you modify the ``nscplugin``, you will need to ``clean`` the project that
  you want to rebuild with its new version (typically ``sandbox/clean`` or
  ``tests/clean``). For a full rebuild, use the global ``clean`` command.

- If you modify the sbt plugin or any of its transitive dependencies
  (``sbt-scala-native``, ``nir``, ``util``, ``tools``, ``test-runner``), you
  will need to ``reload`` for your changes to take effect with most test
  commands (except with the ``scripted`` tests).

- For a completely clean build, from scratch, run ``reload`` *and* ``clean``.

Formatting
----------

- ``./scripts/scalafmt`` - format all Scala codes
- ``./scripts/clangfmt`` - format all C/C++ codes

`Publish Locally <https://www.scala-sbt.org/1.x/docs/Publishing.html>`_
-----------------------------------------------------------------------

``publish-local-dev x.y.z`` publishes the ScalaNative artifact and sbt plugin for specified scala version locally.
For example, ``publish-local-dev 3.3.1``, 

You will see, the log message like the following, which means you have successfully published locally for the version ``0.5.0-SNAPSHOT``.

.. code-block:: text

    [info]  published tools_native0.5.0-SNAPSHOT_3 to ... 
    [info]  published ivy to ...tools_native0.5.0-SNAPSHOT_3/0.5.0-SNAPSHOT/ivys/ivy.xml

Then you'll be able to use locally published version in other projects.

.. code-block:: text
 
    # project/plugins.sbt
    addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.0-SNAPSHOT")

    # build.sbt
    scalaVersion := "3.3.1" # set to locally published version
    enablePlugins(ScalaNativePlugin)

Locally build docs
--------------------

1. With the current directory set to the project root, execute:

.. code-block:: text

    $ bash scripts/makedocs 

2. For use in the next step, note the full, absolute path to the
   ``docs/_build/html`` directory. For example:

.. code-block:: text
		
    /Users/Νίκη/MyScalaNativeProject/docs/_build/html
    
3. Verify the results by opening the ``index.html`` of that directory
   in your favorite browser using a URL of the form
   ``file://<absolute_path>/index.html``,
   where ``<absolute_path>`` is the path
   noted in step 2. For example:

.. code-block:: text

    file:///Users/Νίκη/MyScalaNativeProject/docs/_build/html/index.html

Configure Native Build
----------------------

To configure the native build in this project, you can edit ``project/MyScalaNativePlugin.scala`` instead of ``project/Build.scala``.

``MyScalaNativePlugin`` is a custom sbt plugin that extends ``ScalaNativePlugin`` and overrides some of its settings for this project.


Further Information
-------------------

- How to make a commit and PR :ref:`contributing`
- More detailed build setting explanation :ref:`build`
- Scala Native Internal
    - :ref:`compiler`
    - :ref:`nir`
    - :ref:`name_mangling`
- How to setup IDEs :ref:`ides`
