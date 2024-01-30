package scala.scalanative
package interflow

import scala.scalanative.nir.Defn.Define.DebugInfo
import scala.scalanative.linker._
import scala.scalanative.util.unreachable

trait Inline { self: Interflow =>
  val optimizerConfig = config.compilerConfig.optimizerConfig
  import optimizerConfig.{
    smallFunctionSize,
    maxCallerSize,
    maxCalleeSize,
    maxInlineDepth
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
        def isCtor = name.sig.isCtor
        def isSmall = defn.insts.size <= smallFunctionSize
        def isExtern = defn.attrs.isExtern
        def hasVirtualArgs = args.exists(_.isInstanceOf[nir.Val.Virtual])
        def noOpt = defn.attrs.opt == nir.Attr.NoOpt
        def noInline = defn.attrs.inlineHint == nir.Attr.NoInline
        def alwaysInline = defn.attrs.inlineHint == nir.Attr.AlwaysInline
        def hintInline = defn.attrs.inlineHint == nir.Attr.InlineHint
        def isRecursive = hasContext(s"inlining ${name.show}")
        def isDenylisted = this.isDenylisted(name)
        def calleeTooBig = defn.insts.size > maxCalleeSize
        def callerTooBig = mergeProcessor.currentSize() > maxCallerSize
        def inlineDepthLimitExceeded = state.inlineDepth > maxInlineDepth
        def hasUnwind = defn.hasUnwind

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
        lazy val shallNot =
          noOpt || noInline || isRecursive || isDenylisted || calleeTooBig || callerTooBig || isExtern || hasUnwind || inlineDepthLimitExceeded
        withLogger { logger =>
          if (shall) {
            if (shallNot) {
              logger(s"not inlining ${name.show}, because:")
              if (noInline) logger("* has noinline attr")
              if (isRecursive) logger("* is recursive")
              if (isDenylisted) logger("* is denylisted")
              if (callerTooBig) logger("* caller is too big")
              if (calleeTooBig) logger("* callee is too big")
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
      srcPosition: nir.Position,
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
      srcPosition: nir.Position,
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

      implicit val srcPosition: nir.Position = defn.pos
      val blocks = process(
        insts = defn.insts.toArray,
        debugInfo = defn.debugInfo,
        args = adapt(args, defn.ty),
        state = state,
        doInline = true,
        retTy = origRetTy,
        parentScopeId = parentScopeId
      )

      val emit = new nir.InstructionBuilder()(state.fresh)

      def nothing = {
        emit.label(state.fresh(), Seq.empty)
        nir.Val.Zero(nir.Type.Nothing)
      }

      val (res, endState) = blocks match {
        case Seq() =>
          util.unreachable

        case Seq(block) =>
          block.cf match {
            case nir.Inst.Ret(value) =>
              emit ++= block.end.emit
              (value, block.end)
            case nir.Inst.Throw(value, unwind) =>
              val excv = block.end.materialize(value)
              emit ++= block.end.emit
              emit.raise(excv, unwind)
              (nothing, block.end)
            case nir.Inst.Unreachable(unwind) =>
              emit ++= block.end.emit
              emit.unreachable(unwind)
              (nothing, block.end)
            case _ =>
              unreachable
          }

        case first +: rest =>
          emit ++= first.toInsts().tail

          rest.foreach { block =>
            block.cf match {
              case _: nir.Inst.Ret =>
                ()
              case nir.Inst.Throw(value, unwind) =>
                val excv = block.end.materialize(value)
                emit ++= block.toInsts().init
                emit.raise(excv, unwind)
              case _ =>
                emit ++= block.toInsts()
            }
          }

          rest
            .collectFirst {
              case block if block.cf.isInstanceOf[nir.Inst.Ret] =>
                val nir.Inst.Ret(value) = block.cf: @unchecked
                emit ++= block.toInsts().init
                (value, block.end)
            }
            .getOrElse {
              (nothing, state)
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
