package scala.scalanative
package nir

import fastparse.all.Parsed
import org.scalatest._

class OpParserTest extends FunSuite {
  val ty     = Type.Int
  val global = Global.Top("foo")
  val local  = Val.Local(Local(0), ty)

  Seq[Op](
    Op.Call(Type.Function(Seq.empty, ty), local, Seq()),
    Op.Call(Type.Function(Seq(ty), Type.Unit), local, Seq(local)),
    Op.Load(ty, local, isVolatile = true),
    Op.Load(ty, local, isVolatile = false),
    Op.Store(ty, local, local, isVolatile = true),
    Op.Store(ty, local, local, isVolatile = false),
    Op.Elem(ty, local, Seq(Val.Int(0))),
    Op.Elem(ty, local, Seq(Val.Int(0), Val.Int(1))),
    Op.Elem(ty, local, Seq(Val.Int(0), Val.Int(1), Val.Int(2))),
    Op.Extract(local, Seq(0)),
    Op.Extract(local, Seq(0, 1)),
    Op.Extract(local, Seq(0, 1, 2)),
    Op.Insert(local, local, Seq(0)),
    Op.Insert(local, local, Seq(0, 1)),
    Op.Insert(local, local, Seq(0, 1, 2)),
    Op.Stackalloc(ty, Val.None),
    Op.Stackalloc(ty, Val.Int(32)),
    Op.Bin(Bin.Iadd, ty, local, local),
    Op.Comp(Comp.Ieq, ty, local, local),
    Op.Conv(Conv.Fpext, ty, local),
    Op.Select(local, local, local),
    Op.Classalloc(global),
    Op.Fieldload(ty, local, global),
    Op.Fieldstore(ty, local, global, local),
    Op.Method(local, Sig.Method("foo", Seq(Type.Unit))),
    Op.Dynmethod(local, Sig.Proxy("foo", Seq.empty)),
    Op.Module(global),
    Op.As(ty, local),
    Op.Is(ty, local),
    Op.Copy(local),
    Op.Sizeof(ty, Type.Int),
    Op.Sizeof(ty, Type.Long),
    Op.Box(ty, local),
    Op.Unbox(ty, local),
    Op.Var(ty),
    Op.Varload(local),
    Op.Varstore(local, local),
    Op.Arrayalloc(ty, local),
    Op.Arrayload(ty, local, local),
    Op.Arraylength(local)
  ).foreach { op =>
    test(s"parse op `${op.show}`") {
      val Parsed.Success(result, _) = parser.Op.parser.parse(op.show)
      assert(result == op)
    }
  }
}
