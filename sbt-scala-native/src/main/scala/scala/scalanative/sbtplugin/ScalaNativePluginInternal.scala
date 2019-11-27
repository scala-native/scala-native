package scala.scalanative
package sbtplugin

import java.lang.System.{lineSeparator => nl}
import java.io.ByteArrayInputStream
import java.nio.file.{Files, Path}

import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers._

import scala.scalanative.build.{Build, BuildException, Discover}
import scala.scalanative.sbtplugin.ScalaNativePlugin.autoImport._
import scala.scalanative.sbtplugin.Utilities._
import scala.scalanative.testinterface.ScalaNativeFramework
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
    nativeMode := Option(System.getenv.get("SCALANATIVE_MODE"))
      .getOrElse(build.Mode.default.name),
    nativeLinkStubs := false,
    nativeGC := Option(System.getenv.get("SCALANATIVE_GC"))
      .getOrElse(build.GC.default.name),
    nativeLTO := Discover.LTO(),
    nativeCheck := false,
    nativeDump := false,
    nativeLibraryDependencies := Seq(),
    nativeCodeInclude := false
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

      val nativeCodeProject = {
        if (nativeCodeInclude.value) {
          val projName   = name.value
          val orgName    = organization.value
          val classesDir = crossTarget.value / "classes"
          Some(NativeLib(LibId(orgName, projName), classesDir.toPath()))
        } else {
          None
        }
      }

      def createLibIds(nativeDeps: Seq[ModuleID]): Seq[LibId] = {
        nativeDeps.map(dep => Discover.LibId(dep.organization, dep.name))
      }

      def findNativeLibs(libIds: Seq[LibId],
                         classpath: Seq[Path]): Seq[NativeLib] =
        libIds.map { libId =>
          Discover.nativelib(classpath, libId).getOrElse {
            throw new MessageOnlyException(
              s"""Could not find "${libId.org}" %%% "${libId.artifact}" ... on the classpath.""")
          }
        }

      val classpath =
        fullClasspath.value.map(_.data.toPath).filter(f => Files.exists(f))
      val libIds = createLibIds(nativeLibraryDependencies.value)
      // nativelib needs to be first for compiling gc and optional
      val nativelibs = findNativeLibs(Discover.nativelibId +: libIds, classpath)

      val maincls = mainClass.toString + "$"
      val cwd     = nativeWorkdir.value.toPath
      val clang   = nativeClang.value.toPath
      val clangpp = nativeClangPP.value.toPath
      val gc      = build.GC(nativeGC.value)
      val mode    = build.Mode(nativeMode.value)

      build.Config.empty
        .withNativelibs(nativelibs)
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
          val frameworks = loadedTestFrameworks.value
          val logger     = streams.value.log
          val testBinary = nativeLink.value
          val envVars    = (test / Keys.envVars).value
          frameworks.zipWithIndex.map {
            case ((tf, f), id) =>
              (tf,
               new ScalaNativeFramework(f,
                                        id,
                                        logger.toLogger,
                                        testBinary,
                                        envVars))
          }
        }
      )

  lazy val scalaNativeProjectSettings: Seq[Setting[_]] =
    scalaNativeDependencySettings ++
      scalaNativeBaseSettings ++
      inConfig(Compile)(scalaNativeCompileSettings) ++
      inConfig(Test)(scalaNativeTestSettings)

  /** Run `op`, rethrows `BuildException`s as `MessageOnlyException`s. */
  private def interceptBuildException[T](op: => T): T = {
    try op
    catch {
      case ex: BuildException => throw new MessageOnlyException(ex.getMessage)
    }
  }
}
