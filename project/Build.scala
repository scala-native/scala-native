import sbt._, Keys._
import complete.DefaultParsers._
import scala.io.Source
import scala.scalanative.sbtplugin.ScalaNativePlugin

object ScalaNativeBuild extends Build {
  val fetchScalaSource = taskKey[Unit](
    "Fetches the scala source for the current scala version")

  lazy val commonSettings = Seq(
    scalaVersion := "2.10.6",
    organization := "org.scala-native",
    version      := "0.1-SNAPSHOT",

    fetchScalaSource := {
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
        val to   = file("scalalib/src-base/main/scala/" + base)

        IO.copyFile(from, to)
      }
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

  lazy val sbtplugin =
    project.in(file("sbtplugin")).
      settings(commonSettings).
      settings(
        sbtPlugin := true
      ).
      dependsOn(tools)

  lazy val javalib =
    project.in(file("javalib")).
      settings(nativeSettings)

  lazy val nativelib =
    project.in(file("nativelib")).
      settings(nativeSettings)

  lazy val scalalib =
    project.in(file("scalalib")).
      settings(nativeSettings).
      settings(
        unmanagedSourceDirectories in Compile ++= Seq(
          file("scalalib/src-base")
        )
      )

  lazy val sandbox =
    project.in(file("sandbox")).
      settings(nativeSettings)
}
