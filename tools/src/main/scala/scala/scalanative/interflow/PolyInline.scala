package scala.scalanative
package interflow

import scala.collection.mutable
import scalanative.nir._
import scalanative.linker._

trait PolyInline { self: Interflow =>
  private def polyTargets(
      op: Op.Method
  )(implicit state: State): Seq[(Class, Global.Member)] = {
    val Op.Method(obj, sig) = op

    val objty = obj match {
      case InstanceRef(ty) =>
        ty
      case _ =>
        obj.ty
    }

    val res = objty match {
      case ExactClassRef(cls, _) =>
        cls.resolve(sig).map(g => (cls, g)).toSeq
      case ClassRef(cls) if !sig.isVirtual =>
        cls.resolve(sig).map(g => (cls, g)).toSeq
      case ScopeRef(scope) =>
        val targets = mutable.UnrolledBuffer.empty[(Class, Global.Member)]
        scope.implementors.foreach { cls =>
          if (cls.allocated) {
            cls.resolve(sig).foreach { g => targets += ((cls, g)) }
          }
        }
        targets.toSeq
      case _ =>
        Seq.empty
    }

    // the only case when result won't be empty or one element seq is reading from `scop.implementors`
    // scop.implementors are prestorted by the same way => I don't need sort here.
    res
  }

  def shallPolyInline(op: Op.Method, args: Seq[Val])(implicit
      state: State,
      linked: linker.Result
  ): Boolean = mode match {
    case build.Mode.Debug =>
      false

    case _: build.Mode.Release =>
      val targets = polyTargets(op)
      val classCount = targets.map(_._1).size
      val implCount = targets.map(_._2).distinct.size

      if (mode == build.Mode.ReleaseFast || mode == build.Mode.ReleaseSize) {
        classCount <= 8 && implCount == 2
      } else {
        classCount <= 16 && implCount >= 2 && implCount <= 4
      }
  }

  def polyInline(op: Op.Method, args: Seq[Val])(implicit
      state: State,
      linked: linker.Result,
      srcPosition: Position,
      scopeIdId: ScopeId
  ): Val = {
    import state.{emit, fresh, materialize}

    val obj = materialize(op.obj)
    val margs = args.map(materialize(_))
    val targets = polyTargets(op)
    val classes = targets.map(_._1)
    val impls = targets.map(_._2).distinct

    val checkLabels = (1 until targets.size).map(_ => fresh()).toSeq
    val callLabels = (1 to impls.size).map(_ => fresh()).toSeq
    val callLabelIndex =
      (0 until targets.size).map(i => impls.indexOf(targets(i)._2))
    val mergeLabel = fresh()

    val meth = emit.method(obj, Rt.GetClassSig, Next.None)
    val methty = Type.Function(Seq(Rt.Object), Rt.Class)
    val objcls = emit.call(methty, meth, Seq(obj), Next.None)

    checkLabels.zipWithIndex.foreach {
      case (checkLabel, idx) =>
        if (idx > 0) {
          emit.label(checkLabel)
        }
        val cls = classes(idx)
        val isCls = emit.comp(
          Comp.Ieq,
          Rt.Class,
          objcls,
          Val.Global(cls.name, Rt.Class),
          Next.None
        )
        if (idx < targets.size - 2) {
          emit.branch(
            isCls,
            Next(callLabels(callLabelIndex(idx))),
            Next(checkLabels(idx + 1))
          )
        } else {
          emit.branch(
            isCls,
            Next(callLabels(callLabelIndex(idx))),
            Next(callLabels(callLabelIndex(idx + 1)))
          )
        }
    }

    val rettys = mutable.UnrolledBuffer.empty[Type]

    callLabels.zip(impls).foreach {
      case (callLabel, m) =>
        emit.label(callLabel, Seq.empty)
        val ty = originalFunctionType(m)
        val Type.Function(argtys, retty) = ty
        rettys += retty

        val cargs = margs.zip(argtys).map {
          case (value, argty) =>
            if (Sub.is(value.ty, argty)) value
            else emit.conv(Conv.Bitcast, argty, value, Next.None)
        }
        val res = emit.call(ty, Val.Global(m, Type.Ptr), cargs, Next.None)
        emit.jump(Next.Label(mergeLabel, Seq(res)))
    }

    val result = Val.Local(fresh(), Sub.lub(rettys.toSeq, Some(op.resty)))
    emit.label(mergeLabel, Seq(result))

    result
  }
}
