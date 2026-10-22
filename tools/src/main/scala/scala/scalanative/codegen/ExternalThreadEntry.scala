package scala.scalanative.codegen

import scala.scalanative.nir

/** Symbols and guard policy shared by lowering and bootstrap generation. */
private[codegen] object ExternalThreadEntry {
  private implicit val position: nir.SourcePosition =
    nir.SourcePosition.NoPosition

  val EnterManagedName = Extern("scalanative_enter_managed_automatic")
  val EnterManaged = nir.Val.Global(EnterManagedName, nir.Type.Ptr)
  val EnterManagedSig = nir.Type.Function(Nil, nir.Type.Size)
  val LeaveManagedName = Extern("scalanative_leave_managed")
  val LeaveManaged = nir.Val.Global(LeaveManagedName, nir.Type.Ptr)
  val LeaveManagedSig = nir.Type.Function(Seq(nir.Type.Size), nir.Type.Unit)
  val LeaveManagedAndThrowName = Extern("scalanative_leave_managed_and_throw")
  val LeaveManagedAndThrow =
    nir.Val.Global(LeaveManagedAndThrowName, nir.Type.Ptr)
  val LeaveManagedAndThrowSig =
    nir.Type.Function(Seq(nir.Type.Size, nir.Rt.Throwable), nir.Type.Unit)

  val ThreadBootstrapBeginName = Extern("scalanative_threads_init_begin")
  val ThreadBootstrapCompleteName = Extern("scalanative_threads_init_complete")
  val ThreadBootstrapFailedName = Extern("scalanative_threads_init_failed")
  val ThreadBootstrapBeginSig =
    nir.Type.Function(Seq(nir.Type.Bool), nir.Type.Int)
  val ThreadBootstrapCompleteSig =
    nir.Type.Function(Seq(nir.Type.Bool), nir.Type.Int)
  val ThreadBootstrapFailedSig = nir.Type.Function(Seq.empty, nir.Type.Unit)
  val ThreadBootstrapBegin =
    nir.Val.Global(ThreadBootstrapBeginName, nir.Type.Ptr)
  val ThreadBootstrapComplete =
    nir.Val.Global(ThreadBootstrapCompleteName, nir.Type.Ptr)
  val ThreadBootstrapFailed =
    nir.Val.Global(ThreadBootstrapFailedName, nir.Type.Ptr)
  val ThreadBootstrapDecls = Seq(
    nir.Defn.Declare(
      nir.Attrs.None,
      ThreadBootstrapBeginName,
      ThreadBootstrapBeginSig
    ),
    nir.Defn.Declare(
      nir.Attrs.None,
      ThreadBootstrapCompleteName,
      ThreadBootstrapCompleteSig
    ),
    nir.Defn.Declare(
      nir.Attrs.None,
      ThreadBootstrapFailedName,
      ThreadBootstrapFailedSig
    )
  )

  private val JavaThreadModule = nir.Global.Top("java.lang.Thread$")
  val AdoptThreadName =
    JavaThreadModule.member(nir.Sig.Extern("scalanative_externalThread_adopt"))
  val ReleaseThreadName =
    JavaThreadModule.member(
      nir.Sig.Extern("scalanative_externalThread_release")
    )

  def shouldGuard(
      defn: nir.Defn.Define
  ): Boolean =
    defn.attrs.isExtern && {
      val bootstrap = defn.name match {
        case Extern("ScalaNativeInit" | "main")      => true
        case `AdoptThreadName` | `ReleaseThreadName` => true
        case _                                       => false
      }
      // GC, exception, module and continuation hooks already own their execution
      // context. A collector notification must never attempt GC admission.
      val runtimeHook =
        defn.name.top.id.startsWith("scala.scalanative.runtime.") ||
          defn.name.top.id.startsWith("java.lang.ref.WeakReferenceRegistry$")
      !bootstrap && !runtimeHook
    }
}
