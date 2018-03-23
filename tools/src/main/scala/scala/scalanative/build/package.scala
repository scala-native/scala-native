package scala.scalanative

import java.nio.file.{Files, Path, Paths}
import java.util.Arrays

import scala.sys.process.Process

import build.IO.RichPath
import nir.Global

package object build {
  private[scalanative] type LinkerPath = linker.ClassPath
  private[scalanative] val LinkerPath = linker.ClassPath
  private[scalanative] type LinkerReporter = linker.Reporter
  private[scalanative] val LinkerReporter = linker.Reporter
  private[scalanative] type LinkerResult = linker.Result
  private[scalanative] val LinkerResult = linker.Result
  private[scalanative] type OptimizerDriver = optimizer.Driver
  private[scalanative] val OptimizerDriver = optimizer.Driver
  private[scalanative] type OptimizerReporter = optimizer.Reporter
  private[scalanative] val OptimizerReporter = optimizer.Reporter

  /**
   * Run the complete Scala Native toolchain, from NIR files to native binary.
   *
   * The paths to `clang`, `clangpp` and the target triple will be detected automatically.
   *
   * This is the entry point to use if you're writing a tool that uses the Scala Native
   * toolchain.
   *
   * The `nativelib` is the path to the JAR file distributed with Scala Native. It contains the
   * classfiles, NIR files, and C and C++ sources of the Scala Native `nativelib` module. It will
   * be unarchived and compiled if needed.
   *
   * The `paths` is a sequence of directories that contain NIR files. This is typically the same
   * as your compilation classpath.
   *
   * The `entry` is the name of your application's entry point. For instance, if you define an
   * `object EntryPoint` in package `foo.bar`, then your entry point is `foo.bar.EntryPoint$`.
   *
   * The `outpath` is the path where the Scala Native toolchain should write the resulting native
   * binary.
   *
   * The `workdir` is directory that will be used during compilation to host intermediate
   * results. Reusing the same `workdir` several times is fine and is recommended, so that the
   * nativelib doesn't need to be set up on every call to this method.
   *
   * The `logger` will be called during compilation to pass messages about the state of the Scala
   * Native toolchain.
   *
   * @param nativelib Path to the nativelib jar.
   * @param paths     Sequence of all NIR locations.
   * @param entry     Entry point for linking.
   * @param outpath   The path to the resulting native binary.
   * @param workdir   Directory to emit intermediate compilation results.
   * @param logger    The logger used by the toolchain.
   * @return `outpath`, the path to the resulting native binary.
   */
  def build(nativelib: Path,
            paths: Seq[Path],
            entry: String,
            outpath: Path,
            workdir: Path,
            logger: Logger): Path = {
    val config = Config.default(nativelib, paths, entry, workdir, logger)
    build(config, outpath)
  }

  /**
   * Run the complete Scala Native toolchain, from NIR files to native binary.
   *
   * @param config  The configuration of the toolchain.
   * @param outpath The path to the resulting native binary.
   * @return `outpath`, the path to the resulting native binary.
   */
  def build(config: Config, outpath: Path) = {
    val driver       = OptimizerDriver.default(config.mode)
    val linkerResult = link(config, driver)

    if (linkerResult.unresolved.nonEmpty) {
      linkerResult.unresolved.map(_.show).sorted.foreach { signature =>
        config.logger.error(s"cannot link: $signature")
      }
      throw new BuildException("unable to link")
    }
    val classCount = linkerResult.defns.count {
      case _: nir.Defn.Class | _: nir.Defn.Module | _: nir.Defn.Trait => true
      case _                                                          => false
    }
    val methodCount = linkerResult.defns.count(_.isInstanceOf[nir.Defn.Define])
    config.logger.info(
      s"Discovered ${classCount} classes and ${methodCount} methods")

    val optimized =
      optimize(config, driver, linkerResult.defns, linkerResult.dyns)
    val generated = {
      codegen(config, optimized)
      IO.getAll(config.workdir, "glob:**.ll")
    }
    val objectFiles = LLVM.compileLL(config, generated)
    val unpackedLib = unpackNativeLibrary(config.nativelib, config.workdir)

    val nativelibConfig =
      config.withCompileOptions("-O2" +: config.compileOptions)
    val _ =
      compileNativeLib(nativelibConfig, linkerResult, unpackedLib)

    LLVM.linkLL(config, linkerResult, objectFiles, unpackedLib, outpath)
  }

  /** Given the classpath and main entry point, link under closed-world
   *  assumption.
   */
  private[scalanative] def link(config: Config,
                                driver: OptimizerDriver): LinkerResult = {
    config.logger.time("Linking") {
      val chaDeps   = optimizer.analysis.ClassHierarchy.depends
      val passes    = driver.passes
      val passDeps  = passes.flatMap(_.depends).distinct
      val deps      = (chaDeps ++ passDeps).distinct
      val injects   = passes.flatMap(_.injects)
      val mainClass = nir.Global.Top(config.entry)
      val entry =
        nir.Global
          .Member(mainClass, "main_scala.scalanative.runtime.ObjectArray_unit")
      val result =
        (linker.Linker(config, driver.linkerReporter)).link(entry +: deps)

      result.withDefns(result.defns ++ injects)
    }
  }

  /** Link just the given entries, disregarding the extra ones that are
   *  needed for the optimizer and/or codegen.
   */
  private[scalanative] def linkRaw(config: Config,
                                   reporter: LinkerReporter,
                                   entries: Seq[nir.Global]): LinkerResult =
    config.logger.time("Linking") {
      linker.Linker(config, reporter).link(entries)
    }

  /** Transform high-level closed world to its lower-level counterpart. */
  private[scalanative] def optimize(config: Config,
                                    driver: OptimizerDriver,
                                    assembly: Seq[nir.Defn],
                                    dyns: Seq[String]): Seq[nir.Defn] =
    config.logger.time(s"Optimizing (${config.mode} mode)") {
      optimizer.Optimizer(config, driver, assembly, dyns)
    }

  /** Given low-level assembly, emit LLVM IR for it to the buildDirectory. */
  private[scalanative] def codegen(config: Config,
                                   assembly: Seq[nir.Defn]): Seq[Path] = {
    config.logger.time("Generating intermediate code") {
      scalanative.codegen.CodeGen(config, assembly)
    }
    val produced = IO.getAll(config.workdir, "glob:**.ll")
    config.logger.info(s"Produced ${produced.length} files")
    produced
  }

  /**
   * Unpack the `nativelib` to `workdir/lib`.
   *
   * If the same archive has already been unpacked to this location, this
   * call has no effects.
   *
   * @param nativelib The JAR to unpack.
   * @param workdir   The working directory. The nativelib will be unpacked
   *                  to `workdir/lib`.
   * @return The location where the nativelib has been unpacked, `workdir/lib`.
   */
  private[scalanative] def unpackNativeLibrary(nativelib: Path,
                                               workdir: Path): Path = {
    val lib         = workdir.resolve("lib")
    val jarhash     = IO.sha1(nativelib)
    val jarhashPath = lib.resolve("jarhash")
    def unpacked =
      Files.exists(lib) &&
        Files.exists(jarhashPath) &&
        Arrays.equals(jarhash, Files.readAllBytes(jarhashPath))

    if (!unpacked) {
      IO.deleteRecursive(lib)
      IO.unzip(nativelib, lib)
      IO.write(jarhashPath, jarhash)
    }

    lib
  }

  /**
   * Compile the native lib to `.o` files
   *
   * @param config       The configuration of the toolchain.
   * @param linkerResult The results from the linker.
   * @param libPath      The location where the `.o` files should be written.
   * @return `libPath`
   */
  private[scalanative] def compileNativeLib(config: Config,
                                            linkerResult: LinkerResult,
                                            libPath: Path): Path = {
    val cpaths   = IO.getAll(config.workdir, "glob:**.c").map(_.abs)
    val cpppaths = IO.getAll(config.workdir, "glob:**.cpp").map(_.abs)
    val paths    = cpaths ++ cpppaths

    // predicate to check if given file path shall be compiled
    // we only include sources of the current gc and exclude
    // all optional dependencies if they are not necessary
    val optPath = libPath.resolve("optional").abs
    val (gcPath, gcSelPath) = {
      val gcPath    = libPath.resolve("gc")
      val gcSelPath = gcPath.resolve(config.gc.name)
      (gcPath.abs, gcSelPath.abs)
    }

    def include(path: String) = {
      if (path.contains(optPath)) {
        val name = Paths.get(path).toFile.getName.split("\\.").head
        linkerResult.links.map(_.name).contains(name)
      } else if (path.contains(gcPath)) {
        path.contains(gcSelPath)
      } else {
        true
      }
    }

    // delete .o files for all excluded source files
    paths.foreach { path =>
      if (!include(path)) {
        val ofile = Paths.get(path + ".o")
        if (Files.exists(ofile)) {
          Files.delete(ofile)
        }
      }
    }

    // generate .o files for all included source files in parallel
    paths.par.foreach { path =>
      val opath = path + ".o"
      if (include(path) && !Files.exists(Paths.get(opath))) {
        val isCpp    = path.endsWith(".cpp")
        val compiler = if (isCpp) config.clangpp.abs else config.clang.abs
        val flags    = (if (isCpp) Seq("-std=c++11") else Seq()) ++ config.compileOptions
        val compilec = Seq(compiler) ++ flags ++ Seq("-c", path, "-o", opath)

        config.logger.running(compilec)
        val result = Process(compilec, config.workdir.toFile) ! Logger
          .toProcessLogger(config.logger)
        if (result != 0) {
          sys.error("Failed to compile native library runtime code.")
        }
      }
    }

    libPath
  }

}
