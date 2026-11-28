package scala.scalanative
package interflow

import scala.collection.mutable

/** An exceptional edge from an emitted instruction to a handler.
 *
 *  Interflow evaluates a basic block as a whole, but the state seen by an
 *  exception handler is the one of the instruction that has thrown. Every
 *  instruction that may throw and is emitted for an instruction with a handler
 *  is a site: it leaves its block for a block of its own, the trampoline, which
 *  starts from the state at the site and jumps to the handler. Whatever the
 *  handler needs materialized is emitted in the trampoline, on the exceptional
 *  path only.
 *
 *  A site emitted while inlining may have no handler in the callee. It is
 *  passed to the caller when the callee is spliced into it, until it reaches an
 *  instruction that has a handler.
 *
 *  A site is created by the evaluation of a block and belongs to the end state
 *  of that block. Only inlining changes it afterwards, with `passTo`, before
 *  the block of the call gets its trampolines.
 *
 *  @param trampoline
 *    label of the trampoline
 *  @param origin
 *    state at the site in the method that emitted it
 *  @param ownHandler
 *    handler of the site in the method that emitted it, `nir.Next.None` if it
 *    has none there
 */
private[interflow] sealed abstract class UnwindSite(
    val trampoline: nir.Local,
    origin: State,
    ownHandler: nir.Next
) {

  /** State at the site in the outermost method it has reached. */
  private var outermost: State = origin

  /** States at the site in the callees inlined below `outermost`, outermost
   *  first, each with the values that link it to the state of its caller.
   */
  private var inlined: List[(State, Seq[nir.Val])] = Nil

  private var reached: nir.Next = ownHandler

  /** Handler of the site, `nir.Next.None` until it reaches a method that has
   *  one.
   */
  final def handler: nir.Next = reached

  /** Values of the site that the state of a caller has to keep. */
  protected def carried: Seq[nir.Val]

  /** Passes a site without a handler to the method its emitter is inlined into.
   *
   *  @param caller
   *    state of the caller at the call
   *  @param roots
   *    values passed from the caller to the callee
   *  @param handler
   *    handler of the call, `nir.Next.None` if it has none
   */
  final def passTo(
      caller: State,
      roots: Seq[nir.Val],
      handler: nir.Next
  ): Unit = {
    assert(reached eq nir.Next.None, "a handled site stays in its method")
    inlined = (outermost, roots ++ carried) :: inlined
    outermost = caller
    reached = handler
  }

  /** State at the site, as seen by the method that handles it. */
  final def state(): State =
    if (inlined.isEmpty) outermost
    else {
      val state = outermost.fullClone(trampoline)
      inlined.foreach { case (frame, roots) => state.inherit(frame, roots) }
      state
    }
}

private[interflow] object UnwindSite {

  /** An instruction with an unwind edge. The exception is known only when it is
   *  thrown: the instruction binds it to `exc` and the trampoline takes it as a
   *  parameter.
   */
  final class Unwinding(
      trampoline: nir.Local,
      val exc: nir.Val.Local,
      origin: State,
      ownHandler: nir.Next
  ) extends UnwindSite(trampoline, origin, ownHandler) {
    protected def carried: Seq[nir.Val] = Nil

    def unwind: nir.Next.Unwind =
      nir.Next.Unwind(exc, nir.Next.Label(trampoline, Seq(exc)))
  }

  /** A throw of an inlined callee, replaced by a jump to the trampoline. The
   *  exception is a value of the callee that may still be virtual, and the
   *  handler takes it as is.
   */
  final class Thrown(trampoline: nir.Local, val value: nir.Val, origin: State)
      extends UnwindSite(trampoline, origin, nir.Next.None) {
    protected def carried: Seq[nir.Val] = Seq(value)
  }

  /** Whether an operation needs an unwind edge when emitted in a try region.
   *  Every operation that is lowered to code that may throw has to be here.
   */
  def mayThrow(op: nir.Op): Boolean = op match {
    case _: nir.Op.Load | _: nir.Op.Store | _: nir.Op.Fence | _: nir.Op.Var |
        _: nir.Op.Varload | _: nir.Op.Varstore | _: nir.Op.AlignmentOf =>
      false
    // An LLVM intrinsic cannot be invoked with an unwind destination
    case nir.Op.Call(_, nir.Val.Global(nir.Global.Member(_, sig), _), _)
        if isLLVMIntrinsic(sig) =>
      false
    case _ =>
      !op.isPure
  }

  /** Lets instructions unwind to their handler directly, rather than through a
   *  trampoline which has nothing to do but jump to it.
   */
  def bypassTrampolines(insts: Seq[nir.Inst]): Seq[nir.Inst] = {
    // Blocks that take an exception and jump
    val forwarders =
      mutable.Map.empty[nir.Local, (nir.Val.Local, nir.Next.Label)]
    insts.sliding(2).foreach {
      case Seq(
            nir.Inst.Label(id, Seq(param)),
            nir.Inst.Jump(next: nir.Next.Label)
          ) =>
        forwarders(id) = (param, next)
      case _ =>
        ()
    }

    // Only the unwind edge of a let or of an unreachable can be redirected
    def keep(next: nir.Next): Unit = next match {
      case nir.Next.None            => ()
      case nir.Next.Label(id, _)    => forwarders -= id
      case nir.Next.Case(_, next)   => keep(next)
      case nir.Next.Unwind(_, next) => keep(next)
    }
    val unwoundTo = mutable.Set.empty[nir.Local]
    if (forwarders.nonEmpty) insts.foreach {
      case nir.Inst.Let(
            _,
            _,
            nir.Next.Unwind(exc, nir.Next.Label(id, Seq(arg)))
          ) if arg == exc =>
        unwoundTo += id
      case nir.Inst.Unreachable(
            nir.Next.Unwind(exc, nir.Next.Label(id, Seq(arg)))
          ) if arg == exc =>
        unwoundTo += id
      case nir.Inst.Let(_, _, unwind)         => keep(unwind)
      case nir.Inst.Jump(next)                => keep(next)
      case nir.Inst.If(_, thenp, elsep)       => keep(thenp); keep(elsep)
      case nir.Inst.Switch(_, default, cases) =>
        keep(default); cases.foreach(keep)
      case nir.Inst.Throw(_, unwind)    => keep(unwind)
      case nir.Inst.Unreachable(unwind) => keep(unwind)
      case _                            => ()
    }
    forwarders --= forwarders.keys.filterNot(unwoundTo.contains).toList

    // The handler may use the exception of its only trampoline directly,
    // instead of taking it as an argument
    if (forwarders.nonEmpty) {
      val params = forwarders.valuesIterator.map(_._1.id).toSet
      val used = mutable.Set.empty[nir.Local]
      val uses = new nir.Traverse {
        override def onVal(value: nir.Val): Unit = value match {
          case nir.Val.Local(id, _) => if (params.contains(id)) used += id
          case _                    => super.onVal(value)
        }
      }
      // A forwarder passes its own parameter on, any other one it passes
      // has to stay defined
      var forwarder: Option[nir.Local] = None
      insts.foreach {
        case nir.Inst.Label(id, _) =>
          forwarder = forwarders.get(id).map(_._1.id)
        case nir.Inst.Jump(nir.Next.Label(_, args)) if forwarder.isDefined =>
          args.foreach {
            case nir.Val.Local(id, _) if forwarder.contains(id) => ()
            case arg => uses.onVal(arg)
          }
          forwarder = None
        case inst =>
          uses.onInst(inst)
      }
      forwarders --= forwarders.collect {
        case (id, (param, _)) if used.contains(param.id) => id
      }.toList
    }

    if (forwarders.isEmpty) insts
    else {
      val result = Seq.newBuilder[nir.Inst]
      var dropped = false
      insts.foreach {
        case nir.Inst.Label(id, _) if forwarders.contains(id) =>
          dropped = true
        case _: nir.Inst.Jump if dropped =>
          dropped = false
        case inst @ nir.Inst.Let(
              _,
              _,
              nir.Next.Unwind(exc, nir.Next.Label(id, _))
            ) if forwarders.contains(id) =>
          val (param, nir.Next.Label(handler, args)) = forwarders(id)
          val handlerArgs = args.map(arg => if (arg == param) exc else arg)
          val unwind =
            nir.Next.Unwind(exc, nir.Next.Label(handler, handlerArgs))
          result += inst.copy(unwind = unwind)(inst.pos, inst.scopeId)
        case inst @ nir.Inst.Unreachable(
              nir.Next.Unwind(exc, nir.Next.Label(id, _))
            ) if forwarders.contains(id) =>
          val (param, nir.Next.Label(handler, args)) = forwarders(id)
          val handlerArgs = args.map(arg => if (arg == param) exc else arg)
          val unwind =
            nir.Next.Unwind(exc, nir.Next.Label(handler, handlerArgs))
          result += nir.Inst.Unreachable(unwind)(inst.pos)
        case inst =>
          result += inst
      }
      result.result()
    }
  }

  private def isLLVMIntrinsic(sig: nir.Sig): Boolean =
    sig.isExtern && (sig.unmangled match {
      case nir.Sig.Extern(id) => id.startsWith("llvm.")
      case _                  => false
    })
}
