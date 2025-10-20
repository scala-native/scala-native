package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scala.scalanative.util.Scope
import scala.scalanative.linker.ReachabilityAnalysis
import scala.scalanative.codegen.llvm.CodeGen.IRGenerators
import scala.util.Try
import java.nio.file.FileVisitOption
import java.nio.file.StandardOpenOption
import java.util.Optional
import java.nio.file.attribute.FileTime
import java.util.concurrent.Executors
import scala.concurrent.*
import scala.concurrent.duration.Duration
import scala.util.{Success, Properties}
import scala.collection.immutable
import ScalaNative.*

/** Utility methods for building code using Scala Native. */
object Build {

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
    val checksumPath = config.workDir.resolve("build-checksum")

    if (Files.exists(config.artifactPath) &&
        IO.readFully(checksumPath).contains(checkSum(config).toString)) {
      config.logger.info(
        "Build skipped: No changes detected in build configuration and class path contents since last build."
      )
      Future.successful(config.artifactPath)
    } else {
      build(config).andThen {
        case Success(_) =>
          // Need to re-calculate the checksum because the content of `output` have changed.
          IO.write(
            path = checksumPath,
            content = checkSum(config).toString
          )
      }
    }
  }

  /** Run the complete Scala Native pipeline, LLVM optimizer and system linker,
   *  producing a native binary in the end, same as `build` method.
   *
   *  This method skips the whole build and link process if the input hasn't
   *  changed from the previous build, and the previous build artifact is
   *  available at Config#artifactPath.
   *
   *  This method would block infinitly long for the result of
   *  `Build.buildCached` executed using dedicated ExecutionContext
   *
   *  @param config
   *    The configuration of the toolchain.
   *  @return
   *    [[Config#artifactPath]], the path to the resulting native binary.
   */
  @throws(classOf[InterruptedException])
  @throws(classOf[BuildException])
  @throws(classOf[linker.LinkingException])
  def buildCachedAwait(config: Config)(implicit scope: Scope): Path = {
    await { implicit ec: ExecutionContext =>
      Build.buildCached(config)
    }(logTrace = config.logger.trace(_))
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
   *        .withMultithreading(enabled = false)
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
      var config = Validator.validate(initialConfig)
      config.logger.debug(config.toString())
      def linkNIRForEntries = ScalaNative.link(config, entries(config))

      linkNIRForEntries
        .flatMap { linkerResult =>
          val (updatedConfig, needsToReload) =
            postReachabilityAnalysisConfigUpdate(config, linkerResult)
          config = updatedConfig
          if (needsToReload) linkNIRForEntries
          else Future.successful(linkerResult)
        }
        .flatMap(optimize(config, _))
        .flatMap { linkerResult =>
          ScalaNative
            .codegen(config, linkerResult)
            .flatMap { irGenerators =>
              compile(config, linkerResult, irGenerators)
            }
            .map(objects => link(config, linkerResult, objects))
            .map(artifact => postProcess(config, artifact))
        }
        .andThen { case Success(_) => dumpUserConfigHash(config) }
    }
  }

  /** Compiles `generatedIR`, which is a sequence of LLVM IR files. */
  private def compile(
      config: Config,
      analysis: ReachabilityAnalysis.Result,
      irGenerators: Seq[Future[Path]]
  )(implicit ec: ExecutionContext): Future[Seq[Path]] =
    config.logger.timeAsync("Compiling to native code") {
      // compile generated LLVM IR
      val compileGeneratedIR = Future
        .sequence {
          irGenerators.map(irGenerator =>
            irGenerator.flatMap(generatedIR =>
              LLVM.compile(config, analysis, generatedIR)
            )
          )
        }

      /* Finds all the libraries on the classpath that contain native
       * code and then compiles them.
       */
      val compileNativeLibs = findAndCompileNativeLibraries(config, analysis)

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

  /** Based on reachability analysis check if config can be tuned for better
   *  performance
   */
  private def postReachabilityAnalysisConfigUpdate(
      config: Config,
      analysis: ReachabilityAnalysis.Result
  ): (Config, Boolean) = {
    var currentConfig = config
    var needsToReload = false

    // Each block can modify currentConfig stat,
    // modification should be lazy to not reconstruct object when not required
    locally { // disable unused mulithreading
      if (config.compilerConfig.multithreading.isEmpty) {
        // format: off
        val jlRunnable = nir.Global.Top("java.lang.Runnable")
        val jlThread = nir.Global.Top("java.lang.Thread")
        val jlThreadBuildersOfPlatform = nir.Global.Top("java.lang.ThreadBuilders$PlatformThreadBuilder")
        val jlThreadBuildersOfPlatformStart = jlThreadBuildersOfPlatform.member(nir.Sig.Method("start", Seq(jlRunnable, jlThread).map(nir.Type.Ref(_))))
        val jlThreadStart = jlThread.member(nir.Sig.Method("start", Seq(nir.Type.Unit)))
        val usesSystemThreads =
          analysis.infos.get(jlThreadBuildersOfPlatformStart).isDefined ||
          analysis.infos.get(jlThreadStart).isDefined
        // format: on
        if (!usesSystemThreads) {
          config.logger.info(
            "Multithreading was not explicitly enabled - initial class loading has not detected any usage of system threads. " +
              "Multithreading support will be disabled to improve performance."
          )
          currentConfig = currentConfig.withCompilerConfig(
            _.withMultithreading(false)
          )
          needsToReload = true
        }
      }
    }
    currentConfig -> needsToReload
  }

  /** Links the DWARF debug information found in the object files. */
  private def postProcess(config: Config, artifact: Path): Path =
    config.logger.time("Postprocessing") {
      if (config.targetsMac && config.compilerConfig.sourceLevelDebuggingConfig.generateFunctionSourcePositions) {
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
  private[scala] def findAndCompileNativeLibraries(
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

  private[scalanative] final val userConfigHashFile = "userConfigHash"

  private[scalanative] def userConfigHasChanged(config: Config): Boolean =
    IO.readFully(config.workDir.resolve(userConfigHashFile))
      .forall(_.trim() != config.compilerConfig.##.toString())

  private[scalanative] def dumpUserConfigHash(config: Config): Unit =
    IO.write(
      path = config.workDir.resolve(userConfigHashFile),
      content = config.compilerConfig.##.toString()
    )

  private def await[T](
      task: ExecutionContext => Future[T]
  )(logTrace: Throwable => Unit): T = {
    // Fatal errors, e.g. StackOverflowErrors are not propagated by Futures
    // Use a helper promise to get notified about the underlying problem
    val promise = Promise[T]()
    val executor = Executors.newFixedThreadPool(
      Runtime.getRuntime().availableProcessors(),
      (task: Runnable) => {
        val thread = Executors.defaultThreadFactory().newThread(task)
        val defaultExceptionHandler = thread.getUncaughtExceptionHandler()
        thread.setUncaughtExceptionHandler { (thread: Thread, ex: Throwable) =>
          promise.tryFailure(ex)
          ex match {
            case _: InterruptedException => logTrace(ex)
            case _ => defaultExceptionHandler.uncaughtException(thread, ex)
          }
        }
        thread
      }
    )
    implicit val ec: ExecutionContext =
      ExecutionContext.fromExecutor(executor, logTrace(_))

    // Schedule the task and record completion
    task(ec).onComplete(promise.complete)
    try Await.result(promise.future, Duration.Inf)
    finally executor.shutdown()
  }
}
