package scala.scalanative
package sbtplugin

import java.nio.file.Files
import java.util.concurrent.atomic.AtomicReference
import java.nio.file.Files
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

  lazy val scalaNativeDependencySettings: Seq[Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      "org.scala-native" %%% "nativelib" % nativeVersion,
      "org.scala-native" %%% "javalib" % nativeVersion,
      "org.scala-native" %%% "auxlib" % nativeVersion,
      "org.scala-native" %%% "scalalib" % nativeVersion,
      "org.scala-native" %%% "test-interface" % nativeVersion % Test
    ),
    addCompilerPlugin(
      "org.scala-native" % "nscplugin" % nativeVersion cross CrossVersion.full)
  )

  lazy val scalaNativeBaseSettings: Seq[Setting[_]] = Seq(
    nativeConfig := {
      build.Config.empty
        .withClang(interceptBuildException(Discover.clang()))
        .withClangPP(interceptBuildException(Discover.clangpp()))
    },
    crossVersion := ScalaNativeCrossVersion.binary,
    platformDepsCrossVersion := ScalaNativeCrossVersion.binary,
    nativeClang := nativeConfig.value.clang.toFile,
    nativeClangPP := nativeConfig.value.clangPP.toFile,
    nativeCompileOptions := nativeConfig.value.compileOptions,
    nativeLinkingOptions := nativeConfig.value.linkingOptions,
    nativeMode := nativeConfig.value.mode.name,
    nativeGC := nativeConfig.value.gc.name,
    nativeLTO := nativeConfig.value.LTO.name,
    nativeLinkStubs := nativeConfig.value.linkStubs,
    nativeCheck := nativeConfig.value.check,
    nativeDump := nativeConfig.value.dump
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
      () =>
        {
          prev()
          testAdapters.getAndSet(Nil).foreach(_.close())
        }
    }
  )

  def scalaNativeConfigSettings(key: TaskKey[File]): Seq[Setting[_]] = {
    Seq(
      nativeTarget in key := interceptBuildException {
        val cwd = (nativeWorkdir in key).value.toPath
        val clang = (nativeClang in key).value.toPath
        Discover.targetTriple(clang, cwd)
      },
      artifactPath in nativeLink in key := {
        (crossTarget in key).value / (moduleName.value + "-out")
      },
      nativeWorkdir in key := {
        val workdir = (crossTarget in key).value / "native"
        if (!workdir.exists) {
          IO.createDirectory(workdir)
        }
        workdir
      },
      nativeConfig in key := {
        build.Config.empty
          .withClang((nativeClang in key).value.toPath)
          .withClangPP((nativeClangPP in key).value.toPath)
          .withCompileOptions((nativeCompileOptions in key).value)
          .withLinkingOptions((nativeLinkingOptions in key).value)
          .withGC(build.GC((nativeGC in key).value))
          .withMode(build.Mode((nativeMode in key).value))
          .withLTO(build.LTO((nativeLTO in key).value))
          .withLinkStubs((nativeLinkStubs in key).value)
          .withCheck((nativeCheck in key).value)
          .withDump((nativeDump in key).value)
      },
      nativeLink := {
        val mainClass = (selectMainClass in key).value.getOrElse {
          throw new MessageOnlyException("No main class detected.")
        }
        val fullCp = (fullClasspath in key).value
        val classpath = fullCp.map(_.data.toPath).filter(f => Files.exists(f))
        val nativelib = {
          /* Find the entry of the classpath that is the nativelib.
		   * We use the `moduleID.key` attribute of the entries to find the one
		   * whose organization is `org.scala-native` and whose name is
		   * `nativelib`. The name might include the cross-version suffix,
		   * which is why we also accept names that start with
		   * `nativelib_native0.`.
		   */
          fullCp
            .find { entry =>
              entry.get((moduleID in key).key).exists { module =>
                module.organization == "org.scala-native" &&
                (module.name == "nativelib" || module.name.startsWith(
                  "nativelib_native0."))
              }
            }
            .getOrElse {
              throw new MessageOnlyException(
                "Could not find nativelib on classpath.")
            }
            .data
            .toPath
        }
        val maincls = mainClass + "$"
        val cwd = (nativeWorkdir in key).value.toPath

        val logger = streams.value.log.toLogger
        val config = (nativeConfig in key).value
          .withLogger(logger)
          .withMainClass(maincls)
          .withNativelib(nativelib)
          .withClassPath(classpath)
          .withWorkdir(cwd)
          .withTargetTriple((nativeTarget in key).value)
        val outpath = (artifactPath in nativeLink in key).value

        interceptBuildException(Build.build(config, outpath.toPath))

        outpath
      },
      run := {
        val env = (envVars in run).value.toSeq
        val logger = streams.value.log
        val binary = nativeLink.value.getAbsolutePath
        val args = spaceDelimited("<arg>").parsed

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
  }

  lazy val scalaNativeCompileSettings: Seq[Setting[_]] =
    scalaNativeConfigSettings(nativeLink)

  lazy val scalaNativeTestSettings: Seq[Setting[_]] =
    scalaNativeConfigSettings(nativeLink) ++
      Seq(
        mainClass := Some("scala.scalanative.testinterface.TestMain"),
        loadedTestFrameworks := {
          val configName = configuration.value.name

          if (fork.value) {
            throw new MessageOnlyException(
              s"`$configName / test` tasks in a Scala Native project require $configName / fork := false`.")
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
