package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scala.scalanative.util.Scope
import scala.util.Try
import java.security.MessageDigest
import java.nio.file.FileVisitOption
import scala.collection.JavaConverters._

/** Utility methods for building code using Scala Native. */
object Build {

  private var previousBuildInputHash: Int = 0

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
  def build(config: Config)(implicit scope: Scope): Path = {
    def makeKey(config: Config): Int = {
      // skip the whole nativeLink process if the followings are unchanged since the previous build
      // - build configuration
      // - class paths' mtime
      // - the output native binary ('s mtime)
      // Since the NIR code is shipped in jars, we should be able to detect the changes in NIRs.
      // One thing we miss is, we cannot detect changes in c libraries somewhere in `/usr/lib`.
      val key = (
        config.toString, // we use toString because `config` object contains Logger instance that changes for every invocation
        config.classPath.map(getNewestMtime(_)),
        getLastModifiedTimeMillis(config.artifactPath)
      )
      key.hashCode()
    }
    config.logger.time("Total") {
      // called each time for clean or directory removal
      checkWorkdirExists(config)

      // validate Config
      val fconfig = Validator.validate(config)
      val inputHash = makeKey(fconfig)

      if (Files.exists(fconfig.artifactPath) &&
          previousBuildInputHash == inputHash) {
        fconfig.logger.info(
          "Build skipped: No changes detected in build configuration and class path contents since last build."
        )
        fconfig.artifactPath
      } else {
        // find and link
        val linked = {
          val entries = ScalaNative.entries(fconfig)
          val linked = ScalaNative.link(fconfig, entries)
          ScalaNative.logLinked(fconfig, linked)
          linked
        }

        // optimize and generate ll
        val generated = {
          val optimized = ScalaNative.optimize(config, linked)
          ScalaNative.codegen(config, optimized) ++:
            ScalaNative.genBuildInfo(fconfig) // ident list may be empty
        }

        val objectPaths = fconfig.logger.time("Compiling to native code") {
          // compile generated LLVM IR
          val llObjectPaths = LLVM.compile(fconfig, generated)

          /* Used to pass alternative paths of compiled native (lib) sources,
           * eg: reused native sources used in partests.
           */
          val libObjectPaths = scala.util.Properties
            .propOrNone("scalanative.build.paths.libobj") match {
            case None =>
              /* Finds all the libraries on the classpath that contain native
               * code and then compiles them.
               */
              findAndCompileNativeLibs(fconfig, linked)
            case Some(libObjectPaths) =>
              libObjectPaths
                .split(java.io.File.pathSeparatorChar)
                .toSeq
                .map(Paths.get(_))
          }

          libObjectPaths ++ llObjectPaths
        }

        // finally link
        val output = fconfig.logger.time(
          s"Linking native code (${fconfig.gc.name} gc, ${fconfig.LTO.name} lto)"
        ) {
          LLVM.link(fconfig, linked, objectPaths)
        }
        previousBuildInputHash = makeKey(fconfig)
        output
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

  private def getLastModifiedTimeMillis(path: Path): Long =
    if (Files.exists(path)) Files.getLastModifiedTime(path).toMillis()
    else 0L

  /** Get the newest file's last modified time in millis under the given path.
   */
  private def getNewestMtime(path: Path): Long =
    Files
      .walk(path, FileVisitOption.FOLLOW_LINKS)
      .iterator()
      .asScala
      .map(p => Files.getLastModifiedTime(p).toMillis())
      .max

}
