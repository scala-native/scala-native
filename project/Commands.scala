package build

import sbt._
import Keys._
import Build._
import com.typesafe.tools.mima.plugin.MimaPlugin.autoImport._
import ScriptedPlugin.autoImport._

object Commands {
  lazy val values = Seq(
    testAll,
    testSandboxGC,
    testTools,
    testRuntime,
    testMima,
    testScripted,
    publishLocalDev,
    publishRelease
  )

  lazy val testAll = Command.command("test-all") {
    "test-tools" ::
      "test-mima" ::
      "test-runtime" ::
      "test-scripted" :: _ // test-scripted will publish artifacts locally
  }

  // Compile and run the sandbox for each GC as a minimal check
  lazy val testSandboxGC = projectVersionCommand("test-sandbox-gc") {
    case (version, state) =>
      val runs =
        List(sandbox)
          .map(_.forBinaryVersion(version).id)
          .flatMap(id =>
            List("none", "boehm", "immix", "commix").map(gc =>
              s"set ThisBuild / nativeConfig ~= (_.withGC(scala.scalanative.build.GC.$gc)); $id/run"
            )
          )
      runs :::
        state
  }

  lazy val testRuntime = projectVersionCommand("test-runtime") {
    case (version, state) =>
      val runs =
        List(sandbox)
          .map(_.forBinaryVersion(version).id)
          .map(id => s"$id/run")

      val tests = List(
        testsJVM,
        Build.tests,
        testsExtJVM,
        testsExt,
        junitTestOutputsJVM,
        junitTestOutputsNative,
        scalaPartestJunitTests
      ).map(_.forBinaryVersion(version).id)
        .map(id => s"$id/test")
      runs :::
        tests :::
        state
  }

  lazy val testTools = projectVersionCommand("test-tools") {
    case (version, state) =>
      val tests = List(
        nscPlugin, // compiler plugin
        // Toolchain JVM
        nirJVM,
        toolsJVM,
        // Testing infrastrucutre
        testRunner,
        testInterface,
        // Toolchain Native
        nir,
        tools
      )
        .map(_.forBinaryVersion(version).id)
        .map(id => s"$id/test")
      tests :::
        List("test-mima") :::
        state
  }

  lazy val testMima = projectVersionCommand("test-mima") {
    case (version, state) =>
      val tests = Build.publishedMultiScalaProjects
        .map(_.forBinaryVersion(version).id)
        .map(id => s"$id/mimaReportBinaryIssues")
        .toList

      tests ::: state
  }

  lazy val testScripted = Command.args("test-scripted", "<args>") {
    case (state, args) =>
      val version = args.headOption
        .flatMap(MultiScalaProject.scalaVersions.get)
        .orElse(state.getSetting(scalaVersion))
        .getOrElse(
          sys.error(
            "Used command needs explicit Scala version as an argument"
          )
        )
      val setScriptedLaunchOpts =
        s"""set sbtScalaNative/scriptedLaunchOpts := {
            |  (sbtScalaNative/scriptedLaunchOpts).value
            |   .filterNot(_.startsWith("-Dscala.version=")) :+
            |   "-Dscala.version=$version" :+
            |   "-Dscala213.version=${ScalaVersions.scala213}"
            |}""".stripMargin
      // Scala 3 is supported since sbt 1.5.0. 1.5.8 is used.
      // Older versions set incorrect binary version
      val isScala3 = version.startsWith("3.")
      val scalaVersionTests =
        if (isScala3) "scala3/*"
        else ""

      setScriptedLaunchOpts ::
        s"sbtScalaNative/scripted ${scalaVersionTests} run/*" ::
        state
  }

  private def projectVersionCommand(
      name: String
  )(fn: (String, State) => State): Command = {
    Command.args(name, "<args>") {
      case (state, args) =>
        val version = args.headOption
          .map(CrossVersion.binaryScalaVersion)
          .orElse(state.getSetting(scalaBinaryVersion))
          .getOrElse(
            "Used command needs explicit Scala binary version as an argument"
          )

        fn(version, state)
    }
  }

  private def projectFullVersionCommand(
      name: String
  )(fn: (String, State) => State): Command = {
    Command.args(name, "<args>") {
      case (state, args) =>
        val version = args.headOption
          .flatMap(MultiScalaProject.scalaVersions.get)
          .orElse(state.getSetting(scalaVersion))
          .getOrElse(
            "Used command needs explicit full Scala version as an argument"
          )

        fn(version, state)
    }
  }

  lazy val publishLocalDev = {
    projectFullVersionCommand("publish-local-dev") {
      case (version, state) =>
        List(
          // Sbt plugin and it's dependencies
          s"++${ScalaVersions.scala212} publishLocal",
          // Artifact for current version
          s"++${version} publishLocal"
        ) ::: state
    }
  }

  lazy val publishRelease = Command.command("publishRelease") { state =>
    val isSnapshot = state
      .getSetting(Keys.isSnapshot)
      .getOrElse(sys.error("Cannot resolve isSnapshot setting"))

    import ScalaVersions._
    val publishEachVersion = for {
      version <- List(scala212, scala213, scala3)
    } yield s"++$version; publishSigned; crossPublishSigned"

    "clean" :: publishEachVersion ::: state
  }

}
