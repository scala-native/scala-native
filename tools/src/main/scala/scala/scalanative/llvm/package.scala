package scala.scalanative

import java.nio.file.{Files, Path, Paths}

import scala.sys.process._

package object llvm {

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
            throw new Exception(
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
      case path if Files.exists(path) => Some(path.toAbsolutePath.toString)
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
      throw new Exception(
        s"No recent installation of clang found " +
          s"at $pathToClangBinary.\nSee http://scala-native.readthedocs.io" +
          s"/en/latest/user/setup.html for details.")
    }
  }

  private val SilentLogger = ProcessLogger(_ => (), _ => ())

}
