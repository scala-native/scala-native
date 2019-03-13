package scala.scalanative
package interflow

import scalanative.nir._
import scalanative.linker._
import scalanative.interflow.UseDef.eliminateDeadCode

trait Visit { self: Interflow =>
  def shallVisit(name: Global): Boolean =
    originals
      .get(originalName(name))
      .fold {
        false
      } { defn =>
        val notExtern = !defn.attrs.isExtern
        val hasInsts  = defn.insts.size > 0
        val hasSema   = linked.infos.contains(defn.name)

        notExtern && hasInsts && hasSema
      }

  def shallDuplicate(name: Global, argtys: Seq[Type]): Boolean =
    mode match {
      case build.Mode.Debug =>
        false
      case build.Mode.Release =>
        argumentTypes(name) != argtys
    }

  def visitEntry(name: Global): Unit = {
    if (!name.isTop) {
      visitEntry(name.top)
    }
    linked.infos(name) match {
      case meth: Method =>
        visitRoot(name)
      case cls: Class if cls.isModule =>
        val init = cls.name member Sig.Ctor(Seq.empty)
        if (originals.contains(init)) {
          visitRoot(init)
        }
      case _ =>
        ()
    }
  }

  def visitRoot(name: Global): Unit =
    if (shallVisit(name)) {
      todo.enqueue(name)
    }

  def visitDuplicate(name: Global, argtys: Seq[Type]): Option[Defn.Define] = {
    val dup = duplicateName(name, argtys)
    if (shallVisit(dup)) {
      if (!done.contains(dup)) {
        visitMethod(dup)
      }
      done.get(dup)
    } else {
      None
    }
  }

  def visitLoop(): Unit = {
    while (todo.nonEmpty) {
      val name = todo.dequeue()
      if (!done.contains(name)) {
        visitMethod(name)
      }
    }
  }

  def visitMethod(name: Global): Unit =
    if (!started.contains(name)) {
      started += name
      try {
        done(name) = visitInsts(name)
      } catch {
        case exc: BailOut =>
          blacklist += name
          log(s"failed to expand ${name.show}: ${exc.toString}")
          done(name) = originals(originalName(name))
      }
    }

  def visitInsts(name: Global): Defn.Define = in(s"visit ${name.show}") {
    val orig     = originalName(name)
    val origtys  = argumentTypes(orig)
    val origdefn = originals(orig)
    val argtys   = argumentTypes(name)

    // Wrap up the result.
    def result(retty: Type, rawInsts: Seq[Inst]) =
      origdefn.copy(name = name,
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
    val blocks = util.ScopedVar.scoped(
      blockFresh := fresh
    ) {
      process(origdefn.insts.toArray, args, state, inline = false)
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

  def originalName(name: Global): Global = name match {
    case Global.Member(owner, Sig.Duplicate(origSig, argtys)) =>
      originalName(Global.Member(owner, origSig))
    case _ =>
      name
  }

  def duplicateName(name: Global, argtys: Seq[Type]): Global = {
    val orig = originalName(name)
    if (!shallDuplicate(orig, argtys)) {
      orig
    } else {
      val Global.Member(top, sig) = orig
      Global.Member(top, Sig.Duplicate(sig, argtys))
    }
  }

  def argumentTypes(name: Global): Seq[Type] = name match {
    case Global.Member(_, Sig.Duplicate(_, argtys)) =>
      argtys
    case _ =>
      val Type.Function(argtys, _) = linked.infos(name).asInstanceOf[Method].ty
      argtys
  }

  def process(insts: Array[Inst],
              args: Seq[Val],
              state: State,
              inline: Boolean): Seq[MergeBlock] = {
    val processor =
      MergeProcessor.fromEntry(insts, args, state, inline, blockFresh.get, this)

    util.ScopedVar.scoped(
      mergeProcessor := processor
    ) {
      while (!processor.done()) {
        processor.advance()
      }

      processor.toSeq()
    }
  }
}
