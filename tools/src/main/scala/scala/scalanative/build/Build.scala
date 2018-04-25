package scala.scalanative
package build

import java.nio.file.Path

/** Utility methods for building code using Scala Native. */
object Build {

  /** Run the complete Scala Native pipeline,
   *  LLVM optimizer and system linker, producing
   *  a native binary in the end.
   *
   *  For example, to produce a binary one needs
   *  a classpath, a working directory and a main
   *  class entry point:
   *
   *  {{{
   *  val classpath: Seq[Path] = ...
   *  val workdir: Path        = ...
   *  val main: String         = ...
   *
   *  val clang     = Discover.clang()
   *  val clangpp   = Discover.clangpp()
   *  val linkopts  = Discover.linkingOptions()
   *  val compopts  = Discover.compileOptions()
   *  val triple    = Discover.targetTriple(clang, workdir)
   *  val nativelib = Discover.nativelib(classpath).get
   *  val outpath   = workdir.resolve("out")
   *
   *  val config =
   *    Config.empty
   *      .withGC(GC.default)
   *      .withMode(Mode.default)
   *      .withClang(clang)
   *      .withClangPP(clangpp)
   *      .withLinkingOptions(linkopts)
   *      .withCompileOptions(compopts)
   *      .withTargetTriple(triple)
   *      .withNativelib(nativelib)
   *      .withMainClass(main)
   *      .withClassPath(classpath)
   *      .withLinkStubs(true)
   *      .withWorkdir(workdir)
   *
   *  Build.build(config, outpath)
   *  }}}
   *
   *  @param config  The configuration of the toolchain.
   *  @param outpath The path to the resulting native binary.
   *  @return `outpath`, the path to the resulting native binary.
   */
  def build(config: Config, outpath: Path): Path = {
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
