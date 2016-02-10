package native
package compiler

import scala.collection.mutable
import native.nir._
import native.nir.serialization._

final class Compiler(opts: Opts) {
  def entry() =
    Global(opts.entry, "m")

  def load(): Seq[Defn] =
    (new Loader(opts.classpath)).load(entry())

  def passes(): Seq[Pass] =
    Seq(
      new pass.Lowering(entry()),
      new pass.CopyLowering
    )

  def output(module: Seq[Defn]): Unit = {
    //serializeFile(opts.gen.apply _, module, opts.outpath)
    serializeFile(codegen.GenTextualNIR, module, opts.outpath + ".hnir")
    serializeFile(codegen.GenTextualLLVM, module, opts.outpath + ".ll")
  }

  def apply(): Unit = {
    def loop(module: Seq[Defn], passes: Seq[Pass]): Seq[Defn] =
      passes match {
        case Seq() =>
          module
        case pass +: rest =>
          loop(pass.onCompilationUnit(module), rest)
      }
    output(loop(load(), passes()))
  }
}
object Compiler extends App {
  val compile = new Compiler(Opts.fromArgs(args))
  compile()
}
