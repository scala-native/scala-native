package scala.scalanative
package interflow

import scala.scalanative.nir._
import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.scalanative.linker._
import scala.collection.mutable
import scala.scalanative.util.ScopedVar.scoped

trait Opt { self: Interflow =>

  def shallOpt(name: Global.Member): Boolean = {
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

  def opt(name: Global.Member): Defn.Define = in(s"visit ${name.show}") {
    val orig = originalName(name)
    val origtys = argumentTypes(orig)
    val origdefn = getOriginal(orig)
    val argtys = argumentTypes(name)
    val Inst.Label(_, origargs) = origdefn.insts.head: @unchecked
    implicit val pos = origdefn.pos
    // Wrap up the result.
    def result(retty: Type, rawInsts: Seq[Inst], debugInfo: DebugInfo) = {
      val insts = ControlFlow.removeDeadBlocks(rawInsts)
      // TODO: filter-out unreachable scopes
      val scopes = debugInfo.lexicalScopes.sorted
      origdefn.copy(
        name = name,
        attrs = origdefn.attrs.copy(opt = Attr.DidOpt),
        ty = Type.Function(argtys, retty),
        insts = insts,
        debugInfo = debugInfo.copy(lexicalScopes = scopes)
      )(origdefn.pos)
    }

    // Create new fresh and state for the first basic block.
    val fresh = Fresh(0)
    val state = new State(Local(0))(preserveDebugInfo)

    // Interflow usually infers better types on our erased type system
    // than scalac, yet we live it as a benefit of the doubt and make sure
    // that if original return type is more specific, we keep it as is.
    val Type.Function(_, origRetTy) = origdefn.ty

    // Compute opaque fresh locals for the arguments. Argument types
    // are always a subtype of the original declared type, but in
    // some cases they might not be obviously related, despite
    // having the same concrete allocated class inhabitants.
    val args = argtys.zip(origtys).zip(origargs).map {
      case ((argty, origty), origarg) =>
        val ty = if (!Sub.is(argty, origty)) {
          log(
            s"using original argument type ${origty.show} instead of ${argty.show}"
          )
          origty
        } else argty

        val id = fresh()
        if (preserveDebugInfo) {
          origdefn.debugInfo.localNames
            .get(origarg.id)
            .foreach(state.localNames.update(id, _))
        }
        Val.Local(id, ty)
    }

    // If any of the argument types is nothing, this method
    // is never going to be called, so we don't have to visit it.
    if (args.exists(_.ty == Type.Nothing)) {
      val insts = Seq(Inst.Label(Local(0), args), Inst.Unreachable(Next.None))
      result(Type.Nothing, insts, DebugInfo.empty)
    } else
      scoped(
        currentFreshScope := Fresh(0L),
        currentLexicalScopes := mutable.UnrolledBuffer.empty
      ) {
        // Run a merge processor starting from the entry basic block.
        val blocks =
          try {
            pushBlockFresh(fresh)
            process(
              origdefn.insts.toArray,
              debugInfo = origdefn.debugInfo,
              args = args,
              state = state,
              doInline = false,
              retTy = origRetTy,
              parentScopeId = ScopeId.TopLevel
            )
          } finally {
            popBlockFresh()
          }

        // Collect instructions, materialize all returned values
        // and compute the result type.
        val insts = blocks.flatMap { block =>
          block.cf = block.cf match {
            case inst @ Inst.Ret(retv) =>
              Inst.Ret(block.end.materialize(retv))(inst.pos)
            case inst @ Inst.Throw(excv, unwind) =>
              Inst.Throw(block.end.materialize(excv), unwind)(inst.pos)
            case cf =>
              cf
          }
          block.toInsts()
        }
        val debugInfo: DebugInfo = if (preserveDebugInfo) {
          val localNames = mutable.OpenHashMap.empty[Local, LocalName]
          for {
            block <- blocks
            state = block.end
          } {
            localNames.addMissing(block.end.localNames)
          }
          DebugInfo(
            localNames = localNames.toMap,
            lexicalScopes = currentLexicalScopes.get.toSeq
          )
        } else DebugInfo.empty

        val rets = insts.collect {
          case Inst.Ret(v) => v.ty
        }

        val retty0 = rets match {
          case Seq()   => Type.Nothing
          case Seq(ty) => ty
          case tys     => Sub.lub(tys, Some(origRetTy))
        }
        // Make sure to not override expected BoxedUnit with primitive Unit
        val retty =
          if (retty0 == Type.Unit && origRetTy.isInstanceOf[Type.Ref]) origRetTy
          else retty0

        result(retty, insts, debugInfo)
      }
  }

  def process(
      insts: Array[Inst],
      debugInfo: DebugInfo,
      args: Seq[Val],
      state: State,
      doInline: Boolean,
      retTy: Type,
      parentScopeId: ScopeId
  ): Seq[MergeBlock] = {
    val processor =
      MergeProcessor.fromEntry(
        insts = insts,
        args = args,
        debugInfo = debugInfo,
        state = state,
        doInline = doInline,
        blockFresh = blockFresh,
        eval = this,
        parentScopeId = parentScopeId
      )

    try {
      pushMergeProcessor(processor)

      while (!processor.done()) {
        processor.advance()
      }
    } finally {
      popMergeProcessor()
    }

    val blocks = processor.toSeq(retTy)
    MergePostProcessor.postProcess(blocks)
  }

}
