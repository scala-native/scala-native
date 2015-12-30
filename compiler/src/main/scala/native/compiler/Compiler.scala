package native
package compiler

import scala.collection.mutable
import native.nir._
import native.nir.serialization._
import native.compiler.passes._

final class Compiler(opts: Opts) {
  def load(): Seq[Defn] =
    (new Loader(opts.classpath)).load(
      Global.Tagged(Global.Atom(opts.entry), Global.Atom("m"))
    )

  def passes(): Seq[Pass] =
    Seq(
      BoxLowering,
      ModuleLowering
    )

  def output(scope: Seq[Defn]): Unit =
    serializeTextFile(scope, opts.outpath)

  def apply(): Unit = {
    def loop(scope: Seq[Defn], passes: Seq[Pass]): Seq[Defn] =
      passes match {
        case Seq() =>
          scope
        case pass +: rest =>
          loop(pass(scope), rest)
      }
    output(loop(load(), passes()))
  }
}
object Compiler extends App {
  val compile = new Compiler(Opts.fromArgs(args))
  compile()
}
