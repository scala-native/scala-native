package scala.scalanative
package interflow

import scalanative.nir._
import scalanative.linker._
import scala.concurrent._
import scala.annotation.tailrec

trait Visit { self: Interflow =>
  def shallVisit(name: Global.Member): Boolean = {
    val orig = originalName(name)

    if (!hasOriginal(orig)) {
      false
    } else {
      val defn = getOriginal(orig)
      val hasInsts = defn.insts.size > 0
      val hasSema = analysis.infos.contains(defn.name)

      hasInsts && hasSema
    }
  }

  def shallDuplicate(name: Global.Member, argtys: Seq[Type]): Boolean =
    mode match {
      case build.Mode.Debug | build.Mode.ReleaseFast | build.Mode.ReleaseSize =>
        false

      case build.Mode.ReleaseFull =>
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

  def visitEntries(): Unit =
    mode match {
      case build.Mode.Debug =>
        analysis.defns.foreach(defn => visitEntry(defn.name))
      case _: build.Mode.Release =>
        analysis.entries.foreach(visitEntry)
    }

  def visitEntry(name: Global): Unit = {
    if (!name.isTop) {
      visitEntry(name.top)
    }
    analysis.infos(name) match {
      case meth: Method =>
        visitRoot(meth.name)
      case cls: Class if cls.isModule =>
        val init = cls.name member Sig.Ctor(Seq.empty)
        if (hasOriginal(init)) {
          visitRoot(init)
        }
      case _ =>
        ()
    }
  }

  def visitRoot(name: Global.Member): Unit =
    if (shallVisit(name)) {
      pushTodo(name)
    }

  def visitDuplicate(
      name: Global.Member,
      argtys: Seq[Type]
  ): Option[Defn.Define] = {
    mode match {
      case build.Mode.Debug =>
        None
      case _: build.Mode.Release =>
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

  def visitLoop()(implicit ec: ExecutionContext): Future[Unit] = {
    def visit(name: Global.Member): Unit = {
      if (!isDone(name)) {
        visitMethod(name)
      }
    }

    @tailrec def loop(): Unit = popTodo() match {
      case name: Global.Member => visit(name); loop()
      case Global.None         => ()
      case name: Global.Top =>
        throw new IllegalStateException(
          s"Unexpected Global.Top in visit loop: ${name}"
        )
    }

    mode match {
      case build.Mode.Debug =>
        Future
          .traverse(allTodo()) { defn => Future(visit(defn)) }
          .map(_ => ())
      case _: build.Mode.Release =>
        Future(loop())
    }
  }

  def visitMethod(name: Global.Member): Unit =
    if (!hasStarted(name)) {
      markStarted(name)
      val origname = originalName(name)
      val origdefn = getOriginal(origname)
      try {
        if (shallOpt(name)) {
          setDone(name, opt(name))
        } else {
          noOpt(origdefn)
          setDone(name, origdefn)
          setDone(origname, origdefn)
        }
      } catch {
        case BailOut(msg) =>
          log(s"failed to expand ${name.show}: $msg")
          val baildefn =
            origdefn.copy(attrs = origdefn.attrs.copy(opt = Attr.BailOpt(msg)))(
              origdefn.pos
            )
          noOpt(origdefn)
          setDone(name, baildefn)
          setDone(origname, baildefn)
          markBlacklisted(name)
          markBlacklisted(origname)
      }
    }

  def originalName(name: Global.Member): Global.Member = name match {
    case Global.Member(owner, sig) if sig.isDuplicate =>
      val Sig.Duplicate(origSig, argtys) = sig.unmangled: @unchecked
      originalName(Global.Member(owner, origSig))
    case _ =>
      name
  }

  def duplicateName(name: Global.Member, argtys: Seq[Type]): Global.Member = {
    val orig = originalName(name)
    if (!shallDuplicate(orig, argtys)) orig
    else {
      val origargtys = argumentTypes(name)
      val dupargtys = argtys.zip(origargtys).map {
        case (argty, origty) =>
          // Duplicate argument type should not be
          // less specific than the original declare type.
          val tpe = if (!Sub.is(argty, origty)) origty else argty
          // Lift Unit to BoxedUnit, only in that form it can be passed as a function argument
          // It would be better to eliminate void arguments, but currently generates lots of problmes
          if (tpe == nir.Type.Unit) Rt.BoxedUnit
          else tpe
      }
      val Global.Member(top, sig) = orig
      Global.Member(top, Sig.Duplicate(sig, dupargtys))
    }
  }

  def argumentTypes(name: Global.Member): Seq[Type] = name match {
    case Global.Member(_, sig) if sig.isDuplicate =>
      val Sig.Duplicate(_, argtys) = sig.unmangled: @unchecked
      argtys
    case _ =>
      val Type.Function(argtys, _) =
        analysis.infos(name).asInstanceOf[Method].ty: @unchecked
      argtys
  }

  def originalFunctionType(name: Global.Member): Type.Function = name match {
    case Global.Member(owner, sig) if sig.isDuplicate =>
      val Sig.Duplicate(base, _) = sig.unmangled: @unchecked
      originalFunctionType(Global.Member(owner, base))
    case _ =>
      analysis.infos(name).asInstanceOf[Method].ty
  }
}
