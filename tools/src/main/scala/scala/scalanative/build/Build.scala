package scala.scalanative
package build

import java.nio.file.Path

object Build {

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
      ScalaNative.optimize(config,
                           driver,
                           linkerResult.defns,
                           linkerResult.dyns)
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
