package scala.scalanative
package sbtplugin

import util._
import sbtcross.CrossPlugin.autoImport._
import ScalaNativePlugin.autoImport._

import scalanative.nir
import scalanative.tools
import scalanative.io.VirtualDirectory
import scalanative.util.{Scope => ResourceScope}

import sbt._, Keys._, complete.DefaultParsers._
import xsbti.{Maybe, Reporter, Position, Severity, Problem}
import KeyRanks.DTask

import scala.util.Try

import System.{lineSeparator => nl}

object ScalaNativePluginInternal {

  val nativeUnpackNativelib =
    taskKey[Unit]("Unpack and precompile native lib.")

  val nativeTarget = taskKey[String]("Target triple.")

  val nativeLinkerReporter = settingKey[tools.LinkerReporter](
    "A reporter that gets notified whenever a linking event happens.")

  val nativeOptimizerReporter = settingKey[tools.OptimizerReporter](
    "A reporter that gets notified whenever an optimizer event happens.")

  val nativeExternalDependencies =
    taskKey[Seq[String]]("List all external dependencies at link time.")

  val nativeAvailableDependencies =
    taskKey[Seq[String]]("List all symbols available at link time")

  val nativeMissingDependencies =
    taskKey[Seq[String]]("List all symbols not available at link time")

  lazy val nativelib: File =
    Path.userHome / ".scalanative" / ("nativelib-" + nir.Versions.current)

  private lazy val includes = {
    val includedir =
      Try(Process("llvm-config --includedir").lines_!.toSeq)
        .getOrElse(Seq.empty)
    ("/usr/local/include" +: includedir).map(s => s"-I$s")
  }

  private lazy val libs = {
    val libdir =
      Try(Process("llvm-config --libdir").lines_!.toSeq).getOrElse(Seq.empty)
    ("/usr/local/lib" +: libdir).map(s => s"-L$s")
  }

  private def abs(file: File): String =
    file.getAbsolutePath

  private def discover(binaryName: String,
                       binaryVersions: Seq[(String, String)]): File = {

    val docInstallUrl =
      "http://scala-native.readthedocs.io/en/latest/user/setup.html#installing-llvm-clang-and-boehm-gc"

    val envName =
      if (binaryName == "clang") "CLANG"
      else if (binaryName == "clang++") "CLANGPP"
      else binaryName

    sys.env.get(s"${envName}_PATH") match {
      case Some(path) => file(path)
      case None => {
        val binaryNames = binaryVersions.flatMap {
          case (major, minor) =>
            Seq(s"$binaryName$major$minor", s"$binaryName-$major.$minor")
        } :+ binaryName

        Process("which" +: binaryNames).lines_!
          .map(file(_))
          .headOption
          .getOrElse {
            throw new MessageOnlyException(
              s"no ${binaryNames.mkString(", ")} found in $$PATH. Install clang ($docInstallUrl)")
          }
      }
    }
  }

  private def reportLinkingErrors(unresolved: Seq[nir.Global],
                                  logger: Logger): Nothing = {
    unresolved.map(_.show).sorted.foreach { signature =>
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

  private def running(command: Seq[String]): String =
    "running" + nl + command.mkString(nl + "\t")

  /** Compiles *.c[pp] in `cwd`. */
  def compileCSources(clang: File,
                      clangpp: File,
                      cwd: File,
                      logger: Logger): Boolean = {
    val cpaths     = (cwd ** "*.c").get.map(abs)
    val cpppaths   = (cwd ** "*.cpp").get.map(abs)
    val compilec   = abs(clang) +: (includes ++ ("-c" +: cpaths))
    val compilecpp = abs(clangpp) +: (includes ++ ("-c" +: cpppaths))

    logger.info(running(compilec))
    val cExit = Process(compilec, cwd) ! logger

    logger.info(running(compilecpp))
    val cppExit = Process(compilecpp, cwd) ! logger

    cExit == 0 && cppExit == 0
  }

  /** Detect target platform. */
  private def compileTargetProbe(clang: File,
                                 cwd: File,
                                 logger: Logger): Boolean = {
    val targetcfile  = cwd / "target.c"
    val targetllfile = cwd / "target.ll"
    val targetfile   = cwd / "target"
    val compilec     = Seq(abs(clang), "-S", "-emit-llvm", abs(targetcfile))
    IO.write(targetcfile, "int probe;")
    logger.info(running(compilec))
    val exit = Process(compilec, cwd) ! logger
    if (exit != 0) {
      return false
    }

    val targetvalue = IO
      .readLines(targetllfile)
      .collectFirst {
        case line if line.startsWith("target triple") =>
          line.split("\"").apply(1)
      }
      .getOrElse {
        return false
      }
    IO.write(targetfile, targetvalue)
    true
  }

  /** Compiles application and runtime llvm ir file to binary using clang. */
  private def compileLl(clangpp: File,
                        target: File,
                        appll: File,
                        binary: File,
                        compileTarget: String,
                        applinks: Seq[String],
                        linkage: Map[String, String],
                        opts: Seq[String],
                        logger: Logger): Unit = {
    val outpath = abs(binary)
    val apppath = abs(appll)
    val opaths  = (nativelib ** "*.o").get.map(abs)
    val paths   = apppath +: opaths
    val links = {
      val os   = Option(sys props "os.name").getOrElse("")
      val arch = compileTarget.split("-").head
      val librt = os match {
        case "Linux" => Seq("rt")
        case _       => Seq.empty
      }
      val libunwind = os match {
        case "Mac OS X" => Seq.empty
        case _          => Seq("unwind", "unwind-" + arch)
      }
      librt ++ libunwind ++ applinks
    }
    val linkopts = links.zip(links.map(linkage.get(_))).flatMap {
      case (name, Some("static")) =>
        Seq("-static", "-l" + name)
      case (name, Some("dynamic") | None) =>
        Seq("-l" + name)
      case (name, Some(kind)) =>
        throw new MessageOnlyException(s"uknown linkage kind $kind for $name")
    }
    val targetopt = Seq("-target", compileTarget)
    val flags     = Seq("-o", outpath) ++ linkopts ++ targetopt ++ opts
    val compile   = abs(clangpp) +: (flags ++ paths)

    logger.info(running(compile))

    Process(compile, target) ! logger
  }

  private def externalDependenciesTask[T](compileTask: TaskKey[T]) =
    nativeExternalDependencies := ResourceScope { implicit scope =>
      val forceCompile = compileTask.value

      val classes = classDirectory.value
      val progDir = VirtualDirectory.real(classes)
      val prog    = linker.Path(progDir)

      val config =
        tools.Config.empty.withPaths(Seq(prog)).withTargetDirectory(progDir)

      val (unresolved, _, _) = (linker.Linker(config)).link(prog.globals.toSeq)

      unresolved.map(_.show).sorted
    }

  private def availableDependenciesTask[T](compileTask: TaskKey[T]) =
    nativeAvailableDependencies := ResourceScope { implicit scope =>
      val forceCompile = compileTask.value

      val globals = fullClasspath.value.flatMap(p =>
        tools.LinkerPath(VirtualDirectory.real(p.data)).globals.toSeq)

      globals.map(_.show).sorted
    }

  def nativeMissingDependenciesTask =
    nativeMissingDependencies := {
      (nativeExternalDependencies.value.toSet --
        nativeAvailableDependencies.value.toSet).toList.sorted
    }

  lazy val projectSettings =
    unscopedSettings ++
      inConfig(Compile)(externalDependenciesTask(compile)) ++
      inConfig(Test)(externalDependenciesTask(compile in Test)) ++
      inConfig(Compile)(availableDependenciesTask(compile)) ++
      inConfig(Test)(availableDependenciesTask(compile in Test)) ++
      inConfig(Compile)(nativeMissingDependenciesTask) ++
      inConfig(Test)(nativeMissingDependenciesTask)

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
      discover("clang", Seq(("3", "8"), ("3", "7")))
    },
    nativeClangPP := {
      discover("clang++", Seq(("3", "8"), ("3", "7")))
    },
    nativeClangOptions := {
      includes ++ libs ++ maybeInjectShared(nativeSharedLibrary.value)
    },
    nativeTarget := {
      val doUnpack = nativeUnpackNativelib.value
      IO.read(nativelib / "target")
    },
    artifactPath in nativeLink := {
      (crossTarget in Compile).value / (moduleName.value + "-out")
    },
    nativeLinkerReporter := tools.LinkerReporter.empty,
    nativeOptimizerReporter := tools.OptimizerReporter.empty,
    nativeUnpackNativelib := {
      val clang   = nativeClang.value
      val clangpp = nativeClangPP.value
      val nativelibjar = (fullClasspath in Compile).value
        .map(entry => abs(entry.data))
        .collectFirst {
          case p if p.contains("scala-native") && p.contains("nativelib") =>
            file(p)
        }
        .get
      val logger = streams.value.log

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

        val compiledC      = compileCSources(clang, clangpp, nativelib, logger)
        val detectedTarget = compileTargetProbe(clang, nativelib, logger)

        if (!compiledC || !detectedTarget) {
          throw new MessageOnlyException("failed to unpack nativelib")
        }
      }
    },
    nativeLink := ResourceScope { implicit scope =>
      val clang     = nativeClang.value
      val clangpp   = nativeClangPP.value
      val clangOpts = nativeClangOptions.value
      checkThatClangIsRecentEnough(clang)

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
        .withTarget(nativeTarget.value)

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
                            FilesInfo.hash) { _ =>
          IO.createDirectory(target)
          val links =
            compileNir(config, logger, linkerReporter, optimizerReporter)
          compileLl(clangpp,
                    target,
                    appll,
                    binary,
                    nativeTarget.value,
                    links.map(_.name),
                    linkage,
                    clangOpts,
                    logger)
          Set(binary)
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

  /**
   * Tests whether the clang compiler is recent enough.
   * <p/>
   * This is determined through looking up a built-in #define which is
   * more reliable than testing for a specific version.
   * <p/>
   * It might be better to use feature checking macros:
   * http://clang.llvm.org/docs/LanguageExtensions.html#feature-checking-macros
   */
  private def checkThatClangIsRecentEnough(pathToClangBinary: File): Unit = {
    def maybeFile(f: File) = f match {
      case file if file.exists => Some(abs(file))
      case none                => None
    }

    def definesBuiltIn(
        pathToClangBinary: Option[String]): Option[Seq[String]] = {
      def commandLineToListBuiltInDefines(clang: String) =
        Seq("echo", "") #| Seq(clang, "-dM", "-E", "-")
      def splitIntoLines(s: String)      = s.split(f"%n")
      def removeLeadingDefine(s: String) = s.substring(s.indexOf(' ') + 1)

      for {
        clang <- pathToClangBinary
        output = commandLineToListBuiltInDefines(clang).!!
        lines  = splitIntoLines(output)
      } yield lines map removeLeadingDefine
    }

    val clang                = maybeFile(pathToClangBinary)
    val defines: Seq[String] = definesBuiltIn(clang).to[Seq].flatten
    val clangIsRecentEnough =
      defines.contains("__DECIMAL_DIG__ __LDBL_DECIMAL_DIG__")

    if (!clangIsRecentEnough) {
      throw new MessageOnlyException(
        s"No recent installation of clang found " +
          s"at $pathToClangBinary.\nSee http://scala-native.readthedocs.io" +
          s"/en/latest/user/setup.html for details.")
    }
  }
}
