package scala.scalanative
package nir

import util.sh
import Shows._

import fastparse.all.Parsed
import org.scalatest._

class TypeParserTest extends FlatSpec with Matchers {

  val global = Global.Top("test")

  "The NIR parser" should "parse `Type.None`" in {
    val none: Type = Type.None
    val Parsed.Success(result, _) =
      parser.Type.None.parse(sh"$none".toString)
    result should be(none)
  }

  it should "parse `Type.Void`" in {
    val void: Type = Type.Void
    val Parsed.Success(result, _) =
      parser.Type.Void.parse(sh"$void".toString)
    result should be(void)
  }

  it should "parse `Type.Vararg`" in {
    val vararg: Type = Type.Vararg
    val Parsed.Success(result, _) =
      parser.Type.Vararg.parse(sh"$vararg".toString)
    result should be(vararg)
  }

  it should "parse `Type.Ptr`" in {
    val ptr: Type                 = Type.Ptr
    val Parsed.Success(result, _) = parser.Type.Ptr.parse(sh"$ptr".toString)
    result should be(ptr)
  }

  it should "parse `Type.Bool`" in {
    val bool: Type = Type.Bool
    val Parsed.Success(result, _) =
      parser.Type.Bool.parse(sh"$bool".toString)
    result should be(bool)
  }

  it should "parse `Type.I8`" in {
    val i8: Type                  = Type.I8
    val Parsed.Success(result, _) = parser.Type.I8.parse(sh"$i8".toString)
    result should be(i8)
  }

  it should "parse `Type.I16`" in {
    val i16: Type                 = Type.I16
    val Parsed.Success(result, _) = parser.Type.I16.parse(sh"$i16".toString)
    result should be(i16)
  }

  it should "parse `Type.I32`" in {
    val i32: Type                 = Type.I32
    val Parsed.Success(result, _) = parser.Type.I32.parse(sh"$i32".toString)
    result should be(i32)
  }

  it should "parse `Type.I64`" in {
    val i64: Type                 = Type.I64
    val Parsed.Success(result, _) = parser.Type.I64.parse(sh"$i64".toString)
    result should be(i64)
  }

  it should "parse `Type.F32`" in {
    val f32: Type                 = Type.F32
    val Parsed.Success(result, _) = parser.Type.F32.parse(sh"$f32".toString)
    result should be(f32)
  }

  it should "parse `Type.F64`" in {
    val f64: Type                 = Type.F64
    val Parsed.Success(result, _) = parser.Type.F64.parse(sh"$f64".toString)
    result should be(f64)
  }

  it should "parse `Type.Array`" in {
    val array: Type = Type.Array(Type.None, 10)
    val Parsed.Success(result, _) =
      parser.Type.Array.parse(sh"$array".toString)
    result should be(array)
  }

  it should "parse `Type.Function`" in {
    val function: Type = Type.Function(Seq.empty, Type.None)
    val Parsed.Success(result, _) =
      parser.Type.Function.parse(sh"$function".toString)
    result should be(function)
  }

  it should "parse `Type.Struct`" in {
    val struct: Type = Type.Struct(global, Seq.empty)
    val Parsed.Success(result, _) =
      parser.Type.Struct.parse(sh"$struct".toString)
    result should be(struct)
  }

  it should "parse `Type.Nothing`" in {
    val nothing: Type = Type.Nothing
    val Parsed.Success(result, _) =
      parser.Type.Nothing.parse(sh"$nothing".toString)
    result should be(nothing)
  }

  it should "parse `Type.Unit`" in {
    val unit: Type = Type.Unit
    val Parsed.Success(result, _) =
      parser.Type.Unit.parse(sh"$unit".toString)
    result should be(unit)
  }

  it should "parse `Type.Class`" in {
    val `class`: Type = Type.Class(global)
    val Parsed.Success(result, _) =
      parser.Type.Class.parse(sh"${`class`}".toString)
    result should be(`class`)
  }

  it should "parse `Type.Trait`" in {
    val `trait`: Type = Type.Trait(global)
    val Parsed.Success(result, _) =
      parser.Type.Trait.parse(sh"${`trait`}".toString)
    result should be(`trait`)
  }

  it should "parse `Type.Module`" in {
    val module: Type = Type.Module(global)
    val Parsed.Success(result, _) =
      parser.Type.Module.parse(sh"$module".toString)
    result should be(module)
  }

}