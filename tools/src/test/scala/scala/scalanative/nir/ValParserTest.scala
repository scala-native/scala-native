package scala.scalanative
package nir

import util.sh
import Shows._

import fastparse.all.Parsed
import org.scalatest._

class ValParserTest extends FlatSpec with Matchers {

  val global = Global.Top("test")
  val noTpe  = Type.None

  "The NIR parser" should "parse `Val.None`" in {
    val none: Val = Val.None
    val Parsed.Success(result, _) =
      parser.Val.None.parse(sh"$none".toString)
    result should be(none)
  }

  it should "parse `Val.True`" in {
    val `true`: Val = Val.True
    val Parsed.Success(result, _) =
      parser.Val.True.parse(sh"${`true`}".toString)
    result should be(`true`)
  }

  it should "parse `Val.False`" in {
    val `false`: Val = Val.False
    val Parsed.Success(result, _) =
      parser.Val.False.parse(sh"${`false`}".toString)
    result should be(`false`)
  }

  it should "parse `Val.Zero`" in {
    val zero: Val = Val.Zero(noTpe)
    val Parsed.Success(result, _) =
      parser.Val.Zero.parse(sh"$zero".toString)
    result should be(zero)
  }

  it should "parse `Val.Undef`" in {
    val undef: Val = Val.Undef(noTpe)
    val Parsed.Success(result, _) =
      parser.Val.Undef.parse(sh"$undef".toString)
    result should be(undef)
  }

  it should "parse `Val.I8`" in {
    val i8: Val                   = Val.I8(1)
    val Parsed.Success(result, _) = parser.Val.I8.parse(sh"$i8".toString)
    result should be(i8)
  }

  it should "parse `Val.I16`" in {
    val i16: Val                  = Val.I16(2)
    val Parsed.Success(result, _) = parser.Val.I16.parse(sh"$i16".toString)
    result should be(i16)
  }

  it should "parse `Val.I32`" in {
    val i32: Val                  = Val.I32(3)
    val Parsed.Success(result, _) = parser.Val.I32.parse(sh"$i32".toString)
    result should be(i32)
  }

  it should "parse `Val.I64`" in {
    val i64: Val                  = Val.I64(4)
    val Parsed.Success(result, _) = parser.Val.I64.parse(sh"$i64".toString)
    result should be(i64)
  }

  it should "parse `Val.F32`" in {
    val f32: Val                  = Val.F32(5.6.toFloat)
    val Parsed.Success(result, _) = parser.Val.F32.parse(sh"$f32".toString)
    result should be(f32)
  }

  it should "parse `Val.F64`" in {
    val f64: Val                  = Val.F64(7.8)
    val Parsed.Success(result, _) = parser.Val.F64.parse(sh"$f64".toString)
    result should be(f64)
  }

  it should "parse `Val.Struct`" in {
    val struct: Val = Val.Struct(global, Seq.empty)
    val Parsed.Success(result, _) =
      parser.Val.Struct.parse(sh"$struct".toString)
    result should be(struct)
  }

  it should "parse `Val.Array`" in {
    val array: Val = Val.Array(noTpe, Seq.empty)
    val Parsed.Success(result, _) =
      parser.Val.Array.parse(sh"$array".toString)
    result should be(array)
  }

  it should "parse `Val.Chars`" in {
    val chars: Val = Val.Chars("test")
    val Parsed.Success(result, _) =
      parser.Val.Chars.parse(sh"$chars".toString)
    result should be(chars)
  }

  it should "parse `Val.Local`" in {
    val local: Val = Val.Local(Local("test", 1), noTpe)
    val Parsed.Success(result, _) =
      parser.Val.Local.parse(sh"$local".toString)
    result should be(local)
  }

  it should "parse `Val.Global`" in {
    val global: Val = Val.Global(this.global, noTpe)
    val Parsed.Success(result, _) =
      parser.Val.Global.parse(sh"$global".toString)
    result should be(global)
  }

  it should "parse `Val.Unit`" in {
    val unit: Val = Val.Unit
    val Parsed.Success(result, _) =
      parser.Val.Unit.parse(sh"$unit".toString)
    result should be(unit)
  }

  it should "parse `Val.Const`" in {
    val const: Val = Val.Const(Val.None)
    val Parsed.Success(result, _) =
      parser.Val.Const.parse(sh"$const".toString)
    result should be(const)
  }

  it should "parse `Val.String`" in {
    val string: Val = Val.String("test")
    val Parsed.Success(result, _) =
      parser.Val.String.parse(sh"$string".toString)
    result should be(string)
  }

}