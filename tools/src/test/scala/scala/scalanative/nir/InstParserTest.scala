package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class InstParserTest extends FlatSpec with Matchers {

  val local = Local("test", 1)
  val next  = Next(local)
  val noTpe = Type.None

  "The NIR parser" should "parse `Inst.None`" in {
    val none: Inst                = Inst.None
    val Parsed.Success(result, _) = parser.Inst.None.parse(none.show)
    result should be(none)
  }

  it should "parse `Inst.Label`" in {
    val label: Inst               = Inst.Label(local, Seq.empty)
    val Parsed.Success(result, _) = parser.Inst.Label.parse(label.show)
    result should be(label)
  }

  it should "parse `Inst.Let`" in {
    val let: Inst                 = Inst.Let(local, Op.As(noTpe, Val.None))
    val Parsed.Success(result, _) = parser.Inst.Let.parse(let.show)
    result should be(let)
  }

  it should "parse `Inst.Unreachable`" in {
    val unreachable: Inst = Inst.Unreachable
    val Parsed.Success(result, _) =
      parser.Inst.Unreachable.parse(unreachable.show)
    result should be(unreachable)
  }

  it should "parse `Inst.Ret`" in {
    val ret: Inst                 = Inst.Ret(Val.None)
    val Parsed.Success(result, _) = parser.Inst.Ret.parse(ret.show)
    result should be(ret)
  }

  it should "parse `Inst.Jump`" in {
    val jump: Inst                = Inst.Jump(next)
    val Parsed.Success(result, _) = parser.Inst.Jump.parse(jump.show)
    result should be(jump)
  }

  it should "parse `Inst.If`" in {
    val `if`: Inst                = Inst.If(Val.None, next, next)
    val Parsed.Success(result, _) = parser.Inst.If.parse(`if`.show)
    result should be(`if`)
  }

  it should "parse `Inst.Switch`" in {
    val switch: Inst              = Inst.Switch(Val.None, next, Seq.empty)
    val Parsed.Success(result, _) = parser.Inst.Switch.parse(switch.show)
    result should be(switch)
  }

  it should "parse `Inst.Throw` with unwind" in {
    val throw_ : Inst =
      Inst.Throw(Val.Zero(Type.Ptr), Next.Unwind(Local("foobar", 0)))
    val Parsed.Success(result, _) = parser.Inst.Throw.parse(throw_.show)
    result should be(throw_)
  }

  it should "parse `Inst.Throw` without unwind" in {
    val throw_ : Inst             = Inst.Throw(Val.Zero(Type.Ptr), Next.None)
    val Parsed.Success(result, _) = parser.Inst.Throw.parse(throw_.show)
    result should be(throw_)
  }

}
