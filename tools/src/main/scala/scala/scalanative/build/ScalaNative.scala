package scala.scalanative
package build

import java.nio.file.{Path, Files}
import scala.collection.mutable
import scala.scalanative.checker.Check
import scala.scalanative.codegen.CodeGen
import scala.scalanative.interflow.Interflow
import scala.scalanative.linker.Link
import scala.scalanative.nir._
import scala.scalanative.util.Scope

/** Internal utilities to instrument Scala Native linker, optimizer and codegen.
 */
private[scalanative] object ScalaNative {

  /** Compute all globals that must be reachable based on given configuration.
   */
  def entries(config: Config): Seq[Global] = {
    val entry = encodedMainClass(config).map(_.member(Rt.ScalaMainSig))
    val dependencies = CodeGen.depends ++ Interflow.depends
    entry ++: dependencies
  }

  /** Given the classpath and main entry point, link under closed-world
   *  assumption.
   */
  def link(config: Config, entries: Seq[Global])(implicit
      scope: Scope
  ): linker.Result =
    dump(config, "linked") {
      check(config) {
        val mtSupport = config.compilerConfig.multithreadingSupport.toString()
        val linkingMsg = s"Linking (multithreading ${mtSupport})"
        config.logger.time(linkingMsg)(Link(config, entries))
      }
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
  def optimize(config: Config, linked: linker.Result): linker.Result =
    dump(config, "optimized") {
      check(config) {
        if (config.compilerConfig.optimize) {
          config.logger.time(s"Optimizing (${config.mode} mode)") {
            val optimized =
              interflow.Interflow(config, linked)

            linker.Link(config, linked.entries, optimized)
          }
        } else {
          config.logger.time("Optimizing (skipped)") {
            linked
          }
        }
      }
    }

  /** Given low-level assembly, emit LLVM IR for it to the buildDirectory. */
  def codegen(config: Config, linked: linker.Result): Seq[Path] = {
    val withMetadata =
      if (config.compilerConfig.debugMetadata) " (with debug metadata)" else ""

    val llPaths =
      config.logger.time(s"Generating intermediate code$withMetadata") {
        CodeGen(config, linked)
      }
    config.logger.info(s"Produced ${llPaths.length} files")
    llPaths
  }

  /** Run NIR checker on the linker result. */
  def check(
      config: Config
  )(linked: scalanative.linker.Result): scalanative.linker.Result = {
    if (config.check) {
      config.logger.time("Checking intermediate code") {
        def warn(s: String) =
          if (config.compilerConfig.checkFatalWarnings) config.logger.error(s)
          else config.logger.warn(s)
        val errors = Check(linked)
        if (errors.nonEmpty) {
          val grouped =
            mutable.Map.empty[Global, mutable.UnrolledBuffer[Check.Error]]
          errors.foreach { err =>
            val errs =
              grouped.getOrElseUpdate(err.name, mutable.UnrolledBuffer.empty)
            errs += err
          }
          grouped.foreach {
            case (name, errs) =>
              warn("")
              warn(s"Found ${errs.length} errors on ${name.show} :")
              warn("")
              linked.defns
                .collectFirst {
                  case defn if defn != null && defn.name == name => defn
                }
                .foreach { defn =>
                  val str = defn.show
                  val lines = str.split("\n")
                  lines.zipWithIndex.foreach {
                    case (line, idx) =>
                      warn(
                        String
                          .format(
                            "  %04d  ",
                            java.lang.Integer.valueOf(idx)
                          ) + line
                      )
                  }
                }
              warn("")
              errs.foreach { err =>
                warn("  in " + err.ctx.reverse.mkString(" / ") + " : ")
                warn("    " + err.msg)
              }

          }
          warn("")
          warn(s"${errors.size} errors found")

          if (config.compilerConfig.checkFatalWarnings) {
            throw new BuildException(
              "Fatal warning(s) found; see the error output for details."
            )
          }
        }
      }
    }

    linked
  }

  def dump(config: Config, phase: String)(
      linked: scalanative.linker.Result
  ): scalanative.linker.Result = {
    dumpDefns(config, phase, linked.defns)
    linked
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

  def genBuildInfo(config: Config): Seq[java.nio.file.Path] =
    LLVM.generateLLVMIdent(config)

}
