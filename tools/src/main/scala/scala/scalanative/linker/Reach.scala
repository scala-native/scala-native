package scala.scalanative
package linker

import scala.collection.mutable
import scalanative.nir._
import scalanative.codegen.Metadata

class Reach(config: build.Config, entries: Seq[Global], loader: ClassLoader) {
  val unavailable = mutable.Set.empty[Global]
  val loaded      = mutable.Map.empty[Global, mutable.Map[Global, Defn]]
  val enqueued    = mutable.Set.empty[Global]
  val todo        = mutable.Stack.empty[Global]
  val done        = mutable.Map.empty[Global, Defn]
  val stack       = mutable.Stack.empty[Global]
  val links       = mutable.Set.empty[Attr.Link]
  val infos       = mutable.Map.empty[Global, Info]
  val from        = mutable.Map.empty[Global, Global]

  val dyncandidates = mutable.Map.empty[Sig, mutable.Set[Global]]
  val dynsigs       = mutable.Set.empty[Sig]
  val dynimpls      = mutable.Set.empty[Global]

  entries.foreach(reachEntry)

  def result(): Result = {
    cleanup()

    val defns = mutable.UnrolledBuffer.empty[Defn]
    defns ++= done.valuesIterator

    new Result(infos,
               entries,
               unavailable.toSeq,
               links.toSeq,
               defns,
               dynsigs.toSeq,
               dynimpls.toSeq)
  }

  def cleanup(): Unit = {
    // Remove all unreachable methods from the
    // responds map of every class. Optimizer and
    // codegen may never increase reachability past
    // what's known now, so it's safe to do this.
    infos.values.foreach {
      case cls: Class =>
        val entries = cls.responds.toArray
        entries.foreach {
          case (sig, name) =>
            if (!done.contains(name)) {
              cls.responds -= sig
            }
        }
      case _ =>
        ()
    }
  }

  def lookup(global: Global): Option[Defn] = {
    val owner = global.top
    if (!loaded.contains(owner) && !unavailable.contains(owner)) {
      loader
        .load(owner)
        .fold[Unit] {
          unavailable += owner
        } { defns =>
          val scope = mutable.Map.empty[Global, Defn]
          defns.foreach { defn =>
            scope(defn.name) = defn
          }
          loaded(owner) = scope
        }
    }
    loaded.get(owner).flatMap(_.get(global))
  }

  def process(): Unit =
    while (todo.nonEmpty) {
      val name = todo.pop()
      if (!done.contains(name)) {
        reachDefn(name)
      }
    }

  def reachDefn(name: Global): Unit = {
    stack.push(name)
    lookup(name).fold[Unit] {
      reachUnavailable(name)
    } { defn =>
      if (defn.attrs.isStub && !config.linkStubs) {
        reachUnavailable(name)
      } else {
        reachDefn(defn)
      }
    }
    stack.pop()
  }

  def reachDefn(defn: Defn): Unit = {
    defn match {
      case defn: Defn.Var =>
        reachVar(defn)
      case defn: Defn.Const =>
        reachConst(defn)
      case defn: Defn.Declare =>
        reachDeclare(defn)
      case defn: Defn.Define =>
        val Global.Member(_, sig) = defn.name
        if (Rt.arrayAlloc.contains(sig)) {
          classInfo(Rt.arrayAlloc(sig)).foreach(reachAllocation)
        }
        reachDefine(defn)
      case defn: Defn.Trait =>
        reachTrait(defn)
      case defn: Defn.Class =>
        reachClass(defn)
      case defn: Defn.Module =>
        reachModule(defn)
    }
    done(defn.name) = defn
  }

  def reachEntry(name: Global): Unit = {
    if (!name.isTop) {
      reachEntry(name.top)
    }
    from(name) = Global.None
    reachGlobalNow(name)
    infos.get(name) match {
      case Some(cls: Class) =>
        if (!cls.attrs.isAbstract) {
          reachAllocation(cls)
          if (cls.isModule) {
            val init = cls.name.member(Sig.Ctor(Seq()))
            if (loaded(cls.name).contains(init)) {
              reachGlobal(init)
            }
          }
        }
      case _ =>
        ()
    }
  }

  def reachGlobal(name: Global): Unit =
    if (!enqueued.contains(name) && name.ne(Global.None)) {
      enqueued += name
      from(name) = if (stack.isEmpty) Global.None else stack.head
      todo.push(name)
    }

  def reachGlobalNow(name: Global): Unit =
    if (done.contains(name)) {
      ()
    } else if (!stack.contains(name)) {
      enqueued += name
      reachDefn(name)
    } else {
      val lines = (s"cyclic reference to ${name.show}:" +:
        stack.map(el => s"* ${el.show}"))
      val msg = lines.mkString("\n")
      throw new Exception(msg)
    }

  def newInfo(info: Info): Unit = {
    infos(info.name) = info
    info match {
      case info: MemberInfo =>
        info.owner match {
          case owner: ScopeInfo =>
            owner.members += info
          case _ =>
            ()
        }
      case info: Trait =>
        def loopTraits(traitInfo: Trait): Unit = {
          traitInfo.subtraits += info
          traitInfo.traits.foreach(loopTraits)
        }
        info.traits.foreach(loopTraits)
      case info: Class =>
        // Register given class as a subclass of all
        // transitive parents and as an implementation
        // of all transitive traits.
        def loopParent(parentInfo: Class): Unit = {
          parentInfo.implementors += info
          parentInfo.subclasses += info
          parentInfo.parent.foreach(loopParent)
          parentInfo.traits.foreach(loopTraits)
        }
        def loopTraits(traitInfo: Trait): Unit = {
          traitInfo.implementors += info
          traitInfo.traits.foreach(loopTraits)
        }
        info.parent.foreach(loopParent)
        info.traits.foreach(loopTraits)

        // Initialize responds map to keep track of all
        // signatures this class responds to and its
        // corresponding implementation. Some of the entries
        // may end up being not reachable, we remove those
        // in the cleanup right before we return the result.
        info.parent.foreach { parentInfo =>
          info.responds ++= parentInfo.responds
        }
        loaded(info.name).foreach {
          case (_, defn: Defn.Define) =>
            val Global.Member(_, sig) = defn.name
            def update(sig: Sig): Unit = {
              info.responds(sig) = resolve(info, sig).get
            }
            sig match {
              case Rt.JavaEqualsSig =>
                update(Rt.ScalaEqualsSig)
                update(Rt.JavaEqualsSig)
              case Rt.JavaHashCodeSig =>
                update(Rt.ScalaHashCodeSig)
                update(Rt.JavaHashCodeSig)
              case sig if sig.isMethod || sig.isCtor =>
                update(sig)
              case _ =>
                ()
            }
          case _ =>
            ()
        }
      case _ =>
        ()
    }
  }

  def reachAllocation(info: Class): Unit =
    if (!info.allocated) {
      info.allocated = true

      // Handle all class and trait virtual calls
      // on this class. This includes virtual calls
      // on the traits that this class implements and
      // calls on all transitive parents.
      val calls = mutable.Set.empty[Sig]
      calls ++= info.calls
      def loopParent(parentInfo: Class): Unit = {
        calls ++= parentInfo.calls
        parentInfo.parent.foreach(loopParent)
        parentInfo.traits.foreach(loopTraits)
      }
      def loopTraits(traitInfo: Trait): Unit = {
        calls ++= traitInfo.calls
        traitInfo.traits.foreach(loopTraits)
      }
      info.parent.foreach(loopParent)
      info.traits.foreach(loopTraits)
      calls.foreach { sig =>
        info.responds.get(sig).foreach(reachGlobal)
      }

      // Handle all dynamic methods on this class.
      // Any method that implements a known dynamic
      // signature becomes reachable. The others are
      // stashed as dynamic candidates.
      info.responds.foreach {
        case (sig, impl) if sig.isMethod =>
          val dynsig = sig.toProxy
          if (!dynsigs.contains(dynsig)) {
            val buf =
              dyncandidates.getOrElseUpdate(dynsig, mutable.Set.empty[Global])
            buf += impl
          } else {
            dynimpls += impl
            reachGlobal(impl)
          }
        case _ =>
          ()
      }
    }

  def scopeInfo(name: Global): Option[ScopeInfo] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: ScopeInfo => Some(info)
      case _               => None
    }
  }

  def scopeInfoOrUnavailable(name: Global): Info = {
    reachGlobalNow(name)
    infos(name) match {
      case info: ScopeInfo   => info
      case info: Unavailable => info
      case _                 => util.unreachable
    }
  }

  def classInfo(name: Global): Option[Class] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: Class => Some(info)
      case _           => None
    }
  }

  def classInfoOrObject(name: Global): Class =
    classInfo(name).getOrElse {
      classInfo(Rt.Object.name).get
    }

  def traitInfo(name: Global): Option[Trait] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: Trait => Some(info)
      case _           => None
    }
  }

  def methodInfo(name: Global): Option[Method] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: Method => Some(info)
      case _            => None
    }
  }

  def fieldInfo(name: Global): Option[Field] = {
    reachGlobalNow(name)
    infos(name) match {
      case info: Field => Some(info)
      case _           => None
    }
  }

  def reachUnavailable(name: Global): Unit = {
    newInfo(new Unavailable(name))
    unavailable += name
    // Put a null definition to indicate that name
    // is effectively done and doesn't need to be
    // visited any more. This saves us the need to
    // check the unavailable set every time we check
    // if something is truly handled.
    done(name) = null
  }

  def reachVar(defn: Defn.Var): Unit = {
    val Defn.Var(attrs, name, ty, rhs) = defn
    newInfo(
      new Field(attrs,
                scopeInfoOrUnavailable(name.top),
                name,
                isConst = false,
                ty,
                rhs))
    reachAttrs(attrs)
    reachType(ty)
    reachVal(rhs)
  }

  def reachConst(defn: Defn.Const): Unit = {
    val Defn.Const(attrs, name, ty, rhs) = defn
    newInfo(
      new Field(attrs,
                scopeInfoOrUnavailable(name.top),
                name,
                isConst = true,
                ty,
                rhs))
    reachAttrs(attrs)
    reachType(ty)
    reachVal(rhs)
  }

  def reachDeclare(defn: Defn.Declare): Unit = {
    val Defn.Declare(attrs, name, ty) = defn
    newInfo(
      new Method(attrs, scopeInfoOrUnavailable(name.top), name, ty, Array()))
    reachAttrs(attrs)
    reachType(ty)
  }

  def reachDefine(defn: Defn.Define): Unit = {
    val Defn.Define(attrs, name, ty, insts) = defn
    newInfo(
      new Method(attrs,
                 scopeInfoOrUnavailable(name.top),
                 name,
                 ty,
                 insts.toArray))
    reachAttrs(attrs)
    reachType(ty)
    reachInsts(insts)
  }

  def reachTrait(defn: Defn.Trait): Unit = {
    val Defn.Trait(attrs, name, traits) = defn
    newInfo(new Trait(attrs, name, traits.flatMap(traitInfo)))
    reachAttrs(attrs)
  }

  def reachClass(defn: Defn.Class): Unit = {
    val Defn.Class(attrs, name, parent, traits) = defn
    newInfo(
      new Class(attrs,
                name,
                parent.map(classInfoOrObject),
                traits.flatMap(traitInfo),
                isModule = false))
    reachAttrs(attrs)
  }

  def reachModule(defn: Defn.Module): Unit = {
    val Defn.Module(attrs, name, parent, traits) = defn
    newInfo(
      new Class(attrs,
                name,
                parent.map(classInfoOrObject),
                traits.flatMap(traitInfo),
                isModule = true))
    reachAttrs(attrs)
  }

  def reachAttrs(attrs: Attrs): Unit =
    links ++= attrs.links

  def reachType(ty: Type): Unit = ty match {
    case Type.ArrayValue(ty, n) =>
      reachType(ty)
    case Type.StructValue(tys) =>
      tys.foreach(reachType)
    case Type.Function(args, ty) =>
      args.foreach(reachType)
      reachType(ty)
    case Type.Ref(name, _, _) =>
      reachGlobal(name)
    case Type.Var(ty) =>
      reachType(ty)
    case Type.Array(ty, _) =>
      reachType(ty)
    case _ =>
      ()
  }

  def reachVal(value: Val): Unit = value match {
    case Val.Zero(ty) =>
      reachType(ty)
    case Val.StructValue(values) =>
      values.foreach(reachVal)
    case Val.ArrayValue(ty, values) =>
      reachType(ty)
      values.foreach(reachVal)
    case Val.Local(n, ty) =>
      reachType(ty)
    case Val.Global(n, ty) =>
      reachGlobal(n); reachType(ty)
    case Val.Const(v) =>
      reachVal(v)
    case _ =>
      ()
  }

  def reachInsts(insts: Seq[Inst]): Unit =
    insts.foreach(reachInst)

  def reachInst(inst: Inst): Unit = inst match {
    case Inst.Label(n, params) =>
      params.foreach(p => reachType(p.ty))
    case Inst.Let(n, op, unwind) =>
      reachOp(op)
      reachNext(unwind)
    case Inst.Ret(v) =>
      reachVal(v)
    case Inst.Jump(next) =>
      reachNext(next)
    case Inst.If(v, thenp, elsep) =>
      reachVal(v)
      reachNext(thenp)
      reachNext(elsep)
    case Inst.Switch(v, default, cases) =>
      reachVal(v)
      reachNext(default)
      cases.foreach(reachNext)
    case Inst.Throw(v, unwind) =>
      reachVal(v)
      reachNext(unwind)
    case Inst.Unreachable(unwind) =>
      reachNext(unwind)
  }

  def reachOp(op: Op): Unit = op match {
    case Op.Call(ty, ptrv, argvs) =>
      reachType(ty)
      reachVal(ptrv)
      argvs.foreach(reachVal)
    case Op.Load(ty, ptrv) =>
      reachType(ty)
      reachVal(ptrv)
    case Op.Store(ty, ptrv, v) =>
      reachType(ty)
      reachVal(ptrv)
      reachVal(v)
    case Op.Elem(ty, ptrv, indexvs) =>
      reachType(ty)
      reachVal(ptrv)
      indexvs.foreach(reachVal)
    case Op.Extract(aggrv, indexvs) =>
      reachVal(aggrv)
    case Op.Insert(aggrv, v, indexvs) =>
      reachVal(aggrv)
      reachVal(v)
    case Op.Stackalloc(ty, v) =>
      reachType(ty)
      reachVal(v)
    case Op.Bin(bin, ty, lv, rv) =>
      reachType(ty)
      reachVal(lv)
      reachVal(rv)
    case Op.Comp(comp, ty, lv, rv) =>
      reachType(ty)
      reachVal(lv)
      reachVal(rv)
    case Op.Conv(conv, ty, v) =>
      reachType(ty)
      reachVal(v)

    case Op.Classalloc(n) =>
      classInfo(n).foreach(reachAllocation)
    case Op.Fieldload(ty, v, n) =>
      reachType(ty)
      reachVal(v)
      reachGlobal(n)
    case Op.Fieldstore(ty, v1, n, v2) =>
      reachType(ty)
      reachVal(v1)
      reachGlobal(n)
      reachVal(v2)
    case Op.Method(obj, sig) =>
      reachVal(obj)
      reachMethodTargets(obj.ty, sig)
    case Op.Dynmethod(obj, dynsig) =>
      reachVal(obj)
      reachDynamicMethodTargets(dynsig)
    case Op.Module(n) =>
      classInfo(n).foreach(reachAllocation)
      val init = n.member(Sig.Ctor(Seq()))
      if (loaded(n).contains(init)) {
        reachGlobal(init)
      }
    case Op.As(ty, v) =>
      reachType(ty)
      reachVal(v)
    case Op.Is(ty, v) =>
      reachType(ty)
      reachVal(v)
    case Op.Copy(v) =>
      reachVal(v)
    case Op.Sizeof(ty) =>
      reachType(ty)
    case Op.Box(code, obj) =>
      reachVal(obj)
    case Op.Unbox(code, obj) =>
      reachVal(obj)
    case Op.Var(ty) =>
      reachType(ty)
    case Op.Varload(slot) =>
      reachVal(slot)
    case Op.Varstore(slot, value) =>
      reachVal(slot)
      reachVal(value)
    case Op.Arrayalloc(ty, init) =>
      classInfo(Type.toArrayClass(ty)).foreach(reachAllocation)
      reachType(ty)
      reachVal(init)
    case Op.Arrayload(ty, arr, idx) =>
      reachType(ty)
      reachVal(arr)
      reachVal(idx)
    case Op.Arraystore(ty, arr, idx, value) =>
      reachType(ty)
      reachVal(arr)
      reachVal(idx)
      reachVal(value)
    case Op.Arraylength(arr) =>
      reachVal(arr)
  }

  def reachNext(next: Next): Unit = next match {
    case Next.Label(_, args) =>
      args.foreach(reachVal)
    case _ =>
      ()
  }

  def reachMethodTargets(ty: Type, sig: Sig): Unit = ty match {
    case Type.Array(ty, _) =>
      reachMethodTargets(Type.Ref(Type.toArrayClass(ty)), sig)
    case Type.Ref(name, _, _) =>
      scopeInfo(name).foreach { scope =>
        if (!scope.calls.contains(sig)) {
          scope.calls += sig
          scope.targets(sig).foreach(reachGlobal)
        }
      }
    case _ =>
      ()
  }

  def reachDynamicMethodTargets(dynsig: Sig) = {
    if (!dynsigs.contains(dynsig)) {
      dynsigs += dynsig
      if (dyncandidates.contains(dynsig)) {
        dyncandidates(dynsig).foreach { impl =>
          dynimpls += impl
          reachGlobal(impl)
        }
        dyncandidates -= dynsig
      }
    }
  }

  def resolve(cls: Class, sig: Sig): Option[Global] = {
    assert(loaded.contains(cls.name))

    def lookupSig(cls: Class, sig: Sig): Option[Global] = {
      val tryMember = cls.name.member(sig)
      if (loaded(cls.name).contains(tryMember)) {
        Some(tryMember)
      } else {
        cls.parent.flatMap(lookupSig(_, sig))
      }
    }

    sig match {
      // We short-circuit scala_== and scala_## to immeditately point to the
      // equals and hashCode implementation for the reference types to avoid
      // double virtual dispatch overhead. This optimization is *not* optional
      // as implementation of scala_== on java.lang.Object assumes it's only
      // called on classes which don't overrider java_==.
      case Rt.ScalaEqualsSig =>
        val scalaImpl = lookupSig(cls, Rt.ScalaEqualsSig).get
        val javaImpl  = lookupSig(cls, Rt.JavaEqualsSig).get
        if (javaImpl.top != Rt.Object.name &&
            scalaImpl.top == Rt.Object.name) {
          Some(javaImpl)
        } else {
          Some(scalaImpl)
        }
      case Rt.ScalaHashCodeSig =>
        val scalaImpl = lookupSig(cls, Rt.ScalaHashCodeSig).get
        val javaImpl  = lookupSig(cls, Rt.JavaHashCodeSig).get
        if (javaImpl.top != Rt.Object.name &&
            scalaImpl.top == Rt.Object.name) {
          Some(javaImpl)
        } else {
          Some(scalaImpl)
        }
      case _ =>
        lookupSig(cls, sig)
    }
  }
}

object Reach {
  def apply(config: build.Config,
            entries: Seq[Global],
            loader: ClassLoader): Result = {
    val reachability = new Reach(config, entries, loader)
    reachability.process()
    reachability.result()
  }
}
