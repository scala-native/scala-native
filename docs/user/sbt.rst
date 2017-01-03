.. _sbt:

Building projects with SBT
==========================

If you have reached this section you probably have a system that is now able to compile and run Scala Native programs.

Start within a new folder, and create a file ``project/plugins.sbt`` as follows::

    resolvers += Resolver.sonatypeRepo("snapshots")

    addSbtPlugin("org.scala-native" % "sbt-scala-native"  % "0.1.0-SNAPSHOT")

Create a file ``project/build.properties`` to define the sbt version as follows::

    sbt.version=0.13.13

define a new ``build.sbt``::

    resolvers += Resolver.sonatypeRepo("snapshots")

    enablePlugins(ScalaNativePlugin)

    scalaVersion := "2.11.8"

and now you can write your first application in ``./src/main/scala/HelloWorld.scala``::

    package example

    object Main {
      def main(args: Array[String]): Unit =
        println("Hello, world!")
    }

now simply run ``sbt run`` to get everything compiled and have the expected output!

Cross compilation between JS, JVM and Native
--------------------------------------------

We created `sbt-cross <https://github.com/scala-native/sbt-cross>`_ to be a
drop-in replacement of Scala.js' crossProject. Please refer to the documentation
in the README.

Continue to :ref:`lang`.
