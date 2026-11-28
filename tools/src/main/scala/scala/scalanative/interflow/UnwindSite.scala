package scala.scalanative
package interflow

import scala.collection.mutable

/** An exceptional edge from an emitted instruction to a handler.
 *
 *  Interflow evaluates a basic block as a whole, but the state seen by an
 *  exception handler is the one of the instruction that has thrown. Every
 *  instruction that may throw and is emitted for an instruction with a handler
 *  is a site: it unwinds to a block of its own, the trampoline, which starts
 *  from the state at the site and jumps to the handler. Whatever the handler
 *  needs materialized is emitted in the trampoline, on the exceptional path
 *  only.
 *
 *  A site emitted while inlining has no handler of its own. It is passed to the
 *  caller when the callee is spliced into it, until it reaches the instruction
 *  that has a handler. `frames` holds the state at the site in each of these
 *  methods, innermost first, and `links` the values that were passed from each
 *  caller to its callee.
 *
 *  @param tramp
 *    label of the trampoline
 *  @param exc
 *    name of the exception in the unwind edge of the instruction
 *  @param thrown
 *    exception thrown by a callee inlined at this site, or null if the
 *    exception is unknown and comes from the unwind edge of an instruction
 *  @param handler
 *    handler of the site, or null until the site reaches a method that has one
 */
private[interflow] final class UnwindSite(
    val tramp: nir.Local,
    val exc: nir.Val.Local,
    val thrown: nir.Val,
    var frames: List[State],
    var links: List[Seq[nir.Val]],
    var handler: nir.Next.Unwind
) {
  def unwind: nir.Next.Unwind =
    nir.Next.Unwind(exc, nir.Next.Label(tramp, Seq(exc)))

  /** State at the site, as seen by the method that handles it. */
  def state(): State = frames.reverse match {
    case outer :: Nil =>
      outer
    case outer :: inner =>
      val state = outer.fullClone(tramp)
      state.handler = null
      inner.zip(links.reverse).foreach {
        case (frame, link) => state.inherit(frame, link)
      }
      state
    case Nil =>
      util.unreachable
  }
}

private[interflow] object UnwindSite {

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
      var forwarder = false
      insts.foreach {
        case nir.Inst.Label(id, _) =>
          forwarder = forwarders.contains(id)
        case _: nir.Inst.Jump if forwarder =>
          forwarder = false
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
