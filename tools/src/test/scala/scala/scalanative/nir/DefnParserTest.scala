package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class DefnParserTest extends FlatSpec with Matchers {

  val global = Global.Top("test")
  val noTpe  = Type.None

  "The NIR parser" should "parse `Defn.Var`" in {
    val `var`: Defn               = Defn.Var(Attrs.None, global, noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Defn.Var.parse(`var`.show)
    result should be(`var`)
  }

  it should "parse `Defn.Const`" in {
    val const: Defn               = Defn.Const(Attrs.None, global, noTpe, Val.None)
    val Parsed.Success(result, _) = parser.Defn.Const.parse(const.show)
    result should be(const)
  }

  it should "parse `Defn.Declare`" in {
    val declare: Defn             = Defn.Declare(Attrs.None, global, noTpe)
    val Parsed.Success(result, _) = parser.Defn.Declare.parse(declare.show)
    result should be(declare)
  }

  it should "parse `Defn.Define`" in {
    val define: Defn              = Defn.Define(Attrs.None, global, noTpe, Seq.empty)
    val Parsed.Success(result, _) = parser.Defn.Define.parse(define.show)
    result should be(define)
  }

  it should "parse `Defn.Struct`" in {
    val struct: Defn              = Defn.Struct(Attrs.None, global, Seq.empty)
    val Parsed.Success(result, _) = parser.Defn.Struct.parse(struct.show)
    result should be(struct)
  }

  it should "parse `Defn.Trait`" in {
    val `trait`: Defn             = Defn.Trait(Attrs.None, global, Seq.empty)
    val Parsed.Success(result, _) = parser.Defn.Trait.parse(`trait`.show)
    result should be(`trait`)
  }

  it should "parse `Defn.Class`" in {
    val `class`: Defn             = Defn.Class(Attrs.None, global, None, Seq.empty)
    val Parsed.Success(result, _) = parser.Defn.Class.parse(`class`.show)
    result should be(`class`)
  }

  it should "parse `Defn.Module`" in {
    val module: Defn              = Defn.Module(Attrs.None, global, None, Seq.empty)
    val Parsed.Success(result, _) = parser.Defn.Module.parse(module.show)
    result should be(module)
  }
}
