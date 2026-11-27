package build

import sbt._

import com.typesafe.tools.mima.plugin.MimaPlugin.autoImport._

import Build._
import Keys._
import ScriptedPlugin.autoImport._

object Commands {
  lazy val values = Seq(
    testAll,
    testGC,
    testTools,
    compileAll,
    compileSbtPlugin,
    testCrossCompile,
    testRuntime,
    testRuntimeNative,
    testMima,
    testScripted,
    publishScripted,
    runScripted,
    disableActionCache,
    publishLocalDev,
    publishReleaseForVersion,
    compilePublishVersion
  )

  // Recommended for working with compiler plugins, the actions cache might make testing local changes harder.
  lazy val disableActionCache = Command.command("disable-action-cache") {
    "set Global / cacheStores := Seq.empty[sbt.util.ActionCacheStore]" :: _
  }

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
      val isWindows =
        System.getProperty("os.name").toLowerCase.contains("windows")
      val targetsAppleEmbedded = sys.env
        .get("SCALANATIVE_TARGET_TRIPLE")
        .exists(t => Seq("-ios", "-tvos", "-watchos").exists(t.contains))
      val runs =
        for {
          gc <- List(GC.none, GC.immix, GC.commix, GC.boehm)
          // Exclude running Boehm GC on Windows for Scala 3 as deadlocks for unknown reasons
          if !(isWindows && gc == GC.boehm && version == "3")
          // No Boehm GC library for iOS, tvOS and watchOS SDKs
          if !(gc == GC.boehm && targetsAppleEmbedded)
          (project, command) <- Map(
            sandbox -> "run",
            testInterface -> "test",
            junitTestOutputsNative -> "test"
          )
        } yield {
          val projectId = project.forBinaryVersion(version).id
          // GC smoke checks do not need debug metadata or source-JAR downloads.
          // The full runtime suite still exercises source-level debugging.
          val configure =
            s"""_.withGC(scala.scalanative.build.GC("${gc.name}"))""" +
              ".withSourceLevelDebuggingConfig(_.disableAll)"
          s"""set ${project.name}.forBinaryVersion("${version}")/nativeConfig ~= ($configure); $projectId/$command"""
        }
      runs :::
        state
  }

  // Compile main and test sources of all projects except sbt plugins, see compile-sbt-plugin
  lazy val compileAll = projectVersionCommand("compile-all") {
    case (version, state) =>
      val projects = Build.allMultiScalaProjects
        .filterNot(Build.sbtPlugins.contains)
        .flatMap(_.forBinaryVersionIfDefined(version))
      val benchmarks = // Scala 3 only
        if (version == "3") Build.benchmarks.filterProjects(Nil) else Nil
      allTasks("Test/compile", projects ++ benchmarks) :: state
  }

  // Compile the sbt plugin with the Scala version of its sbt: 2.12 - sbt 1.x, 3 - sbt 2.x
  lazy val compileSbtPlugin = projectVersionCommand("compile-sbt-plugin") {
    case (version, state) =>
      s"++${sbtScalaVersion(version)}" ::
        tasks(version, "compile")(Build.sbtPlugins: _*) ::: state
  }

  // Compile compiler plugins and the Scala library for every supported Scala version
  lazy val testCrossCompile = projectVersionCommand("test-cross-compile") {
    case (version, state) =>
      val stdlib = (if (version == "3") scala3lib else scalalib)
        .forBinaryVersion(version)
        .id
      MultiScalaProject
        .scalaCrossVersions(version)
        .toList
        .flatMap(v => List(s"++$v", s"$stdlib/compile")) ::: state
  }

  private def runtimeJVMTests(version: String): List[String] =
    tasks(version, "test")(testsJVM, testsExtJVM, junitTestOutputsJVM)

  private def runtimeNativeTests(version: String): List[String] =
    tasks(version, "run")(sandbox) ::: tasks(version, "test")(
      Build.tests,
      testsExt,
      junitTestOutputsNative,
      scalaPartestJunitTests
    )

  lazy val testRuntime = projectVersionCommand("test-runtime") {
    case (version, state) =>
      runtimeJVMTests(version) ::: runtimeNativeTests(version) ::: state
  }

  // Native part of the runtime tests, for other configurations of the runtime
  lazy val testRuntimeNative =
    projectVersionCommand("test-runtime-native") {
      case (version, state) => runtimeNativeTests(version) ::: state
    }

  lazy val testTools = projectVersionCommand("test-tools") {
    case (version, state) =>
      tasks(version, "test")(
        nscPlugin,
        nirJVM,
        toolsJVM,
        testRunner,
        testInterface,
        nir,
        tools
      ) ::: state
  }

  lazy val testMima = projectVersionCommand("test-mima") {
    case (version, state) =>
      // sbt plugins depend on the other projects, use the Scala version of sbt for all of them
      val prepareForMiMa =
        ScalaVersions.sbtScalaVersions.get(version).map(v => s"++$v").toList
      val tests = Build.publishedMultiScalaProjects
        .flatMap(_.forBinaryVersionIfDefined(version).map(_.id))
        .flatMap(id =>
          Seq(s"$id/mimaReportBinaryIssues", s"$id/nirExternReportIssues")
        )
        .toList

      prepareForMiMa ::: tests ::: state
  }

  // Scala version of the sbt plugin under test, Scala 3 tests the sbt 2.x plugin
  private def scriptedScalaVersion(args: Seq[String], state: State): String =
    args.headOption
      .flatMap(MultiScalaProject.scalaVersions.get)
      .orElse(state.getSetting(scalaVersion))
      .map { version =>
        (
          CrossVersion.partialVersion(version),
          CrossVersion.partialVersion(ScalaVersions.sbt2ScalaVersion)
        ) match {
          case (Some((3, minor)), Some((3, sbtScalaVersionMinor)))
              if minor > sbtScalaVersionMinor =>
            System.err.println(
              s"Downgrading Scala ${version} version to ${ScalaVersions.sbt2ScalaVersion} for sbt scripted tests: cannot use forward incompatible artifacts"
            )
            ScalaVersions.sbt2ScalaVersion
          case _ => version
        }
      }
      .getOrElse(
        sys.error("Used command needs explicit Scala version as an argument")
      )

  private def scriptedSbtProject(version: String): Project = {
    val binVersion = CrossVersion.binaryScalaVersion(version)
    sbtScalaNative
      .forBinaryVersionIfDefined(binVersion)
      .getOrElse(
        throw new MessageOnlyException(
          s"Unsupported Scala binary version for sbt scripted tests: $binVersion (for Scala ${version})"
        )
      )
  }

  /** Runtime artifacts for the sbt Scala version, the scripted task publishes
   *  the sbt plugin itself. Tests in `build/` also use Scala 2.13 and an older
   *  Scala 3, see `scriptedLaunchOpts`.
   */
  private def scriptedPublishCommands(
      version: String,
      tests: Seq[String]
  ): List[String] = {
    def publish(scalaVersion: String, projects: List[MultiScalaProject]) = {
      val binVersion = CrossVersion.binaryScalaVersion(scalaVersion)
      s"++$scalaVersion; " +
        allTasks(
          "publishLocal",
          projects.flatMap(_.forBinaryVersionIfDefined(binVersion))
        )
    }
    val binVersion = CrossVersion.binaryScalaVersion(version)
    val runtime = compilerPlugins ++ runtimeMultiScalaProjects
    if (tests.isEmpty || tests.exists(_.startsWith("build/")))
      List(
        publish(ScalaVersions.scala213, runtime),
        publish(ScalaVersions.scriptedTestsScala3Version, runtime),
        // Overrides the Scala 3 libraries that differ from the older Scala 3 build
        publish(
          sbtScalaVersion(binVersion),
          if (binVersion == "3") crossPublishedMultiScalaProjects else runtime
        )
      )
    else List(publish(sbtScalaVersion(binVersion), runtime))
  }

  lazy val publishScripted =
    Command.args("publish-scripted", "<scala version> [tests]") {
      case (state, args) =>
        val version = scriptedScalaVersion(args, state)
        logged(scriptedPublishCommands(version, args.drop(1)), state)
    }

  // Runs scripted tests using artifacts published with publish-scripted
  lazy val runScripted =
    Command.args("run-scripted", "<scala version> [tests]") {
      case (state, args) =>
        val version = scriptedScalaVersion(args, state)
        val sbtProject = scriptedSbtProject(version)
        val testsFilter = args.drop(1).mkString(" ").trim
        val commands = List(
          s"++$version",
          s"show ${sbtProject.id}/sbtVersion",
          s"show ${sbtProject.id}/scalaVersion",
          s"${sbtProject.id}/scripted $testsFilter".trim
        )
        logged(commands, state)
    }

  lazy val testScripted =
    Command.args("test-scripted", "<scala version> [tests]") {
      case (state, args) =>
        List("publish-scripted", "run-scripted")
          .map(command => (command +: args).mkString(" ")) ::: state
    }

  private def tasks(version: String, task: String)(
      projects: MultiScalaProject*
  ): List[String] =
    projects.toList.map(p => s"${p.forBinaryVersion(version).id}/$task")

  private def allTasks(task: String, projects: Seq[Project]): String =
    projects.map(p => s"${p.id}/$task").mkString("all ", " ", "")

  private def sbtScalaVersion(binVersion: String): String =
    ScalaVersions.sbtScalaVersions.getOrElse(
      binVersion,
      throw new MessageOnlyException(
        s"No sbt plugin for Scala binary version $binVersion"
      )
    )

  private def logged(commands: List[String], state: State): State = {
    println("Will execute following commands:")
    commands.foreach(println)
    commands ::: state
  }

  private def projectVersionCommand(
      name: String
  )(fn: (String, State) => State): Command =
    projectVersionArgsCommand(name, "<args>") {
      case (version, _, state) => fn(version, state)
    }

  // Scala binary version followed by command specific arguments
  private def projectVersionArgsCommand(name: String, display: String)(
      fn: (String, Seq[String], State) => State
  ): Command = {
    Command.args(name, display) {
      case (state, args) =>
        val version = args.headOption
          .map(CrossVersion.binaryScalaVersion)
          .orElse(state.getSetting(scalaBinaryVersion))
          .getOrElse(
            "Used command needs explicit Scala binary version as an argument"
          )

        fn(version, args.drop(1), state)
    }
  }

  // Publish Scala version and cross-published versions
  private def publishScalaVersions(binVersion: String): (String, Seq[String]) =
    binVersion match {
      case "2.12" => ScalaVersions.scala212 -> ScalaVersions.crossScala212
      case "2.13" => ScalaVersions.scala213 -> ScalaVersions.crossScala213
      case "3"    =>
        ScalaVersions.scala3PublishVersion -> ScalaVersions.crossScala3
      case _ => sys.error(s"Invalid Scala binary version: '$binVersion'")
    }

  // Compile all sources with the publish Scala version
  lazy val compilePublishVersion =
    projectVersionCommand("compile-publish-version") {
      case (version, state) =>
        s"++${publishScalaVersions(version)._1}" :: "Test/compile" :: state
    }

  private def projectFullVersionCommand(
      name: String
  )(fn: (String, State) => State): Command = {
    Command.args(name, "<args>") {
      case (state, args) =>
        val arg = args.headOption
        val version = arg
          // Try translating 2.12, 2.13, 3 to full version string such as 3.3.1
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
          ScalaVersions.scala212,
          // Artifact for current version
          version
        ).distinct
          .map(v => s"++$v publishLocal") ::: state
    }
  }

  // Optional part: `base` or `cross` for a range of versions, e.g. `cross 3.1-3.3`, `cross 3.9+`
  lazy val publishReleaseForVersion = projectVersionArgsCommand(
    "publish-release-for-version",
    "<binary version> [base | cross <from>-<to> | cross <from>+]"
  ) {
    case (binVersion, part, state) =>
      val (scalaVersion, crossScalaVersions) = publishScalaVersions(binVersion)
      // exclude already published base version
      val allCrossVersions = crossScalaVersions.diff(scalaVersion :: Nil).toList
      def binary(version: String): (Long, Long) =
        CrossVersion
          .partialVersion(version)
          .getOrElse(sys.error(s"Invalid Scala version: $version"))
      def crossVersionsIn(from: String, to: Option[String]) =
        allCrossVersions.filter { v =>
          import scala.math.Ordering.Implicits._
          binary(v) >= binary(from) && to.forall(binary(v) <= binary(_))
        }
      val Range = """(\d+\.\d+)-(\d+\.\d+)""".r
      val From = """(\d+\.\d+)\+""".r
      val (publishBase, crossVersions) = part match {
        case Nil                           => (true, allCrossVersions)
        case Seq("base")                   => (true, Nil)
        case Seq("cross", Range(from, to)) =>
          (false, crossVersionsIn(from, Some(to)))
        case Seq("cross", From(from)) => (false, crossVersionsIn(from, None))
        case other                    =>
          sys.error(s"Unknown part '${other.mkString(" ")}'")
      }
      val publishCommand = "publishSigned"
      val publishBaseVersion =
        if (publishBase) List(s"++$scalaVersion; $publishCommand") else Nil
      val publishCrossVersions = crossVersions.map { crossVersion =>
        s"++$crossVersion; " + allTasks(
          publishCommand,
          crossPublishedMultiScalaProjects.map(_.forBinaryVersion(binVersion))
        )
      }
      val isSnapshotVersion =
        state.getSetting(ThisBuild / version).exists(_.endsWith("-SNAPSHOT"))
      val release = if (isSnapshotVersion) Nil else ("sonaUpload" :: Nil)
      val commandsToExecute =
        "clean" ::
          publishBaseVersion :::
          publishCrossVersions :::
          release

      logged(commandsToExecute, state)
  }
}
