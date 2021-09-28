package scala.scalanative
package build

import java.nio.file.{Path, Paths}
import scala.scalanative.util.Scope

/** Utility methods for building code using Scala Native. */
object Build {

  /** Run the complete Scala Native pipeline, LLVM optimizer and system linker,
   *  producing a native binary in the end.
   *
   *  For example, to produce a binary one needs a classpath, a working
   *  directory and a main class entry point:
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
   *
   *  val outpath  = workdir.resolve("out")
   *
   *  val config =
   *    Config.empty
   *      .withCompilerConfig{
   *        NativeConfig.empty
   *        .withGC(GC.default)
   *        .withMode(Mode.default)
   *        .withClang(clang)
   *        .withClangPP(clangpp)
   *        .withLinkingOptions(linkopts)
   *        .withCompileOptions(compopts)
   *        .withLinkStubs(true)
   *      }
   *      .withMainClass(main)
   *      .withClassPath(classpath)
   *      .withWorkdir(workdir)
   *
   *  Build.build(config, outpath)
   *  }}}
   *
   *  @param config
   *    The configuration of the toolchain.
   *  @param outpath
   *    The path to the resulting native binary.
   *  @return
   *    `outpath`, the path to the resulting native binary.
   */
  def build(config: Config, outpath: Path)(implicit scope: Scope): Path =
    config.logger.time("Total") {
      val fclasspath = NativeLib.filterClasspath(config.classPath)
      val fconfig = config.withClassPath(fclasspath)

      // create optimized code and generate ll
      val entries = ScalaNative.entries(fconfig)
      val linked = ScalaNative.link(fconfig, entries)
      ScalaNative.logLinked(fconfig, linked)
      val optimized = ScalaNative.optimize(fconfig, linked)
      val generated = ScalaNative.codegen(fconfig, optimized)

      val compilationOutput = config.logger.time("Compiling to native code") {
        // compile generated LLVM IR
        val llCompilationOutput = LLVM.compile(fconfig, generated)

        /* Used to pass alternative paths of compiled native (lib) sources,
         * eg: reused native sources used in partests.
         */
        val libObjectOutput = scala.util.Properties
          .propOrNone("scalanative.build.paths.libobj") match {
          case None =>
            findAndCompileNativeSources(fconfig, linked)
          case Some(libObjectPaths) =>
            libObjectPaths
              .split(java.io.File.pathSeparatorChar)
              .toSeq
              .map(Paths.get(_))
              .map(ObjectFile)
        }

        libObjectOutput ++ llCompilationOutput
      }

      LLVM.link(config, linked, compilationOutput, outpath)
    }

  def findAndCompileNativeSources(
      config: Config,
      linkerResult: linker.Result
  ): Seq[CompilationResult] = {
    import NativeLib._
    findNativeLibs(config.classPath, config.workdir)
      .map(unpackNativeCode)
      .flatMap { destPath =>
        val paths = findNativePaths(config.workdir, destPath)
        val (projPaths, projConfig) =
          Filter.filterNativelib(config, linkerResult, destPath, paths)
        LLVM.compile(projConfig, projPaths)
      }
  }
}
