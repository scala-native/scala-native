package scala.scalanative
package llvm

import java.io.File

import scala.sys.process.{Process, ProcessLogger}
import scala.util.Try

import System.{lineSeparator => nl}

object LLVM {

  private def abs(file: File): String =
    file.getAbsolutePath

  private def running(command: Seq[String]): String =
    "running" + nl + command.mkString(nl + "\t")

  private def getFiles(base: File, filter: File => Boolean): Seq[File] = {
    val included = if (filter(base)) Seq(base) else Seq()
    val inBase   = Option(base.listFiles()) getOrElse Array.empty
    included ++ inBase.flatMap(getFiles(_, filter))
  }

  private[scalanative] def discover(
      binaryName: String,
      binaryVersions: Seq[(String, String)]): File = {

    val docInstallUrl =
      "http://scala-native.readthedocs.io/en/latest/user/setup.html#installing-llvm-clang-and-boehm-gc"

    val envName =
      if (binaryName == "clang") "CLANG"
      else if (binaryName == "clang++") "CLANGPP"
      else binaryName

    sys.env.get(s"${envName}_PATH") match {
      case Some(path) => new File(path)
      case None => {
        val binaryNames = binaryVersions.flatMap {
          case (major, minor) =>
            Seq(s"$binaryName$major$minor", s"$binaryName-$major.$minor")
        } :+ binaryName

        Process("which" +: binaryNames).lines_!
          .map(new File(_))
          .headOption
          .getOrElse {
            throw new Exception(
              s"no ${binaryNames.mkString(", ")} found in $$PATH. Install clang ($docInstallUrl)")
          }
      }
    }
  }

  private[scalanative] lazy val includes = {
    val includedir =
      Try(Process("llvm-config --includedir").lines_!.toSeq)
        .getOrElse(Seq.empty)
    ("/usr/local/include" +: includedir).map(s => s"-I$s")
  }

  private[scalanative] lazy val libs = {
    val libdir =
      Try(Process("llvm-config --libdir").lines_!.toSeq).getOrElse(Seq.empty)
    ("/usr/local/lib" +: libdir).map(s => s"-L$s")
  }

  /** Compiles *.c[pp] in `cwd`. */
  def compileCSources(clang: File,
                      clangpp: File,
                      cwd: File,
                      logger: ProcessLogger): Boolean = {
    val cpaths     = getFiles(cwd, _.getName endsWith ".c").map(abs)
    val cpppaths   = getFiles(cwd, _.getName endsWith ".cpp").map(abs)
    val compilec   = abs(clang) +: (includes ++ ("-c" +: cpaths))
    val compilecpp = abs(clangpp) +: (includes ++ ("-c" +: cpppaths))

    logger.out(running(compilec))
    val cExit = Process(compilec, cwd) ! logger

    logger.out(running(compilecpp))
    val cppExit = Process(compilecpp, cwd) ! logger

    cExit == 0 && cppExit == 0
  }

  /** Compiles application and runtime llvm ir file to binary using clang. */
  private[scalanative] def compileLl(clangpp: File,
                                     target: File,
                                     nativelib: File,
                                     appll: File,
                                     binary: File,
                                     links: Seq[String],
                                     linkage: Map[String, String],
                                     opts: Seq[String],
                                     logger: ProcessLogger): Unit = {
    val outpath = abs(binary)
    val apppath = abs(appll)
    val opaths  = getFiles(nativelib, _.getName endsWith ".o").map(abs)
    val paths   = apppath +: opaths
    val linkopts = links.zip(links.map(linkage.get(_))).flatMap {
      case (name, Some("static"))         => Seq("-static", "-l", name)
      case (name, Some("dynamic") | None) => Seq("-l", name)
      case (name, Some(kind)) =>
        throw new Exception(s"uknown linkage kind $kind for $name")
    }
    val flags   = Seq("-o", outpath) ++ linkopts ++ opts
    val compile = abs(clangpp) +: (flags ++ paths)

    logger.out(running(compile))

    Process(compile, target) ! logger
  }

  /**
   * Tests whether the clang compiler is recent enough.
   * <p/>
   * This is determined through looking up a built-in #define which is
   * more reliable than testing for a specific version.
   * <p/>
   * It might be better to use feature checking macros:
   * http://clang.llvm.org/docs/LanguageExtensions.html#feature-checking-macros
   */
  private[scalanative] def checkThatClangIsRecentEnough(
      pathToClangBinary: File): Unit = {
    def maybeFile(f: File) = f match {
      case file if file.exists => Some(abs(file))
      case none                => None
    }

    def definesBuiltIn(
        pathToClangBinary: Option[String]): Option[Seq[String]] = {
      def commandLineToListBuiltInDefines(clang: String) =
        Process(Seq("echo", "")) #| Seq(clang, "-dM", "-E", "-")
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
      throw new Exception(s"No recent installation of clang found " +
        s"at $pathToClangBinary.\nSee https://github.com/scala-native/scala-" +
        s"native/blob/master/docs/building.md for details.")
    }
  }

}
