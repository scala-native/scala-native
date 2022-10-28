package scala.scalanative
package build

import java.io.{File, PrintWriter}
import java.nio.file.{Files, Path, Paths}
import scala.sys.process._
import scalanative.build.core.IO.RichPath
import scalanative.compat.CompatParColls.Converters._
import scalanative.nir.Attr.Link

/** Internal utilities to interact with LLVM command-line tools. */
private[scalanative] object LLVM {

  /** Object file extension: ".o" */
  val oExt = ".o"

  /** C++ file extension: ".cpp" */
  val cppExt = ".cpp"

  /** LLVM intermediate file extension: ".ll" */
  val llExt = ".ll"

  /** List of source patterns used: ".c, .cpp, .S" */
  val srcExtensions = Seq(".c", cppExt, ".S")

  /** Compile the given files to object files
   *
   *  @param config
   *    The configuration of the toolchain.
   *  @param paths
   *    The directory paths containing native files to compile.
   *  @return
   *    The paths of the `.o` files.
   */
  def compile(config: Config, paths: Seq[Path])(implicit
      incCompilationContext: IncCompilationContext
  ): Seq[Path] = {
    // generate .o files for all included source files in parallel
    paths.par.map { srcPath =>
      val inpath = srcPath.abs
      val outpath = inpath + oExt
      val objPath = Paths.get(outpath)
      // compile if out of date or no object file
      if (needsCompiling(srcPath, objPath)) {
        compileFile(config, srcPath, objPath)
      } else objPath
    }.seq
  }

  private def compileFile(config: Config, srcPath: Path, objPath: Path)(implicit
      incCompilationContext: IncCompilationContext
  ): Path = {
    val inpath = srcPath.abs
    val outpath = objPath.abs
    val isCpp = inpath.endsWith(cppExt)
    val isLl = inpath.endsWith(llExt)
    val workdir = config.workdir

    val compiler = if (isCpp) config.clangPP.abs else config.clang.abs
    val stdflag = {
      if (isLl) Seq()
      else if (isCpp) {
        // C++14 or newer standard is needed to compile code using Windows API
        // shipped with Windows 10 / Server 2016+ (we do not plan supporting older versions)
        if (config.targetsWindows) Seq("-std=c++14")
        else Seq("-std=c++11")
      } else Seq("-std=gnu11")
    }
    val platformFlags = {
      if (config.targetsWindows) Seq("-g")
      else Nil
    }
    val expectionsHandling =
      List("-fexceptions", "-fcxx-exceptions", "-funwind-tables")
    val flags = opt(config) +: "-fvisibility=hidden" +:
      stdflag ++: platformFlags ++: expectionsHandling ++: config.compileOptions
    val compilec =
      Seq(compiler) ++ flto(config) ++ flags ++
        asan(config) ++ target(config) ++
        Seq("-c", inpath, "-o", outpath)

    // compile
    config.logger.running(compilec)
    val result = Process(compilec, workdir.toFile) !
      Logger.toProcessLogger(config.logger)
    if (result != 0) {
      throw new BuildException(s"Failed to compile ${inpath}")
    }

    objPath
  }

  /** Links a collection of `.ll.o` files and the `.o` files from the
   *  `nativelib`, other libaries, and the application project into the native
   *  binary.
   *
   *  @param config
   *    The configuration of the toolchain.
   *  @param linkerResult
   *    The results from the linker.
   *  @param objectPaths
   *    The paths to all the `.o` files.
   *  @return
   *    `outpath` The config.artifactPath
   */
  def link(
      config: Config,
      linkerResult: linker.Result,
      objectsPaths: Seq[Path]
  ): Path = {
    val outpath = config.artifactPath
    val workdir = config.workdir

    // don't link if no changes
    if (!needsLinking(objectsPaths, outpath)) return outpath

    val links = {
      val srclinks = linkerResult.links.collect {
        case Link("z") if config.targetsWindows => "zlib"
        case Link(name)                         => name
      }
      val gclinks = config.gc.links
      // We need extra linking dependencies for:
      // * libdl for our vendored libunwind implementation.
      // * libpthread for process APIs and parallel garbage collection.
      // * Dbghelp for windows implementation of unwind libunwind API
      val platformsLinks =
        if (config.targetsWindows) Seq("Dbghelp")
        else Seq("pthread", "dl")
      platformsLinks ++ srclinks ++ gclinks
    }
    val linkopts = config.linkingOptions ++ links.map("-l" + _)
    val flags = {
      val platformFlags =
        if (config.targetsWindows) {
          // https://github.com/scala-native/scala-native/issues/2372
          // When using LTO make sure to use lld linker instead of default one
          // LLD might find some duplicated symbols defined in both C and C++,
          // runtime libraries (libUCRT, libCPMT), we ignore this warnings.
          val ltoSupport = config.compilerConfig.lto match {
            case LTO.None => Nil
            case _        => Seq("-fuse-ld=lld", "-Wl,/force:multiple")
          }
          Seq("-g") ++ ltoSupport
        } else Seq("-rdynamic")
      flto(config) ++ platformFlags ++
        Seq("-o", outpath.abs) ++
        asan(config) ++ target(config)
    }
    val paths = objectsPaths.map(_.abs)
    // it's a fix for passing too many file paths to the clang compiler,
    // If too many packages are compiled and the platform is windows, windows
    // terminal doesn't support too many characters, which will cause an error.
    val llvmLinkInfo = flags ++ paths ++ linkopts
    locally {
      val pw = new PrintWriter(workdir.resolve("llvmLinkInfo").toFile)
      try
        llvmLinkInfo.foreach {
          // in windows system, the file separator doesn't work very well, so we
          // replace it to linux file separator
          str => pw.println(str.replace("\\", "/"))
        }
      finally pw.close()
    }
    val compile = config.clangPP.abs +: Seq(s"@llvmLinkInfo")

    // link
    config.logger.running(compile)
    val result = Process(compile, workdir.toFile) !
      Logger.toProcessLogger(config.logger)
    if (result != 0) {
      throw new BuildException(s"Failed to link ${outpath}")
    }

    outpath
  }

  /** Checks the input timestamp to see if the file needs compiling. The call to
   *  lastModified will return 0 for a non existent output file but that makes
   *  the timestamp always less forcing a recompile.
   *
   *  @param in
   *    the source file
   *  @param out
   *    the object file
   *  @return
   *    true if it needs compiling false otherwise.
   */
  @inline private def needsCompiling(in: Path, out: Path): Boolean = {
    in.toFile().lastModified() > out.toFile().lastModified()
  }

  /** Looks at all the object files to see if one is newer than the output
   *  (executable). All object files will be compiled at this time so
   *  lastModified will always be a real time stamp. The output executable
   *  lastModified can be 0 but that forces the link to occur.
   *
   *  @param in
   *    the list of object file to link
   *  @param out
   *    the executable
   *  @return
   *    true if it need linking
   */
  @inline private def needsLinking(in: Seq[Path], out: Path): Boolean = {
    val inmax = in.map(_.toFile().lastModified()).max
    val outmax = out.toFile().lastModified()
    inmax > outmax
  }

  private def flto(config: Config): Seq[String] =
    config.compilerConfig.lto match {
      case LTO.None => Seq.empty
      case lto      => Seq(s"-flto=${lto.name}")
    }

  private def asan(config: Config): Seq[String] =
    config.compilerConfig.asan match {
      case true  => Seq("-fsanitize=address", "-fno-omit-frame-pointer")
      case false => Seq.empty
    }

  private def target(config: Config): Seq[String] =
    config.compilerConfig.targetTriple match {
      case Some(tt) => Seq("-target", tt)
      case None     => Seq("-Wno-override-module")
    }

  private def opt(config: Config): String =
    config.mode match {
      case Mode.Debug       => "-O0"
      case Mode.ReleaseFast => "-O2"
      case Mode.ReleaseFull => "-O3"
    }
}
