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
import java.nio.file.{Files, Path}

/** ScalaNativePlugin delegates to this object
 *
 *  Note: All logic should be in the Config, NativeConfig, or the build itself.
 *  Logic should not be in this plugin to avoid logic duplication in downstream
 *  build tools.
 *
 *  Call order on load:
 *    - scalaNativeProjectSettings
 *    - scalaNativeBaseSettings
 *    - scalaNativeCompileSettings
 *    - scalaNativeTestSettings
 *    - scalaNativeGlobalSettings
 *    - scalaNativeConfigSettings -> 6 times for each project, Scala versions,
 *      test true and false,
 */
object ScalaNativePluginInternal {

  val nativeWarnOldJVM =
    taskKey[Unit]("Warn if JVM 7 or older is used.")

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

  /** Sets the 10 default raw setting values otherwise if they are not set we
   *  cannot access them. Use an empty config to get defaults so if they change
   *  later the settings will be preserved.
   */
  lazy val scalaNativeBaseSettings: Seq[Setting[_]] = {
    Seq(
      crossVersion := ScalaNativeCrossVersion.binary,
      platformDepsCrossVersion := ScalaNativeCrossVersion.binary
    )
  }

  /** Called in overridden method in plugin
   *
   *  A nativeConfig object is created to satisfy sbt scope: `Global /
   *  nativeConfig` otherwise we get errors in configSettings because
   *  nativeConfig does not exist. Discovery is delayed until after build is
   *  called.
   */
  lazy val scalaNativeGlobalSettings: Seq[Setting[_]] = {
    Seq(
      nativeConfig := build.NativeConfig.empty,
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
  }

  /** Config settings are called for each project, for each version, and for
   *  test and app configurations. The total 3 version or 6 times per project.
   */
  def scalaNativeConfigSettings(testConfig: Boolean): Seq[Setting[_]] = Seq(
    nativeConfig := {
      // do we need a new one for each config?
      val config = nativeConfig.value
      config
    },
    nativeLink := Def
      .task {
        println("Running nativeLink")
        val classpath = fullClasspath.value.map(_.data.toPath)
        val logger = streams.value.log.toLogger

        val config =
          build.Config.empty
            .withLogger(logger)
            .withClassPath(classpath)
            .withBasedir(crossTarget.value.toPath())
            .withDefaultBasename(moduleName.value)
            .withMainClass(selectMainClass.value)
            .withTestConfig(testConfig)
            .withCompilerConfig(nativeConfig.value)

        interceptBuildException {
          // returns config.artifactPath
          Build.build(config)(sharedScope).toFile()
        }
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
      println("Running run")
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

  lazy val scalaNativeCompileSettings: Seq[Setting[_]] = {
    scalaNativeConfigSettings(false)
  }

  lazy val scalaNativeTestSettings: Seq[Setting[_]] = {
    scalaNativeConfigSettings(true) ++
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
        }
      )
  }

  // called in overridden method in plugin
  lazy val scalaNativeProjectSettings: Seq[Setting[_]] = {
    scalaNativeDependencySettings ++
      scalaNativeBaseSettings ++
      inConfig(Compile)(scalaNativeCompileSettings) ++
      inConfig(Test)(scalaNativeTestSettings)
  }

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
