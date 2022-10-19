package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scala.scalanative.util.Scope
import scala.scalanative.build.core.Filter
import scala.scalanative.build.core.NativeLib
import scala.scalanative.build.core.ScalaNative

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
   *  val basedir: Path        = ...
   *  val main: String         = ...
   *  val logger: Logger       = ...
   *
   *  val clang    = Discover.clang()
   *  val clangpp  = Discover.clangpp()
   *  val linkopts = Discover.linkingOptions()
   *  val compopts = Discover.compileOptions()
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
   *        .withBasename("myapp")
   *      }
   *      .withMainClass(main)
   *      .withClassPath(classpath)
   *      .withBasedir(basedir)
   *      .withTestConfig(false)
   *      .withLogger(logger)
   *
   *  Build.build(config)
   *  }}}
   *
   *  @param config
   *    The configuration of the toolchain.
   *  @return
   *    `outpath`, the path to the resulting native binary.
   */
  def build(config: Config)(implicit scope: Scope): Path =
    config.logger.time("Total") {
      // create workdir if needed
      if (Files.notExists(config.workdir)) {
        Files.createDirectories(config.workdir)
      }
      // validate classpath
      val fconfig = {
        val fclasspath = NativeLib.filterClasspath(config.classPath)
        config.withClassPath(fclasspath)
      }

      // find and link
      val linked = {
        val entries = ScalaNative.entries(fconfig)
        val linked = ScalaNative.link(fconfig, entries)
        ScalaNative.logLinked(fconfig, linked)
        linked
      }

      implicit val incCompilationContext: IncCompilationContext =
        new IncCompilationContext(fconfig.workdir)
      if (config.compilerConfig.useIncrementalCompilation) {
        incCompilationContext.collectFromPreviousState()
      }

      // optimize and generate ll
      val generated = {
        val optimized = ScalaNative.optimize(fconfig, linked)
        ScalaNative.codegen(fconfig, optimized)
      }

      if (config.compilerConfig.useIncrementalCompilation) {
        incCompilationContext.dump()
      }

      val objectPaths = config.logger.time("Compiling to native code") {
        // compile generated LLVM IR
        val llObjectPaths = LLVM.compile(fconfig, generated)

        /* Used to pass alternative paths of compiled native (lib) sources,
         * eg: reused native sources used in partests.
         */
        val libObjectPaths = scala.util.Properties
          .propOrNone("scalanative.build.paths.libobj") match {
          case None =>
            findAndCompileNativeSources(fconfig, linked)
          case Some(libObjectPaths) =>
            libObjectPaths
              .split(java.io.File.pathSeparatorChar)
              .toSeq
              .map(Paths.get(_))
        }

        libObjectPaths ++ llObjectPaths
      }
      if (config.compilerConfig.useIncrementalCompilation) {
        incCompilationContext.clear()
      }
      LLVM.link(fconfig, linked, objectPaths)
    }

  def findAndCompileNativeSources(
      config: Config,
      linkerResult: linker.Result
  ): Seq[Path] = {
    import NativeLib._
    findNativeLibs(config.classPath, config.workdir)
      .map(unpackNativeCode)
      .flatMap { destPath =>
        val paths = findNativePaths(config.workdir, destPath)
        val (projPaths, projConfig) =
          Filter.filterNativelib(config, linkerResult, destPath, paths)
        implicit val incCompilationContext: IncCompilationContext =
          new IncCompilationContext(config.workdir)
        LLVM.compile(projConfig, projPaths)
      }
  }
}
