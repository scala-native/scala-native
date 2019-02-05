package scala.scalanative
package interflow

import scala.util.{Try, Success, Failure}
import scalanative.nir._
import scalanative.linker._
import scalanative.interflow.MergeProcessor.process

trait Inline { self: Interflow =>
  def shallInline(name: Global, args: Seq[Val], unwind: Next)(
      implicit state: State,
      linked: linker.Result): Boolean =
    done
      .get(name)
      .fold[Boolean] {
        false
      } { defn =>
        val isCtor = originalName(name) match {
          case Global.Member(_, _: Sig.Ctor) =>
            true
          case Global.Member(_, Sig.Method("$init$", _)) =>
            true
          case _ =>
            false
        }
        val isSmall =
          defn.insts.size <= 8
        val noInline =
          defn.attrs.inline == Attr.NoInline
        val hintInline =
          defn.attrs.inline == Attr.AlwaysInline || defn.attrs.inline == Attr.InlineHint
        val hasUnwind =
          unwind != Next.None
        val isRecursive =
          context.contains(s"inlining ${name.show}")
        val isBlacklisted =
          blacklist.contains(name)

        val shall =
          isCtor || hintInline || isSmall
        val shallNot =
          noInline || hasUnwind || isRecursive || isBlacklisted

        if (shall) {
          if (shallNot) {
            log(s"not inlining ${name.show}, because:")
            if (noInline) { log("* has noinline attr") }
            if (hasUnwind) { log("* has unwind") }
            if (isRecursive) { log("* is recursive") }
            if (isBlacklisted) { log("* is blacklisted") }
          }
        } else {
          log(
            s"no reason to inline ${name.show}(${args.map(_.show).mkString(",")})")
        }

        shall && !shallNot
      }

  def inline(name: Global, args: Seq[Val], unwind: Next, blockFresh: Fresh)(
      implicit state: State,
      linked: linker.Result): Val =
    in(s"inlining ${name.show}") {
      val defn = done(name)
      val defnArgTys = {
        val Type.Function(argtys, _) = defn.ty
        argtys
      }
      val inlineArgs = args.zip(defnArgTys).foreach {
        case (local @ Val.Local(_, ty), argTy) if !Sub.is(ty, argTy) =>
          state.emit.conv(Conv.Bitcast, argTy, local, unwind)
        case v =>
          v
      }
      val inlineInsts = defn.insts
      val blocks =
        process(inlineInsts.toArray,
                args,
                state,
                blockFresh,
                inline = true,
                this)
      def nothing = {
        state.emit.label(state.fresh(), Seq.empty)
        Val.Zero(Type.Nothing)
      }
      blocks match {
        case Seq() =>
          util.unreachable

        case Seq(block) =>
          block.cf match {
            case Inst.Ret(value) =>
              state.emit ++= block.end.emit
              state.inherit(block.end, value +: args)
              value
            case Inst.Throw(value, unwind) =>
              val excv = block.end.materialize(value)
              state.emit ++= block.end.emit
              state.emit.raise(excv, unwind)
              state.inherit(block.end, value +: args)
              nothing
            case Inst.Unreachable(unwind) =>
              state.emit ++= block.end.emit
              state.emit.unreachable(unwind)
              state.inherit(block.end, args)
              nothing
          }

        case first +: rest =>
          state.emit ++= first.toInsts.tail

          rest.foreach { block =>
            block.cf match {
              case _: Inst.Ret =>
                ()
              case Inst.Throw(value, unwind) =>
                val excv = block.end.materialize(value)
                state.emit ++= block.toInsts.init
                state.emit.raise(excv, unwind)
              case _ =>
                state.emit ++= block.toInsts
            }
          }

          rest
            .collectFirst {
              case block if block.cf.isInstanceOf[Inst.Ret] =>
                val Inst.Ret(value) = block.cf
                state.emit ++= block.toInsts.init
                state.inherit(block.end, value +: args)
                value
            }
            .getOrElse {
              nothing
            }
      }
    }
}
