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
   * Filter the `nativelib` source files with special logic
   * to select GC and optional components.
   *
   * @param config The configuration of the toolchain.
   * @param linkerResult The results from the linker.
   * @param destPath The unpacked location of the Scala Native nativelib.
   * @return The paths included to be compiled.
   */
  def filterNativelib(config: Config,
                      linkerResult: linker.Result,
                      destPath: Path): Seq[Path] = {
    val paths   = NativeLib.findNativePaths(config.workdir, destPath).map(_.abs)
    val libPath = destPath.resolve(NativeLib.codeDir)

    // predicate to check if given file path shall be compiled
    // we only include sources of the current gc and exclude
    // all optional dependencies if they are not necessary
    val optPath = libPath.resolve("optional").abs
    val (gcPath, gcIncludePaths, gcSelectedPaths) = {
      val gcPath         = libPath.resolve("gc")
      val gcIncludePaths = config.gc.include.map(gcPath.resolve(_).abs)
      val selectedGC     = gcPath.resolve(config.gc.name).abs
      val selectedGCPath = selectedGC +: gcIncludePaths
      (gcPath.abs, gcIncludePaths, selectedGCPath)
    }

    def include(path: String) = {
      if (path.contains(optPath)) {
        val name = Paths.get(path).toFile.getName.split("\\.").head
        linkerResult.links.map(_.name).contains(name)
      } else if (path.contains(gcPath)) {
        gcSelectedPaths.exists(path.contains)
      } else {
        true
      }
    }

    val (includePaths, excludePaths) = paths.partition(include(_))

    // delete .o files for all excluded source files
    // avoids deleting .o files except when changing
    // optional or garbage collectors
    excludePaths.foreach { path =>
      val opath = Paths.get(path + oExt)
      if (Files.exists(opath)) {
        Files.delete(opath)
      }
    }

    val fltoOpt    = flto(config)
    val targetOpt  = target(config)
    val includeOpt = gcIncludePaths.map("-I" + _)
    includePaths.map(Paths.get(_))
  }

  /**
   * Compile the given files to object files
   *
   * @param config The configuration of the toolchain.
   * @param paths The directory paths containing native files to compile.
   * @return The paths of the `.o` files.
   */
  def compile(config: Config, paths: Seq[Path]): Seq[Path] = {
    // generate .o files for all included source files in parallel
    paths.par.map { path =>
      val inpath  = path.abs
      val outpath = inpath + oExt
      val isCpp   = inpath.endsWith(cppExt)
      val isLl    = inpath.endsWith(llExt)
      val objPath = Paths.get(outpath)
      // scripted tests fail if LL not rebuilt
      if (isLl || !Files.exists(objPath)) {
        val compiler = if (isCpp) config.clangPP.abs else config.clang.abs
        val stdflag =
          if (isLl) "" else if (isCpp) "-std=c++11" else "-std=gnu11"
        val flags = opt(config) +: stdflag +: "-fvisibility=hidden" +:
          config.compileOptions
        val compilec =
          Seq(compiler) ++ flto(config) ++ flags ++ target(config) ++ includeOpt ++
            Seq("-c", path, "-o", opath)

        config.logger.running(compilec)
        val result = Process(compilec, config.workdir.toFile) ! Logger
          .toProcessLogger(config.logger)
        if (result != 0) {
          sys.error("Failed to compile ll or native code.")
        }
      }
      objPath
    }.seq
  }

  /**
   * Links a collection of `.ll.o` files and the `.o` files
   * from the `nativelib`, other libaries, and the
   * application project into the native binary.
   *
   * @param config The configuration of the toolchain.
   * @param linkerResult The results from the linker.
   * @param objectPaths The paths to all the `.o` files.
   * @param outpath The path where to write the resulting binary.
   * @return `outpath`
   */
  def link(config: Config,
           linkerResult: linker.Result,
           objectsPaths: Seq[Path],
           outpath: Path): Path = {
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

  private def opt(config: Config): String =
    config.mode match {
      case Mode.Debug       => "-O0"
      case Mode.ReleaseFast => "-O2"
      case Mode.ReleaseFull => "-O3"
    }
}
