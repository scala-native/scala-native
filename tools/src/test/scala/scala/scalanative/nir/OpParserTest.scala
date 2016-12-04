package scala.scalanative
package nir

import util.sh
import Shows._

import fastparse.all.Parsed
import org.scalatest._

class OpParserTest extends FlatSpec with Matchers {

  val global = Global.Top("test")
  val noTpe  = Type.None

  "The NIR parser" should "parse `Op.Call`" in {
    val call: Op                  = Op.Call(noTpe, Val.None, Seq.empty)
    val Parsed.Success(result, _) = parser.Op.Call.parse(sh"$call".toString)
    result should be(call)
  }

  it should "parse `Op.Load`" in {
    val load: Op                  = Op.Load(noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Op.Load.parse(sh"$load".toString)
    result should be(load)
  }

  it should "parse `Op.Store`" in {
    val store: Op = Op.Store(noTpe, Val.None, Val.None)
    val Parsed.Success(result, _) =
      parser.Op.Store.parse(sh"$store".toString)
    result should be(store)
  }

  it should "parse `Op.Elem`" in {
    val elem: Op                  = Op.Elem(noTpe, Val.None, Seq.empty)
    val Parsed.Success(result, _) = parser.Op.Elem.parse(sh"$elem".toString)
    result should be(elem)
  }

  it should "parse `Op.Extract`" in {
    val extract: Op = Op.Extract(Val.None, Seq.empty)
    val Parsed.Success(result, _) =
      parser.Op.Extract.parse(sh"$extract".toString)
    result should be(extract)
  }

  it should "parse `Op.Insert`" in {
    val insert: Op = Op.Insert(Val.None, Val.None, Seq.empty)
    val Parsed.Success(result, _) =
      parser.Op.Insert.parse(sh"$insert".toString)
    result should be(insert)
  }

  it should "parse `Op.Stackalloc`" in {
    val stackalloc: Op = Op.Stackalloc(noTpe, Val.None)
    val Parsed.Success(result, _) =
      parser.Op.Stackalloc.parse(sh"$stackalloc".toString)
    result should be(stackalloc)
  }

  it should "parse `Op.Bin`" in {
    val bin: Op                   = Op.Bin(Bin.Iadd, noTpe, Val.None, Val.None)
    val Parsed.Success(result, _) = parser.Op.Bin.parse(sh"$bin".toString)
    result should be(bin)
  }

  it should "parse `Op.Comp`" in {
    val comp: Op                  = Op.Comp(Comp.Ieq, noTpe, Val.None, Val.None)
    val Parsed.Success(result, _) = parser.Op.Comp.parse(sh"$comp".toString)
    result should be(comp)
  }

  it should "parse `Op.Conv`" in {
    val conv: Op                  = Op.Conv(Conv.Trunc, noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Op.Conv.parse(sh"$conv".toString)
    result should be(conv)
  }

  it should "parse `Op.Select`" in {
    val select: Op = Op.Select(Val.None, Val.None, Val.None)
    val Parsed.Success(result, _) =
      parser.Op.Select.parse(sh"$select".toString)
    result should be(select)
  }

  it should "parse `Op.Classalloc`" in {
    val classalloc: Op = Op.Classalloc(global)
    val Parsed.Success(result, _) =
      parser.Op.Classalloc.parse(sh"$classalloc".toString)
    result should be(classalloc)
  }

  it should "parse `Op.Field`" in {
    val field: Op = Op.Field(Val.None, global)
    val Parsed.Success(result, _) =
      parser.Op.Field.parse(sh"$field".toString)
    result should be(field)
  }

  it should "parse `Op.Method`" in {
    val method: Op = Op.Method(Val.None, global)
    val Parsed.Success(result, _) =
      parser.Op.Method.parse(sh"$method".toString)
    result should be(method)
  }

  it should "parse `Op.Module`" in {
    val module: Op = Op.Module(global)
    val Parsed.Success(result, _) =
      parser.Op.Module.parse(sh"$module".toString)
    result should be(module)
  }

  it should "parse `Op.As`" in {
    val as: Op                    = Op.As(noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Op.As.parse(sh"$as".toString)
    result should be(as)
  }

  it should "parse `Op.Is`" in {
    val is: Op                    = Op.Is(noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Op.Is.parse(sh"$is".toString)
    result should be(is)
  }

  it should "parse `Op.Copy`" in {
    val copy: Op                  = Op.Copy(Val.None)
    val Parsed.Success(result, _) = parser.Op.Copy.parse(sh"$copy".toString)
    result should be(copy)
  }

  it should "parse `Op.Sizeof`" in {
    val sizeof: Op = Op.Sizeof(noTpe)
    val Parsed.Success(result, _) =
      parser.Op.Sizeof.parse(sh"$sizeof".toString)
    result should be(sizeof)
  }

  it should "parse `Op.Closure`" in {
    val closure: Op = Op.Closure(noTpe, Val.None, Seq.empty)
    val Parsed.Success(result, _) =
      parser.Op.Closure.parse(sh"$closure".toString)
    result should be(closure)
  }

  it should "parse `Op.Box`" in {
    val box: Op                   = Op.Box(noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Op.Box.parse(sh"$box".toString)
    result should be(box)
  }

  it should "parse `Op.Unbox`" in {
    val unbox: Op                 = Op.Unbox(noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Op.Unbox.parse(sh"$unbox".toString)
    result should be(unbox)
  }

}
