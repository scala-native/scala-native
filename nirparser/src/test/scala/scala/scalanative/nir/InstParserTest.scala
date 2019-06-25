package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class InstParserTest extends FunSuite {
  val local  = Local(1)
  val next   = Next(local)
  val noTpe  = Type.Unit
  val value  = Val.Int(42)
  val unwind = Next.Unwind(Val.Local(local, nir.Rt.Object), next)

  Seq[Inst](
    Inst.Label(local, Seq.empty),
    Inst.Let(local, Op.As(noTpe, value), Next.None),
    Inst.Let(local, Op.As(noTpe, value), unwind),
    Inst.Ret(value),
    Inst.Jump(next),
    Inst.If(value, next, next),
    Inst.Switch(value, next, Seq.empty),
    Inst.Throw(Val.Zero(Type.Ptr), Next.None),
    Inst.Throw(Val.Zero(Type.Ptr), unwind),
    Inst.Unreachable(Next.None),
    Inst.Unreachable(unwind)
  ).foreach { inst =>
    test(s"parse inst `${inst.show}`") {
      val Parsed.Success(result, _) = parser.Inst.parser.parse(inst.show)
      assert(result == inst)
    }
  }
}
