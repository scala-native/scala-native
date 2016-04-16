import scala.io.Source
import scala.scalanative.sbtplugin.{ScalaNativePlugin, ScalaNativePluginInternal}
import ScalaNativePlugin.autoImport._

val copyScalaLibrary = taskKey[Unit](
  "Checks out scala standard library from submodules/scala.")

lazy val toolSettings = Seq(
  scalaVersion := "2.10.6",
  organization := "org.scala-native",
  version      := scala.scalanative.nir.Versions.current,

  copyScalaLibrary := {
    IO.delete(file("scalalib/src/main/scala"))
    IO.copyDirectory(
      file("submodules/scala/src/library/scala"),
      file("scalalib/src/main/scala/scala"))
  }
)

lazy val libSettings =
  toolSettings ++ ScalaNativePlugin.projectSettings ++ Seq(
    scalaVersion := "2.11.8"
  )

lazy val util =
  project.in(file("util")).
    settings(toolSettings)

lazy val nir =
  project.in(file("nir")).
    settings(toolSettings).
    dependsOn(util)

lazy val nrt =
  project.in(file("nrt")).
    settings(toolSettings)

lazy val tools =
  project.in(file("tools")).
    settings(toolSettings).
    settings(
      libraryDependencies += "commons-io" % "commons-io" % "2.4"
    ).
    dependsOn(nir)

lazy val nscplugin =
  project.in(file("nscplugin")).
    settings(toolSettings).
    settings(
      scalaVersion := "2.11.8",
      unmanagedSourceDirectories in Compile ++= Seq(
        (scalaSource in (nir, Compile)).value,
        (scalaSource in (util, Compile)).value
      ),
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % scalaVersion.value,
        "org.scala-lang" % "scala-reflect" % scalaVersion.value
      ),
      publishArtifact in (Compile, packageDoc) := false
    )

lazy val sbtplugin =
  project.in(file("sbtplugin")).
    settings(toolSettings).
    settings(
      sbtPlugin := true
    ).
    dependsOn(tools)

lazy val nativelib =
  project.in(file("nativelib")).
    settings(libSettings)

lazy val javalib =
  project.in(file("javalib")).
    settings(libSettings).
    dependsOn(nativelib)

lazy val scalalib =
  project.in(file("scalalib")).
    settings(libSettings).
    dependsOn(javalib)

lazy val sandbox =
  project.in(file("sandbox")).
    settings(libSettings).
    dependsOn(scalalib)
