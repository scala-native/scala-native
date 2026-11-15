package scala.scalanative
package interflow

import scala.scalanative.linker._
import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.scalanative.util.unreachable

private[interflow] trait Inline { self: Interflow =>
  val optimizerConfig = config.compilerConfig.optimizerConfig
  import optimizerConfig.{
    maxCalleeSize, maxCallerSize, maxInlineDepth, smallFunctionSize
  }

  def shallInline(name: nir.Global.Member, args: Seq[nir.Val])(implicit
      state: State,
      analysis: ReachabilityAnalysis.Result
  ): Boolean = {
    val maybeDefn = mode match {
      case build.Mode.Debug      => maybeOriginal(name)
      case _: build.Mode.Release => maybeDone(name)
    }

    maybeDefn
      .fold[Boolean] {
        false
      } { defn =>
        def isCtor = name.sig.isCtor || name.sig.isTraitInit
        def isSmall = defn.insts.size <= smallFunctionSize
        def isExtern = defn.attrs.isExtern
        def hasVirtualArgs = args.exists(_.isInstanceOf[nir.Val.Virtual])
        def noOpt = defn.attrs.opt == nir.Attr.NoOpt
        def noInline = defn.attrs.inlineHint == nir.Attr.NoInline
        def alwaysInline = defn.attrs.inlineHint == nir.Attr.AlwaysInline
        def hintInline = defn.attrs.inlineHint == nir.Attr.InlineHint
        def isRecursive = inliningBacktrace.contains(name)
        def isDenylisted = this.isDenylisted(name)
        def calleeTooBig = defn.insts.size > maxCalleeSize
        def callerTooBig = mergeProcessor.currentSize() > maxCallerSize
        def inlineDepthLimitExceeded = inliningBacktrace.size > maxInlineDepth
        // Debug builds do not evaluate exception handlers
        def hasHandlers = defn.hasUnwind && mode == build.Mode.Debug
        /* A frame with exception handlers does not get stack allocations
         * it would not have without inlining: before handlers were
         * evaluated nothing was inlined into such a frame. A stack
         * allocation outside of the entry block makes the frame dynamically
         * sized, which a continuation cannot relocate when a virtual thread
         * is suspended. Synchronized blocks are the common case: the monitor
         * allocates on the stack, and Object.wait suspends in them.
         */
        def stackallocInHandlers =
          defn.hasStackalloc && (defn.hasUnwind || inHandlerScope)

        val shall = mode match {
          case build.Mode.Debug =>
            alwaysInline || isCtor
          case build.Mode.ReleaseFast =>
            alwaysInline || hintInline || isSmall || isCtor
          case build.Mode.ReleaseSize =>
            alwaysInline || isSmall || isCtor
          case build.Mode.ReleaseFull =>
            alwaysInline || hintInline || isSmall || isCtor || hasVirtualArgs
        }
        lazy val shallNot = {
          def hardLimits =
            isRecursive || isDenylisted || noInline || isExtern ||
              hasHandlers || stackallocInHandlers
          def softLimits =
            calleeTooBig || callerTooBig || inlineDepthLimitExceeded

          if (alwaysInline) hardLimits
          else hardLimits || softLimits
        }
        withLogger { logger =>
          if (shall) {
            if (shallNot) {
              logger(s"not inlining ${name.show}, because:")
              if (noOpt) logger("* has noopt attr")
              if (noInline) logger("* has noinline attr")
              if (isRecursive) logger("* is recursive")
              if (isDenylisted) logger("* is denylisted")
              if (calleeTooBig)
                logger(
                  s"* callee is too big (${defn.insts.size} > $maxCalleeSize)"
                )
              if (callerTooBig)
                logger(
                  s"* caller is too big (${mergeProcessor.currentSize()} > $maxCallerSize)"
                )
              if (isExtern) logger("* is an extern method")
              if (hasHandlers) logger("* has unwind")
              if (stackallocInHandlers)
                logger("* allocates on stack of a method with handlers")
              if (inlineDepthLimitExceeded)
                logger("* inline depth limit exceeded")
            }
          } else {
            logger(
              s"no reason to inline ${name.show}(${args.map(_.show).mkString(",")})"
            )
          }
        }

        shall && !shallNot
      }
  }

  def adapt(value: nir.Val, ty: nir.Type)(implicit
      state: State,
      srcPosition: nir.SourcePosition,
      scopeId: nir.ScopeId
  ): nir.Val = {
    val valuety = value match {
      case InstanceRef(ty) => ty
      case _               => value.ty
    }
    if (!Sub.is(valuety, ty)) {
      combine(nir.Conv.Bitcast, ty, value)
    } else {
      value
    }
  }

  def adapt(args: Seq[nir.Val], sig: nir.Type.Function)(implicit
      state: State,
      srcPosition: nir.SourcePosition,
      scopeId: nir.ScopeId
  ): Seq[nir.Val] = {
    val nir.Type.Function(argtys, _) = sig

    // Varargs signature might appear to have less
    // argument types than arguments at the call site.
    val expected = argtys match {
      case inittys :+ nir.Type.Vararg =>
        val nonvarargs = args.take(inittys.size).zip(inittys)
        val varargs =
          args.drop(inittys.size).map { arg => (arg, nir.Type.Vararg) }
        nonvarargs ++ varargs
      case _ =>
        args.zip(argtys)
    }

    expected.map {
      case (value, nir.Type.Vararg) =>
        value
      case (value, argty) =>
        adapt(value, argty)
    }
  }

  def `inline`(name: nir.Global.Member, args: Seq[nir.Val])(implicit
      state: State,
      analysis: ReachabilityAnalysis.Result,
      parentScopeId: nir.ScopeId
  ): nir.Val =
    in(s"inlining ${name.show}") {
      val defn = mode match {
        case build.Mode.Debug      => getOriginal(name)
        case _: build.Mode.Release => getDone(name)
      }
      val nir.Type.Function(_, origRetTy) = defn.ty

      implicit val srcPosition: nir.SourcePosition = defn.pos
      // Whether an exception thrown by the callee has a handler in this
      // method or in one it is being inlined into
      val inTryRegion = state.inTryRegion
      val adaptedArgs = adapt(args, defn.ty)
      val blocks = inliningBacktrace.tracked(name) {
        if (defn.hasUnwind) enterHandlerScope()
        try
          process(
            insts = defn.insts.toArray,
            debugInfo = defn.debugInfo,
            args = adaptedArgs,
            state = state,
            doInline = true,
            retTy = origRetTy,
            parentScopeId = parentScopeId,
            inOuterRegion = inTryRegion
          )
        finally if (defn.hasUnwind) leaveHandlerScope()
      }

      val emit = new nir.InstructionBuilder()(state.fresh)

      def nothing = {
        emit.label(state.fresh(), Seq.empty)
        nir.Val.Zero(nir.Type.Nothing)
      }

      // The entry block continues the block of the call, the others start
      // with their label
      val entry = blocks.head
      def body(block: MergeBlock): Seq[nir.Inst] =
        if (block eq entry) block.toInsts().tail
        else block.toInsts()

      def emitBlock(block: MergeBlock): Unit = block.cf match {
        case nir.Inst.Throw(value, nir.Next.None)
            if inTryRegion && isNonNull(value)(block.end) =>
          // The exception goes to its handler without unwinding, and without
          // being allocated if the handler does not let it escape
          val tramp =
            nir.Local(blockFresh().id * MergeProcessor.MergeBlockOffset)
          block.end.sites += new UnwindSite(
            tramp = tramp,
            exc = null,
            thrown = value,
            frames = block.end :: Nil,
            links = Nil,
            handler = null
          )
          emit ++= body(block).init
          emit.jump(nir.Next.Label(tramp, Seq.empty))
        case nir.Inst.Throw(value, nir.Next.None) =>
          val excv = block.end.materialize(value)
          val unwind =
            if (inTryRegion) block.end.newUnwindSite(thrown = null).unwind
            else nir.Next.None
          emit ++= body(block).init
          emit.raise(excv, unwind)
        case _ =>
          emit ++= body(block)
      }

      val (res, endState) =
        blocks.find(_.cf.isInstanceOf[nir.Inst.Ret]) match {
          case None =>
            blocks.foreach(emitBlock)
            (nothing, state)
          case Some(block) =>
            val nir.Inst.Ret(value) = block.cf: @unchecked
            if (block eq entry) {
              emit ++= body(block).init
              // Handlers and trampolines of the callee are out of the way
              if (blocks.tail.nonEmpty) {
                val continue = state.fresh()
                emit.jump(nir.Next(continue))
                blocks.tail.foreach(emitBlock)
                emit.label(continue)
              }
            } else {
              blocks.foreach { other =>
                if (other ne block) emitBlock(other)
              }
              emit ++= block.toInsts().init
            }
            (value, block.end)
        }

      // Instructions of the callee that have no handler in it unwind to
      // the handler of the call, or to the one of a caller of this method
      if (inTryRegion) {
        lazy val callerState = {
          val callerState = state.fullClone(state.blockId)
          callerState.handler = null
          callerState
        }
        val roots = adaptedArgs ++ args
        blocks.foreach { block =>
          block.end.sites.foreach { site =>
            if (site.handler eq null) {
              site.frames = site.frames :+ callerState
              site.links = site.links :+ {
                if (site.thrown == null) roots
                else roots :+ site.thrown
              }
              site.handler = state.handler
              state.sites += site
            }
          }
        }
      }
      if (self.preserveDebugInfo) {
        blocks.foreach { block =>
          endState.localNames.addMissing(block.end.localNames)
          endState.virtualNames.addMissing(block.end.virtualNames)
        }

        // Adapt result of inlined call
        // Replace the calle scopeId with the scopeId of caller function, to represent that result of this call is available in parent
        res match {
          case nir.Val.Local(id, _) =>
            emit.updateLetInst(id)(i => i.copy()(i.pos, parentScopeId))
          case nir.Val.Virtual(addr) =>
            endState.heap(addr) = endState.deref(addr) match {
              case inst: EscapedInstance =>
                inst.copy()(inst.srcPosition, parentScopeId)
              case inst: DelayedInstance =>
                inst.copy()(inst.srcPosition, parentScopeId)
              case inst: VirtualInstance =>
                inst.copy()(inst.srcPosition, parentScopeId)
            }
          case _ => ()
        }
      }

      state.emit ++= emit
      state.inherit(endState, res +: args)

      val nir.Type.Function(_, retty) = defn.ty
      adapt(res, retty)
    }
}
