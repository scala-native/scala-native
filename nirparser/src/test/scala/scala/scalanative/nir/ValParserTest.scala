package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class ValParserTest extends FunSuite {
  val global = Global.Top("test")

  Seq[Val](
    Val.True,
    Val.False,
    Val.Null,
    Val.Zero(Type.Int),
    Val.Char('0'),
    Val.Byte(0),
    Val.Short(0),
    Val.Int(0),
    Val.Long(0),
    Val.Float(0),
    Val.Double(0),
    Val.StructValue(Seq.empty),
    Val.StructValue(Seq(Val.Int(32))),
    Val.StructValue(Seq(Val.Int(32), Val.Long(64))),
    Val.ArrayValue(Type.Int, Seq.empty),
    Val.ArrayValue(Type.Int, Seq(Val.Int(32))),
    Val.Chars("foobar"),
    Val.Local(Local(0), Type.Int),
    Val.Global(global, Type.Ptr),
    Val.Unit,
    Val.Const(Val.Int(0)),
    Val.String("foobar"),
    Val.String("foo bar"),
    Val.String("foo \"bar\" baz")
  ).foreach { ty =>
    test(s"parse value `${ty.show}`") {
      val Parsed.Success(result, _) = parser.Val.parser.parse(ty.show)
      assert(result == ty)
    }
  }
}
