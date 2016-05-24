package scala.scalanative
package compiler

import scala.collection.mutable
import codegen.{GenTextualLLVM, GenTextualNIR}
import linker.Linker
import nir._, Shows._
import nir.serialization._
import util.sh

final class Compiler(opts: Opts) {
  private lazy val entry =
    Global.Member(Global.Val(opts.entry), "main_class.ssnr.ObjectArray_unit")

  private lazy val (links, assembly): (Seq[Attr.Link], Seq[Defn]) =
    new Linker(opts.dotpath, opts.classpath).linkClosed(entry)

  private lazy val passes: Seq[Pass] = {
    implicit val fresh     = Fresh("tx")
    implicit val hierarchy = analysis.ClassHierarchy(assembly)

    Seq(
        new pass.LocalBoxingElimination,
        new pass.DeadCodeElimination,
        new pass.MainInjection(entry),
        new pass.ExternHoisting,
        new pass.ModuleLowering,
        new pass.TypeofLowering,
        new pass.AsLowering,
        new pass.TraitLowering,
        new pass.ClassLowering,
        new pass.StringLowering,
        new pass.ConstLowering,
        new pass.SizeofLowering,
        new pass.UnitLowering,
        new pass.NothingLowering,
        new pass.ExceptionLowering,
        new pass.StackallocHoisting,
        new pass.CopyPropagation
    )
  }

  private def codegen(assembly: Seq[Defn]): Unit = {
    val gen = new GenTextualLLVM(assembly)
    serializeFile((defns, bb) => gen.gen(bb), assembly, opts.outpath)
  }

  private def debug(assembly: Seq[Defn], suffix: String) =
    if (opts.verbose) {
      val gen = new GenTextualNIR(assembly)
      serializeFile((defns, bb) => gen.gen(bb),
                    assembly,
                    opts.outpath + s".$suffix.hnir")
    }

  def apply(): Seq[Attr.Link] = {
    def loop(assembly: Seq[Defn], passes: Seq[(Pass, Int)]): Seq[Defn] =
      passes match {
        case Seq() =>
          assembly
        case (pass, id) +: rest =>
          val nassembly = pass(assembly)
          debug(
              nassembly, (id + 1).toString + "-" + pass.getClass.getSimpleName)
          loop(nassembly, rest)
      }

    debug(assembly, "0")
    codegen(loop(assembly, passes.zipWithIndex))

    links
  }
}
