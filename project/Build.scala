import sbt._, Keys._
import complete.DefaultParsers._
import scala.io.Source
import scala.scalanative.sbtplugin.{ScalaNativePlugin, ScalaNativePluginInternal}
import ScalaNativePlugin.autoImport._

object ScalaNativeBuild extends Build {
  val checkoutDottyLibrary = taskKey[Unit](
    "Fetches the scala source for the current scala version")

  lazy val commonSettings = Seq(
    scalaVersion := "2.10.6",
    organization := "org.scala-native",
    version      := "0.1-SNAPSHOT",

    checkoutDottyLibrary := {
      /*val librarysrcpath = "dottylib/library-src/main/scala/"
      val librarysrcfile = file(librarysrcpath)
      val dottysrcpath = "dottylib/dotty-src/main/scala/scala"
      val dottysrcfile = file(dottysrcpath)
      IO.delete(librarysrcfile)
      IO.delete(dottysrcfile)
      IO.createDirectory(librarysrcfile)
      IO.createDirectory(dottysrcfile)

      val path =
        "submodules/dotty/test/dotc/scala-collections.whitelist"
      val whitelist = Source.fromFile(path, "UTF8").getLines()
        .map(_.trim) // allow identation
        .filter(!_.startsWith("#")) // allow comment lines prefixed by #
        .map(_.takeWhile(_ != '#').trim) // allow comments in the end of line
        .filter(_.nonEmpty)
        .toList

      whitelist.foreach { path =>
        val base = path.replace("./scala-scala/src/library/", "")
        val from = file("submodules/dotty-scala/src/library/" + base)
        val to   = file("dottylib/library-src/main/scala/" + base)

        IO.copyFile(from, to)
      }
      IO.delete(file(librarysrcpath + "/scala/collection"))

      IO.copyDirectory(file("submodules/dotty/src/scala/runtime"),
          file(dottysrcpath + "/runtime"))
      */
      ???
    }
  )

  lazy val nativeSettings =
    commonSettings ++ ScalaNativePlugin.projectSettings

  lazy val util =
    project.in(file("util")).
      settings(commonSettings: _*)

  lazy val nir =
    project.in(file("nir")).
      settings(commonSettings: _*).
      dependsOn(util)

  lazy val tools =
    project.in(file("tools")).
      settings(commonSettings: _*).
      settings(
        libraryDependencies += "commons-io" % "commons-io" % "2.4"
      ).
      dependsOn(nir)

  lazy val nscplugin =
    project.in(file("nscplugin")).
      settings(commonSettings: _*).
      settings(
        scalaVersion := "2.11.8",
        unmanagedSourceDirectories in Compile ++= Seq(
          (scalaSource in (nir, Compile)).value,
          (scalaSource in (util, Compile)).value
        ),
        libraryDependencies ++= Seq(
          "org.scala-lang" % "scala-compiler" % scalaVersion.value,
          "org.scala-lang" % "scala-reflect" % scalaVersion.value
        )
      )

  lazy val sbtplugin =
    project.in(file("sbtplugin")).
      settings(commonSettings).
      settings(
        sbtPlugin := true
      ).
      dependsOn(tools)

  lazy val nativelib =
    project.in(file("nativelib")).
      settings(nativeSettings)

  lazy val javalib =
    project.in(file("javalib")).
      settings(nativeSettings)

  lazy val dottylib =
    project.in(file("dottylib")).
      settings(nativeSettings).
      settings(
        filterOutScalaLibraries := false,

        unmanagedSourceDirectories in Compile ++= Seq(
          file("dottylib/library-src"),
          file("dottylib/runtime-src")
        )
      )

  lazy val sandbox =
    project.in(file("sandbox")).
      settings(nativeSettings).
      dependsOn(dottylib)
}
