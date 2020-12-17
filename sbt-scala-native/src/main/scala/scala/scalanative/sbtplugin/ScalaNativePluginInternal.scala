package scala.scalanative
package sbtplugin

import java.util.concurrent.atomic.AtomicReference
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers._
import scala.annotation.tailrec
import scala.scalanative.util.Scope
import scala.scalanative.build.{Build, BuildException, Discover}
import scala.scalanative.linker.LinkingException
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
    nativeClang := nativeConfig.value.clang.toFile,
    nativeClangPP := nativeConfig.value.clangPP.toFile,
    nativeCompileOptions := nativeConfig.value.compileOptions,
    nativeLinkingOptions := nativeConfig.value.linkingOptions,
    nativeMode := nativeConfig.value.mode.name,
    nativeGC := nativeConfig.value.gc.name,
    nativeLTO := nativeConfig.value.lto.name,
    nativeLinkStubs := nativeConfig.value.linkStubs,
    nativeCheck := nativeConfig.value.check,
    nativeDump := nativeConfig.value.dump
  )

  lazy val scalaNativeGlobalSettings: Seq[Setting[_]] = Seq(
    nativeConfig := build.NativeConfig.empty
      .withClang(interceptBuildException(Discover.clang()))
      .withClangPP(interceptBuildException(Discover.clangpp()))
      .withCompileOptions(Discover.compileOptions())
      .withLinkingOptions(Discover.linkingOptions())
      .withLTO(Discover.LTO())
      .withGC(Discover.GC())
      .withMode(Discover.mode())
      .withOptimize(Discover.optimize()),
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
        sharedScope.close()
        sharedScope = Scope.unsafe()
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
      nativeConfig.value
        .withClang(nativeClang.value.toPath)
        .withClangPP(nativeClangPP.value.toPath)
        .withCompileOptions(nativeCompileOptions.value)
        .withLinkingOptions(nativeLinkingOptions.value)
        .withGC(build.GC(nativeGC.value))
        .withMode(build.Mode(nativeMode.value))
        .withLTO(build.LTO(nativeLTO.value))
        .withLinkStubs(nativeLinkStubs.value)
        .withCheck(nativeCheck.value)
        .withDump(nativeDump.value)
    },
    nativeLink := {
      val outpath = (artifactPath in nativeLink).value
      val config = {
        val mainClass = selectMainClass.value.getOrElse {
          throw new MessageOnlyException("No main class detected.")
        }
        val classpath = fullClasspath.value.map(_.data.toPath)
        val maincls   = mainClass + "$"
        val cwd       = nativeWorkdir.value.toPath

        val logger = streams.value.log.toLogger
        build.Config.empty
          .withLogger(logger)
          .withMainClass(maincls)
          .withClassPath(classpath)
          .withWorkdir(cwd)
          .withTargetTriple(nativeTarget.value)
          .withCompilerConfig(nativeConfig.value)
      }

      interceptBuildException {
        Build.build(config, outpath.toPath)(sharedScope)
      }

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

  private var sharedScope  = Scope.unsafe()
  private val testAdapters = new AtomicReference[List[TestAdapter]](Nil)

  private def newTestAdapter(config: TestAdapter.Config): TestAdapter = {
    registerResource(testAdapters, new TestAdapter(config))
  }

  /** Run `op`, rethrows `BuildException`s as `MessageOnlyException`s. */
  private def interceptBuildException[T](op: => T): T = {
    try op
    catch {
      case ex: BuildException   => throw new MessageOnlyException(ex.getMessage)
      case ex: LinkingException => throw new MessageOnlyException(ex.getMessage)
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
