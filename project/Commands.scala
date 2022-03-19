package build

import sbt._
import Keys._
import Build._
import com.typesafe.tools.mima.plugin.MimaPlugin.autoImport._
import ScriptedPlugin.autoImport._

object Commands {
  lazy val values = Seq(testAll, testTools, testRuntime, testMima, testScripted)

  lazy val testAll = Command.command("test-all") {
    "test-tools" ::
      "test-mima" ::
      "test-runtime" ::
      "test-scripted" :: _
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
      val tests = List(tools, testRunner, testInterface)
        .map(_.forBinaryVersion(version).id)
        .map(id => s"$id/test")
      tests :::
        List("test-mima") :::
        state
  }

  lazy val testMima = projectVersionCommand("test-mima") {
    case (version, state) =>
      val tests = List(
        Build.util,
        nir,
        tools,
        testRunner,
        testInterface,
        testInterfaceSbtDefs,
        junitRuntime,
        nativelib,
        clib,
        posixlib,
        windowslib,
        auxlib,
        javalib,
        scalalib
      ).map(_.forBinaryVersion(version).id)
        .map(id => s"$id/mimaReportBinaryIssues")

      tests ::: state
  }

  lazy val testScripted = Command.args("test-scripted", "<args>") {
    case (state, args) =>
      val version = args.headOption
        .orElse(state.getSetting(scalaVersion))
        .getOrElse(
          "Used command needs explicit Scala version as an argument"
        )
      val setScriptedLaunchOpts =
        s"""set sbtScalaNative/scriptedLaunchOpts := {
            |  (sbtScalaNative/scriptedLaunchOpts).value
            |   .filterNot(_.startsWith("-Dscala.version=")) :+
            |   "-Dscala.version=$version"
            |}""".stripMargin
      // Scala 3 is supported since sbt 1.5.0
      // Older versions set incorrect binary version
      val isScala3 = version.startsWith("3.")
      val overrideSbtVersion =
        if (isScala3)
          """set sbtScalaNative/sbtVersion := "1.5.0" """ :: Nil
        else Nil
      val scalaVersionTests =
        if (isScala3) "scala3/*"
        else ""

      setScriptedLaunchOpts ::
        overrideSbtVersion :::
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

}
