package scala.scalanative
package interflow

import scalanative.nir._
import scalanative.linker._
import scalanative.compat.CompatParColls.Converters._

trait Visit { self: Interflow =>
  def shallVisit(name: Global): Boolean = {
    val orig = originalName(name)

    if (!hasOriginal(orig)) {
      false
    } else {
      val defn = getOriginal(orig)
      val hasInsts = defn.insts.size > 0
      val hasSema = linked.infos.contains(defn.name)

      hasInsts && hasSema
    }
  }

  def shallDuplicate(name: Global, argtys: Seq[Type]): Boolean =
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
        linked.defns.foreach(defn => visitEntry(defn.name))
      case _: build.Mode.Release =>
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
      case _: build.Mode.Release =>
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

  def originalName(name: Global): Global = name match {
    case Global.Member(owner, sig) if sig.isDuplicate =>
      val Sig.Duplicate(origSig, argtys) = sig.unmangled: @unchecked
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
          val tpe = if (!Sub.is(argty, origty)) origty else argty
          // Lift Unit to BoxedUnit, only in that form it can be passed as a function argument
          // It would be better to eliminate void arguments, but currently generates lots of problmes
          if (tpe == nir.Type.Unit) Rt.BoxedUnit
          else tpe
      }
      val Global.Member(top, sig) = orig: @unchecked
      Global.Member(top, Sig.Duplicate(sig, dupargtys))
    }
  }

  def argumentTypes(name: Global): Seq[Type] = name match {
    case Global.Member(_, sig) if sig.isDuplicate =>
      val Sig.Duplicate(_, argtys) = sig.unmangled: @unchecked
      argtys
    case _ =>
      val Type.Function(argtys, _) =
        linked.infos(name).asInstanceOf[Method].ty: @unchecked
      argtys
  }

  def originalFunctionType(name: Global): Type = name match {
    case Global.Member(owner, sig) if sig.isDuplicate =>
      val Sig.Duplicate(base, _) = sig.unmangled: @unchecked
      originalFunctionType(Global.Member(owner, base))
    case _ =>
      linked.infos(name).asInstanceOf[Method].ty
  }
}
