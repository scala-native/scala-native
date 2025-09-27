package build

import sbt._
import Keys._
import Build._
import com.typesafe.tools.mima.plugin.MimaPlugin.autoImport._
import ScriptedPlugin.autoImport._

object Commands {
  lazy val values = Seq(
    testAll,
    testGC,
    testTools,
    testRuntime,
    testMima,
    testScripted,
    publishLocalDev,
    publishReleaseForVersion
  )

  lazy val testAll = Command.command("test-all") {
    "test-tools" ::
      "test-mima" ::
      "test-runtime" ::
      "test-scripted" :: _ // test-scripted will publish artifacts locally
  }

  // Compile and run the sandbox for each GC as a minimal check
  lazy val testGC = projectVersionCommand("test-gc") {
    case (version, state) =>
      import scala.scalanative.build.GC
      val runs =
        for {
          gc <- List(GC.none, GC.immix, GC.commix, GC.boehm)
          (project, command) <- Map(
            sandbox -> "run",
            testInterface -> "test",
            junitTestOutputsNative -> "test"
          )
        } yield {
          val projectId = project.forBinaryVersion(version).id
          val selectGC = s"""scala.scalanative.build.GC("${gc.name}")"""
          s"""set ${project.name}.forBinaryVersion("${version}")/nativeConfig ~= (_.withGC($selectGC)); $projectId/$command"""
        }
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
        List(s"test-mima $version") :::
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
        val arg = args.headOption
        val version = arg
          // Try translating 2.12, 2.13, 3, 3-next to full version string such as 3.3.1
          .flatMap(MultiScalaProject.scalaVersions.get)
          // Verify the argument full version string is supported by libCrossScalaVersions
          .orElse(
            arg.flatMap(a => ScalaVersions.libCrossScalaVersions.find(_ == a))
          )
          // Fallback to the current scalaVersion
          .orElse {
            val v = state.getSetting(scalaVersion)
            state.log.warn(
              s"${args.headOption.getOrElse("")} is not supported, fallback to ${v}"
            )
            v
          }
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
          // Sbt plugin and its dependencies
          s"++${ScalaVersions.scala212} publishLocal",
          // Artifact for current version
          s"++${version} publishLocal"
        ) ::: state
    }
  }

  lazy val publishReleaseForVersion =
    projectVersionCommand("publish-release-for-version") {
      case (binVersion, state) =>
        val (scalaVersion, crossScalaVersions) = binVersion match {
          case "2.12" => ScalaVersions.scala212 -> ScalaVersions.crossScala212
          case "2.13" =>
            ScalaVersions.scala213PublishVersion -> ScalaVersions.crossScala213
          case "3" =>
            ScalaVersions.scala3PublishVersion -> ScalaVersions.crossScala3
          case _ => sys.error(s"Invalid Scala binary version: '$binVersion'")
        }
        val publishCommand = "publishSigned"
        val publishBaseVersion = s"++$scalaVersion; $publishCommand"
        val publishCrossVersions = crossScalaVersions
          .diff(scalaVersion :: Nil) // exclude already published base version
          .toList
          .map { crossVersion =>
            Build.crossPublishedMultiScalaProjects
              .map(_.forBinaryVersion(binVersion))
              .map(project => s"${project.id}/$publishCommand")
              .mkString(s"++ $crossVersion; all ", " ", "")
          }
        val isSnapshotVersion =
          state.getSetting(ThisBuild / version).exists(_.endsWith("-SNAPSHOT"))
        val release = if (isSnapshotVersion) Nil else ("sonaUpload" :: Nil)
        val crossPublish = ScalaVersions
        val commandsToExecute =
          "clean" ::
            publishBaseVersion ::
            publishCrossVersions :::
            release

        println(
          s"Publish for Scala $binVersion would execute following commands:"
        )
        commandsToExecute.foreach(println)
        println("")

        commandsToExecute ::: state
    }
}
