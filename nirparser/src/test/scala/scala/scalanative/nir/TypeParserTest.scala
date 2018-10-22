package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class TypeParserTest extends FunSuite {
  val global = Global.Top("global")

  Seq[Type](
    Type.None,
    Type.Void,
    Type.Vararg,
    Type.Ptr,
    Type.Bool,
    Type.Byte,
    Type.UByte,
    Type.Short,
    Type.UShort,
    Type.Int,
    Type.UInt,
    Type.Long,
    Type.ULong,
    Type.Float,
    Type.Double,
    Type.ArrayValue(Type.Int, 10),
    Type.ArrayValue(Type.Ptr, 0),
    Type.StructValue(Seq.empty),
    Type.StructValue(Seq(Type.Int)),
    Type.StructValue(Seq(Type.Int, Type.Ptr)),
    Type.Function(Seq.empty, Type.Unit),
    Type.Function(Seq(Type.Int), Type.Void),
    Type.Function(Seq(Type.Int, Type.Long), Type.Nothing),
    Type.Nothing,
    Type.Var(Type.Int),
    Type.Unit,
    Type.Array(Type.Int),
    Type.Ref(global)
  ).foreach { ty =>
    test(s"parse type `${ty.show}`") {
      val Parsed.Success(result, _) = parser.Type.parser.parse(ty.show)
      assert(result == ty)
    }
  }
}
