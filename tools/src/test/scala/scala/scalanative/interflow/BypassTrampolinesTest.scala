package scala.scalanative
package interflow

import org.junit.Assert._
import org.junit.Test

class BypassTrampolinesTest {
  private implicit val pos: nir.SourcePosition = nir.SourcePosition.NoPosition
  private implicit val scopeId: nir.ScopeId = nir.ScopeId.TopLevel

  private def local(id: Int): nir.Local = nir.Local(id.toLong)
  private def value(id: Int, ty: nir.Type = nir.Rt.Throwable): nir.Val.Local =
    nir.Val.Local(local(id), ty)

  private val fty = nir.Type.Function(Seq.empty, nir.Type.Unit)
  private val callee = nir.Val.Global(
    nir.Global.Top("f").member(nir.Sig.Extern("f")),
    nir.Type.Ptr
  )
  private def call(id: Int, exc: Int, handler: Int, args: nir.Val*) =
    nir.Inst.Let(
      local(id),
      nir.Op.Call(fty, callee, Seq.empty),
      nir.Next.Unwind(value(exc), nir.Next.Label(local(handler), args.toSeq))
    )

  /* The exception of trampoline 10 reaches the handler 30 only through the
   * trampoline 20, so 10 has to keep defining it.
   */
  @Test def keepsParameterPassedByAnotherTrampoline(): Unit = {
    val insts = Seq[nir.Inst](
      nir.Inst.Label(local(0), Seq.empty),
      call(1, exc = 2, handler = 10, value(2)),
      call(3, exc = 4, handler = 30, nir.Val.Null, value(4)),
      nir.Inst.Ret(nir.Val.Unit),
      // trampoline of the first call
      nir.Inst.Label(local(10), Seq(value(11))),
      nir.Inst.Jump(nir.Next.Label(local(15), Seq.empty)),
      // its handler
      nir.Inst.Label(local(15), Seq.empty),
      call(16, exc = 17, handler = 20, value(17)),
      nir.Inst.Ret(nir.Val.Unit),
      // trampoline of the call in the handler
      nir.Inst.Label(local(20), Seq(value(21))),
      nir.Inst.Jump(nir.Next.Label(local(30), Seq(value(11), value(21)))),
      nir.Inst.Label(local(30), Seq(value(31), value(32))),
      nir.Inst.Ret(nir.Val.Unit)
    )
    val result = UnwindSite.bypassTrampolines(insts)
    val labels = result.collect { case nir.Inst.Label(id, _) => id.id }
    assertEquals(
      result.map(_.show).mkString("\n"),
      Seq(0L, 10L, 15L, 30L),
      labels
    )
    val unwinds = result.collect {
      case nir.Inst.Let(id, _, nir.Next.Unwind(_, nir.Next.Label(to, args))) =>
        id.id -> (to.id, args)
    }.toMap
    assertEquals((10L, Seq(value(2))), unwinds(1L))
    assertEquals((30L, Seq(value(11), value(17))), unwinds(16L))
  }
}
