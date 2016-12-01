package scala.scalanative
package sbtplugin

import scala.sys.process.ProcessLogger

import util._
import sbtcross.CrossPlugin.autoImport._
import ScalaNativePlugin.autoImport._

import scalanative.nir
import scalanative.tools
import scalanative.io.VirtualDirectory
import scalanative.util.{Scope => ResourceScope}
import scalanative.llvm.LLVM

import sbt._, Keys._, complete.DefaultParsers._
import xsbti.{Maybe, Reporter, Position, Severity, Problem}
import KeyRanks.DTask

import scala.util.Try

import System.{lineSeparator => nl}

object ScalaNativePluginInternal {

  val nativeLinkerReporter = settingKey[tools.LinkerReporter](
    "A reporter that gets notified whenever a linking event happens.")

  val nativeOptimizerReporter = settingKey[tools.OptimizerReporter](
    "A reporter that gets notified whenever an optimizer event happens.")

  val nativeExternalDependencies =
    taskKey[Seq[String]]("List all external dependencies.")

  private lazy val nativelib: File =
    Path.userHome / ".scalanative" / ("nativelib-" + nir.Versions.current)

  private def abs(file: File): String =
    file.getAbsolutePath

  private def running(command: Seq[String]): String =
    "running" + nl + command.mkString(nl + "\t")

  private def reportLinkingErrors(unresolved: Seq[nir.Global],
                                  logger: Logger): Nothing = {
    import nir.Shows._

    unresolved.map(u => sh"$u".toString).sorted.foreach { signature =>
      logger.error(s"cannot link: $signature")
    }

    throw new MessageOnlyException("unable to link")
  }

  /** Compiles application nir to llvm ir. */
  private def compileNir(
      config: tools.Config,
      logger: Logger,
      linkerReporter: tools.LinkerReporter,
      optimizerReporter: tools.OptimizerReporter): Seq[nir.Attr.Link] = {
    val driver                   = tools.OptimizerDriver(config)
    val (unresolved, links, raw) = tools.link(config, driver, linkerReporter)

    if (unresolved.nonEmpty) { reportLinkingErrors(unresolved, logger) }

    val optimized = tools.optimize(config, driver, raw, optimizerReporter)
    tools.codegen(config, optimized)

    links
  }

  /** Compiles rt to llvm ir using clang. */
  private def unpackNativelib(clang: File,
                              clangpp: File,
                              classpath: Seq[File],
                              logger: Logger): Boolean = {
    val nativelibjar = classpath
      .map(abs)
      .collectFirst {
        case p if p.contains("scala-native") && p.contains("nativelib") =>
          file(p)
      }
      .get

    val jarhash     = Hash(nativelibjar).toSeq
    val jarhashfile = nativelib / "jarhash"
    def bootstrapped =
      nativelib.exists &&
        jarhashfile.exists &&
        jarhash == IO.readBytes(jarhashfile).toSeq

    if (!bootstrapped) {
      IO.delete(nativelib)
      IO.unzip(nativelibjar, nativelib)
      IO.write(jarhashfile, Hash(nativelibjar))

      LLVM.compileCSources(clang, clangpp, nativelib, processLogger(logger))
    } else {
      true
    }
  }

  private def externalDependenciesTask[T](compileTask: TaskKey[T]) =
    nativeExternalDependencies := ResourceScope { implicit scope =>
      import nir.Shows._

      val forceCompile = compileTask.value

      val classes = classDirectory.value
      val progDir = VirtualDirectory.real(classes)
      val prog    = linker.Path(progDir)

      val config =
        tools.Config.empty.withPaths(Seq(prog)).withTargetDirectory(progDir)

      val (unresolved, _, _) = (linker.Linker(config)).link(prog.globals.toSeq)

      unresolved.map(u => sh"$u".toString).sorted
    }

  lazy val projectSettings =
    unscopedSettings ++
      inConfig(Compile)(externalDependenciesTask(compile)) ++
      inConfig(Test)(externalDependenciesTask(compile in Test))

  lazy val unscopedSettings = Seq(
    libraryDependencies ++= Seq(
      "org.scala-native" %%% "nativelib" % nativeVersion,
      "org.scala-native" %%% "javalib"   % nativeVersion,
      "org.scala-native" %%% "scalalib"  % nativeVersion
    ),
    addCompilerPlugin(
      "org.scala-native" % "nscplugin" % nativeVersion cross CrossVersion.full),
    nativeLibraryLinkage := Map(),
    nativeSharedLibrary := false,
    nativeClang := {
      LLVM.discover("clang", Seq(("3", "8"), ("3", "7")))
    },
    nativeClangPP := {
      LLVM.discover("clang++", Seq(("3", "8"), ("3", "7")))
    },
    nativeClangOptions := {
      // We need to add `-lrt` for the POSIX realtime lib, which doesn't exist
      // on macOS.
      val lrt = Option(sys props "os.name") match {
        case Some("Linux") => Seq("-lrt")
        case _             => Seq()
      }
      LLVM.includes ++ LLVM.libs ++ maybeInjectShared(
        nativeSharedLibrary.value) ++ lrt
    },
    artifactPath in nativeLink := {
      (crossTarget in Compile).value / (moduleName.value + "-out")
    },
    nativeLinkerReporter := tools.LinkerReporter.empty,
    nativeOptimizerReporter := tools.OptimizerReporter.empty,
    nativeLink := ResourceScope { implicit scope =>
      val clangpp   = nativeClangPP.value
      val clangOpts = nativeClangOptions.value
      val clang     = nativeClang.value
      LLVM.checkThatClangIsRecentEnough(clang)

      val mainClass = (selectMainClass in Compile).value.getOrElse(
        throw new MessageOnlyException("No main class detected.")
      )
      val entry     = nir.Global.Top(mainClass.toString + "$")
      val classpath = (fullClasspath in Compile).value.map(_.data)
      val target    = (crossTarget in Compile).value
      val appll     = target / "out.ll"
      val binary    = (artifactPath in nativeLink).value

      val linkage           = nativeLibraryLinkage.value
      val linkerReporter    = nativeLinkerReporter.value
      val optimizerReporter = nativeOptimizerReporter.value
      val sharedLibrary     = nativeSharedLibrary.value
      val logger            = streams.value.log

      val config = tools.Config.empty
        .withEntry(entry)
        .withPaths(classpath.map(p =>
          tools.LinkerPath(VirtualDirectory.real(p))))
        .withTargetDirectory(VirtualDirectory.real(target))
        .withInjectMain(!nativeSharedLibrary.value)

      val nirFiles   = (Keys.target.value ** "*.nir").get.toSet
      val configFile = (streams.value.cacheDirectory / "native-config")
      val inputFiles = nirFiles + configFile

      writeConfigHash(configFile,
                      config,
                      clang,
                      clangpp,
                      classpath,
                      target,
                      appll,
                      binary,
                      linkage,
                      clangOpts)

      val compileIfChanged =
        FileFunction.cached(streams.value.cacheDirectory / "native-cache",
                            FilesInfo.hash) {
          _ =>
            IO.createDirectory(target)

            val unpackSuccess =
              unpackNativelib(clang, clangpp, classpath, logger)

            if (unpackSuccess) {
              val links =
                compileNir(config, logger, linkerReporter, optimizerReporter)
              LLVM.compileLl(clangpp,
                             target,
                             nativelib,
                             appll,
                             binary,
                             links.map(_.name),
                             linkage,
                             clangOpts,
                             processLogger(logger))
              Set(binary)
            } else {
              throw new MessageOnlyException("Couldn't unpack nativelib.")
            }
        }

      val result = compileIfChanged(inputFiles)
      binary
    },
    run := {
      val logger = streams.value.log
      val binary = abs(nativeLink.value)
      val args   = spaceDelimited("<arg>").parsed

      logger.info(running(binary +: args))
      val exitCode = Process(binary +: args).!

      val message =
        if (exitCode == 0) None
        else Some("Nonzero exit code: " + exitCode)

      Defaults.toError(message)
    }
  )

  private def writeConfigHash(file: File, config: Any*): Unit = {
    val force = config.## // Force evaluation of lazy structures
    IO.write(file, Hash(config.toString))
  }

  val scalaNativeEcosystemSettings = Seq(
    crossVersion := ScalaNativeCrossVersion.binary,
    crossPlatform := NativePlatform
  )

  private def maybeInjectShared(lib: Boolean): Seq[String] =
    if (lib) Seq("-shared") else Seq.empty

  private def processLogger(logger: Logger): ProcessLogger =
    ProcessLogger(l => logger.info(l), l => logger.error(l))
}
