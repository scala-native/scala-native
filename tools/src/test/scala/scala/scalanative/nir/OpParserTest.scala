package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class OpParserTest extends FlatSpec with Matchers {

  val global = Global.Top("test")
  val noTpe  = Type.None

  "The NIR parser" should "parse `Op.Call` without unwind" in {
    val call: Op                  = Op.Call(noTpe, Val.None, Seq.empty, Next.None)
    val Parsed.Success(result, _) = parser.Op.Call.parse(call.show)
    result should be(call)
  }

  "The NIR parser" should "parse `Op.Call` with unwind" in {
    val call: Op =
      Op.Call(noTpe, Val.None, Seq.empty, Next.Unwind(Local("foobar", 0)))
    val Parsed.Success(result, _) = parser.Op.Call.parse(call.show)
    result should be(call)
  }

  it should "parse non-volatile `Op.Load`" in {
    val load: Op                  = Op.Load(noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Op.Load.parse(load.show)
    result should be(load)
  }

  it should "parse volatile `Op.Load`" in {
    val load: Op                  = Op.Load(noTpe, Val.None, isVolatile = true)
    val Parsed.Success(result, _) = parser.Op.Load.parse(load.show)
    result should be(load)
  }

  it should "parse non-volatile `Op.Store`" in {
    val store: Op                 = Op.Store(noTpe, Val.None, Val.None)
    val Parsed.Success(result, _) = parser.Op.Store.parse(store.show)
    result should be(store)
  }

  it should "parse volatile `Op.Store`" in {
    val store: Op                 = Op.Store(noTpe, Val.None, Val.None, isVolatile = true)
    val Parsed.Success(result, _) = parser.Op.Store.parse(store.show)
    result should be(store)
  }

  it should "parse `Op.Elem`" in {
    val elem: Op                  = Op.Elem(noTpe, Val.None, Seq.empty)
    val Parsed.Success(result, _) = parser.Op.Elem.parse(elem.show)
    result should be(elem)
  }

  it should "parse `Op.Extract`" in {
    val extract: Op               = Op.Extract(Val.None, Seq.empty)
    val Parsed.Success(result, _) = parser.Op.Extract.parse(extract.show)
    result should be(extract)
  }

  it should "parse `Op.Insert`" in {
    val insert: Op                = Op.Insert(Val.None, Val.None, Seq.empty)
    val Parsed.Success(result, _) = parser.Op.Insert.parse(insert.show)
    result should be(insert)
  }

  it should "parse `Op.Stackalloc`" in {
    val stackalloc: Op            = Op.Stackalloc(noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Op.Stackalloc.parse(stackalloc.show)
    result should be(stackalloc)
  }

  it should "parse `Op.Bin`" in {
    val bin: Op                   = Op.Bin(Bin.Iadd, noTpe, Val.None, Val.None)
    val Parsed.Success(result, _) = parser.Op.Bin.parse(bin.show)
    result should be(bin)
  }

  it should "parse `Op.Comp`" in {
    val comp: Op                  = Op.Comp(Comp.Ieq, noTpe, Val.None, Val.None)
    val Parsed.Success(result, _) = parser.Op.Comp.parse(comp.show)
    result should be(comp)
  }

  it should "parse `Op.Conv`" in {
    val conv: Op                  = Op.Conv(Conv.Trunc, noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Op.Conv.parse(conv.show)
    result should be(conv)
  }

  it should "parse `Op.Select`" in {
    val select: Op                = Op.Select(Val.None, Val.None, Val.None)
    val Parsed.Success(result, _) = parser.Op.Select.parse(select.show)
    result should be(select)
  }

  it should "parse `Op.Classalloc`" in {
    val classalloc: Op            = Op.Classalloc(global)
    val Parsed.Success(result, _) = parser.Op.Classalloc.parse(classalloc.show)
    result should be(classalloc)
  }

  it should "parse `Op.Field`" in {
    val field: Op = Op.Field(Val.None, global)
    val Parsed.Success(result, _) =
      parser.Op.Field.parse(field.show)
    result should be(field)
  }

  it should "parse `Op.Method`" in {
    val method: Op                = Op.Method(Val.None, global)
    val Parsed.Success(result, _) = parser.Op.Method.parse(method.show)
    result should be(method)
  }

  it should "parse `Op.Dynmethod`" in {
    val signature     = "signature"
    val dynmethod: Op = Op.Dynmethod(Val.None, signature)
    val Parsed.Success(result, _) =
      parser.Op.Dynmethod.parse(dynmethod.show)
    result should be(dynmethod)
  }
  it should "parse `Op.Module` without unwind" in {
    val module: Op                = Op.Module(global, Next.None)
    val Parsed.Success(result, _) = parser.Op.Module.parse(module.show)
    result should be(module)
  }

  it should "parse `Op.Module` with unwind" in {
    val module: Op                = Op.Module(global, Next.Unwind(Local("foobar", 0)))
    val Parsed.Success(result, _) = parser.Op.Module.parse(module.show)
    result should be(module)
  }

  it should "parse `Op.As`" in {
    val as: Op                    = Op.As(noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Op.As.parse(as.show)
    result should be(as)
  }

  it should "parse `Op.Is`" in {
    val is: Op                    = Op.Is(noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Op.Is.parse(is.show)
    result should be(is)
  }

  it should "parse `Op.Copy`" in {
    val copy: Op                  = Op.Copy(Val.None)
    val Parsed.Success(result, _) = parser.Op.Copy.parse(copy.show)
    result should be(copy)
  }

  it should "parse `Op.Sizeof`" in {
    val sizeof: Op                = Op.Sizeof(noTpe)
    val Parsed.Success(result, _) = parser.Op.Sizeof.parse(sizeof.show)
    result should be(sizeof)
  }

  it should "parse `Op.Closure`" in {
    val closure: Op               = Op.Closure(noTpe, Val.None, Seq.empty)
    val Parsed.Success(result, _) = parser.Op.Closure.parse(closure.show)
    result should be(closure)
  }

  it should "parse `Op.Box`" in {
    val box: Op                   = Op.Box(noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Op.Box.parse(box.show)
    result should be(box)
  }

  it should "parse `Op.Unbox`" in {
    val unbox: Op                 = Op.Unbox(noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Op.Unbox.parse(unbox.show)
    result should be(unbox)
  }
}
