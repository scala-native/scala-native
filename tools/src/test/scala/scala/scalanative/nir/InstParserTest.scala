package scala.scalanative
package nir

import util.sh
import Shows._

import fastparse.all.Parsed
import org.scalatest._

class InstParserTest extends FlatSpec with Matchers {

  val local = Local("test", 1)
  val next  = Next(local)
  val noTpe = Type.None

  "The NIR parser" should "parse `Inst.None`" in {
    val none: Inst = Inst.None
    val Parsed.Success(result, _) =
      parser.Inst.None.parse(sh"$none".toString)
    result should be(none)
  }

  it should "parse `Inst.Label`" in {
    val label: Inst = Inst.Label(local, Seq.empty)
    val Parsed.Success(result, _) =
      parser.Inst.Label.parse(sh"$label".toString)
    result should be(label)
  }

  it should "parse `Inst.Let`" in {
    val let: Inst                 = Inst.Let(local, Op.As(noTpe, Val.None))
    val Parsed.Success(result, _) = parser.Inst.Let.parse(sh"$let".toString)
    result should be(let)
  }

  it should "parse `Inst.Unreachable`" in {
    val unreachable: Inst = Inst.Unreachable
    val Parsed.Success(result, _) =
      parser.Inst.Unreachable.parse(sh"$unreachable".toString)
    result should be(unreachable)
  }

  it should "parse `Inst.Ret`" in {
    val ret: Inst                 = Inst.Ret(Val.None)
    val Parsed.Success(result, _) = parser.Inst.Ret.parse(sh"$ret".toString)
    result should be(ret)
  }

  it should "parse `Inst.Jump`" in {
    val jump: Inst = Inst.Jump(next)
    val Parsed.Success(result, _) =
      parser.Inst.Jump.parse(sh"$jump".toString)
    result should be(jump)
  }

  it should "parse `Inst.If`" in {
    val `if`: Inst = Inst.If(Val.None, next, next)
    val Parsed.Success(result, _) =
      parser.Inst.If.parse(sh"${`if`}".toString)
    result should be(`if`)
  }

  it should "parse `Inst.Switch`" in {
    val switch: Inst = Inst.Switch(Val.None, next, Seq.empty)
    val Parsed.Success(result, _) =
      parser.Inst.Switch.parse(sh"$switch".toString)
    result should be(switch)
  }

  it should "parse `Inst.Invoke`" in {
    val invoke: Inst = Inst.Invoke(noTpe, Val.None, Seq.empty, next, next)
    val Parsed.Success(result, _) =
      parser.Inst.Invoke.parse(sh"$invoke".toString)
    result should be(invoke)
  }

}
