package scala.scalanative
package build

import java.nio.file.{Path, Files}

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
  def build(config: Config, outpath: Path): Path = config.logger.time("Total") {
    val entries = ScalaNative.entries(config)
    val linked  = ScalaNative.link(config, entries)

    nir.Show.dump(linked.defns, "linked.hnir")
    ScalaNative.check(config, linked)

    if (linked.unavailable.nonEmpty) {
      linked.unavailable.map(_.show).sorted.foreach { signature =>
        config.logger.error(s"cannot link: $signature")
      }
      throw new BuildException("unable to link")
    }
    val classCount = linked.defns.count {
      case _: nir.Defn.Class | _: nir.Defn.Module => true
      case _                                      => false
    }
    val methodCount = linked.defns.count(_.isInstanceOf[nir.Defn.Define])
    config.logger.info(
      s"Discovered ${classCount} classes and ${methodCount} methods")

    val optimized = ScalaNative.optimize(config, linked)
    nir.Show.dump(optimized.defns, "optimized.hnir")
    ScalaNative.check(config, optimized)

    IO.getAll(config.workdir, "glob:**.ll").foreach(Files.delete)
    ScalaNative.codegen(config, optimized)
    val generated = IO.getAll(config.workdir, "glob:**.ll")

    val unpackedLib = LLVM.unpackNativelib(config.nativelib, config.workdir)
    val objectFiles = config.logger.time("Compiling to native code") {
      val nativelibConfig =
        config.withCompileOptions("-O2" +: config.compileOptions)
      LLVM.compileNativelib(nativelibConfig, linked, unpackedLib)
      LLVM.compile(config, generated)
    }

    LLVM.link(config, linked, objectFiles, unpackedLib, outpath)
  }
}
