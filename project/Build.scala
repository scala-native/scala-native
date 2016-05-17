import sbt._
import Keys._
import scala.io.Source
import scala.scalanative.sbtplugin._
import ScalaNativePlugin.autoImport._
import org.scalafmt.sbt.ScalaFmtPlugin.autoImport._

object Build {
  val assembleScalaLibrary =
    taskKey[Unit](
      "Clone scala standard library from the scala repo and then applies overrides.")

  val toolScalaVersion = "2.10.6"

  val libScalaVersion = "2.11.8"

  val baseSettings = Seq(
    organization := "org.scala-native",
    version := nativeVersion,
    scalafmtConfig := Some(file(".scalafmt"))
  )

  val toolSettings =
    baseSettings ++
      Seq(
        scalaVersion := toolScalaVersion
      )

  val libSettings =
    baseSettings ++
      ScalaNativePlugin.projectSettings ++
      Seq(
        scalaVersion := libScalaVersion,
        nativeEmitDependencyGraphPath := Some(file("out.dot"))
      )

  lazy val util: Project =
    Project(
      id = "util",
      base = file("util"),
      settings = toolSettings
    )

  lazy val nir: Project =
    Project(
      id = "nir",
      base = file("nir"),
      settings = toolSettings
    ).dependsOn(util)

  lazy val tools: Project =
    Project(
      id = "tools",
      base = file("tools"),
      settings =
        toolSettings ++
          Seq(
            libraryDependencies += "commons-io" % "commons-io" % "2.4"
          )
    ).dependsOn(nir, util)

  lazy val nscplugin: Project =
    Project(
      id = "nscplugin",
      base = file("nscplugin"),
      settings =
        toolSettings ++
          Seq(
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
    )

  lazy val sbtplugin: Project =
    Project(
      id = "sbtplugin",
      base = file("sbtplugin"),
      settings =
        toolSettings ++
          Seq(
            sbtPlugin := true
          )
    ).dependsOn(tools)

  lazy val rtlib: Project =
    Project(
      id = "rtlib",
      base = file("rtlib"),
      settings =
        baseSettings ++
          Seq(
            scalaVersion := libScalaVersion
          )
    )

  lazy val nativelib: Project =
    Project(
      id = "nativelib",
      base = file("nativelib"),
      settings = libSettings
    )

  lazy val javalib: Project =
    Project(
      id = "javalib",
      base = file("javalib"),
      settings = libSettings
    ).dependsOn(nativelib)


  lazy val scalalib: Project =
    Project(
      id = "scalalib",
      base = file("scalalib"),
      settings =
        libSettings ++
          Seq(
            assembleScalaLibrary := {
              import org.eclipse.jgit.api._

              val s = streams.value
              val ver = scalaVersion.value
              val trgDir = file("submodules/scala")

              if (!trgDir.exists) {
                s.log.info(s"Fetching Scala source version $ver")

                IO.createDirectory(trgDir)

                new CloneCommand()
                  .setDirectory(trgDir)
                  .setURI("https://github.com/scala/scala.git")
                  .call()
              }

              val git = Git.open(trgDir)
              s.log.info(s"Checking out Scala source version $ver")
              git.checkout().setName(s"v$ver").call()

              IO.delete(file("scalalib/src/main/scala"))

              IO.copyDirectory(
                trgDir / "src" / "library" / "scala",
                file("scalalib/src/main/scala/scala"))

              val epoch :: major :: _ = scalaVersion.value.split("\\.").toList
              IO.copyDirectory(
                file(s"scalalib/overrides-$epoch.$major/scala"),
                file("scalalib/src/main/scala/scala"),
                overwrite = true)
            },
            compile in Compile <<= (compile in Compile) dependsOn assembleScalaLibrary
          )
    ).dependsOn(javalib)

  lazy val demoNative: Project =
    Project(
      id = "demo-native",
      base = file("demo-native"),
      settings =
        libSettings ++
          Seq(
            nativeVerbose := true,
            nativeClangOptions := Seq("-O2")
          )
    ).dependsOn(scalalib)

  lazy val demoJVM: Project =
    Project(
      id = "demo-jvm",
      base = file("demo-jvm"),
      settings =
        Seq(
          fork in run := true,
          javaOptions in run ++= Seq("-Xms64m", "-Xmx64m")
        )
    )
}
