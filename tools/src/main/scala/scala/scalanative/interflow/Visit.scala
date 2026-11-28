package scala.scalanative
package interflow

import scala.annotation.tailrec
import scala.concurrent._

import scalanative.linker._

private[interflow] trait Visit { self: Interflow =>

  def shallVisit(name: nir.Global.Member): Boolean = {
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

  def shallDuplicate(name: nir.Global.Member, argtys: Seq[nir.Type]): Boolean =
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
            defn.attrs.opt != nir.Attr.NoOpt
          val canSpecialize =
            defn.attrs.specialize != nir.Attr.NoSpecialize
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

  def visitEntry(name: nir.Global): Unit = {
    if (!name.isTop) {
      visitEntry(name.top)
    }
    analysis.infos(name) match {
      case meth: Method =>
        visitRoot(meth.name)
      case cls: Class if cls.isModule =>
        val init = cls.name.member(nir.Sig.Ctor(Seq.empty))
        if (hasOriginal(init)) {
          visitRoot(init)
        }
      case _ =>
        ()
    }
  }

  def visitRoot(name: nir.Global.Member): Unit =
    if (shallVisit(name)) {
      pushTodo(name)
    }

  def visitDuplicate(
      name: nir.Global.Member,
      argtys: Seq[nir.Type]
  ): Option[nir.Defn.Define] = {
    mode match {
      case build.Mode.Debug =>
        None
      case _: build.Mode.Release =>
        val dup = duplicateName(name, argtys)
        if (shallVisit(dup)) {
          if (!isDone(dup)) {
            visitMethod(dup)
          }
          val origtys = argumentTypes(originalName(name))
          lazy val boxedArgtys = argumentTypes(dup).zip(origtys).map {
            case (argty, origty) =>
              unboxedArgClass(argty, origty).fold(argty) { boxty =>
                nir.Type.Ref(boxty.name, exact = true, nullable = false)
              }
          }
          def takesUnboxedArgs = boxedArgtys != argumentTypes(dup)
          maybeDone(dup) match {
            // Passing a box by value pays off only if the callee does not
            // need the object on every path. Otherwise the box would be
            // allocated by each callee it is passed through instead of once
            // by the caller.
            case Some(defn) if allocatesUnboxedArgs(defn) =>
              log(s"rejected ${dup.show}: allocates a box passed by value")
              dropDone(dup)
              visitDuplicate(name, boxedArgtys)
            // Recursive call from the duplicate that is being visited,
            // or a duplicate that was rejected before
            case None if dup.sig.isDuplicate && takesUnboxedArgs =>
              visitDuplicate(name, boxedArgtys)
            case res =>
              res
          }
        } else {
          None
        }
    }
  }

  def visitLoop()(implicit ec: ExecutionContext): Future[Unit] = {
    def visit(name: nir.Global.Member): Unit = {
      if (!isDone(name)) {
        visitMethod(name)
      }
    }

    @tailrec def loop(): Unit = popTodo() match {
      case name: nir.Global.Member =>
        visit(name); loop()
      case nir.Global.None =>
        ()
      case name: nir.Global.Top =>
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

  def visitMethod(name: nir.Global.Member): Unit =
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
            origdefn.copy(attrs =
              origdefn.attrs.withOpt(nir.Attr.BailOpt(msg))
            )(
              origdefn.pos
            )
          noOpt(origdefn)
          setDone(name, baildefn)
          setDone(origname, baildefn)
          markDenylisted(name)
          markDenylisted(origname)
      }
    }

  def originalName(name: nir.Global.Member): nir.Global.Member = name match {
    case nir.Global.Member(owner, sig) if sig.isDuplicate =>
      val nir.Sig.Duplicate(origSig, argtys) = sig.unmangled: @unchecked
      originalName(nir.Global.Member(owner, origSig))
    case _ =>
      name
  }

  def duplicateName(
      name: nir.Global.Member,
      argtys: Seq[nir.Type]
  ): nir.Global.Member = {
    val orig = originalName(name)
    if (!shallDuplicate(orig, argtys)) orig
    else {
      val origargtys = argumentTypes(name)
      val dupargtys = argtys.zip(origargtys).map {
        case (argty, origty) =>
          // Duplicate argument type should not be
          // less specific than the original declare type.
          val tpe =
            if (Sub.is(argty, origty) || isUnboxedArg(argty, origty)) argty
            else origty
          // Lift Unit to BoxedUnit, only in that form it can be passed as a function argument
          // It would be better to eliminate void arguments, but currently generates lots of problmes
          if (tpe == nir.Type.Unit) nir.Rt.BoxedUnit
          else tpe
      }
      val nir.Global.Member(top, sig) = orig
      nir.Global.Member(top, nir.Sig.Duplicate(sig, dupargtys))
    }
  }

  /** Boxes that can be passed to a duplicate by value: the primitive type
   *  identifies the class of the box, so the callee can box it again if it
   *  needs an object. Unsigned, size and pointer boxes are left out, their
   *  primitive type is shared by many box classes.
   */
  private val unboxedArgClasses: Map[nir.Type, nir.Type.Ref] =
    Seq(
      nir.Type.Bool -> "java.lang.Boolean",
      nir.Type.Char -> "java.lang.Character",
      nir.Type.Byte -> "java.lang.Byte",
      nir.Type.Short -> "java.lang.Short",
      nir.Type.Int -> "java.lang.Integer",
      nir.Type.Long -> "java.lang.Long",
      nir.Type.Float -> "java.lang.Float",
      nir.Type.Double -> "java.lang.Double"
    ).map {
      case (ty, cls) => (ty: nir.Type) -> nir.Type.Ref(nir.Global.Top(cls))
    }.toMap

  /** The class of a box passed by value as an argument of type `argty` to a
   *  duplicate of a method declaring a parameter of the type `origty`.
   */
  def unboxedArgClass(
      argty: nir.Type,
      origty: nir.Type
  ): Option[nir.Type.Ref] =
    origty match {
      case _: nir.Type.RefKind =>
        unboxedArgClasses.get(argty).filter(Sub.is(_, origty))
      case _ =>
        None
    }

  def isUnboxedArg(argty: nir.Type, origty: nir.Type): Boolean =
    unboxedArgClass(argty, origty).isDefined

  /** Does the duplicate allocate a box of an argument passed by value on each
   *  path through its body? A box needed only on some of the paths, e.g. to
   *  report an error, is still worth to be passed by value.
   */
  private def allocatesUnboxedArgs(defn: nir.Defn.Define): Boolean =
    defn.name.sig.isDuplicate && {
      val nir.Inst.Label(_, params) = defn.insts.head: @unchecked
      val origtys = argumentTypes(originalName(defn.name))
      val unboxed = params
        .zip(origtys)
        .collect {
          case (param, origty) if isUnboxedArg(param.ty, origty) => param.id
        }
        .toSet
      unboxed.nonEmpty && defn.insts.exists(allocatesBoxOf(unboxed)) && {
        val cfg = nir.ControlFlow.Graph(defn.insts)
        val visited = scala.collection.mutable.Set.empty[nir.Local]
        // Is there a way out of the method not allocating the box?
        def leaves(block: nir.ControlFlow.Block): Boolean =
          visited.add(block.id) &&
            !block.insts.exists(allocatesBoxOf(unboxed)) &&
            (block.succ.isEmpty || block.succ.exists(leaves))
        !leaves(cfg.entry)
      }
    }

  /** Does the instruction allocate a box of one of the given arguments? */
  def allocatesBoxOf(args: Set[nir.Local])(inst: nir.Inst): Boolean =
    inst match {
      case nir.Inst.Let(_, nir.Op.Box(_, nir.Val.Local(id, _)), _) =>
        args.contains(id)
      case _ =>
        false
    }

  /** The type of an argument in the signature of a duplicate: an exact class of
   *  a virtual allocation, or a primitive type when the argument is a box that
   *  was not allocated yet and can be passed by value.
   */
  def duplicateArgumentType(value: nir.Val)(implicit state: State): nir.Type =
    value match {
      case UnboxedArg(_, primty) => primty
      case VirtualRef(_, cls, _) => cls.ty
      case DelayedRef(op)        => op.resty
      case value                 => value.ty
    }

  /** The value of a box that can be passed by value, and its type */
  object UnboxedArg {
    def unapply(
        value: nir.Val
    )(implicit state: State): Option[(nir.Val, nir.Type)] =
      value match {
        case VirtualRef(BoxKind, cls, Array(prim)) =>
          val primty = prim match {
            case InstanceRef(ty) => ty
            case _               => prim.ty
          }
          if (unboxedArgClasses.get(primty).exists(_.name == cls.name))
            Some((prim, primty))
          else None
        case _ =>
          None
      }
  }

  def argumentTypes(name: nir.Global.Member): Seq[nir.Type] = name match {
    case nir.Global.Member(_, sig) if sig.isDuplicate =>
      val nir.Sig.Duplicate(_, argtys) = sig.unmangled: @unchecked
      argtys
    case _ =>
      val nir.Type.Function(argtys, _) =
        analysis.infos(name).asInstanceOf[Method].ty: @unchecked
      argtys
  }

  def originalFunctionType(name: nir.Global.Member): nir.Type.Function =
    name match {
      case nir.Global.Member(owner, sig) if sig.isDuplicate =>
        val nir.Sig.Duplicate(base, _) = sig.unmangled: @unchecked
        originalFunctionType(nir.Global.Member(owner, base))
      case _ =>
        analysis.infos(name).asInstanceOf[Method].ty
    }

}
