package scala.scalanative
package build

import java.nio.file.Path

object Build {
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
    val driver       = optimizer.Driver.default(config.mode)
    val linkerResult = ScalaNative.link(config, driver)

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
      ScalaNative.optimize(config, driver, linkerResult.defns, linkerResult.dyns)
    val generated = {
      ScalaNative.codegen(config, optimized)
      IO.getAll(config.workdir, "glob:**.ll")
    }
    val objectFiles = LLVM.compile(config, generated)
    val unpackedLib = LLVM.unpackNativelib(config.nativelib, config.workdir)

    val nativelibConfig =
      config.withCompileOptions("-O2" +: config.compileOptions)
    val _ =
      LLVM.compileNativelib(nativelibConfig, linkerResult, unpackedLib)

    LLVM.link(config, linkerResult, objectFiles, unpackedLib, outpath)
  }
}
