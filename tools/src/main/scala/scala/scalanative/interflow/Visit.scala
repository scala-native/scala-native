package scala.scalanative
package interflow

import scalanative.nir._
import scalanative.linker._
import scalanative.interflow.UseDef.eliminateDeadCode

trait Visit { self: Interflow =>
  def shallVisit(name: Global): Boolean = {
    val orig = originalName(name)

    if (!hasOriginal(orig)) {
      false
    } else {
      val defn     = getOriginal(orig)
      val hasInsts = defn.insts.size > 0
      val hasSema  = linked.infos.contains(defn.name)

      hasInsts && hasSema
    }
  }

  def shallDuplicate(name: Global, argtys: Seq[Type]): Boolean =
    mode match {
      case build.Mode.Debug =>
        false

      case build.Mode.Release =>
        if (!shallVisit(name)) {
          false
        } else {
          val defn =
            getOriginal(name)
          val nonExtern =
            !defn.attrs.isExtern
          val canOptimize =
            defn.attrs.opt != Attr.NoOpt
          val canSpecialize =
            defn.attrs.specialize != Attr.NoSpecialize
          val differentArgumentTypes =
            argumentTypes(name) != argtys

          canOptimize && canSpecialize && nonExtern && differentArgumentTypes
        }
    }

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

  def visitEntries(): Unit =
    mode match {
      case build.Mode.Debug =>
        linked.defns.foreach(defn => visitEntry(defn.name))
      case build.Mode.Release =>
        linked.entries.foreach(visitEntry)
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
        if (hasOriginal(init)) {
          visitRoot(init)
        }
      case _ =>
        ()
    }
  }

  def visitRoot(name: Global): Unit =
    if (shallVisit(name)) {
      pushTodo(name)
    }

  def visitDuplicate(name: Global, argtys: Seq[Type]): Option[Defn.Define] = {
    mode match {
      case build.Mode.Debug =>
        None
      case build.Mode.Release =>
        val dup = duplicateName(name, argtys)
        if (shallVisit(dup)) {
          if (!isDone(dup)) {
            visitMethod(dup)
          }
          maybeDone(dup)
        } else {
          None
        }
    }
  }

  def visitLoop(): Unit = {
    def visit(name: Global): Unit = {
      if (!isDone(name)) {
        visitMethod(name)
      }
    }

    def loop(): Unit = {
      var name = popTodo()
      while (name ne Global.None) {
        visit(name)
        name = popTodo()
      }
    }

    mode match {
      case build.Mode.Debug =>
        allTodo().par.foreach(visit)
      case build.Mode.Release =>
        loop()
    }
  }

  def visitMethod(name: Global): Unit =
    if (!hasStarted(name)) {
      markStarted(name)
      val origname = originalName(name)
      val origdefn = getOriginal(origname)
      try {
        if (shallOpt(name)) {
          setDone(name, visitInstsOpt(name))
        } else {
          visitInstsNoOpt(origdefn)
          setDone(name, origdefn)
          setDone(origname, origdefn)
        }
      } catch {
        case BailOut(msg) =>
          log(s"failed to expand ${name.show}: $msg")
          val baildefn =
            origdefn.copy(attrs = origdefn.attrs.copy(opt = Attr.BailOpt(msg)))
          visitInstsNoOpt(origdefn)
          setDone(name, baildefn)
          setDone(origname, baildefn)
          markBlacklisted(name)
          markBlacklisted(origname)
      }
    }

  def visitInstsNoOpt(defn: Defn.Define): Unit = defn.insts.foreach {
    case Inst.Let(_, Op.Method(obj, sig), _) =>
      obj.ty match {
        case refty: Type.RefKind =>
          val name  = refty.className
          val scope = linked.infos(name).asInstanceOf[ScopeInfo]
          scope.targets(sig).foreach(visitEntry)
        case _ =>
          ()
      }
    case Inst.Let(_, Op.Dynmethod(_, dynsig), _) =>
      linked.dynimpls.foreach {
        case impl @ Global.Member(_, sig) if sig.toProxy == dynsig =>
          visitEntry(impl)
        case _ =>
          ()
      }
    case Inst.Let(_, Op.Module(name), _) =>
      visitEntry(name)
    case Inst.Let(_, Op.Call(_, Val.Global(meth, _), _), _) =>
      visitEntry(meth)
    case _ =>
      ()
  }

  def visitInstsOpt(name: Global): Defn.Define = in(s"visit ${name.show}") {
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
      val origargtys = argumentTypes(name)
      val dupargtys = argtys.zip(origargtys).map {
        case (argty, origty) =>
          // Duplicate argument type should not be
          // less specific than the original declare type.
          if (!Sub.is(argty, origty)) origty else argty
      }
      val Global.Member(top, sig) = orig
      Global.Member(top, Sig.Duplicate(sig, dupargtys))
    }
  }

  def argumentTypes(name: Global): Seq[Type] = name match {
    case Global.Member(_, Sig.Duplicate(_, argtys)) =>
      argtys
    case _ =>
      val Type.Function(argtys, _) = linked.infos(name).asInstanceOf[Method].ty
      argtys
  }

  def originalFunctionType(name: Global): Type = name match {
    case Global.Member(owner, Sig.Duplicate(sig, _)) =>
      originalFunctionType(Global.Member(owner, sig))
    case _ =>
      linked.infos(name).asInstanceOf[Method].ty
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
