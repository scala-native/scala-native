package scala.scalanative

import scalanative.nir.Global

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
  type LinkerPath = linker.ClassPath
  val LinkerPath = linker.ClassPath

  type LinkerReporter = linker.Reporter
  val LinkerReporter = linker.Reporter

  type LinkerResult = linker.Result
  val LinkerResult = linker.Result

  type OptimizerDriver = optimizer.Driver
  val OptimizerDriver = optimizer.Driver

  type OptimizerReporter = optimizer.Reporter
  val OptimizerReporter = optimizer.Reporter

  /** Given the classpath and main entry point, link under closed-world assumption. */
  def link(config: Config,
           driver: OptimizerDriver,
           reporter: LinkerReporter = LinkerReporter.empty): LinkerResult = {
    val deps    = driver.passes.flatMap(_.depends).distinct
    val injects = driver.passes.flatMap(_.injects).distinct
    val entry =
      nir.Global.Member(config.entry,
                        "main_scala.scalanative.runtime.ObjectArray_unit")
    val result =
      (linker.Linker(config, reporter)).link(entry +: deps)

    result.withDefns(result.defns ++ injects)
  }

  def linkRaw(config: Config,
              entries: Seq[Global],
              reporter: LinkerReporter = LinkerReporter.empty): LinkerResult =
    linker.Linker(config, reporter).link(entries)

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
