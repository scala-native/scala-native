package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scala.sys.process._
import scalanative.build.IO.RichPath
import scalanative.compat.CompatParColls.Converters._
import scalanative.nir.Attr.Link

/** Internal utilities to interact with LLVM command-line tools. */
private[scalanative] object LLVM {
  // settings to make sure that exceptions can be caught and unwinded
  private val unwindSettings =
    Seq("-fexceptions", "-fcxx-exceptions", "-funwind-tables")

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
  def compile(config: Config, paths: Seq[Path]): Seq[Path] = {
    // generate .o files for all included source files in parallel
    paths.par.map { path =>
      val inpath = path.abs
      val outpath = inpath + oExt
      val isCpp = inpath.endsWith(cppExt)
      val isLl = inpath.endsWith(llExt)
      val objPath = Paths.get(outpath)
      // LL is generated so always rebuild
      if (isLl || !Files.exists(objPath)) {
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
            unwindSettings ++ asan(config) ++ target(config) ++
            Seq("-c", inpath, "-o", outpath)

        config.logger.running(compilec)
        val result = Process(compilec, config.workdir.toFile) !
          Logger.toProcessLogger(config.logger)
        if (result != 0) {
          throw new BuildException(s"Failed to compile ${inpath}")
        }
      }
      objPath
    }.seq
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
   *  @param outpath
   *    The path where to write the resulting binary.
   *  @return
   *    `outpath`
   */
  def link(
      config: Config,
      linkerResult: linker.Result,
      objectsPaths: Seq[Path],
      outpath: Path
  ): Path = {
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
        } else Seq("-rdynamic", "--rtlib=compiler-rt")
      flto(config) ++ platformFlags ++
        Seq("-o", outpath.abs) ++
        unwindSettings ++ asan(config) ++ target(config)
    }
    val paths = objectsPaths.map(_.abs)
    val compile = config.clangPP.abs +: (flags ++ paths ++ linkopts)

    config.logger.time(
      s"Linking native code (${config.gc.name} gc, ${config.LTO.name} lto)"
    ) {
      config.logger.running(compile)
      val result = Process(compile, config.workdir.toFile) !
        Logger.toProcessLogger(config.logger)
      if (result != 0) {
        throw new BuildException(s"Failed to link ${outpath}")
      }
    }
    outpath
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
