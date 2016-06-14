import scala.scalanative.sbtplugin.{ScalaNativePlugin, ScalaNativePluginInternal}
import ScalaNativePlugin.autoImport._

val toolScalaVersion = "2.10.6"

val libScalaVersion  = "2.11.8"

lazy val baseSettings = Seq(
  organization := "org.scala-native",
  version      := nativeVersion,
  scalafmtConfig := Some(file(".scalafmt"))
)

lazy val toolSettings =
  baseSettings ++
    Seq(
      scalaVersion := toolScalaVersion,
      scalacOptions ++= Seq(
        "-deprecation",
        "-unchecked",
        "-feature",
        "-encoding", "utf8"
      )
    )

lazy val libSettings =
  baseSettings ++
    ScalaNativePlugin.projectSettings ++
    Seq(
      scalaVersion := libScalaVersion,
      scalacOptions ++= Seq("-encoding", "utf8"),
      javacOptions ++= Seq("-encoding", "utf8"),
      nativeEmitDependencyGraphPath := Some(file("out.dot"))
    )

lazy val util =
  project.in(file("util")).
    settings(toolSettings)

lazy val nir =
  project.in(file("nir")).
    settings(toolSettings).
    dependsOn(util)

lazy val tools =
  project.in(file("tools")).
    settings(toolSettings).
    dependsOn(nir, util)

lazy val nscplugin =
  project.in(file("nscplugin")).
    settings(toolSettings).
    settings(
      scalaVersion := "2.11.8",
      crossVersion := CrossVersion.full,
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
      sbtPlugin := true,
      // Scalafmt fails to format source of sbt plugins.
      scalafmtTest := {}
    ).
    dependsOn(tools)

// rt is a library project but it can't use libSettings
// due to the fact that it contains nrt dependency in
// ScalaNativePlugin.projectSettings.
lazy val rtlib =
  project.in(file("rtlib")).
    settings(baseSettings).
    settings(
      scalaVersion := libScalaVersion
    )

lazy val nativelib =
  project.in(file("nativelib")).
    settings(libSettings)

lazy val clib =
  project.in(file("clib")).
    settings(libSettings).
    dependsOn(nativelib)

lazy val javalib =
  project.in(file("javalib")).
    settings(libSettings).
    dependsOn(nativelib)

lazy val assembleScalaLibrary = taskKey[Unit](
  "Checks out scala standard library from submodules/scala and then applies overrides.")

lazy val scalalib =
  project.in(file("scalalib")).
    settings(libSettings).
    settings(
      assembleScalaLibrary := {
        import org.eclipse.jgit.api._

        val s = streams.value
        val trgDir = target.value / "scalaSources" / scalaVersion.value

        if (!trgDir.exists) {
          s.log.info(s"Fetching Scala source version ${scalaVersion.value}")

          // Make parent dirs and stuff
          IO.createDirectory(trgDir)

          // Clone scala source code
          new CloneCommand()
            .setDirectory(trgDir)
            .setURI("https://github.com/scala/scala.git")
            .call()
        }

        // Checkout proper ref. We do this anyway so we fail if
        // something is wrong
        val git = Git.open(trgDir)
        s.log.info(s"Checking out Scala source version ${scalaVersion.value}")
        git.checkout().setName(s"v${scalaVersion.value}").call()

        IO.delete(file("scalalib/src/main/scala"))
        IO.copyDirectory(
          trgDir / "src" / "library" / "scala",
          file("scalalib/src/main/scala/scala"))

        val epoch :: major :: _ = scalaVersion.value.split("\\.").toList
        IO.copyDirectory(
          file(s"scalalib/overrides-$epoch.$major/scala"),
          file("scalalib/src/main/scala/scala"), overwrite = true)
      },

      scalafmtTest := {},

      compile in Compile <<= (compile in Compile) dependsOn assembleScalaLibrary
    ).
    dependsOn(javalib)

lazy val demoNative =
  project.in(file("demo/native")).
    settings(libSettings).
    settings(
      nativeClangOptions ++= Seq("-O2")
    ).
    dependsOn(scalalib, clib)

lazy val demoJVM =
  project.in(file("demo/jvm")).
    settings(
      fork in run := true,
      javaOptions in run ++= Seq("-Xms64m", "-Xmx64m")
    )

lazy val sandbox =
  project.in(file("sandbox")).
    settings(libSettings).
    settings(
      nativeVerbose := true,
      nativeClangOptions ++= Seq("-O2")
    ).
    dependsOn(scalalib, clib)
