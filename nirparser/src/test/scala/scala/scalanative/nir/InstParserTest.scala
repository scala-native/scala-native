package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class InstParserTest extends FunSuite {
  val local = Local(1)
  val next  = Next(local)
  val noTpe = Type.None

  Seq[Inst](
    Inst.None,
    Inst.Label(local, Seq.empty),
    Inst.Let(local, Op.As(noTpe, Val.None), Next.None),
    Inst.Let(local, Op.As(noTpe, Val.None), Next.Unwind(Local(0))),
    Inst.Ret(Val.None),
    Inst.Jump(next),
    Inst.If(Val.None, next, next),
    Inst.Switch(Val.None, next, Seq.empty),
    Inst.Throw(Val.Zero(Type.Ptr), Next.Unwind(Local(0))),
    Inst.Throw(Val.Zero(Type.Ptr), Next.None),
    Inst.Unreachable(Next.Unwind(Local(0))),
    Inst.Unreachable(Next.None)
  ).foreach { inst =>
    test(s"parse inst `${inst.show}`") {
      val Parsed.Success(result, _) = parser.Inst.parser.parse(inst.show)
      assert(result == inst)
    }
  }
}
