package scala.scalanative
package interflow

import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.scalanative.linker._
import scala.collection.mutable
import scala.scalanative.util.ScopedVar.scopedPushIf

private[interflow] trait Opt { self: Interflow =>

  def shallOpt(name: nir.Global.Member): Boolean = {
    val defn =
      getOriginal(originalName(name))
    val noUnwind = defn.insts.forall {
      case nir.Inst.Let(_, _, unwind) =>
        unwind == nir.Next.None
      case nir.Inst.Throw(_, unwind) =>
        unwind == nir.Next.None
      case nir.Inst.Unreachable(unwind) =>
        unwind == nir.Next.None
      case _ =>
        true
    }

    defn.attrs.opt != nir.Attr.NoOpt && noUnwind
  }

  def opt(name: nir.Global.Member): nir.Defn.Define =
    in(s"visit ${name.show}") {
      val orig = originalName(name)
      val origtys = argumentTypes(orig)
      val origdefn = getOriginal(orig)
      val argtys = argumentTypes(name)
      val nir.Inst.Label(_, origargs) = origdefn.insts.head: @unchecked
      implicit val pos = origdefn.pos
      // Wrap up the result.
      def result(
          retty: nir.Type,
          rawInsts: Seq[nir.Inst],
          debugInfo: DebugInfo
      ) = {
        val insts = nir.ControlFlow.removeDeadBlocks(rawInsts)
        val newDebugInfo = if (preserveDebugInfo) {
          // TODO: filter-out unreachable scopes
          val scopes = debugInfo.lexicalScopes.sorted
          debugInfo.copy(lexicalScopes = scopes)
        } else debugInfo
        origdefn.copy(
          name = name,
          attrs = origdefn.attrs.copy(opt = nir.Attr.DidOpt),
          ty = nir.Type.Function(argtys, retty),
          insts = insts,
          debugInfo = newDebugInfo
        )(origdefn.pos)
      }

      // Create new fresh and state for the first basic block.
      val fresh = nir.Fresh(0)
      val state = new State(nir.Local(0))(preserveDebugInfo)

      // Interflow usually infers better types on our erased type system
      // than scalac, yet we live it as a benefit of the doubt and make sure
      // that if original return type is more specific, we keep it as is.
      val nir.Type.Function(_, origRetTy) = origdefn.ty

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
          nir.Val.Local(id, ty)
      }

      // If any of the argument types is nothing, this method
      // is never going to be called, so we don't have to visit it.
      if (args.exists(_.ty == nir.Type.Nothing)) {
        val insts = Seq(
          nir.Inst.Label(nir.Local(0), args),
          nir.Inst.Unreachable(nir.Next.None)
        )
        result(nir.Type.Nothing, insts, DebugInfo.empty)
      } else
        scopedPushIf(preserveDebugInfo)(
          Seq(
            currentFreshScope := nir.Fresh(0L),
            currentLexicalScopes := mutable.UnrolledBuffer.empty
          )
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
                parentScopeId = nir.ScopeId.TopLevel
              )
            } finally {
              popBlockFresh()
            }

          // Collect instructions, materialize all returned values
          // and compute the result type.
          val insts = blocks.flatMap { block =>
            block.cf = block.cf match {
              case inst @ nir.Inst.Ret(retv) =>
                nir.Inst.Ret(block.end.materialize(retv))(inst.pos)
              case inst @ nir.Inst.Throw(excv, unwind) =>
                nir.Inst.Throw(block.end.materialize(excv), unwind)(inst.pos)
              case cf =>
                cf
            }
            block.toInsts()
          }
          val debugInfo: DebugInfo = if (preserveDebugInfo) {
            val localNames = mutable.OpenHashMap.empty[nir.Local, nir.LocalName]
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
            case nir.Inst.Ret(v) => v.ty
          }

          val retty0 = rets match {
            case Seq()   => nir.Type.Nothing
            case Seq(ty) => ty
            case tys     => Sub.lub(tys, Some(origRetTy))
          }
          // Make sure to not override expected BoxedUnit with primitive Unit
          val retty =
            if (retty0 == nir.Type.Unit && origRetTy.isInstanceOf[nir.Type.Ref])
              origRetTy
            else retty0

          result(retty, insts, debugInfo)
        }
    }

  def process(
      insts: Array[nir.Inst],
      debugInfo: DebugInfo,
      args: Seq[nir.Val],
      state: State,
      doInline: Boolean,
      retTy: nir.Type,
      parentScopeId: nir.ScopeId
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
