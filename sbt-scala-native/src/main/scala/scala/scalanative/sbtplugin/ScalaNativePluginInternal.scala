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
  val nativeLinkerReporter = settingKey[tools.LinkerReporter](
    "A reporter that gets notified whenever a linking event happens.")

  val nativeOptimizerReporter = settingKey[tools.OptimizerReporter](
    "A reporter that gets notified whenever an optimizer event happens.")

  private lazy val nativelib: File =
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

      compileCSources(clang, clangpp, nativelib, logger)
    } else {
      true
    }
  }

  /** Compiles application and runtime llvm ir file to binary using clang. */
  private def compileLl(clangpp: File,
                        target: File,
                        appll: File,
                        binary: File,
                        links: Seq[String],
                        linkage: Map[String, String],
                        opts: Seq[String],
                        logger: Logger): Unit = {
    val outpath = abs(binary)
    val apppath = abs(appll)
    val opaths  = (nativelib ** "*.o").get.map(abs)
    val paths   = apppath +: opaths
    val linkopts = links.zip(links.map(linkage.get(_))).flatMap {
      case (name, Some("static"))         => Seq("-static", "-l", name)
      case (name, Some("dynamic") | None) => Seq("-l", name)
      case (name, Some(kind)) =>
        throw new MessageOnlyException(s"uknown linkage kind $kind for $name")
    }
    val flags   = Seq("-o", outpath) ++ linkopts ++ opts
    val compile = abs(clangpp) +: (flags ++ paths)

    logger.info(running(compile))

    Process(compile, target) ! logger
  }

  lazy val projectSettings = Seq(
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
      // We need to add `-lrt` for the POSIX realtime lib, which doesn't exist
      // on macOS.
      val lrt = Option(sys props "os.name") match {
        case Some("Linux") => Seq("-lrt")
        case _             => Seq()
      }
      includes ++ libs ++ maybeInjectShared(nativeSharedLibrary.value) ++ lrt
    },
    artifactPath in nativeLink := {
      (crossTarget in Compile).value / (moduleName.value + "-out")
    },
    nativeLinkerReporter := tools.LinkerReporter.empty,
    nativeOptimizerReporter := tools.OptimizerReporter.empty,
    nativeLink := ResourceScope { implicit in =>
      val mainClass = (selectMainClass in Compile).value.getOrElse(
        throw new MessageOnlyException("No main class detected.")
      )
      val entry             = nir.Global.Top(mainClass.toString + "$")
      val classpath         = (fullClasspath in Compile).value.map(_.data)
      val target            = (crossTarget in Compile).value
      val appll             = target / "out.ll"
      val binary            = (artifactPath in nativeLink).value
      val clang             = nativeClang.value
      val clangpp           = nativeClangPP.value
      val clangOpts         = nativeClangOptions.value
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

      checkThatClangIsRecentEnough(clang)

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
              compileLl(clangpp,
                        target,
                        appll,
                        binary,
                        links.map(_.name),
                        linkage,
                        clangOpts,
                        logger)
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
          s"at $pathToClangBinary.\nSee https://github.com/scala-native/scala-" +
          s"native/blob/master/docs/building.md for details.")
    }
  }
}
