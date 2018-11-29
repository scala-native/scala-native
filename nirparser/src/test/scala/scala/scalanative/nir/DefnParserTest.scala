package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class DefnParserTest extends FunSuite {
  val ty     = Type.Int
  val global = Global.Top("global")

  Seq[Defn](
    Defn.Var(Attrs.None, global, ty, Val.Zero(ty)),
    Defn.Const(Attrs.None, global, ty, Val.Zero(ty)),
    Defn.Declare(Attrs.None, global, ty),
    Defn.Define(Attrs.None, global, ty, Seq.empty),
    Defn.Trait(Attrs.None, global, Seq.empty),
    Defn.Class(Attrs.None, global, None, Seq.empty),
    Defn.Module(Attrs.None, global, None, Seq.empty)
  ).foreach { defn =>
    test(s"parse defn `${defn.show}`") {
      val Parsed.Success(result, _) = parser.Defn.parser.parse(defn.show)
      assert(result == defn)
    }
  }
}
