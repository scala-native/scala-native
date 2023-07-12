package scala.scalanative
package build

import java.nio.file.{Path, Files}
import scala.collection.mutable
import scala.scalanative.checker.Check
import scala.scalanative.codegen.PlatformInfo
import scala.scalanative.codegen.llvm.CodeGen
import scala.scalanative.interflow.Interflow
import scala.scalanative.linker.Link
import scala.scalanative.nir._
import scala.scalanative.util.Scope
import scala.concurrent._
import scala.util.Success

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
  ): Future[linker.Result] =
    check(config, forceQuickCheck = true) {
      val mtSupport = config.compilerConfig.multithreadingSupport.toString()
      val linkingMsg = s"Linking (multithreading ${mtSupport})"
      config.logger.time(linkingMsg) {
        Link(config, entries)
      }
    }.andThen {
      case Success(result) =>
        dumpDefns(config, "linked", result.defns)
        logLinked(config, result)
    }

  /** Show linked universe stats or fail with missing symbols. */
  def logLinked(config: Config, linked: linker.Result): Unit = {
    def showLinkingErrors(): Nothing = {
      config.logger.error("missing symbols:")
      linked.unavailable.sortBy(_.show).foreach { name =>
        config.logger.error("* " + name.mangle)
        val from = linked.referencedFrom
        var current = from(name)
        while (from.contains(current) && current != Global.None) {
          config.logger.error("  - from " + current.mangle)
          current = from(current)
        }
      }
      throw new BuildException("unable to link")
    }

    def showStats(): Unit = {
      val classCount = linked.defns.count {
        case _: nir.Defn.Class | _: nir.Defn.Module => true
        case _                                      => false
      }
      val methodCount = linked.defns.count(_.isInstanceOf[nir.Defn.Define])
      config.logger.info(
        s"Discovered ${classCount} classes and ${methodCount} methods"
      )
    }

    if (linked.unavailable.nonEmpty) {
      showLinkingErrors()
    } else {
      showStats()
    }
  }

  /** Optimizer high-level NIR under closed-world assumption. */
  def optimize(config: Config, linked: linker.Result)(implicit
      ec: ExecutionContext
  ): Future[linker.Result] = {
    import config.logger
    if (config.compilerConfig.optimize)
      logger.timeAsync(s"Optimizing (${config.mode} mode)") {
        Interflow
          .optimize(config, linked)
          .map(Link(config, linked.entries, _))
          .andThen {
            case Success(result) => dumpDefns(config, "optimized", result.defns)
          }
          .flatMap(check(config)(_))
      }
    else {
      logger.info("Optimizing skipped")
      Future.successful(linked)
    }
  }

  /** Given low-level assembly, emit LLVM IR for it to the buildDirectory. */
  def codegen(config: Config, linked: linker.Result)(implicit
      ec: ExecutionContext
  ): Future[Seq[Path]] = {
    val withMetadata =
      if (config.compilerConfig.debugMetadata) " (with debug metadata)"
      else ""

    config.logger.timeAsync(s"Generating intermediate code$withMetadata") {
      CodeGen(config, linked)
        .andThen {
          case Success(paths) =>
            config.logger.info(s"Produced ${paths.length} files")
        }
    }
  }

  /** Run NIR checker on the linker result. */
  def check(config: Config)(
      linked: scalanative.linker.Result
  )(implicit ec: ExecutionContext): Future[scalanative.linker.Result] = {
    check(config, forceQuickCheck = false)(linked)
  }

  private def check(config: Config, forceQuickCheck: Boolean)(
      linked: scalanative.linker.Result
  )(implicit ec: ExecutionContext): Future[scalanative.linker.Result] = {
    val performFullCheck = config.check
    val checkMode = if (performFullCheck) "full" else "quick"
    val fatalWarnings = config.compilerConfig.checkFatalWarnings

    if (config.check || forceQuickCheck) {
      config.logger
        .timeAsync(s"Checking intermediate code ($checkMode)") {
          if (performFullCheck) Check(linked)
          else Check.quick(linked)
        }
        .map {
          case Nil => linked
          case errors =>
            showErrors(
              log =
                if (fatalWarnings) config.logger.error(_)
                else config.logger.warn(_),
              showContext = performFullCheck
            )(errors, linked)

            if (fatalWarnings)
              throw new BuildException(
                "Fatal warning(s) found; see the error output for details."
              )
            linked
        }
    } else Future.successful(linked)
  }

  private def showErrors(
      log: String => Unit,
      showContext: Boolean
  )(errors: Seq[Check.Error], linked: linker.Result): Unit = {
    errors
      .groupBy(_.name)
      .foreach {
        case (name, errs) =>
          log(s"\nFound ${errs.length} errors on ${name.show} :")
          def showError(err: Check.Error): Unit = log("    " + err.msg)
          if (showContext) {
            linked.defns
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
