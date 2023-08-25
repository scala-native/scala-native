package scala.scalanative
package build

import java.nio.file.{Path, Files}
import scala.collection.mutable
import scala.scalanative.checker.Check
import scala.scalanative.codegen.PlatformInfo
import scala.scalanative.codegen.llvm.CodeGen
import scala.scalanative.interflow.Interflow
import scala.scalanative.linker.{ReachabilityAnalysis, Reach, Link}
import scala.scalanative.nir._
import scala.scalanative.util.Scope
import scala.concurrent._
import scala.util.Success
import scala.scalanative.linker.LinkingException

/** Internal utilities to instrument Scala Native linker, optimizer and codegen.
 */
private[scalanative] object ScalaNative {

  /** Compute all globals that must be reachable based on given configuration.
   */
  def entries(config: Config): Seq[Global] = {
    implicit val platform: PlatformInfo = PlatformInfo(config)
    val entry = encodedMainClass(config).map(_.member(Rt.ScalaMainSig))
    val dependencies = CodeGen.depends ++ Interflow.depends
    entry ++: dependencies
  }

  /** Given the classpath and main entry point, link under closed-world
   *  assumption.
   */
  def link(config: Config, entries: Seq[Global])(implicit
      scope: Scope,
      ec: ExecutionContext
  ): Future[ReachabilityAnalysis.Result] = withReachabilityPostprocessing(
    config,
    stage = "classloading",
    dumpFile = "linked",
    forceQuickCheck = true
  )(Future {
    val mtSupport = config.compilerConfig.multithreadingSupport.toString()
    val linkingMsg = s"Linking (multithreading ${mtSupport})"
    config.logger.time(linkingMsg) {
      Link(config, entries)
    }
  })

  /** Optimizer high-level NIR under closed-world assumption. */
  def optimize(config: Config, analysis: ReachabilityAnalysis.Result)(implicit
      ec: ExecutionContext
  ): Future[ReachabilityAnalysis.Result] = {
    import config.logger
    if (config.compilerConfig.optimize)
      logger.timeAsync(s"Optimizing (${config.mode} mode)") {
        withReachabilityPostprocessing(
          config,
          stage = "optimization",
          dumpFile = "optimized",
          forceQuickCheck = false
        ) {
          Interflow
            .optimize(config, analysis)
            .map(Link(config, analysis.entries, _))
        }
      }
    else {
      logger.info("Optimizing skipped")
      Future.successful(analysis)
    }
  }

  private def withReachabilityPostprocessing(
      config: Config,
      stage: String,
      dumpFile: String,
      forceQuickCheck: Boolean
  )(
      analysis: Future[ReachabilityAnalysis]
  )(implicit ec: ExecutionContext): Future[ReachabilityAnalysis.Result] = {
    analysis
      .andThen {
        case Success(result) => dumpDefns(config, dumpFile, result.defns)
      }
      .andThen {
        case Success(result) => logLinked(config, result, stage)
      }
      .flatMap {
        case result: ReachabilityAnalysis.Result =>
          check(config, forceQuickCheck = forceQuickCheck)(result)
        case result: ReachabilityAnalysis.UnreachableSymbolsFound =>
          Future.failed(
            new LinkingException(
              s"Unreachable symbols found after $stage run. It can happen when using dependencies not cross-compiled for Scala Native or not yet ported JDK definitions."
            )
          )
      }
  }

  /** Show linked universe stats or fail with missing symbols. */
  private[scalanative] def logLinked(
      config: Config,
      analysis: ReachabilityAnalysis,
      stage: String
  ): Unit = {
    def showUnreachable(
        analysis: ReachabilityAnalysis.UnreachableSymbolsFound
    ): Unit = {
      val log = config.logger
      log.error(s"Found ${analysis.unreachable.size} unreachable symbols")
      analysis.unreachable.foreach {
        case Reach.UnreachableSymbol(_, kind, name, backtrace) =>
          // Build stacktrace in memory to prevent its spliting when logging asynchronously
          val buf = new StringBuilder()
          buf.append(s"Found unknown $kind $name, referenced from:\n")
          val padding = backtrace.foldLeft(0)(_ max _.kind.length())
          backtrace.foreach {
            case Reach.BackTraceElement(_, kind, name, filename, line) =>
              val pad = " " * (padding - kind.length())
              buf.append(s"    $pad$kind at $name($filename:$line)\n")
          }
          buf.append("\n")
          log.error(buf.toString())
      }
    }

    def showStats(): Unit = {
      val classCount = analysis.defns.count {
        case _: nir.Defn.Class | _: nir.Defn.Module => true
        case _                                      => false
      }
      val methodCount = analysis.defns.count(_.isInstanceOf[nir.Defn.Define])
      config.logger.info(
        s"Discovered ${classCount} classes and ${methodCount} methods after $stage"
      )
    }

    analysis match {
      case result: ReachabilityAnalysis.UnreachableSymbolsFound =>
        showStats()
        showUnreachable(result)
      case _ =>
        showStats()
    }
  }

  /** Given low-level assembly, emit LLVM IR for it to the buildDirectory. */
  def codegen(config: Config, analysis: ReachabilityAnalysis.Result)(implicit
      ec: ExecutionContext
  ): Future[Seq[Path]] = {
    val withMetadata =
      if (config.compilerConfig.debugMetadata) " (with debug metadata)"
      else ""

    config.logger.timeAsync(s"Generating intermediate code$withMetadata") {
      CodeGen(config, analysis)
        .andThen {
          case Success(paths) =>
            config.logger.info(s"Produced ${paths.length} files")
        }
    }
  }

  /** Run NIR checker on the linker result. */
  def check(config: Config)(
      analysis: ReachabilityAnalysis.Result
  )(implicit ec: ExecutionContext): Future[ReachabilityAnalysis.Result] = {
    check(config, forceQuickCheck = false)(analysis)
  }

  private def check(config: Config, forceQuickCheck: Boolean)(
      analysis: ReachabilityAnalysis.Result
  )(implicit ec: ExecutionContext): Future[ReachabilityAnalysis.Result] = {
    val performFullCheck = config.check
    val checkMode = if (performFullCheck) "full" else "quick"
    val fatalWarnings = config.compilerConfig.checkFatalWarnings

    if (config.check || forceQuickCheck) {
      config.logger
        .timeAsync(s"Checking intermediate code ($checkMode)") {
          if (performFullCheck) Check(analysis)
          else Check.quick(analysis)
        }
        .map {
          case Nil => analysis
          case errors =>
            showErrors(
              log =
                if (fatalWarnings) config.logger.error(_)
                else config.logger.warn(_),
              showContext = performFullCheck
            )(errors, analysis)

            if (fatalWarnings)
              throw new BuildException(
                "Fatal warning(s) found; see the error output for details."
              )
            analysis
        }
    } else Future.successful(analysis)
  }

  private def showErrors(
      log: String => Unit,
      showContext: Boolean
  )(errors: Seq[Check.Error], analysis: ReachabilityAnalysis.Result): Unit = {
    errors
      .groupBy(_.name)
      .foreach {
        case (name, errs) =>
          log(s"\nFound ${errs.length} errors on ${name.show} :")
          def showError(err: Check.Error): Unit = log("    " + err.msg)
          if (showContext) {
            analysis.defns
              .collectFirst {
                case defn if defn != null && defn.name == name => defn
              }
              .foreach { defn =>
                val str = defn.show
                val lines = str.split("\n")
                lines.zipWithIndex.foreach {
                  case (line, idx) =>
                    log(String.format("  %04d  ", Integer.valueOf(idx)) + line)
                }
              }
            log("")
            errs.foreach { err =>
              log("  in " + err.ctx.reverse.mkString(" / ") + " : ")
              showError(err)
            }
          } else errs.foreach(showError)
      }
    log(s"\n${errors.size} errors found")
  }

  def dumpDefns(config: Config, phase: String, defns: Seq[Defn]): Unit = {
    if (config.dump) {
      config.logger.time(s"Dumping intermediate code ($phase)") {
        val path = config.workDir.resolve(phase + ".hnir")
        nir.Show.dump(defns, path.toFile.getAbsolutePath)
      }
    }
  }

  private[scalanative] def encodedMainClass(
      config: Config
  ): Option[Global.Top] =
    config.mainClass.map { mainClass =>
      import scala.reflect.NameTransformer.encode
      val encoded = mainClass.split('.').map(encode).mkString(".")
      Global.Top(encoded)
    }

  def genBuildInfo(
      config: Config
  )(implicit ec: ExecutionContext): Future[Seq[Path]] = Future {
    LLVM.generateLLVMIdent(config)
  }

}
