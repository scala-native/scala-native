package native
package compiler

import native.nir._
import native.compiler.passes._

final class Compiler(opts: Opts) {
  def load(): Seq[Defn] = ???

  def passes(): Seq[Pass] =
    Seq(
      BoxLowering
    )

  def output(scope: Seq[Defn]): Unit = ???

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
