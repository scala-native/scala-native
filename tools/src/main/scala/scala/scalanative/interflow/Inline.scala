package scala.scalanative
package interflow

import scala.util.{Try, Success, Failure}
import scalanative.nir._
import scalanative.linker._

trait Inline { self: Interflow =>
  def shallInline(name: Global, args: Seq[Val])(
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
        val hasVirtualArgs =
          args.exists(_.isInstanceOf[Val.Virtual])
        val noInline =
          defn.attrs.inline == Attr.NoInline
        val hintInline =
          defn.attrs.inline == Attr.AlwaysInline || defn.attrs.inline == Attr.InlineHint
        val isRecursive =
          context.contains(s"inlining ${name.show}")
        val isBlacklisted =
          blacklist.contains(name)
        val calleeTooBig =
          defn.insts.size > 8192
        val callerTooBig =
          mergeProcessor.currentSize() > 8192

        val shall =
          isCtor || hintInline || isSmall || (mode == build.Mode.Release && hasVirtualArgs)
        val shallNot =
          noInline || isRecursive || isBlacklisted || calleeTooBig || callerTooBig

        if (shall) {
          if (shallNot) {
            log(s"not inlining ${name.show}, because:")
            if (noInline) { log("* has noinline attr") }
            if (isRecursive) { log("* is recursive") }
            if (isBlacklisted) { log("* is blacklisted") }
            if (callerTooBig) { log("* caller is too big") }
            if (calleeTooBig) { log("* callee is too big") }
          }
        } else {
          log(
            s"no reason to inline ${name.show}(${args.map(_.show).mkString(",")})")
        }

        shall && !shallNot
      }

  def inline(name: Global, args: Seq[Val])(implicit state: State,
                                           linked: linker.Result): Val =
    in(s"inlining ${name.show}") {
      val defn   = done(name)
      val blocks = process(defn.insts.toArray, args, state, inline = true)

      val emit = new nir.Buffer()(state.fresh)

      def nothing = {
        emit.label(state.fresh(), Seq.empty)
        Val.Zero(Type.Nothing)
      }

      val (res, endState) = blocks match {
        case Seq() =>
          util.unreachable

        case Seq(block) =>
          block.cf match {
            case Inst.Ret(value) =>
              emit ++= block.end.emit
              (value, block.end)
            case Inst.Throw(value, unwind) =>
              val excv = block.end.materialize(value)
              emit ++= block.end.emit
              emit.raise(excv, unwind)
              (nothing, block.end)
            case Inst.Unreachable(unwind) =>
              emit ++= block.end.emit
              emit.unreachable(unwind)
              (nothing, block.end)
          }

        case first +: rest =>
          emit ++= first.toInsts.tail

          rest.foreach { block =>
            block.cf match {
              case _: Inst.Ret =>
                ()
              case Inst.Throw(value, unwind) =>
                val excv = block.end.materialize(value)
                emit ++= block.toInsts.init
                emit.raise(excv, unwind)
              case _ =>
                emit ++= block.toInsts
            }
          }

          rest
            .collectFirst {
              case block if block.cf.isInstanceOf[Inst.Ret] =>
                val Inst.Ret(value) = block.cf
                emit ++= block.toInsts.init
                (value, block.end)
            }
            .getOrElse {
              (nothing, state)
            }
      }

      state.emit ++= emit
      state.inherit(endState, res +: args)
      res
    }
}
