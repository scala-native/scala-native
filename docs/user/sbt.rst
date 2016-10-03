.. _sbt:

Building projects with SBT
==========================

If you have reached this section you probably have a system that is now able to compile and run Scala Native programs.

Start within a new folder, and create a file ``project/plugins.sbt`` as follows::

    resolvers += Resolver.sonatypeRepo("snapshots")

    addSbtPlugin("org.scala-native" % "sbtplugin"  % "0.1-SNAPSHOT")

define a new ``build.sbt``::

    enablePlugins(ScalaNativePlugin)

    scalaVersion := "2.11.8"

and now you can write your first application in ``./src/main/scala/HelloWorld.scala``::

    package example

    object Main {
      def main(args: Array[String]): Unit =
        println("Hello, world!")
    }

now simply run ``sbt run`` to get everything compiled and have the expected output!

Continue to :ref:`lang`.
