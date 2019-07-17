package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scala.collection.JavaConverters._
import scala.util.Try
import scala.sys.process._
import scalanative.build.IO.RichPath

/** Utilities for discovery of command-line tools and settings required
 *  to build Scala Native applications.
 */
object Discover {

  /** Compilation mode name that takes SCALANATIVE_MODE into account or default otherwise. */
  def mode(): String =
    getenv("SCALANATIVE_MODE").getOrElse(build.Mode.default.name)

  /** LTO variant used for release mode. */
  def LTO(): String =
    getenv("SCALANATIVE_LTO").getOrElse("none")

  /** Find nativelib jar on the classpath. */
  def nativelib(classpath: Seq[Path]): Option[Path] =
    classpath.find { path =>
      val absolute = path.toAbsolutePath.toString
      absolute.contains("scala-native") && absolute.contains("nativelib")
    }

  /** Find the newest compatible clang binary. */
  def clang(): Path = {
    val path = discover("clang", clangVersions)
    checkThatClangIsRecentEnough(path)
    path
  }

  /** Find the newest compatible clang++ binary. */
  def clangpp(): Path = {
    val path = discover("clang++", clangVersions)
    checkThatClangIsRecentEnough(path)
    path
  }

  /** Find default clang compilation options. */
  def compileOptions(): Seq[String] = {
    val includes = {
      val includedir =
        Try(Process("llvm-config --includedir").lines_!.toSeq)
          .getOrElse(Seq.empty)
      ("/usr/local/include" +: includedir).map(s => s"-I$s")
    }
    includes :+ "-Qunused-arguments"
  }

  /** Find default options passed to the system's native linker. */
  def linkingOptions(): Seq[String] = {
    val libs = {
      val libdir =
        Try(Process("llvm-config --libdir").lines_!.toSeq)
          .getOrElse(Seq.empty)
      ("/usr/local/lib" +: libdir).map(s => s"-L$s")
    }
    libs
  }

  /** Detect the target architecture.
   *
   *  @param clang   A path to the executable `clang`.
   *  @param workdir A working directory where the compilation will take place.
   *  @return The detected target triple describing the target architecture.
   */
  def targetTriple(clang: Path, workdir: Path): String = {
    // Use non-standard extension to not include the ll file when linking (#639)
    val targetc  = workdir.resolve("target").resolve("c.probe")
    val targetll = workdir.resolve("target").resolve("ll.probe")
    val compilec =
      Seq(clang.abs, "-S", "-xc", "-emit-llvm", "-o", targetll.abs, targetc.abs)
    def fail =
      throw new BuildException("Failed to detect native target.")

    IO.write(targetc, "int probe;".getBytes("UTF-8"))
    val exit = Process(compilec, workdir.toFile).!
    if (exit != 0) {
      fail
    } else {
      Files
        .readAllLines(targetll)
        .asScala
        .collectFirst {
          case line if line.startsWith("target triple") =>
            line.split("\"").apply(1)
        }
        .getOrElse(fail)
    }
  }

  /** Tests whether the clang compiler is recent enough.
   *  It is determined through looking up a built-in #define which
   *  is more reliable than testing for a specific version.
   */
  private[scalanative] def checkThatClangIsRecentEnough(
      pathToClangBinary: Path): Unit = {
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

  /** Versions of clang which are known to work with Scala Native. */
  private[scalanative] val clangVersions =
    Seq(("8", ""),
        ("8", "0"),
        ("7", ""),
        ("7", "0"), // LLVM changed version numbering scheme, try both.
        ("6", "0"),
        ("5", "0"),
        ("4", "0"),
        ("3", "9"),
        ("3", "8"),
        ("3", "7"))

  /** Discover concrete binary path using command name and
   *  a sequence of potential supported versions.
   */
  private[scalanative] def discover(
      binaryName: String,
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
            val sep = if (minor == "") "" else "."
            Seq(s"$binaryName$major$minor", s"$binaryName-$major${sep}$minor")
        } :+ binaryName

        Process("which" +: binaryNames)
          .lines_!(silentLogger())
          .map(Paths.get(_))
          .headOption
          .getOrElse {
            throw new BuildException(
              s"no ${binaryNames.mkString(", ")} found in $$PATH. Install clang ($docSetup)")
          }
      }
    }
  }

  private def silentLogger(): ProcessLogger =
    ProcessLogger(_ => (), _ => ())

  private def getenv(key: String): Option[String] =
    Option(System.getenv.get(key))
}
