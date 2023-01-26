package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scala.scalanative.util.Scope
import scala.collection.mutable
import scala.util.Try

/** Utility methods for building code using Scala Native. */
object Build {

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
    val name = config.basename
    println(s"Check cache: $name")
    // always use a fresh logger
    cache.get(name) match {
      case Some(value) =>
        value.withLogger(config.logger)
      case None =>
        println("No config in cache")
        val vconfig = Validator.validate(Discover.discover(config))
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
  def build(config: Config)(implicit scope: Scope): Path = {
    config.logger.time("Total") {
      val cconfig = config.logger.time("Cache config, discover and validate") {
        val cconfig = checkCache(config)
        // called each time for clean or directory removal
        checkWorkdirExists(cconfig)
        cconfig
      }

      // find and link
      val linked = {
        val entries = ScalaNative.entries(cconfig)
        val linked = ScalaNative.link(cconfig, entries)
        ScalaNative.logLinked(cconfig, linked)
        linked
      }

      // optimize and generate ll
      val generated = {
        val optimized = ScalaNative.optimize(cconfig, linked)
        ScalaNative.codegen(cconfig, optimized)
      }

      val objectPaths = config.logger.time("Compiling to native code") {
        // compile generated LLVM IR
        val llObjectPaths = LLVM.compile(cconfig, generated)

        /* Used to pass alternative paths of compiled native (lib) sources,
         * eg: reused native sources used in partests.
         */
        val libObjectPaths = scala.util.Properties
          .propOrNone("scalanative.build.paths.libobj") match {
          case None =>
            /* Finds all the libraries on the classpath that contain native
             * code and then compiles them.
             */
            findAndCompileNativeLibs(cconfig, linked)
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
        s"Linking native code (${cconfig.gc.name} gc, ${cconfig.LTO.name} lto)"
      ) {
        LLVM.link(cconfig, linked, objectPaths)
      }
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
  private def findAndCompileNativeLibs(
      config: Config,
      linkerResult: linker.Result
  ): Seq[Path] = {
    NativeLib
      .findNativeLibs(config)
      .flatMap(nativeLib =>
        NativeLib.compileNativeLibrary(config, linkerResult, nativeLib)
      )
  }

  private def checkWorkdirExists(config: Config): Unit = {
    // create workdir if needed
    val workdir = config.workdir
    if (Files.notExists(workdir)) {
      Files.createDirectories(workdir)
    }
  }
}
