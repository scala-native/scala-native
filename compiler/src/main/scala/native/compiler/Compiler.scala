package native
package compiler

import scala.collection.mutable
import native.nir._
import native.nir.serialization._

final class Compiler(opts: Opts) {
  def entry() =
    Global.Val(opts.entry)

  def load(): Seq[Defn] =
    (new Loader(opts.classpath)).load(entry())

  def passes(assembly: Seq[Defn]): Seq[Pass] = {
    implicit val fresh = Fresh("tx")
    implicit val hierarchy = analysis.ClassHierarchy(assembly)

    Seq(
      new pass.MainInjection(entry()),
      new pass.ClosureLowering,
      new pass.ModuleLowering,
      new pass.UnitLowering,
      new pass.TraitLowering,
      new pass.ClassLowering,
      new pass.StringLowering,
      new pass.IntrinsicLowering,
      new pass.NothingLowering,
      new pass.SizeLowering,
      new pass.TryLowering,
      new pass.CopyElimination
    )
  }

  def output(assembly: Seq[Defn]): Unit =
    serializeFile(codegen.GenTextualLLVM, assembly, opts.outpath + ".ll")

  def debug(assembly: Seq[Defn], suffix: String) =
    serializeFile(codegen.GenTextualNIR, assembly, opts.outpath + s".$suffix.hnir")

  def apply(): Unit = {
    def loop(assembly: Seq[Defn], passes: Seq[(Pass, Int)]): Seq[Defn] =
      passes match {
        case Seq() =>
          assembly
        case (pass, id) +: rest =>
          val nassembly = pass(assembly)
          debug(nassembly, (id + 1).toString + "-" + pass.getClass.getSimpleName)
          loop(nassembly, rest)
      }
    val assembly = load()
    debug(assembly, "0")
    output(loop(load(), passes(assembly).zipWithIndex))
  }
}
object Compiler extends App {
  val compile = new Compiler(Opts.fromArgs(args))
  compile()
}
