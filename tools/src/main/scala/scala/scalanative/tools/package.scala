package scala.scalanative

import java.nio.file.Paths
import scalanative.io.withScratchBuffer

// API use-cases
//
// sbt plugin:
//   1. link & compile code
//
// compiler  test : VirtualDirectory -> World
//   1. virtual file system of some kind
//   2. instantiate scalac+nscplugin
//   3. run it and check invariants
//
// optimizer test : World -> World
//   1. define what passes we want
//   2. run them and check invariants
//
// code gen test : World -> Seq[String]
//   1. run code gen and check the string

package object tools {
  type LinkerPath = linker.Path
  val LinkerPath = linker.Path

  type LinkerReporter = linker.Reporter
  val LinkerReporter = linker.Reporter

  type OptimizerDriver = optimizer.Driver
  val OptimizerDriver = optimizer.Driver

  type OptimizerReporter = optimizer.Reporter
  val OptimizerReporter = optimizer.Reporter

  /** Given the classpath and entry point, link under closed-world assumption. */
  def link(config: Config,
           driver: OptimizerDriver,
           reporter: LinkerReporter = LinkerReporter.empty)
    : (Seq[nir.Global], Seq[nir.Attr.Link], Seq[nir.Defn], Seq[String]) = {
    val deps    = driver.passes.flatMap(_.depends).distinct
    val injects = driver.passes.flatMap(_.injects).distinct
    val entry =
      nir.Global.Member(config.entry, "main_class.ssnr.ObjectArray_unit")
    val (unresolved, links, defns, dyns) =
      (linker.Linker(config, reporter)).link(entry +: deps)

    (unresolved, links, defns ++ injects, dyns)
  }

  /** Transform high-level closed world to its lower-level counterpart. */
  def optimize(
      config: Config,
      driver: OptimizerDriver,
      assembly: Seq[nir.Defn],
      dyns: Seq[String],
      reporter: OptimizerReporter = OptimizerReporter.empty): Seq[nir.Defn] =
    optimizer.Optimizer(config, driver, assembly, dyns, reporter)

  /** Given low-level assembly, emit LLVM IR for it to the buildDirectory. */
  def codegen(config: Config, assembly: Seq[nir.Defn]): Unit =
    scalanative.codegen.CodeGen(config, assembly)
}
