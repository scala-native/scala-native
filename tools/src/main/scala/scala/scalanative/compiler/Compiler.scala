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
    Global.Member(Global.Top(opts.entry), "main_class.ssnr.ObjectArray_unit")

  private lazy val passCompanions: Seq[PassCompanion] = Seq(
      Seq(pass.LocalBoxingElimination,
          pass.DeadCodeElimination,
          pass.MainInjection,
          pass.ExternHoisting,
          pass.ModuleLowering,
          pass.RuntimeTypeInfoInjection,
          pass.AsLowering,
          pass.IsLowering,
          pass.MethodLowering,
          pass.TraitLowering,
          pass.ClassLowering,
          pass.StringLowering,
          pass.ConstLowering,
          pass.UnitLowering,
          pass.ThrowLowering,
          pass.NothingLowering,
          pass.TryLowering,
          pass.AllocLowering,
          pass.SizeofLowering,
          pass.CopyPropagation)).flatten

  private lazy val (links, assembly): (Seq[Attr.Link], Seq[Defn]) = {
    val deps           = passCompanions.flatMap(_.depends).distinct
    val injects        = passCompanions.flatMap(_.injects).distinct
    val linker         = new Linker(opts.dotpath, opts.classpath)
    val (links, defns) = linker.linkClosed(entry +: deps)

    (links, defns ++ injects)
  }

  private lazy val passes = {
    val ctx = Ctx(fresh = Fresh("tx"),
                  entry = entry,
                  top = analysis.ClassHierarchy(assembly),
                  options = opts)

    passCompanions.map(_.apply(ctx))
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

        case (pass.EmptyPass, _) +: rest =>
          loop(assembly, rest)

        case (pass, id) +: rest =>
          val nassembly = pass(assembly)
          val n         = id + 1
          val padded    = if (n < 10) "0" + n else "" + n

          debug(nassembly, padded + "-" + pass.getClass.getSimpleName)
          loop(nassembly, rest)
      }

    debug(assembly, "00")
    codegen(loop(assembly, passes.zipWithIndex))

    links
  }
}
