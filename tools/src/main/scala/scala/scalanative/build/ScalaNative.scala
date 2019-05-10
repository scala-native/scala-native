package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scala.collection.mutable
import scala.sys.process.Process
import scalanative.build.IO.RichPath
import scalanative.nir.{Type, Rt, Sig, Global}
import scalanative.linker.Link
import scalanative.codegen.CodeGen
import scalanative.interflow.Interflow
import scalanative.checker.Check

/** Internal utilities to instrument Scala Native linker, otimizer and codegen. */
private[scalanative] object ScalaNative {

  /** Compute all globals that must be reachable
   *  based on given configuration.
   */
  def entries(config: Config): Seq[Global] = {
    val mainClass = Global.Top(config.mainClass)
    val entry =
      mainClass.member(
        Sig.Method("main", Seq(Type.Array(Rt.String), Type.Unit)))
    entry +: CodeGen.depends
  }

  /** Given the classpath and main entry point, link under closed-world
   *  assumption.
   */
  def link(config: Config, entries: Seq[Global]): linker.Result =
    dump(config, "linked") {
      check(config) {
        config.logger.time("Linking")(Link(config, entries))
      }
    }

  /** Show linked universe stats or fail with missing symbols. */
  def logLinked(config: Config, linked: linker.Result): Unit = {
    def showLinkingErrors(): Nothing = {
      config.logger.error("missing symbols:")
      linked.unavailable.sortBy(_.show).foreach { name =>
        config.logger.error("* " + name.mangle)
        val from    = linked.referencedFrom
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
        s"Discovered ${classCount} classes and ${methodCount} methods")
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
        config.logger.time(s"Optimizing (${config.mode} mode)") {
          val optimized =
            interflow.Interflow(config, linked)

          linker.Link(config, linked.entries, optimized)
        }
      }
    }

  /** Given low-level assembly, emit LLVM IR for it to the buildDirectory. */
  def codegen(config: Config, linked: linker.Result): Seq[Path] = {
    config.logger.time("Generating intermediate code") {
      CodeGen(config, linked)
    }
    val produced = IO.getAll(config.workdir, "glob:**.ll")
    config.logger.info(s"Produced ${produced.length} files")
    produced
  }

  /** Run NIR checker on the linker result. */
  def check(config: Config)(
      linked: scalanative.linker.Result): scalanative.linker.Result = {
    if (config.check) {
      config.logger.time("Checking intermediate code") {
        def warn(s: String) = config.logger.warn(s)
        val errors          = Check(linked)
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
                  val str   = defn.show
                  val lines = str.split("\n")
                  lines.zipWithIndex.foreach {
                    case (line, idx) =>
                      warn(String
                        .format("  %04d  ", java.lang.Integer.valueOf(idx)) + line)
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
        }
      }
    }

    linked
  }

  def dump(config: Config, phase: String)(
      linked: scalanative.linker.Result): scalanative.linker.Result = {
    if (config.dump) {
      config.logger.time("Dumping intermediate code") {
        val path = config.workdir.resolve(phase + ".hnir")
        nir.Show.dump(linked.defns, path.toFile.getAbsolutePath)
      }
    }

    linked
  }
}
