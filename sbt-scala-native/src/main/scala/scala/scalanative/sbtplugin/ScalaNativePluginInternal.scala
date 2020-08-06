package scala.scalanative
package sbtplugin

import java.nio.file.Files
import java.util.concurrent.atomic.AtomicReference
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers._
import scala.annotation.tailrec
import scala.scalanative.build.{Build, BuildException, Discover}
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._
import scala.scalanative.sbtplugin.Utilities._
import scala.scalanative.testinterface.adapter.TestAdapter
import scala.sys.process.Process
import scala.util.Try

object ScalaNativePluginInternal {

  val nativeWarnOldJVM =
    taskKey[Unit]("Warn if JVM 7 or older is used.")

  val nativeTarget =
    taskKey[String]("Target triple.")

  val nativeWorkdir =
    taskKey[File]("Working directory for intermediate build files.")

  val nativeConfig =
    taskKey[build.Config]("Aggregate config object that's used for tools.")

  lazy val scalaNativeDependencySettings: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      "org.scala-native" %%% "nativelib"      % nativeVersion,
      "org.scala-native" %%% "javalib"        % nativeVersion,
      "org.scala-native" %%% "auxlib"         % nativeVersion,
      "org.scala-native" %%% "scalalib"       % nativeVersion,
      "org.scala-native" %%% "test-interface" % nativeVersion % Test
    ),
    addCompilerPlugin(
      "org.scala-native" % "nscplugin" % nativeVersion cross CrossVersion.full)
  )

  lazy val scalaNativeBaseSettings: Seq[Setting[_]] = Seq(
    crossVersion := ScalaNativeCrossVersion.binary,
    platformDepsCrossVersion := ScalaNativeCrossVersion.binary,
    nativeClang := interceptBuildException(Discover.clang().toFile),
    nativeClangPP := interceptBuildException(Discover.clangpp().toFile),
    nativeCompileOptions := Discover.compileOptions(),
    nativeLinkingOptions := Discover.linkingOptions(),
    nativeMode := Discover.mode(),
    nativeLinkStubs := false,
    nativeGC := Discover.GC(),
    nativeLTO := Discover.LTO(),
    nativeCheck := false,
    nativeDump := false
  )

  lazy val scalaNativeGlobalSettings: Seq[Setting[_]] = Seq(
    nativeWarnOldJVM := {
      val logger = streams.value.log
      Try(Class.forName("java.util.function.Function")).toOption match {
        case None =>
          logger.warn("Scala Native is only supported on Java 8 or newer.")
        case Some(_) =>
          ()
      }
    },
    onComplete := {
      val prev: () => Unit = onComplete.value
      () => {
        prev()
        testAdapters.getAndSet(Nil).foreach(_.close())
      }
    }
  )

  lazy val scalaNativeConfigSettings: Seq[Setting[_]] = Seq(
    nativeTarget := interceptBuildException {
      val cwd   = nativeWorkdir.value.toPath
      val clang = nativeClang.value.toPath
      Discover.targetTriple(clang, cwd)
    },
    artifactPath in nativeLink := {
      crossTarget.value / (moduleName.value + "-out")
    },
    nativeWorkdir := {
      val workdir = crossTarget.value / "native"
      if (!workdir.exists) {
        IO.createDirectory(workdir)
      }
      workdir
    },
    nativeConfig := {
      val mainClass = selectMainClass.value.getOrElse {
        throw new MessageOnlyException("No main class detected.")
      }

      val classpath =
        fullClasspath.value.map(_.data.toPath).filter(f => Files.exists(f))
      val maincls = mainClass.toString + "$"
      val cwd     = nativeWorkdir.value.toPath
      val clang   = nativeClang.value.toPath
      val clangpp = nativeClangPP.value.toPath
      val gc      = build.GC(nativeGC.value)
      val mode    = build.Mode(nativeMode.value)

      build.Config.empty
        .withMainClass(maincls)
        .withClassPath(classpath)
        .withWorkdir(cwd)
        .withClang(clang)
        .withClangPP(clangpp)
        .withTargetTriple(nativeTarget.value)
        .withCompileOptions(nativeCompileOptions.value)
        .withLinkingOptions(nativeLinkingOptions.value)
        .withGC(gc)
        .withMode(mode)
        .withLinkStubs(nativeLinkStubs.value)
        .withLTO(nativeLTO.value)
        .withCheck(nativeCheck.value)
        .withDump(nativeDump.value)
        .withOptimize(Discover.optimize())
    },
    nativeLink := {
      val logger  = streams.value.log.toLogger
      val config  = nativeConfig.value.withLogger(logger)
      val outpath = (artifactPath in nativeLink).value

      interceptBuildException(Build.build(config, outpath.toPath))

      outpath
    },
    run := {
      val env    = (envVars in run).value.toSeq
      val logger = streams.value.log
      val binary = nativeLink.value.getAbsolutePath
      val args   = spaceDelimited("<arg>").parsed

      logger.running(binary +: args)
      val exitCode = Process(binary +: args, None, env: _*)
        .run(connectInput = false)
        .exitValue

      val message =
        if (exitCode == 0) None
        else Some("Nonzero exit code: " + exitCode)

      message.foreach(sys.error)
    }
  )

  lazy val scalaNativeCompileSettings: Seq[Setting[_]] =
    scalaNativeConfigSettings

  lazy val scalaNativeTestSettings: Seq[Setting[_]] =
    scalaNativeConfigSettings ++
      Seq(
        mainClass := Some("scala.scalanative.testinterface.TestMain"),
        loadedTestFrameworks := {
          val configName = configuration.value.name

          if (fork.value) {
            throw new MessageOnlyException(
              s"`$configName / test` tasks in a Scala Native project require $configName / fork := false`.")
          }

          val frameworks     = testFrameworks.value
          val frameworkNames = frameworks.map(_.implClassNames.toList).toList

          val logger     = streams.value.log.toLogger
          val testBinary = nativeLink.value
          val envVars    = (test / Keys.envVars).value

          val config = TestAdapter
            .Config()
            .withBinaryFile(testBinary)
            .withEnvVars(envVars)
            .withLogger(logger)

          val adapter           = newTestAdapter(config)
          val frameworkAdapters = adapter.loadFrameworks(frameworkNames)

          frameworks
            .zip(frameworkAdapters)
            .collect {
              case (tf, Some(adapter)) => (tf, adapter)
            }
            .toMap
        }
      )

  lazy val scalaNativeProjectSettings: Seq[Setting[_]] =
    scalaNativeDependencySettings ++
      scalaNativeBaseSettings ++
      inConfig(Compile)(scalaNativeCompileSettings) ++
      inConfig(Test)(scalaNativeTestSettings)

  private val testAdapters = new AtomicReference[List[TestAdapter]](Nil)

  private def newTestAdapter(config: TestAdapter.Config): TestAdapter = {
    registerResource(testAdapters, new TestAdapter(config))
  }

  /** Run `op`, rethrows `BuildException`s as `MessageOnlyException`s. */
  private def interceptBuildException[T](op: => T): T = {
    try op
    catch {
      case ex: BuildException => throw new MessageOnlyException(ex.getMessage)
    }
  }

  @tailrec
  final private def registerResource[T <: AnyRef](l: AtomicReference[List[T]],
                                                  r: T): r.type = {
    val prev = l.get()
    if (l.compareAndSet(prev, r :: prev)) r
    else registerResource(l, r)
  }

}
