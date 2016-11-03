package scala.scalanative
package nir

import util.sh
import Shows._

import fastparse.all.Parsed
import org.scalatest._

class DefnParserTest extends FlatSpec with Matchers {

  val global = Global.Top("test")
  val noTpe  = Type.None

  "The NIR parser" should "parse `Defn.Var`" in {
    val `var`: Defn = Defn.Var(Attrs.None, global, noTpe, Val.None)
    val Parsed.Success(result, _) =
      parser.Defn.Var.parse(sh"${`var`}".toString)
    result should be(`var`)
  }

  it should "parse `Defn.Const`" in {
    val const: Defn = Defn.Const(Attrs.None, global, noTpe, Val.None)
    val Parsed.Success(result, _) =
      parser.Defn.Const.parse(sh"$const".toString)
    result should be(const)
  }

  it should "parse `Defn.Declare`" in {
    val declare: Defn = Defn.Declare(Attrs.None, global, noTpe)
    val Parsed.Success(result, _) =
      parser.Defn.Declare.parse(sh"$declare".toString)
    result should be(declare)
  }

  it should "parse `Defn.Define`" in {
    val define: Defn = Defn.Define(Attrs.None, global, noTpe, Seq.empty)
    val Parsed.Success(result, _) =
      parser.Defn.Define.parse(sh"$define".toString)
    result should be(define)
  }

  it should "parse `Defn.Struct`" in {
    val struct: Defn = Defn.Struct(Attrs.None, global, Seq.empty)
    val Parsed.Success(result, _) =
      parser.Defn.Struct.parse(sh"$struct".toString)
    result should be(struct)
  }

  it should "parse `Defn.Trait`" in {
    val `trait`: Defn = Defn.Trait(Attrs.None, global, Seq.empty)
    val Parsed.Success(result, _) =
      parser.Defn.Trait.parse(sh"${`trait`}".toString)
    result should be(`trait`)
  }

  it should "parse `Defn.Class`" in {
    val `class`: Defn = Defn.Class(Attrs.None, global, None, Seq.empty)
    val Parsed.Success(result, _) =
      parser.Defn.Class.parse(sh"${`class`}".toString)
    result should be(`class`)
  }

  it should "parse `Defn.Module`" in {
    val module: Defn = Defn.Module(Attrs.None, global, None, Seq.empty)
    val Parsed.Success(result, _) =
      parser.Defn.Module.parse(sh"$module".toString)
    result should be(module)
  }

}