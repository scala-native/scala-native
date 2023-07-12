package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scala.scalanative.util.Scope
import scala.util.Try
import java.nio.file.FileVisitOption
import java.util.Optional
import java.nio.file.attribute.FileTime
import scala.concurrent._
import scala.util.{Success, Properties}
import scala.collection.immutable
import ScalaNative._

/** Utility methods for building code using Scala Native. */
object Build {

  private var prevBuildInputCheckSum: Int = 0

  /** Run the complete Scala Native pipeline, LLVM optimizer and system linker,
   *  producing a native binary in the end, same as `build` method.
   *
   *  This method skips the whole build and link process if the input hasn't
   *  changed from the previous build, and the previous build artifact is
   *  available at Config#artifactPath.
   *
   *  @param config
   *    The configuration of the toolchain.
   *  @return
   *    [[Config#artifactPath]], the path to the resulting native binary.
   */
  def buildCached(
      config: Config
  )(implicit scope: Scope, ec: ExecutionContext): Future[Path] = {
    val inputHash = checkSum(config)
    if (Files.exists(config.artifactPath) &&
        prevBuildInputCheckSum == inputHash) {
      config.logger.info(
        "Build skipped: No changes detected in build configuration and class path contents since last build."
      )
      Future.successful(config.artifactPath)
    } else {
      build(config).andThen {
        case Success(_) =>
          // Need to re-calculate the checksum because the content of `output` have changed.
          prevBuildInputCheckSum = checkSum(config)
      }
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
  def build(
      config: Config
  )(implicit scope: Scope, ec: ExecutionContext): Future[Path] = {
    val initialConfig = config
    import config.logger
    logger.timeAsync("Total") {
      // called each time for clean or directory removal
      checkWorkdirExists(initialConfig)

      // validate Config
      val config = Validator.validate(initialConfig)
      config.logger.debug(config.toString())

      link(config, entries(config))
        .flatMap(optimize(config, _))
        .flatMap { linkerResult =>
          val backend = new BackendPipeline(config, linkerResult)
          backend
            .codegen()
            .flatMap(backend.compile)
            .map(backend.link)
        }
    }
  }

  private class BackendPipeline(config: Config, linkerResult: linker.Result) {
    private val logger = config.logger
    import logger._

    def codegen()(implicit ec: ExecutionContext): Future[Seq[Path]] = {
      val tasks = immutable.Seq(
        ScalaNative.codegen(config, linkerResult),
        genBuildInfo(config)
      )
      Future.reduceLeft(tasks)(_ ++ _)
    }

    def compile(
        generatedIR: Seq[Path]
    )(implicit ec: ExecutionContext): Future[Seq[Path]] =
      timeAsync("Compiling to native code") {
        // compile generated LLVM IR
        val compileGeneratedIR = LLVM.compile(config, generatedIR)

        /* Used to pass alternative paths of compiled native (lib) sources,
         * eg: reused native sources used in partests.
         */
        val compileNativeLibs = {
          Properties.propOrNone("scalanative.build.paths.libobj") match {
            case None =>
              /* Finds all the libraries on the classpath that contain native
               * code and then compiles them.
               */
              findAndCompileNativeLibs(config, linkerResult)
            case Some(libObjectPaths) =>
              Future.successful {
                libObjectPaths
                  .split(java.io.File.pathSeparatorChar)
                  .toSeq
                  .map(Paths.get(_))
              }
          }
        }

        Future.reduceLeft(
          immutable.Seq(compileGeneratedIR, compileNativeLibs)
        )(_ ++ _)
      }

    def link(compiled: Seq[Path]): Path = time(
      s"Linking native code (${config.gc.name} gc, ${config.LTO.name} lto)"
    ) {
      LLVM.link(config, linkerResult, compiled)
    }
  }

  private def checkSum(config: Config): Int = {
    // skip the whole nativeLink process if the followings are unchanged since the previous build
    // - build configuration
    // - class paths' mtime
    // - the output native binary ('s mtime)
    // Since the NIR code is shipped in jars, we should be able to detect the changes in NIRs.
    // One thing we miss is, we cannot detect changes in c libraries somewhere in `/usr/lib`.
    (
      config,
      config.classPath.map(getLatestMtime(_)),
      getLastModified(config.artifactPath)
    ).hashCode()
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
  )(implicit ec: ExecutionContext): Future[Seq[Path]] = {
    import NativeLib.{findNativeLibs, compileNativeLibrary}
    Future
      .traverse(findNativeLibs(config))(
        compileNativeLibrary(config, linkerResult, _)
      )
      .map(_.flatten)
  }

  // create workDir if it doesn't exist
  private def checkWorkdirExists(config: Config): Unit = {
    val workDir = config.workDir
    if (Files.notExists(workDir)) {
      Files.createDirectories(workDir)
    }
  }

  private def getLastModified(path: Path): FileTime =
    if (Files.exists(path))
      Try(Files.getLastModifiedTime(path)).getOrElse(FileTime.fromMillis(0L))
    else FileTime.fromMillis(0L)

  /** Get the latest last modified time under the given path.
   */
  private def getLatestMtime(path: Path): Optional[FileTime] =
    if (Files.exists(path))
      Files
        .walk(path, FileVisitOption.FOLLOW_LINKS)
        .map[FileTime](getLastModified(_))
        .max(_.compareTo(_))
    else Optional.empty()

}
