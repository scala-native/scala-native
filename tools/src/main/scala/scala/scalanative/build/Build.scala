package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scala.scalanative.util.Scope
import scala.scalanative.build.core.Filter
import scala.scalanative.build.core.NativeLib
import scala.scalanative.build.core.ScalaNative
import scala.util.Try

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

      if (canSkipBuild(config)) {
        config.logger.debug(
          "Skipping Scala Native build - inputs and configuration have not changed."
        )
        config.artifactPath
      } else {
        // validate classpath - use fconfig below
        val fconfig = {
          val fclasspath = NativeLib.filterClasspath(config.classPath)
          config.withClassPath(fclasspath)
        }
        buildProject(fconfig)
      }
    }

  private def buildProject(config: Config)(implicit scope: Scope): Path = {
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
      ScalaNative.codegen(config, optimized)
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

  /** Check if build can be skipped. This function returns true only when inputs
   *  of the build has not changed since last compilation. It means that all
   *  files on the classpath were not modified after building last artifact and
   *  the cached checksum of config is equal to the checksum of current config.
   */
  private def canSkipBuild(config: Config): Boolean = {
    val artifactExists = Files.exists(config.artifactPath)

    def classpathWasModified = {
      val artifactMT = Files.getLastModifiedTime(config.artifactPath)
      config.classPath.exists {
        Files.getLastModifiedTime(_).compareTo(artifactMT) > 0
      }
    }

    val workdir = io.VirtualDirectory.local(config.workdir)
    val configCheckSumFile = Paths.get("config_hash")
    val configChecksum = config.checksum
    val configChanged = workdir.contains(configCheckSumFile) &&
      Try {
        val content = new String(workdir.read(configCheckSumFile).array())
        content.toLong != configChecksum
      }.getOrElse(true)

    val hasChanged = !artifactExists || configChanged || classpathWasModified
    if (hasChanged && configChanged) Try {
      workdir.write(configCheckSumFile)(_.write(configChecksum.toString()))
    }
    !hasChanged
  }
}
