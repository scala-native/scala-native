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
  type World = optimizer.analysis.ClassHierarchy.Top

  type Driver = optimizer.Driver
  val Driver = optimizer.Driver

  type Path = linker.Path
  val Path = linker.Path

  /** Given the classpath and entry point, link under closed-world assumption. */
  def link(config: Config,
           driver: Driver): (Seq[nir.Attr.Link], Seq[nir.Defn]) = {
    val deps    = driver.passes.flatMap(_.depends).distinct
    val injects = driver.passes.flatMap(_.injects).distinct
    val entry =
      nir.Global.Member(config.entry, "main_class.ssnr.ObjectArray_unit")
    val (links, defns) = (linker.Linker(config)).link(entry +: deps)

    (links, defns ++ injects)
  }

  /** Transform high-level closed world to its lower-level counterpart. */
  def optimize(config: Config,
               driver: Driver,
               assembly: Seq[nir.Defn]): Seq[nir.Defn] =
    optimizer.Optimizer(config, driver, assembly)

  /** Given low-level assembly, emit LLVM IR for it to the buildDirectory. */
  def codegen(config: Config, assembly: Seq[nir.Defn]): Unit = {
    val gen = scalanative.codegen.CodeGen(assembly)

    withScratchBuffer { buffer =>
      gen.gen(buffer)
      buffer.flip
      config.targetDirectory.write(Paths.get("out.ll"), buffer)
    }
  }
}
