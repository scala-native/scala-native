package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scala.scalanative.util.Scope
import scala.scalanative.linker.ReachabilityAnalysis
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
    config.logger.timeAsync("Total") {
      // called each time for clean or directory removal
      checkWorkdirExists(config)

      val validatedConfig = Validator.validate(config)
      validatedConfig.logger.debug(config.toString())

      ScalaNative
        .link(validatedConfig, entries(validatedConfig))
        .flatMap(linkerResult => optimize(validatedConfig, linkerResult))
        .flatMap(linkerResult => compileBinary(validatedConfig, linkerResult))
    }
  }

  /** Compiles the deinitions in `linkerResult` into a native binary.
   *
   *  @param config
   *    The configuration of the toolchain, which has been validated.
   *  @param linkerResult
   *    The result of reachability analysis.
   *  @return
   *    [[Config#artifactPath]], the path to the resulting native binary.
   */
  private def compileBinary(
      config: Config,
      linkerResult: ReachabilityAnalysis.Result
  )(implicit ec: ExecutionContext): Future[Path] = {
    codegen(config, linkerResult)
      .flatMap(ir => compile(config, linkerResult, ir))
      .map(objects => link(config, linkerResult, objects))
      .map(artifact => postProcess(config, artifact))
  }

  /** Emits LLVM IR for the definitions in `analysis` to
   *  `config.buildDirectory`.
   */
  private def codegen(
      config: Config,
      analysis: ReachabilityAnalysis.Result
  )(implicit ec: ExecutionContext): Future[Seq[Path]] = {
    val tasks = immutable.Seq(
      ScalaNative.codegen(config, analysis),
      genBuildInfo(config)
    )
    Future.reduceLeft(tasks)(_ ++ _)
  }

  /** Compiles `llvmAssembly`, which is a sequence of LLVM IR files. */
  private def compile(
      config: Config,
      analysis: ReachabilityAnalysis.Result,
      llvmAssembly: Seq[Path]
  )(implicit ec: ExecutionContext): Future[Seq[Path]] =
    config.logger.timeAsync("Compiling to native code") {
      // compile generated LLVM IR
      val compileGeneratedIR = LLVM.compile(config, llvmAssembly)

      /* Used to pass alternative paths of compiled native (lib) sources,
       * eg: reused native sources used in partests.
       */
      val compileNativeLibs = {
        Properties.propOrNone("scalanative.build.paths.libobj") match {
          case None =>
            /* Finds all the libraries on the classpath that contain native
             * code and then compiles them.
             */
            findAndCompileNativeLibraries(config, analysis)
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

  /** Links the given object files using the system's linker. */
  private def link(
      config: Config,
      analysis: ReachabilityAnalysis.Result,
      compiled: Seq[Path]
  ): Path = config.logger.time(
    s"Linking native code (${config.gc.name} gc, ${config.LTO.name} lto)"
  ) {
    LLVM.link(config, analysis, compiled)
  }

  /** Links the DWARF debug information found in the object files. */
  private def postProcess(config: Config, artifact: Path): Path =
    config.logger.time("Postprocessing") {
      if (Platform.isMac && config.compilerConfig.debugMetadata) {
        LLVM.dsymutil(config, artifact)
      }
      artifact
    }

  /** Returns a checksum of a compilation pipeline with the given `config`. */
  private def checkSum(config: Config): Int = {
    // skip the whole nativeLink process if the followings are unchanged since the previous build
    // - build configuration
    // - class paths' mtime
    // - the output native binary ('s mtime)
    // Since the NIR code is shipped in jars, we should be able to detect the changes in NIRs.
    // One thing we miss is, we cannot detect changes in c libraries somewhere in `/usr/lib`.
    (
      config,
      config.classPath.map(getLastModifiedChild(_)),
      getLastModified(config.artifactPath)
    ).hashCode()
  }

  /** Finds and compiles native libaries.
   *
   *  @param config
   *    the compiler configuration
   *  @param analysis
   *    the result from the linker
   *  @return
   *    the paths to the compiled objects
   */
  def findAndCompileNativeLibraries(
      config: Config,
      analysis: ReachabilityAnalysis.Result
  )(implicit ec: ExecutionContext): Future[Seq[Path]] = {
    import NativeLib.{findNativeLibs, compileNativeLibrary}
    Future
      .traverse(findNativeLibs(config))(
        compileNativeLibrary(config, analysis, _)
      )
      .map(_.flatten)
  }

  /** Creates a directory at `config.workDir` if one doesn't exist. */
  private def checkWorkdirExists(config: Config): Unit = {
    val workDir = config.workDir
    if (Files.notExists(workDir)) {
      Files.createDirectories(workDir)
    }
  }

  /** Returns the last time the file at `path` was modified, or the epoch
   *  (1970-01-01T00:00:00Z) if such a file doesn't exist.
   */
  private def getLastModified(path: Path): FileTime =
    if (Files.exists(path))
      Try(Files.getLastModifiedTime(path)).getOrElse(FileTime.fromMillis(0L))
    else FileTime.fromMillis(0L)

  /** Returns the last time a file rooted at `path` was modified.
   *
   *  `path` is the root of a file tree, expanding symbolic links. The result is
   *  the most recent value returned by `getLastModified` a node of this tree or
   *  `empty` if there is no file at `path`.
   */
  private def getLastModifiedChild(path: Path): Optional[FileTime] =
    if (Files.exists(path))
      Files
        .walk(path, FileVisitOption.FOLLOW_LINKS)
        .map[FileTime](getLastModified(_))
        .max(_.compareTo(_))
    else Optional.empty()

}
