package scala.scalanative
package sbtplugin

import java.util.concurrent.atomic.AtomicReference
import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers._
import scala.annotation.tailrec
import scala.scalanative.util.Scope
import scala.scalanative.build._
import scala.scalanative.linker.LinkingException
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport.{
  ScalaNativeCrossVersion => _,
  _
}
import scala.scalanative.sbtplugin.Utilities._
import scala.scalanative.testinterface.adapter.TestAdapter
import scala.sys.process.Process
import scala.util.Try
import scala.scalanative.build.Platform
import sjsonnew.BasicJsonProtocol._
import java.nio.file.{Files, Path}

object ScalaNativePluginInternal {

  val nativeWarnOldJVM =
    taskKey[Unit]("Warn if JVM 7 or older is used.")

  val nativeWorkdir =
    taskKey[File]("Working directory for intermediate build files.")

  private val nativeStandardLibraries =
    Seq("nativelib", "clib", "posixlib", "windowslib", "javalib", "auxlib")

  lazy val scalaNativeDependencySettings: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      "org.scala-native" %%% "test-interface" % nativeVersion % Test
    ),
    libraryDependencies += CrossVersion
      .partialVersion(scalaVersion.value)
      .fold(throw new RuntimeException("Unsupported Scala Version")) {
        // Add only dependency to scalalib, nativeStanardLibraries would be added transitively
        case (2, _) => "org.scala-native" %%% "scalalib" % nativeVersion
        case (3, _) => "org.scala-native" %%% "scala3lib" % nativeVersion
      },
    excludeDependencies ++= {
      // Exclude cross published version dependencies leading to conflicts in Scala 3 vs 2.13
      // When using Scala 3 exclude Scala 2.13 standard native libraries,
      // when using Scala 2.13 exclude Scala 3 standard native libraries
      // Use full name, Maven style published artifacts cannot use artifact/cross version for exclusion rules
      nativeStandardLibraries.map { lib =>
        val scalaBinVersion =
          if (scalaVersion.value.startsWith("3.")) "2.13"
          else "3"
        ExclusionRule()
          .withOrganization("org.scala-native")
          .withName(
            s"${lib}_native${ScalaNativeCrossVersion.currentBinaryVersion}_${scalaBinVersion}"
          )
      }
    },
    addCompilerPlugin(
      "org.scala-native" % "nscplugin" % nativeVersion cross CrossVersion.full
    )
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

  def scalaNativeConfigSettings(nameSuffix: String): Seq[Setting[_]] = Seq(
    nativeLink / artifactPath := {
      val ext = if (Platform.isWindows) ".exe" else ""
      crossTarget.value / s"${moduleName.value}$nameSuffix-out$ext"
    },
    nativeWorkdir := {
      val workdir = crossTarget.value / s"native$nameSuffix"
      if (!workdir.exists) {
        IO.createDirectory(workdir)
      }
      workdir
    },
    nativeConfig := {
      val config = nativeConfig.value
      config
        // Use overrides defined in legacy setting keys
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
    nativeLink := Def
      .task {
        val classpath = fullClasspath.value.map(_.data.toPath)

        val config = {
          val mainClass = nativeConfig.value.buildTarget match {
            case BuildTarget.Application =>
              selectMainClass.value.orElse {
                throw new MessageOnlyException("No main class detected.")
              }
            case _: BuildTarget.Library => None
          }
          val cwd = nativeWorkdir.value.toPath

          val logger = streams.value.log.toLogger

          val baseConfig =
            build.Config.empty
              .withLogger(logger)
              .withClassPath(classpath)
              .withWorkdir(cwd)
              .withCompilerConfig(nativeConfig.value)

          mainClass.foldLeft(baseConfig)(_.withMainClass(_))
        }

        val outpath = {
          val originalOutPath = (nativeLink / artifactPath).value.toPath()
          val directory = Option(originalOutPath.getParent())
            .getOrElse(originalOutPath.getRoot())
          val filename = originalOutPath.getFileName().toString()
          val baseFilename = filename.lastIndexOf(".") match {
            case -1  => filename
            case idx => filename.substring(0, idx)
          }

          def compilerConfig = config.compilerConfig
          val ext = compilerConfig.buildTarget match {
            case BuildTarget.Application =>
              if (config.targetsWindows) ".exe" else ""
            case BuildTarget.LibraryDynamic =>
              if (config.targetsWindows) ".dll"
              else if (config.targetsMac) ".dylib"
              else ".so"
            case BuildTarget.LibraryStatic =>
              if (config.targetsWindows) ".lib"
              else ".a"
          }
          val namePrefix = compilerConfig.buildTarget match {
            case BuildTarget.Application => ""
            case _: BuildTarget.Library =>
              if (config.targetsWindows) "" else "lib"
          }
          directory.resolve(s"$namePrefix${baseFilename}$ext").toFile()
        }

        def buildNew(): Unit = interceptBuildException {
          Build.build(config, outpath.toPath)(sharedScope)
        }

        def buildIfChanged(): Unit = {
          import sbt.util.CacheImplicits._
          import NativeLinkCacheImplicits._
          import collection.JavaConverters._

          // Products of compilation for Scala 2 are always defined in `target/scala-<scalaBinaryVersion` directory,
          // but in case of Scala 3 there is always a dedicated directory for each (minor) Scala version.
          // This allows us to cache binaries for each Scala version instead of each binary Scala version.
          val scalaVersionDir =
            if (scalaVersion.value.startsWith("2.")) scalaBinaryVersion.value
            else scalaVersion.value
          val cacheFactory =
            streams.value.cacheStoreFactory / "fileInfo" / s"scala-${scalaVersionDir}"
          val classpathTracker =
            Tracked.inputChanged[
              (Seq[HashFileInfo], build.Config),
              HashFileInfo
            ](
              cacheFactory.make("inputFileInfo")
            ) {
              case (
                    changed: Boolean,
                    (filesInfo: Seq[HashFileInfo], config: build.Config)
                  ) =>
                val outputTracker =
                  Tracked
                    .lastOutput[Seq[HashFileInfo], HashFileInfo](
                      cacheFactory.make("outputFileInfo")
                    ) { (_, prev) =>
                      val outputHashInfo = FileInfo.hash(outpath)
                      if (changed || !prev.contains(outputHashInfo)) {
                        buildNew()
                        FileInfo.hash(outpath)
                      } else outputHashInfo
                    }
                outputTracker(filesInfo)
            }

          val classpathFilesInfo = classpath
            .flatMap { classpath =>
              if (Files.exists(classpath))
                Files
                  .walk(classpath)
                  .iterator()
                  .asScala
                  .filter(path =>
                    Files.exists(path) && !Files.isDirectory(path)
                  )
                  .toList
              else Nil
            }
            .map(path => FileInfo.hash(path.toFile()))

          classpathTracker(classpathFilesInfo, config)
        }

        buildIfChanged()
        outpath
      }
      .tag(NativeTags.Link)
      .value,
    console := console
      .dependsOn(Def.task {
        streams.value.log.warn(
          "Scala REPL doesn't work with Scala Native. You " +
            "are running a JVM REPL. Native things won't work."
        )
      })
      .value,
    run := {
      val env = (run / envVars).value.toSeq
      val logger = streams.value.log
      val binary = nativeLink.value.getAbsolutePath
      val args = spaceDelimited("<arg>").parsed

      logger.running(binary +: args)

      val exitCode = {
        // It seems that previously used Scala Process has some bug leading
        // to possible ignoring of inherited IO and termination of wrapper
        // thread with an exception. We use java.lang ProcessBuilder instead
        val proc = new ProcessBuilder()
          .command((Seq(binary) ++ args): _*)
          .inheritIO()
        env.foreach((proc.environment().put(_, _)).tupled)
        proc.start().waitFor()
      }

      val message =
        if (exitCode == 0) None
        else Some("Nonzero exit code: " + exitCode)

      message.foreach(sys.error)
    },
    runMain := {
      throw new MessageOnlyException(
        "`runMain` is not supported in Scala Native"
      )
    }
  )

  lazy val scalaNativeCompileSettings: Seq[Setting[_]] =
    scalaNativeConfigSettings(nameSuffix = "")

  lazy val scalaNativeTestSettings: Seq[Setting[_]] =
    scalaNativeConfigSettings(nameSuffix = "-test") ++
      Seq(
        mainClass := Some("scala.scalanative.testinterface.TestMain"),
        loadedTestFrameworks := {
          val configName = configuration.value.name

          if (fork.value) {
            throw new MessageOnlyException(
              s"`$configName / test` tasks in a Scala Native project require $configName / fork := false`."
            )
          }

          val frameworks = testFrameworks.value
          val frameworkNames = frameworks.map(_.implClassNames.toList).toList

          val logger = streams.value.log.toLogger
          val testBinary = nativeLink.value
          val envVars = (test / Keys.envVars).value

          val config = TestAdapter
            .Config()
            .withBinaryFile(testBinary)
            .withEnvVars(envVars)
            .withLogger(logger)

          val adapter = newTestAdapter(config)
          val frameworkAdapters = adapter.loadFrameworks(frameworkNames)

          frameworks
            .zip(frameworkAdapters)
            .collect {
              case (tf, Some(adapter)) => (tf, adapter)
            }
            .toMap
        },
        // Override default to avoid triggering a Test/nativeLink in a Test/compile
        // without losing autocompletion.
        definedTestNames := {
          definedTests
            .map(_.map(_.name).distinct)
            .storeAs(definedTestNames)
            .triggeredBy(loadedTestFrameworks)
            .value
        }
      )

  lazy val scalaNativeProjectSettings: Seq[Setting[_]] =
    scalaNativeDependencySettings ++
      scalaNativeBaseSettings ++
      inConfig(Compile)(scalaNativeCompileSettings) ++
      inConfig(Test)(scalaNativeTestSettings)

  private var sharedScope = Scope.unsafe()
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
  final private def registerResource[T <: AnyRef](
      l: AtomicReference[List[T]],
      r: T
  ): r.type = {
    val prev = l.get()
    if (l.compareAndSet(prev, r :: prev)) r
    else registerResource(l, r)
  }

}
