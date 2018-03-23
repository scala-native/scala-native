package scala.scalanative
package build

import java.nio.file.{Files, Path, Paths}
import scala.sys.process.Process
import scalanative.build.IO.RichPath
import scalanative.nir.Global

/** Internal utilities to instrument Scala Native linker, otimizer and codegen. */
private[scalanative] object ScalaNative {

  /** Given the classpath and main entry point, link under closed-world
   *  assumption.
   */
  def link(config: Config, driver: optimizer.Driver): linker.Result = {
    config.logger.time("Linking") {
      val chaDeps   = optimizer.analysis.ClassHierarchy.depends
      val passes    = driver.passes
      val passDeps  = passes.flatMap(_.depends).distinct
      val deps      = (chaDeps ++ passDeps).distinct
      val injects   = passes.flatMap(_.injects)
      val mainClass = nir.Global.Top(config.entry)
      val entry =
        nir.Global
          .Member(mainClass, "main_scala.scalanative.runtime.ObjectArray_unit")
      val result =
        (linker.Linker(config, driver.linkerReporter)).link(entry +: deps)

      result.withDefns(result.defns ++ injects)
    }
  }

  /** Link just the given entries, disregarding the extra ones that are
   *  needed for the optimizer and/or codegen.
   */
  def linkOnly(config: Config,
               reporter: linker.Reporter,
               entries: Seq[nir.Global]): linker.Result =
    config.logger.time("Linking") {
      linker.Linker(config, reporter).link(entries)
    }

  /** Transform high-level closed world to its lower-level counterpart. */
  def optimize(config: Config,
               driver: optimizer.Driver,
               assembly: Seq[nir.Defn],
               dyns: Seq[String]): Seq[nir.Defn] =
    config.logger.time(s"Optimizing (${config.mode} mode)") {
      optimizer.Optimizer(config, driver, assembly, dyns)
    }

  /** Given low-level assembly, emit LLVM IR for it to the buildDirectory. */
  def codegen(config: Config,
              assembly: Seq[nir.Defn]): Seq[Path] = {
    config.logger.time("Generating intermediate code") {
      scalanative.codegen.CodeGen(config, assembly)
    }
    val produced = IO.getAll(config.workdir, "glob:**.ll")
    config.logger.info(s"Produced ${produced.length} files")
    produced
  }
}
