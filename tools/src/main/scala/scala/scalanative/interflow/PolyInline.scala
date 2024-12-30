package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.linker._

private[interflow] trait PolyInline { self: Interflow =>

  object PolyInlined {
    def unapply(input: (nir.Val, Seq[nir.Val]))(implicit
        state: State,
        srcPosition: nir.SourcePosition,
        scopeId: nir.ScopeId
    ): Option[nir.Val] = {
      val (value, eargs) = input
      value match {
        case DelayedRef(op: nir.Op.Method) =>
          mode match {
            case build.Mode.Debug =>
              None

            case _: build.Mode.Release =>
              val targets = polyTargets(op)
              val classes = targets.map(_._1)
              val classesCount = classes.size
              val impls = targets.map(_._2).distinct
              val implsCount = impls.size

              val shallPolyInline =
                if (mode == build.Mode.ReleaseFast || mode == build.Mode.ReleaseSize) {
                  classesCount <= 8 && implsCount == 2
                } else {
                  classesCount <= 16 && implsCount >= 2 && implsCount <= 4
                }

              if (shallPolyInline) {
                Some(
                  polyInline(op, eargs.toIndexedSeq, targets, classes, impls)
                )
              } else {
                None
              }
          }
        case _ => None
      }
    }
  }

  private def polyTargets(
      op: nir.Op.Method
  )(implicit state: State): IndexedSeq[(Class, nir.Global.Member)] = {
    val nir.Op.Method(obj, sig) = op

    val objty = obj match {
      case InstanceRef(ty) =>
        ty
      case _ =>
        obj.ty
    }

    val res = objty match {
      case ExactClassRef(cls, _) =>
        cls.resolve(sig).map(g => (cls, g)).toIndexedSeq
      case ClassRef(cls) if !sig.isVirtual =>
        cls.resolve(sig).map(g => (cls, g)).toIndexedSeq
      case ScopeRef(scope) =>
        val targets = mutable.UnrolledBuffer.empty[(Class, nir.Global.Member)]
        scope.implementors.foreach { cls =>
          if (cls.allocated) {
            cls.resolve(sig).foreach { g => targets += ((cls, g)) }
          }
        }
        targets.toIndexedSeq
      case _ =>
        IndexedSeq.empty
    }

    // the only case when result won't be empty or one element seq is reading from `scop.implementors`
    // scop.implementors are prestorted by the same way => I don't need sort here.
    res
  }

  private def polyInline(
      op: nir.Op.Method,
      args: IndexedSeq[nir.Val],
      targets: IndexedSeq[(Class, nir.Global.Member)],
      classes: IndexedSeq[Class],
      impls: IndexedSeq[nir.Global.Member]
  )(implicit
      state: State,
      analysis: ReachabilityAnalysis.Result,
      srcPosition: nir.SourcePosition,
      scopeIdId: nir.ScopeId
  ): nir.Val = {
    import state.{emit, fresh, materialize}

    val obj = materialize(op.obj)
    val margs = args.map(materialize(_))

    val checkLabels = IndexedSeq.fill(targets.size - 1)(fresh())
    val callLabels = IndexedSeq.fill(impls.size)(fresh())
    val callLabelIndex =
      (0 until targets.size).map(i => impls.indexOf(targets(i)._2)).toIndexedSeq
    val mergeLabel = fresh()

    val meth = emit.method(obj, nir.Rt.GetClassSig, nir.Next.None)
    val methty = nir.Type.Function(Seq(nir.Rt.Object), nir.Rt.Class)
    val objcls = emit.call(methty, meth, Seq(obj), nir.Next.None)

    for (idx <- 0.until(checkLabels.length)) {
      val checkLabel = checkLabels(idx)
      if (idx > 0) {
        emit.label(checkLabel)
      }
      val cls = classes(idx)
      val isCls = emit.comp(
        nir.Comp.Ieq,
        nir.Rt.Class,
        objcls,
        nir.Val.Global(cls.name, nir.Rt.Class),
        nir.Next.None
      )
      if (idx < targets.size - 2) {
        emit.branch(
          isCls,
          nir.Next(callLabels(callLabelIndex(idx))),
          nir.Next(checkLabels(idx + 1))
        )
      } else {
        emit.branch(
          isCls,
          nir.Next(callLabels(callLabelIndex(idx))),
          nir.Next(callLabels(callLabelIndex(idx + 1)))
        )
      }
    }

    val rettys = mutable.UnrolledBuffer.empty[nir.Type]

    for (i <- 0.until(callLabels.length)) {
      val callLabel = callLabels(i)
      val m = impls(i)

      emit.label(callLabel, Seq.empty)
      val ty = originalFunctionType(m)
      val nir.Type.Function(argtys, retty) = ty
      rettys += retty

      val cargs = margs.zip(argtys).map {
        case (value, argty) =>
          if (Sub.is(value.ty, argty)) value
          else emit.conv(nir.Conv.Bitcast, argty, value, nir.Next.None)
      }
      val res =
        emit.call(ty, nir.Val.Global(m, nir.Type.Ptr), cargs, nir.Next.None)
      emit.jump(nir.Next.Label(mergeLabel, Seq(res)))
    }

    val result = nir.Val.Local(fresh(), Sub.lub(rettys.toSeq, Some(op.resty)))
    emit.label(mergeLabel, Seq(result))

    result
  }

}
