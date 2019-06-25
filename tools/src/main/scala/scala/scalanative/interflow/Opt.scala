package scala.scalanative
package interflow

import scalanative.nir._
import scalanative.linker._

trait Opt { self: Interflow =>
  def shallOpt(name: Global): Boolean = {
    val defn =
      getOriginal(originalName(name))
    val noUnwind = defn.insts.forall {
      case Inst.Let(_, _, unwind)   => unwind == Next.None
      case Inst.Throw(_, unwind)    => unwind == Next.None
      case Inst.Unreachable(unwind) => unwind == Next.None
      case _                        => true
    }

    defn.attrs.opt != Attr.NoOpt && noUnwind
  }

  def opt(name: Global): Defn.Define = in(s"visit ${name.show}") {
    val orig     = originalName(name)
    val origtys  = argumentTypes(orig)
    val origdefn = getOriginal(orig)
    val argtys   = argumentTypes(name)

    // Wrap up the result.
    def result(retty: Type, rawInsts: Seq[Inst]) =
      origdefn.copy(name = name,
                    attrs = origdefn.attrs.copy(opt = Attr.DidOpt),
                    ty = Type.Function(argtys, retty),
                    insts = ControlFlow.removeDeadBlocks(rawInsts))

    // Create new fresh and state for the first basic block.
    val fresh = Fresh(0)
    val state = new State(Local(0))

    // Compute opaque fresh locals for the arguments. Argument types
    // are always a subtype of the original declared type, but in
    // some cases they might not be obviously related, despite
    // having the same concrete allocated class inhabitants.
    val args = argtys.zip(origtys).map {
      case (argty, origty) =>
        val ty = if (!Sub.is(argty, origty)) {
          log(
            s"using original argument type ${origty.show} instead of ${argty.show}")
          origty
        } else {
          argty
        }
        Val.Local(fresh(), ty)
    }

    // If any of the argument types is nothing, this method
    // is never going to be called, so we don't have to visit it.
    if (args.exists(_.ty == Type.Nothing)) {
      val insts = Seq(Inst.Label(Local(0), args), Inst.Unreachable(Next.None))
      return result(Type.Nothing, insts)
    }

    // Run a merge processor starting from the entry basic block.
    val blocks = try {
      pushBlockFresh(fresh)
      process(origdefn.insts.toArray, args, state, inline = false)
    } finally {
      popBlockFresh()
    }

    // Collect instructions, materialize all returned values
    // and compute the result type.
    val insts = blocks.flatMap { block =>
      block.cf = block.cf match {
        case Inst.Ret(retv) =>
          Inst.Ret(block.end.materialize(retv))
        case Inst.Throw(excv, unwind) =>
          Inst.Throw(block.end.materialize(excv), unwind)
        case cf =>
          cf
      }
      block.toInsts()
    }
    val rets = insts.collect {
      case Inst.Ret(v) => v.ty
    }
    val retty = rets match {
      case Seq()   => Type.Nothing
      case Seq(ty) => ty
      case tys     => Sub.lub(tys)
    }

    // Interflow usually infers better types on our erased type system
    // than scalac, yet we live it a benefit of the doubt and make sure
    // that if original return type is more specific, we keep it as is.
    val origRetty = {
      val Type.Function(_, ty) = origdefn.ty
      ty
    }
    val resRetty =
      if (!Sub.is(retty, origRetty)) {
        log(
          s"inferred type ${retty.show} is less precise than ${origRetty.show}")
        origRetty
      } else {
        retty
      }

    result(resRetty, insts)
  }

  def process(insts: Array[Inst],
              args: Seq[Val],
              state: State,
              inline: Boolean): Seq[MergeBlock] = {
    val processor =
      MergeProcessor.fromEntry(insts, args, state, inline, blockFresh, this)

    try {
      pushMergeProcessor(processor)

      while (!processor.done()) {
        processor.advance()
      }
    } finally {
      popMergeProcessor()
    }

    processor.toSeq()
  }
}
