package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable
import scala.scalanative.util.Scope

/** Utility methods for building code using Scala Native. */
object Build {

  // holds project basename and the related Config
  private val cache = mutable.HashMap.empty[String, Config]

  /** Reuse config from cache or cache the config after running any needed
   *  discovery and then validation. This avoids the costs on subsequent builds.
   *  Reloading or restarting the build will clear the cache as this class will
   *  be reloaded. This is required to change either settings or environment
   *  variables that change the build.
   *
   *  Note: Runtime environment variables that control the GC can be changed
   *  between runs and will be applied.
   *
   *  @param config
   *    starting config from build
   *  @return
   *    config with discovery if needed and validation
   */
  private def checkCache(config: Config): Config = {
    // Requires that defaultBasename or basename is set
    // and unique across sub-projects in the same project
    val name = config.artifactName
    // always use a fresh logger
    cache.get(name) match {
      case Some(value) =>
        value.withLogger(config.logger)
      case None =>
        val vconfig = Validator.validate(config)
        cache.put(name, vconfig)
        vconfig
    }
  }

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
   *        .withMultithreadingSupport(enabled = false)
   *        .withClang(clang)
   *        .withClangPP(clangpp)
   *        .withLinkingOptions(linkopts)
   *        .withCompileOptions(compopts)
   *        .withLinkStubs(true)
   *      }
   *      .withMainClass(main)
   *      .withClassPath(classpath)
   *      .withBaseDir(basedir)
   *      .withModuleName(moduleName)
   *      .withTestConfig(false)
   *      .withLogger(logger)
   *
   *  Build.build(config)
   *  }}}
   *
   *  @param config
   *    The configuration of the toolchain.
   *  @return
   *    [[Config#artifactPath]], the path to the resulting native binary.
   */
  def build(config: Config)(implicit scope: Scope): Path =
    config.logger.time("Total") {
      val cconfig = config.logger.time("Cache config, discover and validate") {
        val cconfig = checkCache(config)
        // called each time for clean or directory removal
        checkWorkdirExists(cconfig)
        cconfig
      }
      buildImpl(cconfig)
    }

  private def buildImpl(config: Config)(implicit scope: Scope): Path = {
    // find and link
    val linked = {
      val entries = ScalaNative.entries(config)
      val linked = ScalaNative.link(config, entries)
      ScalaNative.logLinked(config, linked)
      linked
    }

    // optimize and generate ll
    val generated = {
      val optimized = ScalaNative.optimize(config, linked)
      ScalaNative.codegen(config, optimized) ++:
        ScalaNative.genBuildInfo(config) // ident list may be empty
    }

    val objectPaths = config.logger.time("Compiling to native code") {
      // compile generated LLVM IR
      val llObjectPaths = LLVM.compile(config, generated)

      /* Used to pass alternative paths of compiled native (lib) sources,
       * eg: reused native sources used in partests.
       */
      val libObjectPaths = scala.util.Properties
        .propOrNone("scalanative.build.paths.libobj") match {
        case None =>
          /* Finds all the libraries on the classpath that contain native
           * code and then compiles them.
           */
          findAndCompileNativeLibs(config, linked)
        case Some(libObjectPaths) =>
          libObjectPaths
            .split(java.io.File.pathSeparatorChar)
            .toSeq
            .map(Paths.get(_))
      }

      libObjectPaths ++ llObjectPaths
    }

    // finally link
    config.logger.time(
      s"Linking native code (${config.gc.name} gc, ${config.LTO.name} lto)"
    ) {
      LLVM.link(config, linked, objectPaths)
    }
  }

  /** Convenience method to combine finding and compiling native libaries.
   *
   *  @param config
   *    the compiler configuration
   *  @param linkerResult
   *    the result from the linker
   *  @return
   *    a sequence of the object file paths
   */
  def findAndCompileNativeLibs(
      config: Config,
      linkerResult: linker.Result
  ): Seq[Path] = {
    NativeLib
      .findNativeLibs(config)
      .flatMap(nativeLib =>
        NativeLib.compileNativeLibrary(config, linkerResult, nativeLib)
      )
  }

  // create workDir if it doesn't exist
  private def checkWorkdirExists(config: Config): Unit = {
    val workDir = config.workDir
    if (Files.notExists(workDir)) {
      Files.createDirectories(workDir)
    }
  }

}
