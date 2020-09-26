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
   *  val clang    = Discover.clang()
   *  val clangpp  = Discover.clangpp()
   *  val linkopts = Discover.linkingOptions()
   *  val compopts = Discover.compileOptions()
   *  val triple   = Discover.targetTriple(clang, workdir)
   *  val outpath  = workdir.resolve("out")
   *
   *  val config =
   *    Config.empty
   *      .withCompilerConfig{
   *        NativeConfig.empty
   *         .withGC(GC.default)
   *         .withMode(Mode.default)
   *         .withClang(clang)
   *         .withClangPP(clangpp)
   *         .withLinkingOptions(linkopts)
   *         .withCompileOptions(compopts)
   *         .withLinkStubs(true)
   *       }
   *      .withTargetTriple(triple)
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
    val workdir = config.workdir
    val entries = ScalaNative.entries(config)
    val linked  = ScalaNative.link(config, entries)
    ScalaNative.logLinked(config, linked)
    val optimized = ScalaNative.optimize(config, linked)

    IO.getAll(workdir, "glob:**.ll").foreach(Files.delete)
    ScalaNative.codegen(config, optimized)
    val generated = IO.getAll(workdir, "glob:**.ll")

    val nativelibs   = NativeLib.findNativeLibs(config.classPath, workdir)
    val nativelib    = NativeLib.findNativeLib(nativelibs)
    val unpackedLibs = nativelibs.map(LLVM.unpackNativeCode(_))

    val msg = s"Compiling to native code (${config.targetTriple})"
    val objectFiles = config.logger.time(msg) {
      val nativelibConfig =
        config.withCompilerConfig(
          _.withCompileOptions("-O2" +: config.compileOptions))
      LLVM.compileNativelibs(nativelibConfig, linked, unpackedLibs, nativelib)
      LLVM.compile(config, generated)
    }

    LLVM.link(config, linked, objectFiles, unpackedLibs, outpath)
  }
}
