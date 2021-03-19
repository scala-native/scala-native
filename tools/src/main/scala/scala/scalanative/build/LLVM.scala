package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import java.util.Arrays
import scala.sys.process._
import scalanative.build.IO.RichPath
import scalanative.build.NativeLib._
import scalanative.compat.CompatParColls.Converters._

/** Internal utilities to interact with LLVM command-line tools. */
private[scalanative] object LLVM {

  /**
   * Called to unpack jars and copy native code.
   *
   * @param nativelib the native lib to copy/unpack
   * @return The destination path of the directory
   */
  def unpackNativeCode(nativelib: NativeLib): Path =
    if (NativeLib.isJar(nativelib)) unpackNativeJar(nativelib)
    else copyNativeDir(nativelib)

  /**
   * Unpack the `src` Jar Path to `workdir/dest` where `dest`
   * is the generated directory where the Scala Native lib or
   * a third party library that includes native code is copied.
   *
   * If the same archive has already been unpacked to this location
   * and hasn't changed, this call has no effect.
   *
   * @param nativelib The NativeLib to unpack.
   * @return The Path where the nativelib has been unpacked, `workdir/dest`.
   */
  private def unpackNativeJar(nativelib: NativeLib): Path = {
    val target      = nativelib.dest
    val source      = nativelib.src
    val jarhash     = IO.sha1(source)
    val jarhashPath = target.resolve("jarhash")
    def unpacked =
      Files.exists(target) &&
        Files.exists(jarhashPath) &&
        Arrays.equals(jarhash, Files.readAllBytes(jarhashPath))

    if (!unpacked) {
      IO.deleteRecursive(target)
      IO.unzip(source, target)
      IO.write(jarhashPath, jarhash)
    }
    target
  }

  /**
   * Copy project code from project `src` Path to `workdir/dest`
   * Path where it can be compiled and linked.
   *
   * This does not copy if no native code has changed.
   *
   * @param nativelib The NativeLib to copy.
   * @return The Path where the code was copied, `workdir/dest`.
   */
  private def copyNativeDir(nativelib: NativeLib): Path = {
    val target        = nativelib.dest
    val source        = nativelib.src
    val files         = IO.getAll(source, NativeLib.allFilesPattern(source))
    val fileshash     = IO.sha1files(files)
    val fileshashPath = target.resolve("fileshash")
    def copied =
      Files.exists(target) &&
        Files.exists(fileshashPath) &&
        Arrays.equals(fileshash, Files.readAllBytes(fileshashPath))
    if (!copied) {
      IO.deleteRecursive(target)
      IO.copyDirectory(source, target)
      IO.write(fileshashPath, fileshash)
    }
    target
  }

  /**
   * Compile all the native lib source files to `.o` files
   * with special logic to select GC and optional components
   * for the Scala Native `nativelib`.
   *
   * @param config        The configuration of the toolchain.
   * @param linkerResult  The results from the linker.
   * @param nativelibPath The generated location of the Scala Native nativelib.
   * @param nativelibs    The paths to the native libraries.
   * @return              The paths to the produced `.o` files.
   */
  def compileNativelibs(config: Config,
                        linkerResult: linker.Result,
                        nativelibs: Seq[Path]): Seq[Path] = {
    val workdir = config.workdir
    // search starting at workdir `native` to find
    // code across all native component libraries
    // including the `nativelib`
    val srcPatterns = NativeLib.destSrcPatterns(workdir, nativelibs)
    val paths       = IO.getAll(workdir, srcPatterns).map(_.abs)

    def include(path: String) = {
      import NativePathsExtractor._
      path match {
        case Platform("shared" :: _)              => true
        case Platform("windows" :: _)             => config.targetsWindows
        case Platform("posix" :: _) | PosixLib(_) => !config.targetsWindows

        case GC("shared" :: _)        => true
        case GC(name :: _)            => name == config.gc.name
        case GC(SharedBy(impls) :: _) => impls.contains(config.gc.name)

        case Optional(_ :+ File(name, _)) =>
        linkerResult.links.map(_.name).contains(name)
        case _ => true
      }
    }

    val (includePaths, excludePaths) = paths.partition(include)

    // delete .o files for all excluded source files
    excludePaths.foreach { path =>
      val opath = Paths.get(path + oExt)
      if (Files.exists(opath))
        Files.delete(opath)
    }

    val fltoOpt   = flto(config)
    val targetOpt = target(config)

    // generate .o files for all included source files in parallel
    includePaths.par.map { path =>
      val opath   = path + oExt
      val objPath = Paths.get(opath)
      if (!Files.exists(objPath)) {
        val isCpp    = path.endsWith(cppExt)
        val compiler = if (isCpp) config.clangPP.abs else config.clang.abs
        val cppStd   = if (config.targetsWindows) "c++17" else "c++11"
        val stdflag  = if (isCpp) s"-std=$cppStd" else "-std=gnu11"
        val flags = Seq(stdflag,
                        "-fvisibility=hidden",
                        "-D_CRT_SECURE_NO_WARNINGS",
                        "-Wdeprecated-declarations") ++ config.compileOptions
        val compilec =
          Seq(compiler) ++ fltoOpt ++ flags ++ targetOpt ++
            Seq("-c", path, "-o", opath)

        config.logger.running(compilec)
        val result = Process(compilec, config.workdir.toFile) ! Logger
          .toProcessLogger(config.logger)
        if (result != 0) {
          sys.error("Failed to compile native library runtime code.")
        }
      }
      objPath
    }.seq
  }

  /**
   * Compile the given ll files to object files
   *
   * @param config  The configuration of the toolchain.
   * @param llPaths The directory paths containing `.ll` files.
   * @return        The paths of the `.o` files.
   */
  def compile(config: Config, llPaths: Seq[Path]): Seq[Path] = {
    val optimizationOpt =
      config.mode match {
        case Mode.Debug       => "-O0"
        case Mode.ReleaseFast => "-O2"
        case Mode.ReleaseFull => "-O3"
      }

    val opts    = Seq(optimizationOpt) ++ target(config) ++ config.compileOptions
    val fltoOpt = flto(config)

    llPaths.par.map { ll =>
      val apppath = ll.abs
      val outpath = apppath + oExt
      val compile =
        Seq(config.clang.abs) ++ fltoOpt ++ Seq("-c", apppath, "-o", outpath) ++ opts
      config.logger.running(compile)
      Process(compile, config.workdir.toFile) ! Logger.toProcessLogger(
        config.logger)
      Paths.get(outpath)
    }.seq
  }

  /**
   * Links a collection of `.ll.o` files and the `.o` files
   * from the `nativelib`, other libaries, and the
   * application project into the native binary.
   *
   * @param config       The configuration of the toolchain.
   * @param linkerResult The results from the linker.
   * @param objectPaths  The paths to all the `.o` files.
   * @param outpath      The path where to write the resulting binary.
   * @return `outpath`
   */
  def link(config: Config,
           linkerResult: linker.Result,
           objectsPaths: Seq[Path],
           outpath: Path): Path = {
    val workdir = config.workdir
    val links = {
      val srclinks = linkerResult.links.map(_.name)
      val gclinks  = config.gc.links
      // We need extra linking dependencies for:
      // * libdl for our vendored libunwind implementation.
      // * libpthread for process APIs and parallel garbage collection.
      "pthread" +: "dl" +: srclinks ++: gclinks
    }
    val linkopts = config.linkingOptions ++ links.map("-l" + _)
    val flags =
      flto(config) ++ Seq("-rdynamic", "-o", outpath.abs) ++ target(config)
    val paths   = objectsPaths.map(_.abs)
    val compile = config.clangPP.abs +: (flags ++ paths ++ linkopts)
    val ltoName = lto(config).getOrElse("none")

    config.logger.time(
      s"Linking native code (${config.gc.name} gc, $ltoName lto)") {
      config.logger.running(compile)
      Process(compile, config.workdir.toFile) ! Logger.toProcessLogger(
        config.logger)
    }
    outpath
  }

  private def lto(config: Config): Option[String] =
    (config.mode, config.LTO) match {
      case (Mode.Debug, _)             => None
      case (_: Mode.Release, LTO.None) => None
      case (_: Mode.Release, lto)      => Some(lto.name)
    }

  private def flto(config: Config): Seq[String] =
    lto(config).fold[Seq[String]] {
      Seq()
    } { name => Seq(s"-flto=$name") }

  private def target(config: Config): Seq[String] =
    config.compilerConfig.targetTriple match {
      case Some(tt) => Seq("-target", tt)
      case None     => Seq("-Wno-override-module")
    }
}
