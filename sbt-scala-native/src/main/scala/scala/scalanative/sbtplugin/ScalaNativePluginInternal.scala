package scala.scalanative
package sbtplugin

import java.util.concurrent.atomic._
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
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.scalanative.build.Platform
import sjsonnew.BasicJsonProtocol._
import java.nio.file.{Files, Path}
import java.lang.Runtime
import java.util.concurrent.locks.ReentrantLock
import sbt.librarymanagement.{
  DependencyResolution,
  UpdateConfiguration,
  UnresolvedWarningConfiguration
}

/** ScalaNativePlugin delegates to this object
 *
 *  Note: All logic should be in the Config, NativeConfig, or the build itself.
 *  Logic should not be in this plugin (sbt) to avoid logic duplication in other
 *  downstream build tools like Mill and scala-cli.
 *
 *  Call order on load:
 *    - scalaNativeProjectSettings
 *    - scalaNativeBaseSettings
 *    - scalaNativeCompileSettings
 *    - scalaNativeTestSettings
 *    - scalaNativeGlobalSettings
 *    - scalaNativeConfigSettings -> 6 times for each project, Scala versions
 *      (currently 3), and test true and false for each
 */
object ScalaNativePluginInternal {

  val nativeWarnOldJVM =
    taskKey[Unit]("Warn if JVM 7 or older is used.")

  val baseBuildConfig =
    settingKey[build.Config]("Base configuration for nativeLink commands")

  lazy val scalaNativeDependencySettings: Seq[Setting[_]] = {
    val organization = "org.scala-native"
    val nativeStandardLibraries =
      Seq("nativelib", "clib", "posixlib", "windowslib", "javalib", "auxlib")

    Seq(
      libraryDependencies ++= Seq(
        organization %%% "test-interface" % nativeVersion % Test
      ),
      libraryDependencies += CrossVersion
        .partialVersion(scalaVersion.value)
        .fold(throw new RuntimeException("Unsupported Scala Version")) {
          case (2, _) =>
            organization %%% "scalalib" % scalalibVersion(
              scalaVersion.value,
              nativeVersion
            )
          case (3, _) =>
            organization %%% "scala3lib" % scalalibVersion(
              scalaVersion.value,
              nativeVersion
            )
        },
      libraryDependencies ++= nativeStandardLibraries.map(
        organization %%% _ % nativeVersion
      ),
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
            .withOrganization(organization)
            .withName(
              s"${lib}_native${ScalaNativeCrossVersion.currentBinaryVersion}_${scalaBinVersion}"
            )
        }
      },
      addCompilerPlugin(
        organization % "nscplugin" % nativeVersion cross CrossVersion.full
      )
    )
  }

  lazy val scalaNativeBaseSettings: Seq[Setting[_]] = Seq(
    crossVersion := ScalaNativeCrossVersion.binary,
    platformDepsCrossVersion := ScalaNativeCrossVersion.binary
  )

  /** Called by overridden method in plugin
   *
   *  A nativeConfig object is created to satisfy sbt scope: `Global /
   *  nativeConfig` otherwise we get errors in configSettings because
   *  nativeConfig does not exist.
   *
   *  @see
   *    [[ScalaNativePlugin#globalSettings]]
   */
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
        testAdapters.getAndSet(Nil).foreach(_.close())
      }
    }
  )

  private def nativeLinkImpl(
      baseConfig: build.Config,
      mainClass: Option[String],
      classpath: Classpath,
      nativeLogger: build.Logger,
      dependencyResolution: DependencyResolution,
      externalClassPath: Classpath
  ) = {
    val config = baseConfig
      .withMainClass(mainClass)
      .withLogger(nativeLogger)
      .withClassPath(classpath.map(_.data.toPath))
      .withSourcesClassPath(
        resolveSourcesClassPath(
          baseConfig.compilerConfig.sourceLevelDebuggingConfig.enabled,
          dependencyResolution,
          externalClassPath,
          nativeLogger
        )
      )
    interceptBuildException {
      SharedScope { implicit sharedScope: Scope =>
        Build
          .buildCachedAwait(config)
          .toFile()
      }
    }
  }

  // see: https://github.com/scalameta/metals/blob/0176a491cd209a09852ab33f99fd7de639e8e2dd/metals/src/main/scala/scala/meta/internal/builds/BloopInstall.scala#L81
  private final val isGeneratingForIDE =
    sys.env.getOrElse("METALS_ENABLED", "false").toBoolean

  /** Config settings are called for each project, for each Scala version, and
   *  for test and app configurations. The total with 3 Scala versions equals 6
   *  times per project.
   */
  def scalaNativeConfigSettings(testConfig: Boolean): Seq[Setting[_]] = {
    def releaseFullConfig(config: build.Config) = config
      .withCompilerConfig(_.withMode(Mode.releaseFull))
      .withModuleName(config.moduleName + "-release-full")

    def releaseFastConfig(config: build.Config) = config
      .withCompilerConfig(_.withMode(Mode.releaseFast))
      .withModuleName(config.moduleName + "-release-fast")

    Seq(
      scalacOptions ++= {
        if (isGeneratingForIDE) None
        else
          Some(
            s"-P:scalanative:positionRelativizationPaths:${sourceDirectories.value.map(_.getAbsolutePath()).mkString(";")}"
          )
      },
      // Base build config is populate using settings, it should be enought to calculate artifactPath
      baseBuildConfig := build.Config.empty
        .withBaseDir(crossTarget.value.toPath())
        .withModuleName(moduleName.value)
        .withCompilerConfig(nativeConfig.value)
        .withTestConfig(testConfig),
      // native-link
      nativeLink / artifactPath := baseBuildConfig.value.artifactPath.toFile,
      nativeLink := Def
        .task {
          nativeLinkImpl(
            baseConfig = baseBuildConfig.value,
            mainClass = selectMainClass.value,
            nativeLogger = streams.value.log.toLogger,
            classpath = fullClasspath.value,
            externalClassPath = externalDependencyClasspath.value,
            dependencyResolution = dependencyResolution.value
          )
        }
        .tag(NativeTags.Link)
        .value,
      // release-full aliases
      nativeLinkReleaseFull / artifactPath :=
        releaseFullConfig(baseBuildConfig.value).artifactPath.toFile(),
      nativeLinkReleaseFull := Def
        .task {
          nativeLinkImpl(
            baseConfig = releaseFullConfig(baseBuildConfig.value),
            mainClass = selectMainClass.value,
            nativeLogger = streams.value.log.toLogger,
            classpath = fullClasspath.value,
            externalClassPath = externalDependencyClasspath.value,
            dependencyResolution = dependencyResolution.value
          )
        }
        .tag(NativeTags.Link)
        .value,
      // release-fast aliases
      nativeLinkReleaseFast / artifactPath :=
        releaseFastConfig(baseBuildConfig.value).artifactPath.toFile(),
      nativeLinkReleaseFast := Def
        .task {
          nativeLinkImpl(
            baseConfig = releaseFastConfig(baseBuildConfig.value),
            mainClass = selectMainClass.value,
            nativeLogger = streams.value.log.toLogger,
            classpath = fullClasspath.value,
            externalClassPath = externalDependencyClasspath.value,
            dependencyResolution = dependencyResolution.value
          )
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
  }

  lazy val scalaNativeCompileSettings: Seq[Setting[_]] = {
    scalaNativeConfigSettings(false)
  }

  lazy val scalaNativeTestSettings: Seq[Setting[_]] =
    scalaNativeConfigSettings(true) ++
      Seq(
        mainClass := Some("scala.scalanative.testinterface.TestMain"),
        nativeConfig ~= { _.withBuildTarget(build.BuildTarget.application) },
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

  /** Called by overridden method in plugin
   *
   *  @see
   *    [[ScalaNativePlugin#projectSettings]]
   */
  lazy val scalaNativeProjectSettings: Seq[Setting[_]] =
    scalaNativeDependencySettings ++
      scalaNativeBaseSettings ++
      inConfig(Compile)(scalaNativeCompileSettings) ++
      inConfig(Test)(scalaNativeTestSettings)

  /* Provider of Scope that can be shared between different threads.
   * Closed automatically when all concurrent users release their access  */
  object SharedScope {
    private val modificationLock = new ReentrantLock()
    private var scope = Scope.unsafe()
    private var counter = 0

    def apply[T](use: Scope => T): T = {
      try use(acquire())
      finally release()
    }

    private def acquire(): Scope = {
      modificationLock.lock()
      try {
        counter += 1
        scope
      } finally modificationLock.unlock()
    }
    private def release() = {
      modificationLock.lock()
      try {
        counter -= 1
        if (counter == 0) {
          scope.close()
          scope = Scope.unsafe()
        }
      } finally modificationLock.unlock()
    }
  }
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

  private def resolveSourcesClassPath(
      debugEnabled: Boolean,
      dependencyResolution: DependencyResolution,
      externalClassPath: Classpath,
      log: build.Logger
  ): Seq[Path] = {
    if (!debugEnabled) Nil
    else
      externalClassPath.par
        .flatMap { classpath =>
          try {
            classpath.metadata
              .get(moduleID.key)
              .toSeq
              .map(_.classifier("sources").withConfigurations(None))
              .map(dependencyResolution.wrapDependencyInModule)
              .map(
                dependencyResolution.update(
                  _,
                  UpdateConfiguration(),
                  UnresolvedWarningConfiguration(),
                  util.Logger.Null
                )
              )
              .flatMap(_.right.toOption)
              .flatMap(_.allFiles)
              .filter(_.name.endsWith("-sources.jar"))
              .map(_.toPath())
          } catch {
            case ex: Throwable =>
              log.warn(
                s"Failed to resolved sources of classpath entry '$classpath', source level debuging might work incorrectly"
              )
              log.trace(ex)
              Nil
          }
        }
        .seq
        .sorted
  }

}
