package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}

import scala.collection.JavaConverters._
import scala.util.Try
import scala.sys.process._

import IO.RichPath

object LLVM {

  /** Discover concrete binary path using command name and
   *  a sequence of potential supported versions.
   */
  def discover(binaryName: String,
               binaryVersions: Seq[(String, String)]): Path = {
    val docSetup =
      "http://www.scala-native.org/en/latest/user/setup.html"

    val envName =
      if (binaryName == "clang") "CLANG"
      else if (binaryName == "clang++") "CLANGPP"
      else binaryName

    sys.env.get(s"${envName}_PATH") match {
      case Some(path) => Paths.get(path)
      case None => {
        val binaryNames = binaryVersions.flatMap {
          case (major, minor) =>
            Seq(s"$binaryName$major$minor", s"$binaryName-$major.$minor")
        } :+ binaryName

        Process("which" +: binaryNames)
          .lines_!(SilentLogger)
          .map(Paths.get(_))
          .headOption
          .getOrElse {
            throw new BuildException(
              s"no ${binaryNames.mkString(", ")} found in $$PATH. Install clang ($docSetup)")
          }
      }
    }
  }

  /** Versions of clang which are known to work with Scala Native. */
  val clangVersions =
    Seq(("6", "0"), ("5", "0"), ("4", "0"), ("3", "9"), ("3", "8"), ("3", "7"))

  /**
   * Tests whether the clang compiler is recent enough.
   * <p/>
   * This is determined through looking up a built-in #define which is
   * more reliable than testing for a specific version.
   * <p/>
   * It might be better to use feature checking macros:
   * http://clang.llvm.org/docs/LanguageExtensions.html#feature-checking-macros
   */
  def checkThatClangIsRecentEnough(pathToClangBinary: Path): Unit = {
    def maybePath(p: Path) = p match {
      case path if Files.exists(path) => Some(path.abs)
      case none                       => None
    }

    def definesBuiltIn(
        pathToClangBinary: Option[String]): Option[Seq[String]] = {
      def commandLineToListBuiltInDefines(clang: String) =
        Process(Seq("echo", "")) #| Process(Seq(clang, "-dM", "-E", "-"))
      def splitIntoLines(s: String): Array[String] =
        s.split(f"%n")
      def removeLeadingDefine(s: String): String =
        s.substring(s.indexOf(' ') + 1)

      for {
        clang <- pathToClangBinary
        output = commandLineToListBuiltInDefines(clang).!!
        lines  = splitIntoLines(output)
      } yield lines map removeLeadingDefine
    }

    val clang                = maybePath(pathToClangBinary)
    val defines: Seq[String] = definesBuiltIn(clang).to[Seq].flatten
    val clangIsRecentEnough =
      defines.contains("__DECIMAL_DIG__ __LDBL_DECIMAL_DIG__")

    if (!clangIsRecentEnough) {
      throw new BuildException(
        s"No recent installation of clang found " +
          s"at $pathToClangBinary.\nSee http://scala-native.readthedocs.io" +
          s"/en/latest/user/setup.html for details.")
    }
  }

  /** Default compilation options passed to clang. */
  def discoverCompilationOptions(): Seq[String] = {
    val includes = {
      val includedir =
        Try(Process("llvm-config --includedir").lines_!.toSeq)
          .getOrElse(Seq.empty)
      ("/usr/local/include" +: includedir).map(s => s"-I$s")
    }
    includes :+ "-Qunused-arguments"
  }

  /** Default options passed to the system linker. */
  def discoverLinkingOptions(): Seq[String] = {
    val libs = {
      val libdir =
        Try(Process("llvm-config --libdir").lines_!.toSeq)
          .getOrElse(Seq.empty)
      ("/usr/local/lib" +: libdir).map(s => s"-L$s")
    }
    libs
  }

  /**
   * Detect the target architecture.
   *
   * @param clang   A path to the executable `clang`.
   * @param workdir A working directory where the compilation will take place.
   * @param logger  A logger that will receive messages about the execution.
   * @return The detected target triple describing the target architecture.
   */
  def discoverTarget(clang: Path, workdir: Path, logger: Logger): String = {
    // Use non-standard extension to not include the ll file when linking (#639)
    val targetc  = workdir.resolve("target").resolve("c.probe")
    val targetll = workdir.resolve("target").resolve("ll.probe")
    val compilec =
      Seq(clang.abs, "-S", "-xc", "-emit-llvm", "-o", targetll.abs, targetc.abs)
    def fail =
      throw new BuildException("Failed to detect native target.")

    IO.write(targetc, "int probe;".getBytes("UTF-8"))
    logger.running(compilec)
    val exit = Process(compilec, workdir.toFile) ! Logger.toProcessLogger(
      logger)
    if (exit != 0) fail
    Files
      .readAllLines(targetll)
      .asScala
      .collectFirst {
        case line if line.startsWith("target triple") =>
          line.split("\"").apply(1)
      }
      .getOrElse(fail)
  }

  /** Compile the given LL files to object files */
  def compileLL(config: Config, llPaths: Seq[Path]): Seq[Path] = {
    val optimizationOpt =
      config.mode match {
        case Mode.Debug   => "-O0"
        case Mode.Release => "-O2"
      }
    val opts = optimizationOpt +: config.compileOptions

    config.logger.time("Compiling to native code") {
      llPaths.par
        .map { ll =>
          val apppath = ll.abs
          val outpath = apppath + ".o"
          val compile = Seq(config.clang.abs, "-c", apppath, "-o", outpath) ++ opts
          config.logger.running(compile)
          Process(compile, config.workdir.toFile) ! Logger.toProcessLogger(
            config.logger)
          Paths.get(outpath)
        }
        .seq
        .toSeq
    }
  }

  /**
   * Links a collection of `.ll` files into native binary.
   *
   * @param config       The configuration of the toolchain.
   * @param linkerResult The results from the linker.
   * @param llPaths      The list of `.ll` files to link.
   * @param nativelib    The path to the nativelib.
   * @param outpath      The path where to write the resulting binary.
   * @return `outpath`
   */
  def linkLL(config: Config,
             linkerResult: LinkerResult,
             llPaths: Seq[Path],
             nativelib: Path,
             outpath: Path): Path = {

    val links = {
      val os   = Option(sys props "os.name").getOrElse("")
      val arch = config.target.split("-").head
      // we need re2 to link the re2 c wrapper (cre2.h)
      val librt = os match {
        case "Linux" => Seq("rt")
        case _       => Seq.empty
      }
      val libunwind = os match {
        case "Mac OS X" => Seq.empty
        case _          => Seq("unwind", "unwind-" + arch)
      }
      librt ++ libunwind ++ linkerResult.links
        .map(_.name) ++ config.gc.links
    }
    val linkopts = links.map("-l" + _) ++ config.linkingOptions ++ Seq(
      "-lpthread")
    val targetopt = Seq("-target", config.target)
    val flags     = Seq("-o", outpath.abs) ++ linkopts ++ targetopt
    val opaths    = IO.getAll(nativelib, "glob:**.o").map(_.abs)
    val paths     = llPaths.map(_.abs) ++ opaths
    val compile   = config.clangpp.abs +: (flags ++ paths)

    config.logger.time(s"Linking native code (${config.gc.name} gc)") {
      config.logger.running(compile)
      Process(compile, config.workdir.toFile) ! Logger.toProcessLogger(
        config.logger)
    }

    outpath

  }

  private val SilentLogger = ProcessLogger(_ => (), _ => ())

}
