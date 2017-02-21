package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class ValParserTest extends FlatSpec with Matchers {

  val global = Global.Top("test")
  val noTpe  = Type.None

  "The NIR parser" should "parse `Val.None`" in {
    val none: Val                 = Val.None
    val Parsed.Success(result, _) = parser.Val.None.parse(none.show)
    result should be(none)
  }

  it should "parse `Val.True`" in {
    val `true`: Val               = Val.True
    val Parsed.Success(result, _) = parser.Val.True.parse(`true`.show)
    result should be(`true`)
  }

  it should "parse `Val.False`" in {
    val `false`: Val              = Val.False
    val Parsed.Success(result, _) = parser.Val.False.parse(`false`.show)
    result should be(`false`)
  }

  it should "parse `Val.Zero`" in {
    val zero: Val                 = Val.Zero(noTpe)
    val Parsed.Success(result, _) = parser.Val.Zero.parse(zero.show)
    result should be(zero)
  }

  it should "parse `Val.Undef`" in {
    val undef: Val                = Val.Undef(noTpe)
    val Parsed.Success(result, _) = parser.Val.Undef.parse(undef.show)
    result should be(undef)
  }

  it should "parse `Val.Byte`" in {
    val i8: Val                   = Val.Byte(1)
    val Parsed.Success(result, _) = parser.Val.Byte.parse(i8.show)
    result should be(i8)
  }

  it should "parse `Val.Short`" in {
    val i16: Val                  = Val.Short(2)
    val Parsed.Success(result, _) = parser.Val.Short.parse(i16.show)
    result should be(i16)
  }

  it should "parse `Val.Int`" in {
    val i32: Val                  = Val.Int(3)
    val Parsed.Success(result, _) = parser.Val.Int.parse(i32.show)
    result should be(i32)
  }

  it should "parse `Val.Long`" in {
    val i64: Val                  = Val.Long(4)
    val Parsed.Success(result, _) = parser.Val.Long.parse(i64.show)
    result should be(i64)
  }

  it should "parse `Val.Float`" in {
    val f32: Val                  = Val.Float(5.6.toFloat)
    val Parsed.Success(result, _) = parser.Val.Float.parse(f32.show)
    result should be(f32)
  }

  it should "parse `Val.Double`" in {
    val f64: Val                  = Val.Double(7.8)
    val Parsed.Success(result, _) = parser.Val.Double.parse(f64.show)
    result should be(f64)
  }

  it should "parse `Val.Struct`" in {
    val struct: Val               = Val.Struct(global, Seq.empty)
    val Parsed.Success(result, _) = parser.Val.Struct.parse(struct.show)
    result should be(struct)
  }

  it should "parse `Val.Array`" in {
    val array: Val                = Val.Array(noTpe, Seq.empty)
    val Parsed.Success(result, _) = parser.Val.Array.parse(array.show)
    result should be(array)
  }

  it should "parse `Val.Chars`" in {
    val chars: Val                = Val.Chars("test")
    val Parsed.Success(result, _) = parser.Val.Chars.parse(chars.show)
    result should be(chars)
  }

  it should "parse `Val.Local`" in {
    val local: Val                = Val.Local(Local("test", 1), noTpe)
    val Parsed.Success(result, _) = parser.Val.Local.parse(local.show)
    result should be(local)
  }

  it should "parse `Val.Global`" in {
    val global: Val               = Val.Global(this.global, noTpe)
    val Parsed.Success(result, _) = parser.Val.Global.parse(global.show)
    result should be(global)
  }

  it should "parse `Val.Unit`" in {
    val unit: Val                 = Val.Unit
    val Parsed.Success(result, _) = parser.Val.Unit.parse(unit.show)
    result should be(unit)
  }

  it should "parse `Val.Const`" in {
    val const: Val                = Val.Const(Val.None)
    val Parsed.Success(result, _) = parser.Val.Const.parse(const.show)
    result should be(const)
  }

  it should "parse `Val.String`" in {
    val string: Val               = Val.String("test")
    val Parsed.Success(result, _) = parser.Val.String.parse(string.show)
    result should be(string)
  }
}
