package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scala.sys.process.Process
import scalanative.build.IO.RichPath
import scalanative.nir.Global
import scalanative.linker.Link
import scalanative.sema.Sema
import scalanative.codegen.CodeGen
import scalanative.optimizer.Optimizer

/** Internal utilities to instrument Scala Native linker, otimizer and codegen. */
private[scalanative] object ScalaNative {

  /** Given the classpath and main entry point, link under closed-world
   *  assumption.
   */
  def link(config: Config, driver: optimizer.Driver): linker.Result = {
    config.logger.time("Linking") {
      val mainClass = Global.Top(config.mainClass)
      val entry =
        Global.Member(mainClass,
                      "main_scala.scalanative.runtime.ObjectArray_unit")
      val result = Link(config, entry +: CodeGen.depends)

      result.withDefns(result.defns ++ CodeGen.injects)
    }
  }

  /** Link just the given entries, disregarding the extra ones that are
   *  needed for the optimizer and/or codegen.
   */
  def linkOnly(config: Config,
               reporter: linker.Reporter,
               entries: Seq[nir.Global]): linker.Result =
    config.logger.time("Linking") {
      Link(config, entries)
    }

  /** Optimizer high-level NIR under closed-world assumption. */
  def optimize(config: Config,
               driver: optimizer.Driver,
               assembly: Seq[nir.Defn]): Seq[nir.Defn] =
    config.logger.time(s"Optimizing (${config.mode} mode)") {
      Optimizer(config, driver, assembly)
    }

  /** Given low-level assembly, emit LLVM IR for it to the buildDirectory. */
  def codegen(config: Config,
              assembly: Seq[nir.Defn],
              dyns: Seq[String]): Seq[Path] = {
    config.logger.time("Generating intermediate code") {
      CodeGen(config, assembly, dyns)
    }
    val produced = IO.getAll(config.workdir, "glob:**.ll")
    config.logger.info(s"Produced ${produced.length} files")
    produced
  }
}
