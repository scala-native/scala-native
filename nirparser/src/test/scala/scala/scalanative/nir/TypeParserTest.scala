package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class TypeParserTest extends FlatSpec with Matchers {

  val global = Global.Top("test")

  "The NIR parser" should "parse `Type.None`" in {
    val none: Type                = Type.None
    val Parsed.Success(result, _) = parser.Type.None.parse(none.show)
    result should be(none)
  }

  it should "parse `Type.Void`" in {
    val void: Type                = Type.Void
    val Parsed.Success(result, _) = parser.Type.Void.parse(void.show)
    result should be(void)
  }

  it should "parse `Type.Vararg`" in {
    val vararg: Type              = Type.Vararg
    val Parsed.Success(result, _) = parser.Type.Vararg.parse(vararg.show)
    result should be(vararg)
  }

  it should "parse `Type.Ptr`" in {
    val ptr: Type                 = Type.Ptr
    val Parsed.Success(result, _) = parser.Type.Ptr.parse(ptr.show)
    result should be(ptr)
  }

  it should "parse `Type.Bool`" in {
    val bool: Type                = Type.Bool
    val Parsed.Success(result, _) = parser.Type.Bool.parse(bool.show)
    result should be(bool)
  }

  it should "parse `Type.Byte`" in {
    val i8: Type                  = Type.Byte
    val Parsed.Success(result, _) = parser.Type.Byte.parse(i8.show)
    result should be(i8)
  }

  it should "parse `Type.Short`" in {
    val i16: Type                 = Type.Short
    val Parsed.Success(result, _) = parser.Type.Short.parse(i16.show)
    result should be(i16)
  }

  it should "parse `Type.Int`" in {
    val i32: Type                 = Type.Int
    val Parsed.Success(result, _) = parser.Type.Int.parse(i32.show)
    result should be(i32)
  }

  it should "parse `Type.Long`" in {
    val i64: Type                 = Type.Long
    val Parsed.Success(result, _) = parser.Type.Long.parse(i64.show)
    result should be(i64)
  }

  it should "parse `Type.Float`" in {
    val f32: Type                 = Type.Float
    val Parsed.Success(result, _) = parser.Type.Float.parse(f32.show)
    result should be(f32)
  }

  it should "parse `Type.Double`" in {
    val f64: Type                 = Type.Double
    val Parsed.Success(result, _) = parser.Type.Double.parse(f64.show)
    result should be(f64)
  }

  it should "parse `Type.Array`" in {
    val array: Type               = Type.Array(Type.None, 10)
    val Parsed.Success(result, _) = parser.Type.Array.parse(array.show)
    result should be(array)
  }

  it should "parse `Type.Function`" in {
    val function: Type            = Type.Function(Seq.empty, Type.None)
    val Parsed.Success(result, _) = parser.Type.Function.parse(function.show)
    result should be(function)
  }

  it should "parse `Type.Struct`" in {
    val struct: Type              = Type.Struct(global, Seq.empty)
    val Parsed.Success(result, _) = parser.Type.Struct.parse(struct.show)
    result should be(struct)
  }

  it should "parse `Type.Nothing`" in {
    val nothing: Type             = Type.Nothing
    val Parsed.Success(result, _) = parser.Type.Nothing.parse(nothing.show)
    result should be(nothing)
  }

  it should "parse `Type.Unit`" in {
    val unit: Type                = Type.Unit
    val Parsed.Success(result, _) = parser.Type.Unit.parse(unit.show)
    result should be(unit)
  }

  it should "parse `Type.Class`" in {
    val `class`: Type             = Type.Class(global)
    val Parsed.Success(result, _) = parser.Type.Class.parse(`class`.show)
    result should be(`class`)
  }

  it should "parse `Type.Trait`" in {
    val `trait`: Type             = Type.Trait(global)
    val Parsed.Success(result, _) = parser.Type.Trait.parse(`trait`.show)
    result should be(`trait`)
  }

  it should "parse `Type.Module`" in {
    val module: Type              = Type.Module(global)
    val Parsed.Success(result, _) = parser.Type.Module.parse(module.show)
    result should be(module)
  }
}
